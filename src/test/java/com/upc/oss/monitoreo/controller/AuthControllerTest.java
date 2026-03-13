package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.request.AuthRequest;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserDetails userDetails;
    private AuthRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        userDetails = User.builder()
                .username("juan@empresa.com")
                .password("hashed_password")
                .roles("ADMIN")
                .build();

        validRequest = new AuthRequest("juan@empresa.com", "password123");
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("Should return 200 OK with JWT token when credentials are valid")
        void shouldReturn200_withJwtToken_whenCredentialsAreValid() throws Exception {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.data.token").value("mocked.jwt.token"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should call authenticationManager with email and password from request")
        void shouldCallAuthenticationManager_withEmailAndPasswordFromRequest() throws Exception {
            // Given
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");

            // When
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Then
            verify(authenticationManager, times(1)).authenticate(
                    argThat(auth ->
                            auth instanceof UsernamePasswordAuthenticationToken
                                    && "juan@empresa.com".equals(auth.getPrincipal())
                                    && "password123".equals(auth.getCredentials())
                    )
            );
        }

        @Test
        @DisplayName("Should load UserDetails by email after successful authentication")
        void shouldLoadUserDetails_byEmailAfterSuccessfulAuthentication() throws Exception {
            // Given
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");

            // When
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Then
            verify(userDetailsService, times(1)).loadUserByUsername("juan@empresa.com");
        }

        @Test
        @DisplayName("Should generate JWT token using loaded UserDetails")
        void shouldGenerateJwtToken_usingLoadedUserDetails() throws Exception {
            // Given
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");

            // When
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());

            // Then
            verify(jwtUtil, times(1)).generateToken(userDetails);
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when credentials are invalid")
        void shouldReturn401_whenCredentialsAreInvalid() throws Exception {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("Should not load UserDetails when authentication fails")
        void shouldNotLoadUserDetails_whenAuthenticationFails() throws Exception {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());

            // Then
            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should not expose password in response body")
        void shouldNotExposePassword_inResponseBody() throws Exception {
            // Given
            when(userDetailsService.loadUserByUsername("juan@empresa.com")).thenReturn(userDetails);
            when(jwtUtil.generateToken(userDetails)).thenReturn("mocked.jwt.token");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.password").doesNotExist())
                    .andExpect(jsonPath("$.data.token").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when email is blank")
        void shouldReturn400_whenEmailIsBlank() throws Exception {
            // Given
            AuthRequest requestWithBlankEmail = new AuthRequest("", "password123");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankEmail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(authenticationManager, never()).authenticate(any());
            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @ParameterizedTest(name = "Should return 400 BAD REQUEST for invalid email = \"{0}\"")
        @ValueSource(strings = {"notanemail", "missing@", "@nodomain", "spaces in@email.com"})
        @DisplayName("Should return 400 BAD REQUEST when email format is invalid")
        void shouldReturn400_whenEmailFormatIsInvalid(String invalidEmail) throws Exception {
            // Given
            AuthRequest requestWithBadEmail = new AuthRequest(invalidEmail, "password123");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBadEmail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(authenticationManager, never()).authenticate(any());
            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when password is blank")
        void shouldReturn400_whenPasswordIsBlank() throws Exception {
            // Given
            AuthRequest requestWithBlankPassword = new AuthRequest("juan@empresa.com", "");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankPassword)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(authenticationManager, never()).authenticate(any());
            verify(userDetailsService, never()).loadUserByUsername(any());
            verify(jwtUtil, never()).generateToken(any());
        }
    }
}