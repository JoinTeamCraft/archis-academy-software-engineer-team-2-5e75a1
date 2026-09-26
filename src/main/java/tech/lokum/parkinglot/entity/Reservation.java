package tech.lokum.parkinglot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * Represents a parking spot reservation linking a vehicle to a parking spot.
 * Overlap prevention uses {@link tech.lokum.parkinglot.repository.ReservationRepository}
 * and pessimistic locking in the service layer.
 * Status transitions: PENDING → CONFIRMED → CANCELLED | EXPIRED | COMPLETED.
 * Booking time-window rules (start not in past, end in future, etc.) belong on request DTOs / the service layer,
 * not on this entity, so historical rows can be loaded and updated safely.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Vehicle vehicle;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private ParkingSpot parkingSpot;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User customer;

    @JsonIgnore
    @OneToOne(mappedBy = "reservation", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Payment payment;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime startTime;

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
    public boolean isEndAfterStart() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "Customer must be the owner of the vehicle")
    public boolean isCustomerOwningVehicle() {
        if (customer == null || vehicle == null || vehicle.getUser() == null) {
            return true;
        }
        User owner = vehicle.getUser();
        if (owner.getId() != null && customer.getId() != null) {
            return owner.getId().equals(customer.getId());
        }
        return owner == customer;
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Payment getPayment() {
        return payment;
    }

    /**
     * Keeps the bidirectional {@link Payment} association in sync (owning side is {@link Payment#reservation}).
     */
    public void setPayment(Payment payment) {
        if (this.payment == payment) {
            return;
        }
        Payment previous = this.payment;
        this.payment = payment;
        if (previous != null) {
            previous.linkReservation(null);
        }
        if (payment != null) {
            payment.linkReservation(this);
        }
    }
}
