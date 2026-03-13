package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import com.upc.oss.monitoreo.exception.ObjectiveStoreActiveNotFound;
import com.upc.oss.monitoreo.repository.SaleRepository;
import com.upc.oss.monitoreo.repository.SalesObjectiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SalesObjectiveRepository salesObjectiveRepository;

    @InjectMocks
    private MonitoringServiceImpl monitoringService;

    private SalesObjective objective;
    private Long storeId;

    @BeforeEach
    void setUp() {

        storeId = 1L;

        objective = SalesObjective.builder()
                .idObjective(1L)
                .targetAmount(new BigDecimal("10000"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .status(SalesObjectiveStatus.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje de cumplimiento")
    void calculatePerformanceCompliance_CalculoCorrecto() {

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        when(saleRepository.sumSalesByStoreAndPeriod(
                eq(storeId),
                any(),
                any()))
                .thenReturn(new BigDecimal("5000"));

        BigDecimal resultado = monitoringService.calculatePerformanceCompliance(storeId);

        assertEquals(new BigDecimal("50.00"), resultado);

        verify(salesObjectiveRepository, times(1))
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando ventas son null")
    void calculatePerformanceCompliance_VentasNull() {

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        when(saleRepository.sumSalesByStoreAndPeriod(
                eq(storeId),
                any(),
                any()))
                .thenReturn(null);

        BigDecimal resultado = monitoringService.calculatePerformanceCompliance(storeId);

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando el objetivo es 0")
    void calculatePerformanceCompliance_ObjetivoCero() {

        objective.setTargetAmount(BigDecimal.ZERO);

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        BigDecimal resultado = monitoringService.calculatePerformanceCompliance(storeId);

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no existe objetivo activo")
    void calculatePerformanceCompliance_SinObjetivoActivo() {

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.empty());

        assertThrows(ObjectiveStoreActiveNotFound.class, () -> monitoringService.calculatePerformanceCompliance(storeId));
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje de tiempo transcurrido")
    void calculateTimeElapsedPercentage_CalculoCorrecto() {

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        BigDecimal resultado = monitoringService.calculateTimeElapsedPercentage(storeId);

        assertNotNull(resultado);
        assertTrue(resultado.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando el total de días es inválido")
    void calculateTimeElapsedPercentage_TotalDiasInvalido() {

        objective.setStartDate(LocalDate.now());
        objective.setEndDate(LocalDate.now());

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        BigDecimal resultado = monitoringService.calculateTimeElapsedPercentage(storeId);

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    @DisplayName("El porcentaje de tiempo no debe superar 100%")
    void calculateTimeElapsedPercentage_NoMayorACien() {

        objective.setStartDate(LocalDate.now().minusDays(50));
        objective.setEndDate(LocalDate.now().minusDays(10));

        when(salesObjectiveRepository
                .findActiveObjectiveByStoreId(storeId, SalesObjectiveStatus.ACTIVO))
                .thenReturn(Optional.of(objective));

        BigDecimal resultado = monitoringService.calculateTimeElapsedPercentage(storeId);

        assertTrue(resultado.compareTo(new BigDecimal("100")) <= 0);
    }
}