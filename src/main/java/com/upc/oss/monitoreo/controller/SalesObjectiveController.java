package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.SalesObjectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales-objectives")
public class SalesObjectiveController {
    private final SalesObjectiveService salesObjectiveService;

    @PostMapping
    public ResponseEntity<DataResponse<SalesObjectiveDto>> createSalesObjective(@RequestBody CreateSalesObjectiveRequest request) {
        SalesObjectiveDto salesObjectiveDto = salesObjectiveService.recordMonthlyGoalAndAssociateWithStore(request);

        DataResponse<SalesObjectiveDto> response = DataResponse.<SalesObjectiveDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Sales Objective created successfully")
                .data(salesObjectiveDto)
                .timestamp(LocalDateTime.now())
                .build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salesObjectiveDto.idObjective())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
