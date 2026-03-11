package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.SaleService;
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
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<DataResponse<List<SaleDto>>>  findAllByStoreId(@PathVariable Long storeId) {
        List<SaleDto> saleDtos = saleService.getSalesByStoreId(storeId);
        DataResponse<List<SaleDto>> response = DataResponse.<List<SaleDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Sales fetching successfully")
                .data(saleDtos)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DataResponse<SaleDto>> createSale(@RequestBody CreateSaleRequest request) {
        SaleDto saleDto = saleService.registerSale(request);

        DataResponse<SaleDto> response = DataResponse.<SaleDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Store created successfully")
                .data(saleDto)
                .timestamp(LocalDateTime.now())
                .build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saleDto.idSale())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
