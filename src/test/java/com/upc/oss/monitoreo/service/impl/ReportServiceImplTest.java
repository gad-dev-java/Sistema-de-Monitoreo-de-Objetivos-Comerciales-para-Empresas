package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Store store1;
    private Store store2;

    @BeforeEach
    void setUp() {

        store1 = Store.builder()
                .idStore(1L)
                .name("Store Lima")
                .status(true)
                .build();

        store2 = Store.builder()
                .idStore(2L)
                .name("Store Cusco")
                .status(true)
                .build();
    }

    @Test
    void generateComplianceCsv_DebeGenerarExcel() {

        when(storeRepository.findByCompanyIdCompanyAndStatusTrue(1L))
                .thenReturn(List.of(store1, store2));

        when(monitoringService.calculatePerformanceCompliance(1L))
                .thenReturn(BigDecimal.valueOf(85));

        when(monitoringService.calculateTimeElapsedPercentage(1L))
                .thenReturn(BigDecimal.valueOf(70));

        when(monitoringService.calculatePerformanceCompliance(2L))
                .thenReturn(BigDecimal.valueOf(40));

        when(monitoringService.calculateTimeElapsedPercentage(2L))
                .thenReturn(BigDecimal.valueOf(60));

        byte[] result = reportService.generateComplianceCsv(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(storeRepository, times(1))
                .findByCompanyIdCompanyAndStatusTrue(1L);

        verify(monitoringService, times(2))
                .calculatePerformanceCompliance(anyLong());

        verify(monitoringService, times(2))
                .calculateTimeElapsedPercentage(anyLong());
    }

    @Test
    void generateComplianceCsv_SinLocales() {

        when(storeRepository.findByCompanyIdCompanyAndStatusTrue(1L))
                .thenReturn(List.of());

        byte[] result = reportService.generateComplianceCsv(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);

        verify(storeRepository, times(1))
                .findByCompanyIdCompanyAndStatusTrue(1L);

        verify(monitoringService, never())
                .calculatePerformanceCompliance(anyLong());
    }
}