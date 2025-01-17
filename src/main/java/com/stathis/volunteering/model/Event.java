package com.stathis.volunteering.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "organizer_id") // Foreign key column
    private User organizer; // The organization creating the event

    @ManyToMany
    @JoinTable(
            name = "event_volunteers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> volunteers;

    @Enumerated(EnumType.STRING)
    private EventStatus status; // Status of the event (PENDING or APPROVED)

    // Constructors
    public Event() {
    }

    public Event(String name, String description, LocalDateTime date, User organizer, EventStatus status) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.organizer = organizer;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public List<User> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<User> volunteers) {
        this.volunteers = volunteers;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    // toString Method
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", organizer=" + (organizer != null ? organizer.getUsername() : null) +
                ", volunteers=" + (volunteers != null ? volunteers.size() : 0) +
                ", status=" + status +
                '}';
    }
}
