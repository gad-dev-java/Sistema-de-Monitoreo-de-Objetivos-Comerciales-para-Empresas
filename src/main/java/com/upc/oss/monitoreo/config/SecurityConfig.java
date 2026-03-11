package com.upc.oss.monitoreo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.response.ErrorResponse;
import com.upc.oss.monitoreo.jwt.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/companies/**", "/api/stores", "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/sales-objectives/**", "/api/notifications/**", "/api/reports/**").hasRole("GERENTE")
                        .requestMatchers("/api/sales/**").hasRole("SUPERVISOR")
                        .requestMatchers("/api/monitoring/**").hasAnyRole("SUPERVISOR", "GERENTE")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                                    .message("Unauthorized access")
                                    .errors(null)
                                    .timestamp(LocalDateTime.now())
                                    .path(request.getMethod() + ": " + request.getRequestURI())
                                    .build();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            objectMapper.writeValue(response.getWriter(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .status(HttpServletResponse.SC_FORBIDDEN)
                                    .message("Access denied: insufficient permissions")
                                    .errors(null)
                                    .timestamp(LocalDateTime.now())
                                    .path(request.getMethod() + ": " + request.getRequestURI())
                                    .build();
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            objectMapper.writeValue(response.getWriter(), errorResponse);
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
