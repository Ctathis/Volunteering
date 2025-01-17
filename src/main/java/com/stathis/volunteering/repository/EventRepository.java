package com.stathis.volunteering.repository;

import com.stathis.volunteering.model.Event;
import com.stathis.volunteering.model.EventStatus;
import com.stathis.volunteering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByStatus(EventStatus status);
}
