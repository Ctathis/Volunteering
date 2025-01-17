package com.stathis.volunteering.controller;

import com.stathis.volunteering.model.*;
import com.stathis.volunteering.repository.EventRepository;
import com.stathis.volunteering.repository.UserRepository;
import com.stathis.volunteering.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, EventRepository eventRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    // User Signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        if (signupRequest.getRoleName() == null || signupRequest.getRoleName().isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error: Role is required. Please specify a valid role.");
        }

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Error: Username is already taken!");
        }

        try {
            User newUser = userService.createUser(
                    signupRequest.getUsername(),
                    signupRequest.getPassword(),
                    signupRequest.getFullName(),
                    signupRequest.getEmail(),
                    signupRequest.getRoleName()
            );
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // User Login
    @GetMapping("/login")
    public ResponseEntity<?> login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in the database."));

        String roleName = user.getRole().getName();
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            return ResponseEntity.ok("""
                Welcome, Admin! Please choose an option:
                1. See all users
                2. See pending users
                3. Approve a user
                4. View pending events
                5. Approve an event
                6. Exit
                """);
        } else if ("ORGANIZATION".equalsIgnoreCase(roleName)) {
            return ResponseEntity.ok("""
                Welcome, Organization User! Please choose an option:
                1. Create a new event
                2. See volunteers registered for an event
                3. Exit
                """);
        }
        else if ("VOLUNTEER".equalsIgnoreCase(roleName)) {
            return ResponseEntity.ok("""
                Welcome, Volunteer! Please choose an option:
                1. View all approved events
                2. Register for an event
                3. Exit
                """);
        } else {
            return ResponseEntity.ok("Welcome, " + username + "! You are logged in.");
        }
    }

    // Admin Menu
    @GetMapping("/admin/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminMenu(
            @RequestParam(required = true) Integer option,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long eventId) {
        try {
            switch (option) {
                case 1 -> {
                    List<User> users = userRepository.findAll();
                    return ResponseEntity.ok(users);
                }
                case 2 -> {
                    List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING);
                    return ResponseEntity.ok(pendingUsers);
                }
                case 3 -> {
                    if (userId == null) {
                        return ResponseEntity.badRequest().body("Please provide a userId to approve.");
                    }
                    return approveUser(userId);
                }
                case 4 -> {
                    List<Event> pendingEvents = eventRepository.findByStatus(EventStatus.PENDING);
                    return ResponseEntity.ok(pendingEvents);
                }
                case 5 -> {
                    if (eventId == null) {
                        return ResponseEntity.badRequest().body("Please provide an eventId to approve.");
                    }
                    return approveEvent(eventId);
                }
                case 6 -> {
                    return ResponseEntity.ok("Exiting menu. Goodbye!");
                }
                default -> {
                    return ResponseEntity.badRequest().body("Invalid option. Please choose 1, 2, 3, 4, 5, or 6.");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Approve User
    @PutMapping("/admin/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == UserStatus.APPROVED) {
            return ResponseEntity.badRequest().body("User is already approved.");
        }

        user.setStatus(UserStatus.APPROVED);
        userRepository.save(user);

        return ResponseEntity.ok("User with ID " + userId + " has been approved.");
    }

    // Organization Menu
    @GetMapping("/organization/menu")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> organizationMenu(
            @RequestParam(required = false) Integer option,
            @RequestParam(required = false) Long eventId) {
        if (option == null) {
            return ResponseEntity.ok("""
                Welcome, Organization User! Please choose an option:
                1. Create a new event
                2. See volunteers registered for an event
                3. Exit
                """);
        }

        switch (option) {
            case 1 -> {
                return ResponseEntity.ok("Use the endpoint `/organization/create-event` to create a new event.");
            }
            case 2 -> {
                if (eventId == null) {
                    return ResponseEntity.badRequest().body("Please provide an eventId to view volunteers.");
                }
                return getVolunteersForEvent(eventId);
            }
            case 3 -> {
                return ResponseEntity.ok("Exiting menu. Goodbye!");
            }
            default -> {
                return ResponseEntity.badRequest().body("Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    // Create Event
    @PostMapping("/organization/create-event")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found in the database."));

        event.setOrganizer(organizer);
        event.setStatus(EventStatus.PENDING);

        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.ok("Event created successfully with ID: " + savedEvent.getId());
    }

    // Approve Event
    @PutMapping("/admin/approve-event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() == EventStatus.APPROVED) {
            return ResponseEntity.badRequest().body("Event is already approved.");
        }

        event.setStatus(EventStatus.APPROVED);
        eventRepository.save(event);

        return ResponseEntity.ok("Event with ID " + eventId + " has been approved.");
    }

    // Get Volunteers for Event
    @GetMapping("/organization/event-volunteers/{eventId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<?> getVolunteersForEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<User> volunteers = event.getVolunteers();
        return ResponseEntity.ok(volunteers);
    }

    @GetMapping("/volunteer/menu")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<?> volunteerMenu(
            @RequestParam(required = false) Integer option,
            @RequestParam(required = false) Long eventId) {
        if (option == null) {
            return ResponseEntity.ok("""
                Welcome, Volunteer! Please choose an option:
                1. View all approved events
                2. Register for an event
                3. Exit
                """);
        }

        switch (option) {
            case 1 -> {
                // View all approved events
                return getApprovedEvents();
            }
            case 2 -> {
                if (eventId == null) {
                    return ResponseEntity.badRequest().body("Please provide an eventId to register.");
                }
                return registerForEvent(eventId);
            }
            case 3 -> {
                return ResponseEntity.ok("Exiting menu. Goodbye!");
            }
            default -> {
                return ResponseEntity.badRequest().body("Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    private ResponseEntity<?> getApprovedEvents() {
        List<Event> approvedEvents = eventRepository.findByStatus(EventStatus.APPROVED);
        return ResponseEntity.ok(approvedEvents);
    }

    private ResponseEntity<?> registerForEvent(Long eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the logged-in user
        User volunteer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Volunteer not found in the database."));

        // Fetch the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if the volunteer is already registered
        if (event.getVolunteers().contains(volunteer)) {
            return ResponseEntity.badRequest().body("You are already registered for this event.");
        }

        // Register the volunteer
        event.getVolunteers().add(volunteer);
        eventRepository.save(event);

        return ResponseEntity.ok("You have successfully registered for the event: " + event.getName());
    }
}
