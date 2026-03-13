package com.upc.oss.monitoreo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;
import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.service.SaleService;
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
@DisplayName("SaleController Unit Tests")
class SaleControllerTest {
    @Mock
    private SaleService saleService;

    @InjectMocks
    private SaleController saleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SaleDto saleDto;
    private CreateSaleRequest validRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(saleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        saleDto = SaleDto.builder()
                .idSale(1L)
                .storeName("Mi Tienda")
                .storeStatus(Boolean.TRUE)
                .amount(new BigDecimal("150.00"))
                .description("Venta de prueba")
                .saleDate(LocalDate.of(2024, 6, 15))
                .build();

        validRequest = new CreateSaleRequest(
                "Mi Tienda",
                new BigDecimal("150.00"),
                "Venta de prueba"
        );
    }

    @Nested
    @DisplayName("GET /api/sales/store/{storeId}")
    class FindAllByStoreId {

        @Test
        @DisplayName("Should return 200 OK with list of sales when store has sales")
        void shouldReturn200_withSalesList_whenStoreHasSales() throws Exception {
            // Given
            when(saleService.getSalesByStoreId(1L)).thenReturn(List.of(saleDto));

            // When / Then
            mockMvc.perform(get("/api/sales/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Sales fetching successfully"))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].idSale").value(1L))
                    .andExpect(jsonPath("$.data[0].storeName").value("Mi Tienda"))
                    .andExpect(jsonPath("$.data[0].storeStatus").value(Boolean.TRUE))
                    .andExpect(jsonPath("$.data[0].amount").value(150.00))
                    .andExpect(jsonPath("$.data[0].description").value("Venta de prueba"));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when store has no sales")
        void shouldReturn200_withEmptyList_whenStoreHasNoSales() throws Exception {
            // Given
            when(saleService.getSalesByStoreId(99L)).thenReturn(List.of());

            // When / Then
            mockMvc.perform(get("/api/sales/store/99"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 OK with timestamp in response")
        void shouldReturn200_withTimestampInResponse() throws Exception {
            // Given
            when(saleService.getSalesByStoreId(1L)).thenReturn(List.of(saleDto));

            // When / Then
            mockMvc.perform(get("/api/sales/store/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store does not exist")
        void shouldReturn404_whenStoreDoesNotExist() throws Exception {
            // Given
            when(saleService.getSalesByStoreId(99L))
                    .thenThrow(new StoreNotFoundException("Store not found with id 99"));

            // When / Then
            mockMvc.perform(get("/api/sales/store/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with id 99"));
        }

        @Test
        @DisplayName("Should call service with correct storeId from path variable")
        void shouldCallService_withCorrectStoreIdFromPathVariable() throws Exception {
            // Given
            when(saleService.getSalesByStoreId(5L)).thenReturn(List.of());

            // When
            mockMvc.perform(get("/api/sales/store/5"))
                    .andExpect(status().isOk());

            // Then
            verify(saleService, times(1)).getSalesByStoreId(5L);
        }
    }

    @Nested
    @DisplayName("POST /api/sales")
    class CreateSale {

        @Test
        @DisplayName("Should return 201 CREATED with SaleDto when request is valid")
        void shouldReturn201_withSaleDto_whenRequestIsValid() throws Exception {
            // Given
            when(saleService.registerSale(any(CreateSaleRequest.class))).thenReturn(saleDto);

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Store created successfully"))
                    .andExpect(jsonPath("$.data.idSale").value(1L))
                    .andExpect(jsonPath("$.data.storeName").value("Mi Tienda"))
                    .andExpect(jsonPath("$.data.amount").value(150.00))
                    .andExpect(jsonPath("$.data.description").value("Venta de prueba"));
        }

        @Test
        @DisplayName("Should return Location header pointing to created sale")
        void shouldReturnLocationHeader_pointingToCreatedSale() throws Exception {
            // Given
            when(saleService.registerSale(any(CreateSaleRequest.class))).thenReturn(saleDto);

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/sales/1")));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when storeName is blank")
        void shouldReturn400_whenStoreNameIsBlank() throws Exception {
            // Given
            CreateSaleRequest requestWithBlankStore = new CreateSaleRequest(
                    "", new BigDecimal("150.00"), "Descripción");

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankStore)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(saleService, never()).registerSale(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when description is blank")
        void shouldReturn400_whenDescriptionIsBlank() throws Exception {
            // Given
            CreateSaleRequest requestWithBlankDesc = new CreateSaleRequest(
                    "Mi Tienda", new BigDecimal("150.00"), "");

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithBlankDesc)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(saleService, never()).registerSale(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when amount is below minimum")
        void shouldReturn400_whenAmountIsBelowMinimum() throws Exception {
            // Given
            CreateSaleRequest requestWithLowAmount = new CreateSaleRequest(
                    "Mi Tienda", new BigDecimal("0.00"), "Descripción");

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithLowAmount)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(saleService, never()).registerSale(any());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when amount exceeds maximum")
        void shouldReturn400_whenAmountExceedsMaximum() throws Exception {
            // Given
            CreateSaleRequest requestWithHighAmount = new CreateSaleRequest(
                    "Mi Tienda", new BigDecimal("100000000.00"), "Descripción");

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithHighAmount)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(saleService, never()).registerSale(any());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store name does not exist")
        void shouldReturn404_whenStoreNameDoesNotExist() throws Exception {
            // Given
            when(saleService.registerSale(any(CreateSaleRequest.class)))
                    .thenThrow(new StoreNotFoundException("Store not found with name Mi Tienda"));

            // When / Then
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with name Mi Tienda"));
        }

        @Test
        @DisplayName("Should call service once with correct request body")
        void shouldCallService_onceWithCorrectRequestBody() throws Exception {
            // Given
            when(saleService.registerSale(any(CreateSaleRequest.class))).thenReturn(saleDto);

            // When
            mockMvc.perform(post("/api/sales")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated());

            // Then
            verify(saleService, times(1)).registerSale(any(CreateSaleRequest.class));
        }
    }

}