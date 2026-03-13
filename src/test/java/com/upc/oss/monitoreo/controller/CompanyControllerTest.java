package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.exception.CompanyAlreadyExistsException;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.service.CompanyService;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyController Unit Tests")
class CompanyControllerTest {

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CompanyDto companyDto;
    private CreateCompanyRequest validCreateRequest;
    private UpdateCompanyRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(companyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        // JavaTimeModule necesario por el campo LocalDate en CompanyDto
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        companyDto = CompanyDto.builder()
                .idCompany(1L)
                .name("Mi Empresa")
                .ruc("20123456789")
                .status(true)
                .createdAt(LocalDate.of(2024, 1, 15))
                .build();

        validCreateRequest = new CreateCompanyRequest("Mi Empresa", "20123456789");
        validUpdateRequest = new UpdateCompanyRequest("Mi Empresa Actualizada", "20987654321");
    }

    @Nested
    @DisplayName("GET /api/companies")
    class GetCompanies {

        @Test
        @DisplayName("Should return 200 OK with list of companies when companies exist")
        void shouldReturn200_withCompaniesList_whenCompaniesExist() throws Exception {
            // Given
            when(companyService.getCompanies()).thenReturn(List.of(companyDto));

            // When / Then
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Company fetched successfully"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].idCompany").value(1L))
                    .andExpect(jsonPath("$.data[0].name").value("Mi Empresa"))
                    .andExpect(jsonPath("$.data[0].ruc").value("20123456789"))
                    .andExpect(jsonPath("$.data[0].status").value(true))
                    .andExpect(jsonPath("$.data[0].createdAt").value("2024-01-15"));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no companies exist")
        void shouldReturn200_withEmptyList_whenNoCompaniesExist() throws Exception {
            // Given
            when(companyService.getCompanies()).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn200_withTimestampInResponse() throws Exception {
            // Given
            when(companyService.getCompanies()).thenReturn(List.of(companyDto));

            // When / Then
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should call service exactly once when fetching companies")
        void shouldCallServiceExactlyOnce_whenFetchingCompanies() throws Exception {
            // Given
            when(companyService.getCompanies()).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());

            // Then
            verify(companyService, times(1)).getCompanies();
        }
    }

    @Nested
    @DisplayName("POST /api/companies")
    class CreateCompany {

        @Test
        @DisplayName("Should return 201 CREATED with CompanyDto when request is valid")
        void shouldReturn201_withCompanyDto_whenRequestIsValid() throws Exception {
            // Given
            when(companyService.createCompany(any(CreateCompanyRequest.class))).thenReturn(companyDto);

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Company created successfully"))
                    .andExpect(jsonPath("$.data.idCompany").value(1L))
                    .andExpect(jsonPath("$.data.name").value("Mi Empresa"))
                    .andExpect(jsonPath("$.data.ruc").value("20123456789"))
                    .andExpect(jsonPath("$.data.status").value(true))
                    .andExpect(jsonPath("$.data.createdAt").value("2024-01-15"));
        }

        @Test
        @DisplayName("Should return Location header pointing to created company")
        void shouldReturnLocationHeader_pointingToCreatedCompany() throws Exception {
            // Given
            when(companyService.createCompany(any(CreateCompanyRequest.class))).thenReturn(companyDto);

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/companies/1")));
        }

        @Test
        @DisplayName("Should return 409 CONFLICT when company name already exists")
        void shouldReturn409_whenCompanyNameAlreadyExists() throws Exception {
            // Given
            when(companyService.createCompany(any(CreateCompanyRequest.class)))
                    .thenThrow(new CompanyAlreadyExistsException("Company already exists with name Mi Empresa"));

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Company already exists with name Mi Empresa"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            CreateCompanyRequest requestWithBlankName = new CreateCompanyRequest("", "20123456789");

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(companyService, never()).createCompany(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when ruc is blank")
        void shouldReturn400_whenRucIsBlank() throws Exception {
            // Given
            CreateCompanyRequest requestWithBlankRuc = new CreateCompanyRequest("Mi Empresa", "");

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankRuc)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(companyService, never()).createCompany(any());
        }

        @ParameterizedTest(name = "Should return 400 BAD REQUEST for invalid ruc = \"{0}\"")
        @ValueSource(strings = {"1234567890", "123456789012", "2012345678A", "20 123456789", "abcdefghijk"})
        @DisplayName("Should return 400 BAD REQUEST when ruc does not match 11 digit pattern")
        void shouldReturn400_whenRucDoesNotMatch11DigitPattern(String invalidRuc) throws Exception {
            // Given
            CreateCompanyRequest requestWithInvalidRuc = new CreateCompanyRequest("Mi Empresa", invalidRuc);

            // When / Then
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidRuc)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(companyService, never()).createCompany(any());
        }

