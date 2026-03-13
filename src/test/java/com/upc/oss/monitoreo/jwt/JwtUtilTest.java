package com.upc.oss.monitoreo.jwt;

import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.entities.User;
import com.upc.oss.monitoreo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private User userEntity;
    private String validToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                "cWhld3FqZXdxamllYndxaWJzamlmYmFzamZhYnJxd29yd3FvcndibnFqc25mamFzZmJqc2FvZm5zYWpvZm5hc29qZm5zYW9hZGFzamRvYXNqZG9hcw==");

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("juan@empresa.com")
                .password("hashed_password")
                .roles("ADMIN")
                .build();

        userEntity = User.builder()
                .idUser(1L)
                .name("Juan Pérez")
                .email("juan@empresa.com")
                .password("hashed_password")
                .role("ADMIN")
                .company(Company.builder()
                        .idCompany(10L)
                        .name("Mi Empresa")
                        .build())
                .build();

        when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(userEntity));
        validToken = jwtUtil.generateToken(userDetails);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Should return a non-blank token when userDetails is valid")
        void shouldReturnNonBlankToken_whenUserDetailsIsValid() {
            // When
            String token = jwtUtil.generateToken(userDetails);

            // Then
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Should return token with three JWT parts separated by dots")
        void shouldReturnTokenWithThreeJwtParts_separatedByDots() {
            // When
            String token = jwtUtil.generateToken(userDetails);

            // Then — formato JWT: header.payload.signature
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length);
        }

        @Test
        @DisplayName("Should query userRepository by email when generating token")
        void shouldQueryUserRepository_byEmailWhenGeneratingToken() {
            // When
            jwtUtil.generateToken(userDetails);

            // Then — una llamada en setUp + una más aquí
            verify(userRepository, times(2)).findByEmail("juan@empresa.com");
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername {

        @Test
        @DisplayName("Should return correct email when token is valid")
        void shouldReturnCorrectEmail_whenTokenIsValid() {
            // When
            String username = jwtUtil.extractUsername(validToken);

            // Then
            assertEquals("juan@empresa.com", username);
        }

        @Test
        @DisplayName("Should throw exception when token is malformed")
        void shouldThrowException_whenTokenIsMalformed() {
            // When / Then
            assertThrows(Exception.class, () -> jwtUtil.extractUsername("token.invalido.aqui"));
        }

        @Test
        @DisplayName("Should throw exception when token is empty")
        void shouldThrowException_whenTokenIsEmpty() {
            // When / Then
            assertThrows(Exception.class, () -> jwtUtil.extractUsername(""));
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("Should return true when token matches userDetails and is not expired")
        void shouldReturnTrue_whenTokenMatchesUserDetailsAndIsNotExpired() {
            // When
            Boolean resultado = jwtUtil.validateToken(validToken, userDetails);

            // Then
            assertTrue(resultado);
        }

        @Test
        @DisplayName("Should return false when token belongs to different user")
        void shouldReturnFalse_whenTokenBelongsToDifferentUser() {
            // Given
            UserDetails otherUser = org.springframework.security.core.userdetails.User.builder()
                    .username("otro@empresa.com")
                    .password("pass")
                    .roles("SUPERVISOR")
                    .build();

            // When
            Boolean resultado = jwtUtil.validateToken(validToken, otherUser);

            // Then
            assertFalse(resultado);
        }
    }

    @Nested
    @DisplayName("extractCompanyId()")
    class ExtractCompanyId {

        @Test
        @DisplayName("Should return correct companyId from token claims")
        void shouldReturnCorrectCompanyId_fromTokenClaims() {
            // When
            Long companyId = jwtUtil.extractCompanyId(validToken);

            // Then
            assertEquals(10L, companyId);
        }

        @Test
        @DisplayName("Should throw exception when token is invalid")
        void shouldThrowException_whenTokenIsInvalid() {
            // When / Then
            assertThrows(Exception.class, () -> jwtUtil.extractCompanyId("token.invalido.aqui"));
        }
    }

    @Nested
    @DisplayName("getUserByEmail()")
    class GetUserByEmail {

        @Test
        @DisplayName("Should return User when email exists")
        void shouldReturnUser_whenEmailExists() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(userEntity));

            // When
            User resultado = jwtUtil.getUserByEmail("juan@empresa.com");

            // Then
            assertNotNull(resultado);
            assertEquals("juan@empresa.com", resultado.getEmail());
            assertEquals("Juan Pérez", resultado.getName());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when email does not exist")
        void shouldThrowUsernameNotFoundException_whenEmailDoesNotExist() {
            // Given
            when(userRepository.findByEmail("noexiste@empresa.com")).thenReturn(Optional.empty());

            // When / Then
            assertThrows(Exception.class,
                    () -> jwtUtil.getUserByEmail("noexiste@empresa.com"));
        }
    }
}