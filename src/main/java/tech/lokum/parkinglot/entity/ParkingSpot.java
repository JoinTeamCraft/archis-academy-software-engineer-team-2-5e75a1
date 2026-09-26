package tech.lokum.parkinglot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single parking spot within a parking lot.
 * Each spot has a type and an availability status.
 */
@Entity
@Table(name = "parking_spots", uniqueConstraints = @UniqueConstraint(columnNames = {"lot_id", "spot_number"}))
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(name = "spot_number", nullable = false)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotStatus status = SpotStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "parkingSpot", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    public enum SpotType {
        CAR, MOTORBIKE, TRUCK, EV
    }

    public enum SpotStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ParkingLot getParkingLot() { return parkingLot; }
    public void setParkingLot(ParkingLot parkingLot) { this.parkingLot = parkingLot; }
    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }
    public SpotType getType() { return type; }
    public void setType(SpotType type) { this.type = type; }
    public SpotStatus getStatus() { return status; }
    public void setStatus(SpotStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Reservation> getReservations() { return reservations; }
    public void setReservations(Set<Reservation> reservations) { this.reservations = reservations; }
}
