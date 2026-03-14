package com.upc.oss.monitoreo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.response.ErrorResponse;
import com.upc.oss.monitoreo.jwt.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final ObjectMapper objectMapper;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_GERENTE = "GERENTE";
    private static final String ROLE_SUPERVISOR = "SUPERVISOR";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Solo ADMIN
                        .requestMatchers("/api/companies/**").hasRole(ROLE_ADMIN)

                        // ADMIN + GERENTE
                        .requestMatchers("/api/users/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE)
                        .requestMatchers("/api/sales-objectives/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE)
                        .requestMatchers("/api/reports/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE)

                        // TODOS — necesarios para el dashboard
                        .requestMatchers("/api/stores/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE, ROLE_SUPERVISOR)
                        .requestMatchers("/api/monitoring/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE, ROLE_SUPERVISOR)
                        .requestMatchers("/api/notifications/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE, ROLE_SUPERVISOR)
                        .requestMatchers("/api/sales/**").hasAnyRole(ROLE_ADMIN, ROLE_GERENTE, ROLE_SUPERVISOR)
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Location"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
