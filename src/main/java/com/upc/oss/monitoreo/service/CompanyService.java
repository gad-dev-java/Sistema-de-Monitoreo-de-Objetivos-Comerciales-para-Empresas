package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;

public interface CompanyService {
    CompanyDto createCompany(CreateCompanyRequest companyRequest);
    CompanyDto updateCompany(UpdateCompanyRequest companyRequest, Long idCompany);
    void deleteCompany(Long idCompany);
}
