package com.upc.oss.monitoreo.service.impl;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Unit Tests")
class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .idUser(1L)
                .name("Juan Pérez")
                .email("juan@empresa.com")
                .password("hashed_password")
                .role("ADMIN")
                .company(Company.builder().idCompany(1L).name("Mi Empresa").build())
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsername {

        @Test
        @DisplayName("Should return UserDetails with correct username when email exists")
        void shouldReturnUserDetails_withCorrectUsername_whenEmailExists() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(existingUser));

            // When
            UserDetails resultado = userDetailsService.loadUserByUsername("juan@empresa.com");

            // Then
            assertNotNull(resultado);
            assertEquals("juan@empresa.com", resultado.getUsername());
        }

        @Test
        @DisplayName("Should return UserDetails with encoded password when email exists")
        void shouldReturnUserDetails_withEncodedPassword_whenEmailExists() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(existingUser));

            // When
            UserDetails resultado = userDetailsService.loadUserByUsername("juan@empresa.com");

            // Then
            assertEquals("hashed_password", resultado.getPassword());
        }

        @Test
        @DisplayName("Should return UserDetails with correct role when email exists")
        void shouldReturnUserDetails_withCorrectRole_whenEmailExists() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(existingUser));

            // When
            UserDetails resultado = userDetailsService.loadUserByUsername("juan@empresa.com");

            // Then
            assertTrue(resultado.getAuthorities().stream()
                    .anyMatch(auth -> Objects.equals(auth.getAuthority(), "ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Should query repository using the provided email as username")
        void shouldQueryRepository_usingProvidedEmailAsUsername() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(existingUser));

            // When
            userDetailsService.loadUserByUsername("juan@empresa.com");

            // Then
            verify(userRepository, times(1)).findByEmail("juan@empresa.com");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when email does not exist")
        void shouldThrowUsernameNotFoundException_whenEmailDoesNotExist() {
            // Given
            String nonExistentEmail = "noexiste@empresa.com";
            when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

            // When
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername(nonExistentEmail));

            // Then
            assertTrue(exception.getMessage().contains(nonExistentEmail));
        }

        @Test
        @DisplayName("Should return UserDetails that is enabled and non-expired when user exists")
        void shouldReturnActiveUserDetails_whenUserExists() {
            // Given
            when(userRepository.findByEmail("juan@empresa.com")).thenReturn(Optional.of(existingUser));

            // When
            UserDetails resultado = userDetailsService.loadUserByUsername("juan@empresa.com");

            // Then
            assertTrue(resultado.isEnabled());
            assertTrue(resultado.isAccountNonExpired());
            assertTrue(resultado.isAccountNonLocked());
            assertTrue(resultado.isCredentialsNonExpired());
        }
    }

}