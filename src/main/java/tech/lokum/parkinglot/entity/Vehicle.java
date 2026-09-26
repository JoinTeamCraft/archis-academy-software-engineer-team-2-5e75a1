package tech.lokum.parkinglot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a vehicle registered by a customer.
 * {@link VehicleType} must match {@link ParkingSpot.SpotType} at booking time (enforced in the service layer).
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User user;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    private String color;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    public enum VehicleType {
        CAR, MOTORBIKE, TRUCK, EV
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Reservation> getReservations() { return reservations; }
    public void setReservations(Set<Reservation> reservations) { this.reservations = reservations; }
}