        @Test
        @DisplayName("Should call service once with correct request body")
        void shouldCallServiceOnce_withCorrectRequestBody() throws Exception {
            // Given
            when(companyService.createCompany(any(CreateCompanyRequest.class))).thenReturn(companyDto);

            // When
            mockMvc.perform(post("/api/companies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated());

            // Then
            verify(companyService, times(1)).createCompany(any(CreateCompanyRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/companies/{id}")
    class UpdateCompany {

        @Test
        @DisplayName("Should return 200 OK with updated CompanyDto when request is valid")
        void shouldReturn200_withUpdatedCompanyDto_whenRequestIsValid() throws Exception {
            // Given
            CompanyDto updatedCompanyDto = CompanyDto.builder()
                    .idCompany(1L)
                    .name("Mi Empresa Actualizada")
                    .ruc("20987654321")
                    .status(true)
                    .createdAt(LocalDate.of(2024, 1, 15))
                    .build();
            when(companyService.updateCompany(any(UpdateCompanyRequest.class), eq(1L)))
                    .thenReturn(updatedCompanyDto);

            // When / Then
            mockMvc.perform(put("/api/companies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Company updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("Mi Empresa Actualizada"))
                    .andExpect(jsonPath("$.data.ruc").value("20987654321"));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when company id does not exist")
        void shouldReturn404_whenCompanyIdDoesNotExist() throws Exception {
            // Given
            when(companyService.updateCompany(any(UpdateCompanyRequest.class), eq(99L)))
                    .thenThrow(new CompanyNotFoundException("Company not found with id 99"));

            // When / Then
            mockMvc.perform(put("/api/companies/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with id 99"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when name is blank on update")
        void shouldReturn400_whenNameIsBlankOnUpdate() throws Exception {
            // Given
            UpdateCompanyRequest requestWithBlankName = new UpdateCompanyRequest("", "20123456789");

            // When / Then
            mockMvc.perform(put("/api/companies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(companyService, never()).updateCompany(any(), any());
        }

        @ParameterizedTest(name = "Should return 400 BAD REQUEST for invalid ruc = \"{0}\" on update")
        @ValueSource(strings = {"1234567890", "123456789012", "2012345678A", "20 123456789", "abcdefghijk"})
        @DisplayName("Should return 400 BAD REQUEST when ruc does not match 11 digit pattern on update")
        void shouldReturn400_whenRucDoesNotMatch11DigitPatternOnUpdate(String invalidRuc) throws Exception {
            // Given
            UpdateCompanyRequest requestWithInvalidRuc = new UpdateCompanyRequest("Mi Empresa", invalidRuc);

            // When / Then
            mockMvc.perform(put("/api/companies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithInvalidRuc)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(companyService, never()).updateCompany(any(), any());
        }

        @Test
        @DisplayName("Should call service with correct id from path variable and request body")
        void shouldCallService_withCorrectIdAndRequestBody() throws Exception {
            // Given
            when(companyService.updateCompany(any(UpdateCompanyRequest.class), eq(1L)))
                    .thenReturn(companyDto);

            // When
            mockMvc.perform(put("/api/companies/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk());

            // Then
            verify(companyService, times(1)).updateCompany(any(UpdateCompanyRequest.class), eq(1L));
        }
    }

    @Nested
    @DisplayName("DELETE /api/companies/{id}")
    class DeleteCompany {

        @Test
        @DisplayName("Should return 204 NO CONTENT when company is deleted successfully")
        void shouldReturn204_whenCompanyIsDeletedSuccessfully() throws Exception {
            // Given
            doNothing().when(companyService).deleteCompany(1L);

            // When / Then
            mockMvc.perform(delete("/api/companies/1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when company id does not exist on delete")
        void shouldReturn404_whenCompanyIdDoesNotExistOnDelete() throws Exception {
            // Given
            doThrow(new CompanyNotFoundException("Company not found with id 99"))
                    .when(companyService).deleteCompany(99L);

            // When / Then
            mockMvc.perform(delete("/api/companies/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with id 99"));
        }

        @Test
        @DisplayName("Should call service with correct id from path variable")
        void shouldCallService_withCorrectIdFromPathVariable() throws Exception {
            // Given
            doNothing().when(companyService).deleteCompany(3L);

            // When
            mockMvc.perform(delete("/api/companies/3"))
                    .andExpect(status().isNoContent());

            // Then
            verify(companyService, times(1)).deleteCompany(3L);
        }

        @Test
        @DisplayName("Should call service exactly once and no more interactions on delete")
        void shouldCallServiceExactlyOnce_andNoMoreInteractionsOnDelete() throws Exception {
            // Given
            doNothing().when(companyService).deleteCompany(1L);

            // When
            mockMvc.perform(delete("/api/companies/1"))
                    .andExpect(status().isNoContent());

            // Then
            verify(companyService, times(1)).deleteCompany(1L);
            verifyNoMoreInteractions(companyService);
        }
    }
}