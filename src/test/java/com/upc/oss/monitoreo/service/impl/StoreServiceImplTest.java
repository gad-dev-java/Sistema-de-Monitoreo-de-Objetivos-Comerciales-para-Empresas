package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.StoreDto;
import com.upc.oss.monitoreo.dto.request.CreateStoreRequest;
import com.upc.oss.monitoreo.dto.request.UpdateStoreRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.CompanyRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreServiceImpl Unit Tests")
class StoreServiceImplTest {
    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SalesObjectiveRepository salesObjectiveRepository;

    @InjectMocks
    private StoreServiceImpl storeService;

    private Company activeCompany;
    private Store savedStore;
    private CreateStoreRequest validCreateRequest;
    private UpdateStoreRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        activeCompany = Company.builder()
                .idCompany(1L)
                .name("Mi Empresa")
                .ruc("20123456789")
                .status(Boolean.TRUE)
                .build();

        savedStore = Store.builder()
                .idStore(10L)
                .name("Tienda Centro")
                .address("Av. Principal 123")
                .city("Lima")
                .status(true)
                .company(activeCompany)
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
    @DisplayName("createStoreAndAssociateWithCompany()")
    class CreateStoreAndAssociateWithCompany {

        @Test
        @DisplayName("Should return StoreDto with correct data when company exists")
        void shouldReturnStoreDto_whenCompanyExists() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

            // When
            StoreDto resultado = storeService.createStoreAndAssociateWithCompany(validCreateRequest);

            // Then
            assertNotNull(resultado);
            assertEquals(10L, resultado.idStore());
            assertEquals("Tienda Centro", resultado.name());
            assertEquals("Av. Principal 123", resultado.address());
            assertEquals("Lima", resultado.city());
            assertEquals("Mi Empresa", resultado.companyName());
            assertEquals("20123456789", resultado.companyRuc());
            assertEquals(Boolean.TRUE, resultado.companyStatus());
        }

        @Test
        @DisplayName("Should persist store with status true and associated company")
        void shouldPersistStore_withStatusTrueAndAssociatedCompany() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

            // When
            storeService.createStoreAndAssociateWithCompany(validCreateRequest);

