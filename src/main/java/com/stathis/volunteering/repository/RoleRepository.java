package com.stathis.volunteering.repository;

import com.stathis.volunteering.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findAllByName(String name);
    boolean existsByName(String name);
    Role findById(long id);
    Optional<Role> findByName(String name);
}