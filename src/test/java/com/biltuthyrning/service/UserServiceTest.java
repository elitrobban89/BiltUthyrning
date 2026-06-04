package com.biltuthyrning.service;

import com.biltuthyrning.model.User;
import com.biltuthyrning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ── hashPassword ──────────────────────────────────────────────────────────

    @Test
    void hashPassword_returnsSameHashForSameInput() {
        String h1 = userService.hashPassword("hemligt123");
        String h2 = userService.hashPassword("hemligt123");

        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void hashPassword_returnsDifferentHashForDifferentInput() {
        String h1 = userService.hashPassword("hemligt123");
        String h2 = userService.hashPassword("annatLösenord");

        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    void hashPassword_returns64CharHexString() {
        // SHA-256 = 256 bitar = 32 bytes = 64 hex-tecken
        String hash = userService.hashPassword("test");

        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void hashPassword_doesNotReturnPlaintext() {
        String hash = userService.hashPassword("admin123");

        assertThat(hash).doesNotContain("admin123");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_returnsUser_whenCredentialsCorrect() {
        User user = userWithHashedPassword("anna", "rätt");
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("anna", "rätt");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("anna");
    }

    @Test
    void login_returnsEmpty_whenUsernameNotFound() {
        when(userRepository.findByUsername("okänd")).thenReturn(Optional.empty());

        Optional<User> result = userService.login("okänd", "lösenord");

        assertThat(result).isEmpty();
    }

    @Test
    void login_returnsEmpty_whenPasswordWrong() {
        User user = userWithHashedPassword("anna", "rätt");
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("anna", "fel");

        assertThat(result).isEmpty();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_savesUserWithHashedPassword() {
        when(userRepository.findByUsername("nytt")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.register("nytt", "lösenord", "USER");

        assertThat(result.getUsername()).isEqualTo("nytt");
        assertThat(result.getRole()).isEqualTo("USER");
        // Lösenordet ska vara hashat, inte i klartext
        assertThat(result.getPassword())
            .isNotEqualTo("lösenord")
            .isEqualTo(userService.hashPassword("lösenord"));
    }

    @Test
    void register_throwsWhenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register("anna", "lösenord", "USER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("redan taget");
    }

    @Test
    void register_doesNotSave_whenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register("anna", "lösenord", "USER"))
            .isInstanceOf(RuntimeException.class);

        verify(userRepository, never()).save(any());
    }

    // ── Hjälpmetod ────────────────────────────────────────────────────────────

    private User userWithHashedPassword(String username, String plainPassword) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(userService.hashPassword(plainPassword));
        u.setRole("USER");
        return u;
    }
}
