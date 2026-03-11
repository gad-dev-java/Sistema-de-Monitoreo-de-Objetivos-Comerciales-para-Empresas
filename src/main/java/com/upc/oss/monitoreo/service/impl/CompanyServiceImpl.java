package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.exception.CompanyAlreadyExistsException;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import com.upc.oss.monitoreo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Override
    public CompanyDto createCompany(CreateCompanyRequest companyRequest) {
        if (companyRepository.findByRucOrNameIgnoreCase(companyRequest.ruc(), companyRequest.name()).isPresent()) {
            throw new CompanyAlreadyExistsException("Company already exists");
        }
        Company companyToCreate = Company.builder()
                .name(companyRequest.name())
                .ruc(companyRequest.ruc())
                .status(true)
                .build();

        Company companyCreated = companyRepository.save(companyToCreate);

        return CompanyDto.builder()
                .idCompany(companyCreated.getIdCompany())
                .name(companyCreated.getName())
                .ruc(companyCreated.getRuc())
                .status(companyCreated.getStatus())
                .createdAt(companyCreated.getCreatedAt())
                .build();
    }

    @Override
    public CompanyDto updateCompany(UpdateCompanyRequest companyRequest, Long idCompany) {
        Company companySaved = companyRepository.findById(idCompany)
                .orElseThrow(()-> new CompanyNotFoundException("Company with id " + idCompany + " not found"));

        companySaved.setName(companyRequest.name());
        companySaved.setRuc(companyRequest.ruc());

        Company companyUpdated = companyRepository.save(companySaved);

        return CompanyDto.builder()
                .idCompany(companyUpdated.getIdCompany())
                .name(companyUpdated.getName())
                .ruc(companyUpdated.getRuc())
                .status(companyUpdated.getStatus())
                .createdAt(companyUpdated.getCreatedAt())
                .build();
    }

    @Override
    public void deleteCompany(Long idCompany) {
        Company companySaved = companyRepository.findById(idCompany)
                .orElseThrow(()-> new CompanyNotFoundException("Company with id " + idCompany + " not found"));
        companySaved.setStatus(false);
        companyRepository.save(companySaved);
    }

    @Override
    public List<CompanyDto> getCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(companyList -> CompanyDto.builder()
                        .idCompany(companyList.getIdCompany())
                        .name(companyList.getName())
                        .ruc(companyList.getRuc())
                        .status(companyList.getStatus())
                        .createdAt(companyList.getCreatedAt())
                        .build())
                .toList();
    }
}
