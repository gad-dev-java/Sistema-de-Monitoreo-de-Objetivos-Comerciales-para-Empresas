package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringController Unit Tests")
class MonitoringControllerTest {

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringController monitoringController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(monitoringController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/monitoring/kpi/{storeId}")
    class GetStoreKpis {

        @Test
        @DisplayName("Should return 200 OK with KPI data when store exists")
        void shouldReturn200_withKpiData_whenStoreExists() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("75.50"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("50.00"));

            // When / Then
            mockMvc.perform(get("/api/monitoring/kpi/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("KPIs calculated successfully for store: 1"))
                    .andExpect(jsonPath("$.data.performanceCompliance").value(75.50))  // 👈 nombre real
                    .andExpect(jsonPath("$.data.timeElapsedPercentage").value(50.00))  // 👈 nombre real
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 200 OK with zero values when store has no sales data")
        void shouldReturn200_withZeroValues_whenStoreHasNoSalesData() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(BigDecimal.ZERO);
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(BigDecimal.ZERO);

            // When / Then
            mockMvc.perform(get("/api/monitoring/kpi/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.performanceCompliance").value(0))
                    .andExpect(jsonPath("$.data.timeElapsedPercentage").value(0));
        }

        @Test
        @DisplayName("Should return 200 OK with 100 percent values when objective is fully met")
        void shouldReturn200_with100PercentValues_whenObjectiveIsFullyMet() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(1L))
                    .thenReturn(new BigDecimal("100.00"));
            when(monitoringService.calculateTimeElapsedPercentage(1L))
                    .thenReturn(new BigDecimal("100.00"));

            // When / Then
            mockMvc.perform(get("/api/monitoring/kpi/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.performanceCompliance").value(100.00))
                    .andExpect(jsonPath("$.data.timeElapsedPercentage").value(100.00));
        }

        @Test
        @DisplayName("Should include storeId in response message")
        void shouldIncludeStoreId_inResponseMessage() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(42L))
                    .thenReturn(new BigDecimal("60.00"));
            when(monitoringService.calculateTimeElapsedPercentage(42L))
                    .thenReturn(new BigDecimal("40.00"));

            // When / Then
            mockMvc.perform(get("/api/monitoring/kpi/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("KPIs calculated successfully for store: 42"));
        }

        @Test
        @DisplayName("Should call both service methods with correct storeId from path variable")
        void shouldCallBothServiceMethods_withCorrectStoreIdFromPathVariable() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(5L))
                    .thenReturn(new BigDecimal("80.00"));
            when(monitoringService.calculateTimeElapsedPercentage(5L))
                    .thenReturn(new BigDecimal("70.00"));

            // When
            mockMvc.perform(get("/api/monitoring/kpi/5"))
                    .andExpect(status().isOk());

            // Then
            verify(monitoringService, times(1)).calculatePerformanceCompliance(5L);
            verify(monitoringService, times(1)).calculateTimeElapsedPercentage(5L);
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when store does not exist")
        void shouldReturn404_whenStoreDoesNotExist() throws Exception {
            // Given
            when(monitoringService.calculatePerformanceCompliance(99L))
                    .thenThrow(new StoreNotFoundException("Store not found with id 99"));

            // When / Then
            mockMvc.perform(get("/api/monitoring/kpi/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Store not found with id 99"));

            verify(monitoringService, never()).calculateTimeElapsedPercentage(any());
        }
    }
}