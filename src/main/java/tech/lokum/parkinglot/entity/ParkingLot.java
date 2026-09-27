package tech.lokum.parkinglot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a parking lot owned by an operator.
 * Contains multiple parking spots.
 * Spots are not physically removed from the lot; use {@link #deactivateSpot(ParkingSpot)} to take a spot
 * method deactivateSpot(ParkingSpot) requires service level validation that the spot is not active/occupied
 * out of service while preserving reservation history and FK relationships.
 *
 * <p><strong>Invariant:</strong> every {@code ParkingLot} must belong to an {@link User operator}.
 * The {@code operator_id} column is {@code NOT NULL} in the database (see V1 migration) and
 * {@code @JoinColumn(nullable = false)} is declared on the field. {@link #setOperator(User)}
 * enforces non-null at the Java level as well, so callers receive an immediate
 * {@link NullPointerException} rather than a cryptic database error. Use
 * {@link User#addParkingLot(ParkingLot)} to set both sides of the bidirectional association.
 *
 * <p>Uniqueness of (name, address) is enforced only among ACTIVE lots. Since JPA/Hibernate's
 * {@code @UniqueConstraint} cannot express a partial/filtered index, that constraint is created
 * directly in the database migration (see {@code V2__parking_lot_partial_unique_index.sql}) rather
 * than declared here. {@link #UNIQUE_NAME_ADDRESS_CONSTRAINT} still names that DB-level index so
 * {@code GlobalExceptionHandler} can recognize the violation and map it to a 409.
 */
@Entity
@Table(name = "parking_lots")
public class ParkingLot {

    public static final String UNIQUE_NAME_ADDRESS_CONSTRAINT = "uk_parking_lot_name_address";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User operator;


    @JsonIgnore
    @OneToMany(mappedBy = "parkingLot", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<ParkingSpot> spots = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE, INACTIVE
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return address; }
    public void setLocation(String location) { this.address = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public User getOperator() { return operator; }

    /**
     * Sets the owning operator of this parking lot.
     * Prefer {@link User#addParkingLot(ParkingLot)} to keep both sides of the
     * bidirectional association in sync.
     *
     * @param operator the owning operator; must not be {@code null}
     * @throws NullPointerException if {@code operator} is {@code null}
     */
    public void setOperator(User operator) {
        this.operator = Objects.requireNonNull(operator, "operator must not be null");
    }
    public Set<ParkingSpot> getSpots() { return spots; }
    public void setSpots(Set<ParkingSpot> spots) { this.spots = spots; }

    public void addSpot(ParkingSpot spot) {
        Objects.requireNonNull(spot, "spot must not be null");
        if (spot.getParkingLot() != null && spot.getParkingLot() != this) {
            throw new IllegalStateException(
                    "Spot is already assigned to a different parking lot (id="
                            + spot.getParkingLot().getId() + ")");
        }
        spots.add(spot);
        spot.setParkingLot(this);
    }

    public void deactivateSpot(ParkingSpot spot) {
        if (!spots.contains(spot)) {
            throw new IllegalArgumentException("Spot is not part of this parking lot");
        }
        spot.setStatus(ParkingSpot.SpotStatus.MAINTENANCE);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}