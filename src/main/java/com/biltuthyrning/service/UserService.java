package com.biltuthyrning.service;

import com.biltuthyrning.model.User;
import com.biltuthyrning.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(hashPassword(password)));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User register(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Användarnamnet är redan taget");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashPassword(password));
        user.setRole(role);
        return userRepository.save(user);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
