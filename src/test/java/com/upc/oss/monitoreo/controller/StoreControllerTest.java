package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.StoreDto;
import com.upc.oss.monitoreo.dto.request.CreateStoreRequest;
import com.upc.oss.monitoreo.dto.request.UpdateStoreRequest;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreController Unit Tests")
class StoreControllerTest {
    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreController storeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private StoreDto storeDto;
    private CreateStoreRequest validCreateRequest;
    private UpdateStoreRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(storeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        storeDto = StoreDto.builder()
                .idStore(10L)
                .name("Tienda Centro")
                .address("Av. Principal 123")
                .city("Lima")
                .companyName("Mi Empresa")
                .companyRuc("20123456789")
                .companyStatus(Boolean.TRUE)
                .build();

        validCreateRequest = new CreateStoreRequest(
                "Tienda Centro",
                "Av. Principal 123",
                "Lima",
                "Mi Empresa"
        );

        validUpdateRequest = new UpdateStoreRequest(
                "Tienda Centro Actualizada",
                "Av. Secundaria 456",
                "Arequipa",
                "Mi Empresa"
        );
    }

    @Nested
    @DisplayName("GET /api/stores/company/{idCompany}")
    class GetStoresByCompanyId {

        @Test
        @DisplayName("Should return 200 OK with list of stores when company has stores")
        void shouldReturn200_withStoresList_whenCompanyHasStores() throws Exception {
            // Given
            when(storeService.getStoresByCompanyId(1L)).thenReturn(List.of(storeDto));

            // When / Then
            mockMvc.perform(get("/api/stores/company/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("stores fetching successfully"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].idStore").value(10L))
                    .andExpect(jsonPath("$.data[0].name").value("Tienda Centro"))
                    .andExpect(jsonPath("$.data[0].address").value("Av. Principal 123"))
                    .andExpect(jsonPath("$.data[0].city").value("Lima"))
                    .andExpect(jsonPath("$.data[0].companyName").value("Mi Empresa"))
                    .andExpect(jsonPath("$.data[0].companyRuc").value("20123456789"))
                    .andExpect(jsonPath("$.data[0].companyStatus").value(Boolean.TRUE));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when company has no stores")
        void shouldReturn200_withEmptyList_whenCompanyHasNoStores() throws Exception {
            // Given
            when(storeService.getStoresByCompanyId(99L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/stores/company/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn200_withTimestampInResponse() throws Exception {
            // Given
            when(storeService.getStoresByCompanyId(1L)).thenReturn(List.of(storeDto));

            // When / Then
            mockMvc.perform(get("/api/stores/company/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when company does not exist")
        void shouldReturn404_whenCompanyDoesNotExist() throws Exception {
            // Given
            when(storeService.getStoresByCompanyId(99L))
                    .thenThrow(new CompanyNotFoundException("Company not found with id 99"));

            // When / Then
            mockMvc.perform(get("/api/stores/company/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with id 99"));
        }

        @Test
        @DisplayName("Should call service with correct idCompany from path variable")
        void shouldCallService_withCorrectIdCompanyFromPathVariable() throws Exception {
            // Given
            when(storeService.getStoresByCompanyId(5L)).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/stores/company/5"))
                    .andExpect(status().isOk());

            // Then
            verify(storeService, times(1)).getStoresByCompanyId(5L);
        }
    }

    @Nested
    @DisplayName("POST /api/stores")
    class CreateStore {

        @Test
        @DisplayName("Should return 201 CREATED with StoreDto when request is valid")
        void shouldReturn201_withStoreDto_whenRequestIsValid() throws Exception {
            // Given
            when(storeService.createStoreAndAssociateWithCompany(
                    any(CreateStoreRequest.class))).thenReturn(storeDto);

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Store created successfully"))
                    .andExpect(jsonPath("$.data.idStore").value(10L))
                    .andExpect(jsonPath("$.data.name").value("Tienda Centro"))
                    .andExpect(jsonPath("$.data.address").value("Av. Principal 123"))
                    .andExpect(jsonPath("$.data.city").value("Lima"))
                    .andExpect(jsonPath("$.data.companyName").value("Mi Empresa"));
        }

        @Test
        @DisplayName("Should return Location header pointing to created store")
        void shouldReturnLocationHeader_pointingToCreatedStore() throws Exception {
            // Given
            when(storeService.createStoreAndAssociateWithCompany(
                    any(CreateStoreRequest.class))).thenReturn(storeDto);

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/stores/10")));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when name is blank")
        void shouldReturn400_whenNameIsBlank() throws Exception {
            // Given
            CreateStoreRequest requestWithBlankName = new CreateStoreRequest(
                    "", "Av. Principal 123", "Lima", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).createStoreAndAssociateWithCompany(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when address is blank")
        void shouldReturn400_whenAddressIsBlank() throws Exception {
            // Given
            CreateStoreRequest requestWithBlankAddress = new CreateStoreRequest(
                    "Tienda Centro", "", "Lima", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankAddress)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).createStoreAndAssociateWithCompany(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when city is blank")
        void shouldReturn400_whenCityIsBlank() throws Exception {
            // Given
            CreateStoreRequest requestWithBlankCity = new CreateStoreRequest(
                    "Tienda Centro", "Av. Principal 123", "", "Mi Empresa");

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankCity)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).createStoreAndAssociateWithCompany(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when companyName is blank")
        void shouldReturn400_whenCompanyNameIsBlank() throws Exception {
            // Given
            CreateStoreRequest requestWithBlankCompany = new CreateStoreRequest(
                    "Tienda Centro", "Av. Principal 123", "Lima", "");

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankCompany)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).createStoreAndAssociateWithCompany(any());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when company name does not exist")
        void shouldReturn404_whenCompanyNameDoesNotExist() throws Exception {
            // Given
            when(storeService.createStoreAndAssociateWithCompany(any(CreateStoreRequest.class)))
                    .thenThrow(new CompanyNotFoundException("Company not found with name Mi Empresa"));

            // When / Then
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with name Mi Empresa"));
        }

        @Test
        @DisplayName("Should call service once with correct request body")
        void shouldCallService_onceWithCorrectRequestBody() throws Exception {
            // Given
            when(storeService.createStoreAndAssociateWithCompany(
                    any(CreateStoreRequest.class))).thenReturn(storeDto);

            // When
            mockMvc.perform(post("/api/stores")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated());

            // Then
            verify(storeService, times(1)).createStoreAndAssociateWithCompany(any(CreateStoreRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/stores/{id}")
    class UpdateStore {

        @Test
        @DisplayName("Should return 200 OK with updated StoreDto when request is valid")
        void shouldReturn200_withUpdatedStoreDto_whenRequestIsValid() throws Exception {
            // Given
            StoreDto updatedStoreDto = StoreDto.builder()
                    .idStore(10L)
                    .name("Tienda Centro Actualizada")
                    .address("Av. Secundaria 456")
                    .city("Arequipa")
                    .companyName("Mi Empresa")
                    .companyRuc("20123456789")
                    .companyStatus(Boolean.TRUE)
                    .build();

            when(storeService.updateLocal(any(UpdateStoreRequest.class), eq(10L)))
                    .thenReturn(updatedStoreDto);

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Store updated successfully"))
                    .andExpect(jsonPath("$.data.idStore").value(10L))
                    .andExpect(jsonPath("$.data.name").value("Tienda Centro Actualizada"))
                    .andExpect(jsonPath("$.data.address").value("Av. Secundaria 456"))
                    .andExpect(jsonPath("$.data.city").value("Arequipa"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when name is blank on update")
        void shouldReturn400_whenNameIsBlankOnUpdate() throws Exception {
            // Given
            UpdateStoreRequest requestWithBlankName = new UpdateStoreRequest(
                    "", "Av. Secundaria 456", "Arequipa", "Mi Empresa");

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).updateLocal(any(), any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when address is blank on update")
        void shouldReturn400_whenAddressIsBlankOnUpdate() throws Exception {
            // Given
            UpdateStoreRequest requestWithBlankAddress = new UpdateStoreRequest(
                    "Tienda Centro", "", "Arequipa", "Mi Empresa");

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankAddress)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).updateLocal(any(), any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when city is blank on update")
        void shouldReturn400_whenCityIsBlankOnUpdate() throws Exception {
            // Given
            UpdateStoreRequest requestWithBlankCity = new UpdateStoreRequest(
                    "Tienda Centro", "Av. Secundaria 456", "", "Mi Empresa");

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankCity)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).updateLocal(any(), any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when companyName is blank on update")
        void shouldReturn400_whenCompanyNameIsBlankOnUpdate() throws Exception {
            // Given
            UpdateStoreRequest requestWithBlankCompany = new UpdateStoreRequest(
                    "Tienda Centro", "Av. Secundaria 456", "Arequipa", "");

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankCompany)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(storeService, never()).updateLocal(any(), any());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store id does not exist")
        void shouldReturn404_whenStoreIdDoesNotExist() throws Exception {
            // Given
            when(storeService.updateLocal(any(UpdateStoreRequest.class), eq(99L)))
                    .thenThrow(new StoreNotFoundException("Store not found with id 99"));

            // When / Then
            mockMvc.perform(put("/api/stores/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with id 99"));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when company name does not exist on update")
        void shouldReturn404_whenCompanyNameDoesNotExistOnUpdate() throws Exception {
            // Given
            when(storeService.updateLocal(any(UpdateStoreRequest.class), eq(10L)))
                    .thenThrow(new CompanyNotFoundException("Company not found with name Mi Empresa"));

            // When / Then
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Company not found with name Mi Empresa"));
        }

        @Test
        @DisplayName("Should call service with correct id from path variable and request body")
        void shouldCallService_withCorrectIdAndRequestBody() throws Exception {
            // Given
            when(storeService.updateLocal(any(UpdateStoreRequest.class), eq(10L)))
                    .thenReturn(storeDto);

            // When
            mockMvc.perform(put("/api/stores/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk());

            // Then
            verify(storeService, times(1)).updateLocal(any(UpdateStoreRequest.class), eq(10L));
        }
    }
}