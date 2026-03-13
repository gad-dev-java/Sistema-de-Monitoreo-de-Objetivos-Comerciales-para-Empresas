package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.RoleInvalidException;
import com.upc.oss.monitoreo.service.UserService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserDto userDto;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        userDto = UserDto.builder()
                .idUser(1L)
                .name("Juan Pérez")
                .email("juan@empresa.com")
                .role("ADMIN")
                .companyName("Mi Empresa")
                .companyStatus(Boolean.TRUE)
                .build();

        validRequest = new CreateUserRequest(
                "Juan Pérez",
                "juan@empresa.com",
                "password123",
                "ADMIN",
                "Mi Empresa"
        );
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @DisplayName("Should return 201 CREATED with UserDto when request is valid")
        void shouldReturn201_withUserDto_whenRequestIsValid() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("User created successfully"))
                    .andExpect(jsonPath("$.data.idUser").value(1L))
                    .andExpect(jsonPath("$.data.name").value("Juan Pérez"))
                    .andExpect(jsonPath("$.data.email").value("juan@empresa.com"))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"))
                    .andExpect(jsonPath("$.data.companyName").value("Mi Empresa"))
                    .andExpect(jsonPath("$.data.companyStatus").value(Boolean.TRUE));
        }

        @Test
        @DisplayName("Should return Location header pointing to created user")
        void shouldReturnLocationHeader_pointingToCreatedUser() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/users/1")));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn201_withTimestampInResponse() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should call service once with correct request body")
        void shouldCallService_onceWithCorrectRequestBody() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);

            // When
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            // Then
            verify(userService, times(1)).createUser(any(CreateUserRequest.class));
        }

        // ─── Validaciones @NotBlank / @Email / @Size ──────────────────────────

        @Test
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            CreateUserRequest requestWithBlankName = new CreateUserRequest(
                    "", "juan@empresa.com", "password123", "ADMIN", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when email is blank")
        void shouldReturn400_whenEmailIsBlank() throws Exception {
            // Given
            CreateUserRequest requestWithBlankEmail = new CreateUserRequest(
                    "Juan Pérez", "", "password123", "ADMIN", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankEmail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @ParameterizedTest(name = "Should return 400 BAD REQUEST for invalid email = \"{0}\"")
        @ValueSource(strings = {"notanemail", "missing@", "@nodomain", "spaces in@email.com"})
        @DisplayName("Should return 400 BAD REQUEST when email format is invalid")
        void shouldReturn400_whenEmailFormatIsInvalid(String invalidEmail) throws Exception {
            // Given
            CreateUserRequest requestWithBadEmail = new CreateUserRequest(
                    "Juan Pérez", invalidEmail, "password123", "ADMIN", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBadEmail)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when password is blank")
        void shouldReturn400_whenPasswordIsBlank() throws Exception {
            // Given
            CreateUserRequest requestWithBlankPassword = new CreateUserRequest(
                    "Juan Pérez", "juan@empresa.com", "", "ADMIN", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankPassword)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when password is shorter than 6 characters")
        void shouldReturn400_whenPasswordIsTooShort() throws Exception {
            // Given
            CreateUserRequest requestWithShortPassword = new CreateUserRequest(
                    "Juan Pérez", "juan@empresa.com", "abc", "ADMIN", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithShortPassword)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when role is blank")
        void shouldReturn400_whenRoleIsBlank() throws Exception {
            // Given
            CreateUserRequest requestWithBlankRole = new CreateUserRequest(
                    "Juan Pérez", "juan@empresa.com", "password123", "", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankRole)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when companyName is blank")
        void shouldReturn400_whenCompanyNameIsBlank() throws Exception {
            // Given
            CreateUserRequest requestWithBlankCompany = new CreateUserRequest(
                    "Juan Pérez", "juan@empresa.com", "password123", "ADMIN", "");

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankCompany)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        // ─── Excepciones de negocio ───────────────────────────────────────────

        @Test
        @DisplayName("Should return 404 NOT FOUND when company name does not exist")
        void shouldReturn404_whenCompanyNameDoesNotExist() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new CompanyNotFoundException("Company not found with name Mi Empresa"));

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with name Mi Empresa"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when role is invalid")
        void shouldReturn400_whenRoleIsInvalid() throws Exception {
            // Given
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new RoleInvalidException("Invalid role. Must be ADMIN or SUPERVISOR"));

            // When / Then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Invalid role. Must be ADMIN or SUPERVISOR"));
        }
    }
}