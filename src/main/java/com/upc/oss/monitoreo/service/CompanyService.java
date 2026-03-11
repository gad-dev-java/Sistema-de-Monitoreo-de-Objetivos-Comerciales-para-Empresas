package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;

import java.util.List;

public interface CompanyService {
    CompanyDto createCompany(CreateCompanyRequest companyRequest);
    CompanyDto updateCompany(UpdateCompanyRequest companyRequest, Long idCompany);
    void deleteCompany(Long idCompany);
    List<CompanyDto> getCompanies();
}
