package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.entities.User;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.RoleInvalidException;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import com.upc.oss.monitoreo.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Company activeCompany;
    private User savedUser;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        activeCompany = Company.builder()
                .idCompany(1L)
                .name("Mi Empresa")
                .status(Boolean.TRUE)
                .build();

        validRequest = new CreateUserRequest(
                "Juan Pérez",
                "juan@empresa.com",
                "password123",
                "ADMIN",
                "Mi Empresa"
        );

        savedUser = User.builder()
                .idUser(50L)
                .name("Juan Pérez")
                .email("juan@empresa.com")
                .password("hashed_password")
                .role("ADMIN")
                .company(activeCompany)
                .build();
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("Should return UserDto with correct data when request is valid")
        void shouldReturnUserDto_whenRequestIsValid() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            UserDto resultado = userService.createUser(validRequest);

            // Then
            assertNotNull(resultado);
            assertEquals(50L, resultado.idUser());
            assertEquals("Juan Pérez", resultado.name());
            assertEquals("juan@empresa.com", resultado.email());
            assertEquals("ADMIN", resultado.role());
            assertEquals("Mi Empresa", resultado.companyName());
            assertEquals(Boolean.TRUE, resultado.companyStatus());
        }

        @Test
        @DisplayName("Should encode password before persisting user")
        void shouldEncodePassword_beforePersistingUser() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            userService.createUser(validRequest);

            // Then
            verify(passwordEncoder, times(1)).encode("password123");
            verify(userRepository).save(argThat(user ->
                    "hashed_password".equals(user.getPassword())
            ));
        }

        @Test
        @DisplayName("Should persist user with company and role from request")
        void shouldPersistUser_withCompanyAndRoleFromRequest() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            userService.createUser(validRequest);

            // Then
            verify(userRepository, times(1)).save(argThat(user ->
                    "Juan Pérez".equals(user.getName())
                            && "juan@empresa.com".equals(user.getEmail())
                            && "ADMIN".equals(user.getRole())
                            && user.getCompany().equals(activeCompany)
            ));
        }

        @ParameterizedTest(name = "Should create user successfully with role = {0}")
        @ValueSource(strings = {"ADMIN", "SUPERVISOR", "GERENTE", "admin", "supervisor", "gerente"})
        @DisplayName("Should create user successfully for all valid roles (case-insensitive)")
        void shouldCreateUser_whenRoleIsValid(String role) {
            // Given
            CreateUserRequest requestWithRole = new CreateUserRequest(
                    "Juan Pérez", "juan@empresa.com", "password123", role, "Mi Empresa");
            User savedUserWithRole = User.builder()
                    .idUser(50L).name("Juan Pérez").email("juan@empresa.com")
                    .role(role).company(activeCompany).build();

            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUserWithRole);

            // When
            UserDto resultado = userService.createUser(requestWithRole);

            // Then
            assertNotNull(resultado);
            assertEquals(role, resultado.role());
        }

        @Test
        @DisplayName("Should throw CompanyNotFoundException when company name does not exist")
        void shouldThrowCompanyNotFoundException_whenCompanyNameDoesNotExist() {
            // Given
            String nonExistentCompany = "Empresa Fantasma";
            CreateUserRequest requestWithBadCompany = new CreateUserRequest(
                    "Juan", "juan@x.com", "pass", "ADMIN", nonExistentCompany);
            when(companyRepository.findByNameIgnoreCase(nonExistentCompany)).thenReturn(Optional.empty());

            // When
            CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class,
                    () -> userService.createUser(requestWithBadCompany));

            // Then
            assertTrue(exception.getMessage().contains(nonExistentCompany));
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @ParameterizedTest(name = "Should throw RoleInvalidException for invalid role = \"{0}\"")
        @ValueSource(strings = {"CAJERO", "EMPLEADO", "USER", "ROOT", "", "director"})
        @DisplayName("Should throw RoleInvalidException for any invalid role")
        void shouldThrowRoleInvalidException_whenRoleIsInvalid(String invalidRole) {
            // Given
            CreateUserRequest requestWithBadRole = new CreateUserRequest(
                    "Juan", "juan@x.com", "pass", invalidRole, "Mi Empresa");
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));

            // When
            RoleInvalidException exception = assertThrows(RoleInvalidException.class,
                    () -> userService.createUser(requestWithBadRole));

            // Then
            assertNotNull(exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should not save user when company is not found")
        void shouldNotSaveUser_whenCompanyIsNotFound() {
            // Given
            when(companyRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

            // When
            assertThrows(CompanyNotFoundException.class, () -> userService.createUser(validRequest));

            // Then
            verify(userRepository, never()).save(any());
        }
    }
}