            // Then
            verify(storeRepository, times(1)).save(argThat(store ->
                    store.getStatus().equals(Boolean.TRUE)
                            && store.getCompany().equals(activeCompany)
                            && "Tienda Centro".equals(store.getName())
                            && "Lima".equals(store.getCity())
            ));
        }

        @Test
        @DisplayName("Should save default sales objective after creating store")
        void shouldSaveDefaultSalesObjective_afterCreatingStore() {
            // Given
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

            // When
            storeService.createStoreAndAssociateWithCompany(validCreateRequest);

            // Then
            verify(salesObjectiveRepository, times(1)).save(argThat(objective ->
                    objective.getStore().equals(savedStore)
                            && objective.getStatus() == SalesObjectiveStatus.ACTIVO
                            && "MENSUAL".equals(objective.getPeriodType())
                            && objective.getTargetAmount().doubleValue() == 1000.00
                            && objective.getStartDate() != null
                            && objective.getEndDate() != null
            ));
        }

        @Test
        @DisplayName("Should throw CompanyNotFoundException when company name does not exist")
        void shouldThrowCompanyNotFoundException_whenCompanyNameDoesNotExist() {
            // Given
            String nonExistentCompany = "Empresa Fantasma";
            CreateStoreRequest requestWithBadCompany = new CreateStoreRequest(
                    "Tienda X", "Dir X", "Ciudad X", nonExistentCompany);
            when(companyRepository.findByNameIgnoreCase(nonExistentCompany)).thenReturn(Optional.empty());

            // When
            CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class,
                    () -> storeService.createStoreAndAssociateWithCompany(requestWithBadCompany));

            // Then
            assertNotNull(exception.getMessage());
            verify(storeRepository, never()).save(any());
            verify(salesObjectiveRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateLocal()")
    class UpdateLocal {

        @Test
        @DisplayName("Should return updated StoreDto when store and company exist")
        void shouldReturnUpdatedStoreDto_whenStoreAndCompanyExist() {
            // Given
            Store updatedStore = Store.builder()
                    .idStore(10L)
                    .name("Tienda Centro Actualizada")
                    .address("Av. Secundaria 456")
                    .city("Arequipa")
                    .company(activeCompany)
                    .build();

            when(storeRepository.findById(10L)).thenReturn(Optional.of(savedStore));
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(storeRepository.save(any(Store.class))).thenReturn(updatedStore);

            // When
            StoreDto resultado = storeService.updateLocal(validUpdateRequest, 10L);

            // Then
            assertNotNull(resultado);
            assertEquals(10L, resultado.idStore());
            assertEquals("Tienda Centro Actualizada", resultado.name());
            assertEquals("Av. Secundaria 456", resultado.address());
            assertEquals("Mi Empresa", resultado.companyName());
            assertEquals("20123456789", resultado.companyRuc());
        }

        @Test
        @DisplayName("Should update store fields before persisting")
        void shouldUpdateStoreFields_beforePersisting() {
            // Given
            when(storeRepository.findById(10L)).thenReturn(Optional.of(savedStore));
            when(companyRepository.findByNameIgnoreCase("Mi Empresa")).thenReturn(Optional.of(activeCompany));
            when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

            // When
            storeService.updateLocal(validUpdateRequest, 10L);

            // Then
            verify(storeRepository, times(1)).save(argThat(store ->
                    "Tienda Centro Actualizada".equals(store.getName())
                            && "Av. Secundaria 456".equals(store.getAddress())
                            && "Arequipa".equals(store.getCity())
                            && store.getCompany().equals(activeCompany)
            ));
        }

        @Test
        @DisplayName("Should throw StoreNotFoundException when store id does not exist")
        void shouldThrowStoreNotFoundException_whenStoreIdDoesNotExist() {
            // Given
            Long nonExistentId = 99L;
            when(storeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When
            StoreNotFoundException exception = assertThrows(StoreNotFoundException.class,
                    () -> storeService.updateLocal(validUpdateRequest, nonExistentId));

            // Then
            assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
            verify(companyRepository, never()).findByNameIgnoreCase(any());
            verify(storeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CompanyNotFoundException when company name does not exist on update")
        void shouldThrowCompanyNotFoundException_whenCompanyNameDoesNotExistOnUpdate() {
            // Given
            String nonExistentCompany = "Empresa Fantasma";
            UpdateStoreRequest requestWithBadCompany = new UpdateStoreRequest(
                    "Tienda X", "Dir X", "Ciudad X", nonExistentCompany);
            when(storeRepository.findById(10L)).thenReturn(Optional.of(savedStore));
            when(companyRepository.findByNameIgnoreCase(nonExistentCompany)).thenReturn(Optional.empty());

            // When
            CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class,
                    () -> storeService.updateLocal(requestWithBadCompany, 10L));

            // Then
            assertNotNull(exception.getMessage());
            verify(storeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getStoresByCompanyId()")
    class GetStoresByCompanyId {

        @Test
        @DisplayName("Should return list of StoreDtos mapped correctly when stores exist")
        void shouldReturnMappedStoreDtoList_whenStoresExistForCompany() {
            // Given
            Store anotherStore = Store.builder()
                    .idStore(11L)
                    .name("Tienda Norte")
                    .address("Av. Norte 789")
                    .city("Trujillo")
                    .company(activeCompany)
                    .build();
            when(storeRepository.findByCompanyIdCompany(1L)).thenReturn(List.of(savedStore, anotherStore));

            // When
            List<StoreDto> resultado = storeService.getStoresByCompanyId(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals(10L, resultado.get(0).idStore());
            assertEquals(11L, resultado.get(1).idStore());
            assertEquals("Mi Empresa", resultado.get(0).companyName());
            assertEquals("Mi Empresa", resultado.get(1).companyName());
        }

        @Test
        @DisplayName("Should return empty list when company has no stores")
        void shouldReturnEmptyList_whenCompanyHasNoStores() {
            // Given
            when(storeRepository.findByCompanyIdCompany(99L)).thenReturn(List.of());

            // When
            List<StoreDto> resultado = storeService.getStoresByCompanyId(99L);

            // Then
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Should map all fields correctly from entity to StoreDto")
        void shouldMapAllFields_correctlyFromEntityToStoreDto() {
            // Given
            when(storeRepository.findByCompanyIdCompany(1L)).thenReturn(List.of(savedStore));

            // When
            List<StoreDto> resultado = storeService.getStoresByCompanyId(1L);

            // Then
            StoreDto dto = resultado.getFirst();
            assertEquals("Tienda Centro", dto.name());
            assertEquals("Av. Principal 123", dto.address());
            assertEquals("Lima", dto.city());
            assertEquals("20123456789", dto.companyRuc());
            assertEquals(Boolean.TRUE, dto.companyStatus());
        }

        @Test
        @DisplayName("Should query repository using the provided companyId")
        void shouldQueryRepository_usingProvidedCompanyId() {
            // Given
            when(storeRepository.findByCompanyIdCompany(1L)).thenReturn(List.of());

            // When
            storeService.getStoresByCompanyId(1L);

            // Then
            verify(storeRepository, times(1)).findByCompanyIdCompany(1L);
        }
    }
}