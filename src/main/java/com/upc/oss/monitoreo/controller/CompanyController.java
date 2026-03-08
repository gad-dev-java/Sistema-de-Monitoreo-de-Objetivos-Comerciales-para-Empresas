package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.CompanyDto;
import com.upc.oss.monitoreo.dto.request.CreateCompanyRequest;
import com.upc.oss.monitoreo.dto.request.UpdateCompanyRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<DataResponse<CompanyDto>> createCompany(@RequestBody CreateCompanyRequest request) {
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
    public ResponseEntity<DataResponse<CompanyDto>> updateCompany(@PathVariable Long id, @RequestBody UpdateCompanyRequest request) {
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
