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
 */
@Entity
@Table(
        name = "parking_lots",
        uniqueConstraints = @UniqueConstraint(
                name = ParkingLot.UNIQUE_NAME_ADDRESS_CONSTRAINT,
                columnNames = {"name", "address"}
        )
)
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
    public void setOperator(User operator) { this.operator = operator; }
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
