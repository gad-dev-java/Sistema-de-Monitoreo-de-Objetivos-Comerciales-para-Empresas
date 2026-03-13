package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;
import com.upc.oss.monitoreo.entities.Sale;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.SaleRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleServiceImpl Unit Tests")
class SaleServiceImplTest {
    @Mock
    private SaleRepository saleRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Store activeStore;
    private Sale savedSale;
    private CreateSaleRequest validRequest;

    @BeforeEach
    void setUp() {
        activeStore = Store.builder()
                .idStore(1L)
                .name("Mi Tienda")
                .status(Boolean.TRUE)
                .build();

        validRequest = new CreateSaleRequest("Mi Tienda", new BigDecimal("150.00"), "Venta de prueba");

        savedSale = Sale.builder()
                .idSale(10L)
                .store(activeStore)
                .amount(new BigDecimal("150.00"))
                .description("Venta de prueba")
                .saleDate(LocalDate.of(2024, 6, 15))
                .build();
    }

    @Nested
    @DisplayName("registerSale()")
    class RegisterSale {

        @Test
        @DisplayName("Should return SaleDto with correct data when store exists")
        void shouldReturnSaleDto_whenStoreExists() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);

            // When
            SaleDto resultado = saleService.registerSale(validRequest);

            // Then
            assertNotNull(resultado);
            assertEquals(10L, resultado.idSale());
            assertEquals("Mi Tienda", resultado.storeName());
            assertEquals(Boolean.TRUE, resultado.storeStatus());
            assertEquals(new BigDecimal("150.00"), resultado.amount());
            assertEquals("Venta de prueba", resultado.description());
            assertEquals(LocalDate.of(2024, 6, 15), resultado.saleDate());
        }

        @Test
        @DisplayName("Should persist sale with correct fields before returning")
        void shouldPersistSaleWithCorrectFields_whenStoreExists() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);

            // When
            saleService.registerSale(validRequest);

            // Then
            verify(saleRepository, times(1)).save(argThat(sale ->
                    sale.getStore().equals(activeStore)
                            && sale.getAmount().compareTo(new BigDecimal("150.00")) == 0
                            && "Venta de prueba".equals(sale.getDescription())
            ));
        }

        @Test
        @DisplayName("Should throw StoreNotFoundException when store name does not exist")
        void shouldThrowStoreNotFoundException_whenStoreNameDoesNotExist() {
            // Given
            String nonExistentName = "Tienda Fantasma";
            CreateSaleRequest requestWithBadStore = new CreateSaleRequest(
                    nonExistentName, new BigDecimal("50.00"), "Desc");
            when(storeRepository.findByNameIgnoreCase(nonExistentName)).thenReturn(Optional.empty());

            // When
            StoreNotFoundException exception = assertThrows(StoreNotFoundException.class,
                    () -> saleService.registerSale(requestWithBadStore));

            // Then
            assertTrue(exception.getMessage().contains(nonExistentName));
            verify(saleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should search store using case-insensitive name from request")
        void shouldSearchStore_usingCaseInsensitiveNameFromRequest() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);

            // When
            saleService.registerSale(validRequest);

            // Then
            verify(storeRepository, times(1)).findByNameIgnoreCase("Mi Tienda");
        }
    }

    @Nested
    @DisplayName("getSalesByStoreId()")
    class GetSalesByStoreId {

        @Test
        @DisplayName("Should return list of SaleDtos mapped correctly when sales exist")
        void shouldReturnMappedSaleDtoList_whenSalesExistForStore() {
            // Given
            Sale anotherSale = Sale.builder()
                    .idSale(11L)
                    .store(activeStore)
                    .amount(new BigDecimal("200.00"))
                    .description("Segunda venta")
                    .saleDate(LocalDate.of(2024, 6, 16))
                    .build();
            when(saleRepository.findByStoreIdStore(1L)).thenReturn(List.of(savedSale, anotherSale));

            // When
            List<SaleDto> resultado = saleService.getSalesByStoreId(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals(10L, resultado.get(0).idSale());
            assertEquals(11L, resultado.get(1).idSale());
            assertEquals("Mi Tienda", resultado.get(0).storeName());
            assertEquals("Mi Tienda", resultado.get(1).storeName());
        }

        @Test
        @DisplayName("Should return empty list when store has no sales")
        void shouldReturnEmptyList_whenStoreHasNoSales() {
            // Given
            when(saleRepository.findByStoreIdStore(99L)).thenReturn(List.of());

            // When
            List<SaleDto> resultado = saleService.getSalesByStoreId(99L);

            // Then
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Should map storeStatus from store entity into each SaleDto")
        void shouldMapStoreStatus_intoEachSaleDto() {
            // Given
            when(saleRepository.findByStoreIdStore(1L)).thenReturn(List.of(savedSale));

            // When
            List<SaleDto> resultado = saleService.getSalesByStoreId(1L);

            // Then
            assertEquals(Boolean.TRUE, resultado.getFirst().storeStatus());
        }

        @Test
        @DisplayName("Should query repository using the provided storeId")
        void shouldQueryRepository_usingProvidedStoreId() {
            // Given
            when(saleRepository.findByStoreIdStore(1L)).thenReturn(List.of());

            // When
            saleService.getSalesByStoreId(1L);

            // Then
            verify(saleRepository, times(1)).findByStoreIdStore(1L);
        }
    }
}