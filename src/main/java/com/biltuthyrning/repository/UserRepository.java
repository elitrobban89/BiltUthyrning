package com.biltuthyrning.repository;

import com.biltuthyrning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** @author Robert Andersson Kopler */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}