package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import com.upc.oss.monitoreo.exception.InvalidDateRangeException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.SalesObjectiveRepository;
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
@DisplayName("SalesObjectiveServiceImpl Unit Tests")
class SalesObjectiveServiceImplTest {
    @Mock
    private SalesObjectiveRepository salesObjectiveRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SalesObjectiveServiceImpl salesObjectiveService;

    private Store activeStore;
    private CreateSalesObjectiveRequest validRequest;
    private SalesObjective savedObjective;

    @BeforeEach
    void setUp() {
        activeStore = Store.builder()
                .idStore(1L)
                .name("Mi Tienda")
                .status(Boolean.TRUE)
                .build();

        validRequest = new CreateSalesObjectiveRequest(
                "Mi Tienda",
                new BigDecimal("5000.00"),
                "MENSUAL",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 30)
        );

        savedObjective = SalesObjective.builder()
                .idObjective(100L)
                .store(activeStore)
                .status(SalesObjectiveStatus.ACTIVO)
                .targetAmount(new BigDecimal("5000.00"))
                .periodType("MENSUAL")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .build();
    }

    @Nested
    @DisplayName("recordMonthlyGoalAndAssociateWithStore()")
    class RecordMonthlyGoalAndAssociateWithStore {

        @Test
        @DisplayName("Should return SalesObjectiveDto with correct data when request is valid")
        void shouldReturnSalesObjectiveDto_whenRequestIsValid() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(salesObjectiveRepository.findActiveObjectiveByStoreId(1L, SalesObjectiveStatus.ACTIVO))
                    .thenReturn(Optional.empty());
            when(salesObjectiveRepository.save(any(SalesObjective.class))).thenReturn(savedObjective);

            // When
            SalesObjectiveDto resultado = salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(validRequest);

            // Then
            assertNotNull(resultado);
            assertEquals(100L, resultado.idObjective());
            assertEquals("Mi Tienda", resultado.nameStore());
            assertEquals(Boolean.TRUE, resultado.statusStore());
            assertEquals(new BigDecimal("5000.00"), resultado.targetAmount());
            assertEquals("MENSUAL", resultado.periodType());
            assertEquals(LocalDate.of(2024, 6, 1), resultado.startDate());
            assertEquals(LocalDate.of(2024, 6, 30), resultado.endDate());
        }

        @Test
        @DisplayName("Should throw InvalidDateRangeException when endDate is before startDate")
        void shouldThrowInvalidDateRangeException_whenEndDateIsBeforeStartDate() {
            // Given
            CreateSalesObjectiveRequest requestWithInvalidDates = new CreateSalesObjectiveRequest(
                    "Mi Tienda",
                    new BigDecimal("5000.00"),
                    "MENSUAL",
                    LocalDate.of(2024, 6, 30),  // startDate
                    LocalDate.of(2024, 6, 1)    // endDate anterior al startDate
            );

            // When
            InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                    () -> salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(requestWithInvalidDates));

            // Then
            assertNotNull(exception.getMessage());
            verify(storeRepository, never()).findByNameIgnoreCase(any());
            verify(salesObjectiveRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw StoreNotFoundException when store name does not exist")
        void shouldThrowStoreNotFoundException_whenStoreNameDoesNotExist() {
            // Given
            String nonExistentName = "Tienda Fantasma";
            CreateSalesObjectiveRequest requestWithBadStore = new CreateSalesObjectiveRequest(
                    nonExistentName,
                    new BigDecimal("1000.00"),
                    "MENSUAL",
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 6, 30)
            );
            when(storeRepository.findByNameIgnoreCase(nonExistentName)).thenReturn(Optional.empty());

            // When
            StoreNotFoundException exception = assertThrows(StoreNotFoundException.class,
                    () -> salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(requestWithBadStore));

            // Then
            assertTrue(exception.getMessage().contains(nonExistentName));
            verify(salesObjectiveRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should finalize existing active objective before saving new one")
        void shouldFinalizeExistingActiveObjective_beforeSavingNewOne() {
            // Given
            SalesObjective existingActiveObjective = SalesObjective.builder()
                    .idObjective(99L)
                    .store(activeStore)
                    .status(SalesObjectiveStatus.ACTIVO)
                    .build();

            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(salesObjectiveRepository.findActiveObjectiveByStoreId(1L, SalesObjectiveStatus.ACTIVO))
                    .thenReturn(Optional.of(existingActiveObjective));
            when(salesObjectiveRepository.save(any(SalesObjective.class))).thenReturn(savedObjective);

            // When
            salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(validRequest);

            // Then
            verify(salesObjectiveRepository, times(2)).save(any(SalesObjective.class));
            assertEquals(SalesObjectiveStatus.FINALIZADO, existingActiveObjective.getStatus());
        }

        @Test
        @DisplayName("Should save only new objective when no active objective exists for store")
        void shouldSaveOnlyNewObjective_whenNoActiveObjectiveExistsForStore() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(salesObjectiveRepository.findActiveObjectiveByStoreId(1L, SalesObjectiveStatus.ACTIVO))
                    .thenReturn(Optional.empty());
            when(salesObjectiveRepository.save(any(SalesObjective.class))).thenReturn(savedObjective);

            // When
            salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(validRequest);

            // Then
            verify(salesObjectiveRepository, times(1)).save(any(SalesObjective.class));
        }

        @Test
        @DisplayName("Should persist new objective with ACTIVO status")
        void shouldPersistNewObjective_withActivoStatus() {
            // Given
            when(storeRepository.findByNameIgnoreCase("Mi Tienda")).thenReturn(Optional.of(activeStore));
            when(salesObjectiveRepository.findActiveObjectiveByStoreId(1L, SalesObjectiveStatus.ACTIVO))
                    .thenReturn(Optional.empty());
            when(salesObjectiveRepository.save(any(SalesObjective.class))).thenReturn(savedObjective);

            // When
            salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(validRequest);

            // Then
            verify(salesObjectiveRepository).save(argThat(objective ->
                    objective.getStatus() == SalesObjectiveStatus.ACTIVO
                            && objective.getStore().equals(activeStore)
                            && objective.getTargetAmount().compareTo(new BigDecimal("5000.00")) == 0
            ));
        }
    }

    @Nested
    @DisplayName("getByStoreId()")
    class GetByStoreId {

        @Test
        @DisplayName("Should return list of SalesObjectiveDtos mapped correctly when objectives exist")
        void shouldReturnMappedSalesObjectiveDtoList_whenObjectivesExistForStore() {
            // Given
            SalesObjective anotherObjective = SalesObjective.builder()
                    .idObjective(101L)
                    .store(activeStore)
                    .status(SalesObjectiveStatus.FINALIZADO)
                    .targetAmount(new BigDecimal("3000.00"))
                    .periodType("MENSUAL")
                    .startDate(LocalDate.of(2024, 5, 1))
                    .endDate(LocalDate.of(2024, 5, 31))
                    .build();
            when(salesObjectiveRepository.findByStoreIdStore(1L)).thenReturn(List.of(savedObjective, anotherObjective));

            // When
            List<SalesObjectiveDto> resultado = salesObjectiveService.getByStoreId(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals(100L, resultado.get(0).idObjective());
            assertEquals(101L, resultado.get(1).idObjective());
            assertEquals("Mi Tienda", resultado.get(0).nameStore());
            assertEquals("Mi Tienda", resultado.get(1).nameStore());
        }

        @Test
        @DisplayName("Should return empty list when store has no objectives")
        void shouldReturnEmptyList_whenStoreHasNoObjectives() {
            // Given
            when(salesObjectiveRepository.findByStoreIdStore(99L)).thenReturn(List.of());

            // When
            List<SalesObjectiveDto> resultado = salesObjectiveService.getByStoreId(99L);

            // Then
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Should map all fields correctly from entity to SalesObjectiveDto")
        void shouldMapAllFields_correctlyFromEntityToDto() {
            // Given
            when(salesObjectiveRepository.findByStoreIdStore(1L)).thenReturn(List.of(savedObjective));

            // When
            List<SalesObjectiveDto> resultado = salesObjectiveService.getByStoreId(1L);

            // Then
            SalesObjectiveDto dto = resultado.getFirst();
            assertEquals(Boolean.TRUE, dto.statusStore());
            assertEquals(new BigDecimal("5000.00"), dto.targetAmount());
            assertEquals("MENSUAL", dto.periodType());
            assertEquals(LocalDate.of(2024, 6, 1), dto.startDate());
            assertEquals(LocalDate.of(2024, 6, 30), dto.endDate());
        }

        @Test
        @DisplayName("Should query repository using the provided storeId")
        void shouldQueryRepository_usingProvidedStoreId() {
            // Given
            when(salesObjectiveRepository.findByStoreIdStore(1L)).thenReturn(List.of());

            // When
            salesObjectiveService.getByStoreId(1L);

            // Then
            verify(salesObjectiveRepository, times(1)).findByStoreIdStore(1L);
        }
    }
}