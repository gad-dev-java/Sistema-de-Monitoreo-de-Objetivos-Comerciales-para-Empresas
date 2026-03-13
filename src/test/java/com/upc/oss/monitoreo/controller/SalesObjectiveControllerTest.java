package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.InvalidDateRangeException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.service.SalesObjectiveService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesObjectiveController Unit Tests")
class SalesObjectiveControllerTest {
    @Mock
    private SalesObjectiveService salesObjectiveService;

    @InjectMocks
    private SalesObjectiveController salesObjectiveController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SalesObjectiveDto salesObjectiveDto;
    private CreateSalesObjectiveRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(salesObjectiveController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(1);

        salesObjectiveDto = SalesObjectiveDto.builder()
                .idObjective(100L)
                .nameStore("Mi Tienda")
                .statusStore(Boolean.TRUE)
                .targetAmount(new BigDecimal("5000.00"))
                .periodType("MENSUAL")
                .startDate(startDate)
                .endDate(endDate)
                .build();

        validRequest = new CreateSalesObjectiveRequest(
                "Mi Tienda",
                new BigDecimal("5000.00"),
                "MENSUAL",
                startDate,
                endDate
        );
    }

    @Nested
    @DisplayName("GET /api/sales-objectives/store/{idStore}")
    class FindByIdStore {

        @Test
        @DisplayName("Should return 200 OK with list of objectives when store has objectives")
        void shouldReturn200_withObjectivesList_whenStoreHasObjectives() throws Exception {
            // Given
            when(salesObjectiveService.getByStoreId(1L)).thenReturn(List.of(salesObjectiveDto));

            // When / Then
            mockMvc.perform(get("/api/sales-objectives/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Sales Objectives fetching successfully"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].idObjective").value(100L))
                    .andExpect(jsonPath("$.data[0].nameStore").value("Mi Tienda"))
                    .andExpect(jsonPath("$.data[0].statusStore").value(Boolean.TRUE))
                    .andExpect(jsonPath("$.data[0].targetAmount").value(5000.00))
                    .andExpect(jsonPath("$.data[0].periodType").value("MENSUAL"));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when store has no objectives")
        void shouldReturn200_withEmptyList_whenStoreHasNoObjectives() throws Exception {
            // Given
            when(salesObjectiveService.getByStoreId(99L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/sales-objectives/store/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn200_withTimestampInResponse() throws Exception {
            // Given
            when(salesObjectiveService.getByStoreId(1L)).thenReturn(List.of(salesObjectiveDto));

            // When / Then
            mockMvc.perform(get("/api/sales-objectives/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store does not exist")
        void shouldReturn404_whenStoreDoesNotExist() throws Exception {
            // Given
            when(salesObjectiveService.getByStoreId(99L))
                    .thenThrow(new StoreNotFoundException("Store not found with id 99"));

            // When / Then
            mockMvc.perform(get("/api/sales-objectives/store/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with id 99"));
        }

        @Test
        @DisplayName("Should call service with correct idStore from path variable")
        void shouldCallService_withCorrectIdStoreFromPathVariable() throws Exception {
            // Given
            when(salesObjectiveService.getByStoreId(3L)).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/sales-objectives/store/3"))
                    .andExpect(status().isOk());

            // Then
            verify(salesObjectiveService, times(1)).getByStoreId(3L);
        }
    }

    @Nested
    @DisplayName("POST /api/sales-objectives")
    class CreateSalesObjective {

        @Test
        @DisplayName("Should return 201 CREATED with SalesObjectiveDto when request is valid")
        void shouldReturn201_withSalesObjectiveDto_whenRequestIsValid() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class))).thenReturn(salesObjectiveDto);

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Sales Objective created successfully"))
                    .andExpect(jsonPath("$.data.idObjective").value(100L))
                    .andExpect(jsonPath("$.data.nameStore").value("Mi Tienda"))
                    .andExpect(jsonPath("$.data.targetAmount").value(5000.00))
                    .andExpect(jsonPath("$.data.periodType").value("MENSUAL"));
        }

        @Test
        @DisplayName("Should return Location header pointing to created objective")
        void shouldReturnLocationHeader_pointingToCreatedObjective() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class))).thenReturn(salesObjectiveDto);

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            containsString("/api/sales-objectives/100")));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store name does not exist")
        void shouldReturn404_whenStoreNameDoesNotExist() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class)))
                    .thenThrow(new StoreNotFoundException("Store not found with name Mi Tienda"));

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with name Mi Tienda"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when endDate is before startDate")
        void shouldReturn400_whenEndDateIsBeforeStartDate() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class)))
                    .thenThrow(new InvalidDateRangeException("End date must be after start date"));

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("End date must be after start date"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when startDate is in the past")
        void shouldReturn400_whenStartDateIsInThePast() throws Exception {
            // Given
            CreateSalesObjectiveRequest requestWithPastDate = new CreateSalesObjectiveRequest(
                    "Mi Tienda",
                    new BigDecimal("5000.00"),
                    "MENSUAL",
                    LocalDate.now().minusDays(1),  // 👈 fecha pasada, viola @FutureOrPresent
                    LocalDate.now().plusMonths(1)
            );

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithPastDate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(salesObjectiveService, never())
                    .recordMonthlyGoalAndAssociateWithStore(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when endDate is not in the future")
        void shouldReturn400_whenEndDateIsNotInTheFuture() throws Exception {
            // Given
            CreateSalesObjectiveRequest requestWithPastEndDate = new CreateSalesObjectiveRequest(
                    "Mi Tienda",
                    new BigDecimal("5000.00"),
                    "MENSUAL",
                    LocalDate.now(),
                    LocalDate.now().minusDays(1)  // 👈 fecha pasada, viola @Future
            );

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithPastEndDate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(salesObjectiveService, never())
                    .recordMonthlyGoalAndAssociateWithStore(any());
        }

        @Test
        @DisplayName("Should return 201 CREATED with correct startDate and endDate in response")
        void shouldReturn201_withCorrectDates_inResponse() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class))).thenReturn(salesObjectiveDto);

            // When / Then
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.startDate")
                            .value(LocalDate.now().toString()))                // 👈 dinámico
                    .andExpect(jsonPath("$.data.endDate")
                            .value(LocalDate.now().plusMonths(1).toString())); // 👈 dinámico
        }

        @Test
        @DisplayName("Should call service once with correct request body")
        void shouldCallService_onceWithCorrectRequestBody() throws Exception {
            // Given
            when(salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(
                    any(CreateSalesObjectiveRequest.class))).thenReturn(salesObjectiveDto);

            // When
            mockMvc.perform(post("/api/sales-objectives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            // Then
            verify(salesObjectiveService, times(1))
                    .recordMonthlyGoalAndAssociateWithStore(any(CreateSalesObjectiveRequest.class));
        }
    }
}