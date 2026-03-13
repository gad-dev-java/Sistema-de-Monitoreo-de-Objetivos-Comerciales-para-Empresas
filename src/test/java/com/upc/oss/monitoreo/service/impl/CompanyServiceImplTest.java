package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company empresa1;
    private Company empresa2;

    @BeforeEach
    void setUp() {

        empresa1 = Company.builder()
                .idCompany(1L)
                .name("Corporación Textil del Norte")
                .ruc("20123456789")
                .status(true)
                .build();

        empresa2 = Company.builder()
                .idCompany(2L)
                .name("Alimentos & Sabores S.A.")
                .ruc("20987654321")
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe listar todas las empresas")
    void getCompanies_DebeRetornarListaEmpresas() {

        List<Company> empresas = Arrays.asList(empresa1, empresa2);

        when(companyRepository.findAll()).thenReturn(empresas);

        List<CompanyDto> resultado = companyService.getCompanies();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        verify(companyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe registrar una empresa")
    void createCompany_DebeGuardarEmpresa() {

        CreateCompanyRequest request =
                new CreateCompanyRequest("Tecnología Global Perú", "20556677889");

        Company empresaGuardada = Company.builder()
                .idCompany(3L)
                .name("Tecnología Global Perú")
                .ruc("20556677889")
                .status(true)
                .build();

        when(companyRepository.save(any(Company.class)))
                .thenReturn(empresaGuardada);

        CompanyDto resultado = companyService.createCompany(request);

        assertNotNull(resultado);
        assertEquals("Tecnología Global Perú", resultado.name());

        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Debe actualizar empresa existente")
    void updateCompany_CuandoExiste_DebeActualizar() {

        UpdateCompanyRequest request =
                new UpdateCompanyRequest("Textil del Norte Actualizado", "20123456789");

        Company empresaActualizada = Company.builder()
                .idCompany(1L)
                .name("Textil del Norte Actualizado")
                .ruc("20123456789")
                .status(true)
                .build();

        when(companyRepository.findById(1L))
                .thenReturn(Optional.of(empresa1));

        when(companyRepository.save(any(Company.class)))
                .thenReturn(empresaActualizada);

        CompanyDto resultado = companyService.updateCompany(request, 1L);

        assertNotNull(resultado);
        assertEquals("Textil del Norte Actualizado", resultado.name());

        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Debe eliminar empresa existente")
    void deleteCompany_CuandoExiste_DebeEliminar() {

        when(companyRepository.findById(1L))
                .thenReturn(Optional.of(empresa1));

        companyService.deleteCompany(1L);

        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si empresa no existe")
    void deleteCompany_CuandoNoExiste_DebeLanzarError() {

        when(companyRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            companyService.deleteCompany(99L);
        });

        verify(companyRepository, times(1)).findById(99L);
        verify(companyRepository, never()).delete(any());
    }
}
