package com.biltuthyrning.config;

import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final BookingService bookingService;

    public SecurityConfig(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**", "/css/**", "/js/**", "/login", "/register", "/error", "/health").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                // Admins skickas till "/?welcome=1" som triggar hype-bootskärmen på dashboarden;
                // vanliga användare går rakt in.
                .successHandler((request, response, authentication) -> {
                    boolean admin = authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                    response.sendRedirect(request.getContextPath() + (admin ? "/?welcome=1" : "/"));
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, authentication) -> bookingService.deleteAllBookings())
                .logoutSuccessUrl("/login?logout")
            )
            // CSRF-token lagras i en cookie i stället för serversessionen, så den överlever
            // omdeployer och idle-spindown på Render free tier. Tidigare dog sessionens token
            // vid omstart och en redan öppen login-/logout-sida fick 403. /logout behålls
            // undantaget (utloggnings-triggern skickar inte alltid token); /api/** är ren JSON.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**", "/logout")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userService.findByUsername(username)
            .map(u -> org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRole() != null ? u.getRole().toUpperCase() : "USER")
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("Användare ej hittad: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return userService.hashPassword(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encode(rawPassword).equals(encodedPassword);
            }
        };
    }
}
