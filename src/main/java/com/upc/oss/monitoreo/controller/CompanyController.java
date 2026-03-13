package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<DataResponse<List<CompanyDto>>> getCompanies() {
        List<CompanyDto> companies = companyService.getCompanies();
        DataResponse<List<CompanyDto>> response = DataResponse.<List<CompanyDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Company fetched successfully")
                .data(companies)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DataResponse<CompanyDto>> createCompany(@RequestBody @Valid CreateCompanyRequest request) {
        CompanyDto savedCompany = companyService.createCompany(request);

        DataResponse<CompanyDto> response = DataResponse.<CompanyDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Company created successfully")
                .data(savedCompany)
                .timestamp(LocalDateTime.now())
                .build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCompany.idCompany())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataResponse<CompanyDto>> updateCompany(@PathVariable Long id, @RequestBody @Valid UpdateCompanyRequest request) {
        CompanyDto companyDto = companyService.updateCompany(request, id);
        DataResponse<CompanyDto> response = DataResponse.<CompanyDto>builder()
                .status(HttpStatus.OK.value())
                .message("Company updated successfully")
                .data(companyDto)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
