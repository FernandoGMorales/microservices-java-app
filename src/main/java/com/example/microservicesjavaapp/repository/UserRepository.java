package com.example.microservicesjavaapp.repository;

import com.example.microservicesjavaapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for the User entity.
 * This interface extends JpaRepository, providing standard CRUD operations
 * for the User entity (e.g., save, findById, findAll, delete).
 * Spring Data JPA automatically provides the implementation for these methods.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User by their username.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param username The username of the user to find.
     * @return An Optional containing the User if found, or empty if not.
     */
    Optional<User> findByUsername(String username);
}
