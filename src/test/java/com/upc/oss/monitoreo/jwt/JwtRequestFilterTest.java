package com.upc.oss.monitoreo.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upc.oss.monitoreo.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtRequestFilter Unit Tests")
class JwtRequestFilterTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        jwtRequestFilter = new JwtRequestFilter(userDetailsService, jwtUtil, objectMapper);

        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");

        response = new MockHttpServletResponse();

        userDetails = User.builder()
                .username("juan@empresa.com")
                .password("hashed_password")
                .roles("ADMIN")
                .build();
    }

    @Nested
    @DisplayName("Request without Authorization header")
    class WithoutAuthorizationHeader {

        @Test
        @DisplayName("Should continue filter chain when Authorization header is missing")
        void shouldContinueFilterChain_whenAuthorizationHeaderIsMissing() throws Exception {
            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate user when Authorization header is missing")
        void shouldNotAuthenticateUser_whenAuthorizationHeaderIsMissing() throws Exception {
            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(userDetailsService, never()).loadUserByUsername(any());
        }

        @Test
        @DisplayName("Should continue filter chain when Authorization header does not start with Bearer")
        void shouldContinueFilterChain_whenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
            // Given
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
            verify(userDetailsService, never()).loadUserByUsername(any());
        }
    }

    @Nested
    @DisplayName("Request with invalid token")
    class WithInvalidToken {

        @Test
        @DisplayName("Should return 401 and not continue filter chain when token is malformed")
        void shouldReturn401_andNotContinueFilterChain_whenTokenIsMalformed() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer token.invalido.aqui");
            when(jwtUtil.extractUsername("token.invalido.aqui"))
                    .thenThrow(new RuntimeException("Invalid token"));

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("Should write JSON error response when token is malformed")
        void shouldWriteJsonErrorResponse_whenTokenIsMalformed() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer token.invalido.aqui");
            when(jwtUtil.extractUsername("token.invalido.aqui"))
                    .thenThrow(new RuntimeException("Invalid token"));

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertEquals("application/json", response.getContentType());
            String body = response.getContentAsString();
            assertTrue(body.contains("Invalid or expired JWT token"));
            assertTrue(body.contains("401"));
        }

        @Test
        @DisplayName("Should not set authentication in SecurityContext when token is malformed")
        void shouldNotSetAuthentication_whenTokenIsMalformed() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer token.invalido.aqui");
            when(jwtUtil.extractUsername("token.invalido.aqui"))
                    .thenThrow(new RuntimeException("Invalid token"));

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Request with valid token")
    class WithValidToken {

        @Test
        @DisplayName("Should authenticate user in SecurityContext when token is valid")
        void shouldAuthenticateUser_whenTokenIsValid() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer valid.jwt.token");
            when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("juan@empresa.com");
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.validateToken("valid.jwt.token", userDetails)).thenReturn(true);

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals("juan@empresa.com",
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }

        @Test
        @DisplayName("Should continue filter chain after successful authentication")
        void shouldContinueFilterChain_afterSuccessfulAuthentication() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer valid.jwt.token");
            when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("juan@empresa.com");
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.validateToken("valid.jwt.token", userDetails)).thenReturn(true);

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not override existing authentication in SecurityContext")
        void shouldNotOverrideExistingAuthentication_inSecurityContext() throws Exception {
            // Given — simulamos que ya hay una autenticación activa
            request.addHeader("Authorization", "Bearer valid.jwt.token");
            when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("juan@empresa.com");

            UsernamePasswordAuthenticationToken existingAuth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then — no se carga el usuario de nuevo porque ya está autenticado
            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not set authentication when token validation fails")
        void shouldNotSetAuthentication_whenTokenValidationFails() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer valid.jwt.token");
            when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("juan@empresa.com");
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.validateToken("valid.jwt.token", userDetails)).thenReturn(false);

            // When
            jwtRequestFilter.doFilterInternal(request, response, filterChain);

            // Then
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}