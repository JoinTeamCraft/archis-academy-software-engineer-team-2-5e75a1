package tech.lokum.parkinglot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Represents a parking spot reservation linking a vehicle to a parking spot.
 * Overlap prevention is enforced via pessimistic locking in the service layer.
 * Status transitions:  PENDING → CONFIRMED → CANCELLED | EXPIRED | COMPLETED
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private ParkingSpot parkingSpot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @FutureOrPresent (message = "Start time cannot be in the past")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Future
    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        PENDING, CONFIRMED, CANCELLED, EXPIRED, COMPLETED
    }

    @AssertTrue(message = "End time must be after start time")
    public boolean isEndAfterStart(){
        if(startTime == null || endTime == null){
            return true;
        }
        return endTime.isAfter(startTime);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public ParkingSpot getParkingSpot() { return parkingSpot; }
    public void setParkingSpot(ParkingSpot parkingSpot) { this.parkingSpot = parkingSpot; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
