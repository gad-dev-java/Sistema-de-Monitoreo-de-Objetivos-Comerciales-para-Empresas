package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.SalesObjectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales-objectives")
public class SalesObjectiveController {
    private final SalesObjectiveService salesObjectiveService;

    @GetMapping("/store/{idStore}")
    public ResponseEntity<DataResponse<List<SalesObjectiveDto>>> findByIdStore(@PathVariable Long idStore) {
        List<SalesObjectiveDto> salesObjectiveDtos = salesObjectiveService.getByStoreId(idStore);
        DataResponse<List<SalesObjectiveDto>> response = DataResponse.<List<SalesObjectiveDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Sales Objectives fetching successfully")
                .data(salesObjectiveDtos)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

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
