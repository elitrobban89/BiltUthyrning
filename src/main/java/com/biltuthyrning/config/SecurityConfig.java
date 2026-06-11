package com.biltuthyrning.config;

import com.biltuthyrning.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**", "/css/**", "/js/**", "/login", "/register", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
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
