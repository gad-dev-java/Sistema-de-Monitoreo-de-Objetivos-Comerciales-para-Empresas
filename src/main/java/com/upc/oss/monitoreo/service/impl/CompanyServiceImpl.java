package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.exception.CompanyAlreadyExists;
import com.upc.oss.monitoreo.exception.CompanyNotFound;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import com.upc.oss.monitoreo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Override
    public CompanyDto createCompany(CreateCompanyRequest companyRequest) {
        if (companyRepository.findByRucOrNameIgnoreCase(companyRequest.ruc(), companyRequest.name()).isPresent()) {
            throw new CompanyAlreadyExists("Company already exists");
        }
        Company companyToCreate = Company.builder()
                .name(companyRequest.name())
                .ruc(companyRequest.ruc())
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
                .orElseThrow(()-> new CompanyNotFound("Company with id " + idCompany + " not found"));

        companySaved.setName(companyRequest.name());
        companySaved.setRuc(companyRequest.ruc());

        Company companyUpdated = companyRepository.save(companySaved);

        return CompanyDto.builder()
                .idCompany(companySaved.getIdCompany())
                .name(companySaved.getName())
                .ruc(companySaved.getRuc())
                .status(companySaved.getStatus())
                .createdAt(companySaved.getCreatedAt())
                .build();
    }

    @Override
    public void deleteCompany(Long idCompany) {
        Company companySaved = companyRepository.findById(idCompany)
                .orElseThrow(()-> new CompanyNotFound("Company with id " + idCompany + " not found"));
    }
}
