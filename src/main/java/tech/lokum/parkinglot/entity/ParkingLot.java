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
 * Represents a parking lot owned by an operator.
 * Contains multiple parking spots.
 */
@Entity
@Table(name = "parking_lots")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
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
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    public Set<ParkingSpot> getSpots() { return spots; }
    public void setSpots(Set<ParkingSpot> spots) { this.spots = spots; }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
        spot.setParkingLot(this);
    }

    public void removeSpot(ParkingSpot spot) {
        spots.remove(spot);
        spot.setParkingLot(null);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
