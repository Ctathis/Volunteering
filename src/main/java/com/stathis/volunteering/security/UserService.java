package com.stathis.volunteering.security;

import com.stathis.volunteering.model.Role;
import com.stathis.volunteering.model.User;
import com.stathis.volunteering.repository.RoleRepository;
import com.stathis.volunteering.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    /**
     * Load user by username for authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetails(userEntity);
    }

    /**
     * Create a new user with a hashed password, default VOLUNTEER role, and PENDING status.
     */
    public User createUser(String username, String rawPassword, String fullName, String email, String roleName) {
        // Validate roleName
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role must be specified.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Fetch role from the database
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role '" + roleName + "' not found."));

        // Create and save the new user
        User newUser = new User(
                username,
                encodedPassword,
                fullName,
                email,
                userRole
        );

        return userRepository.save(newUser);
    }

}