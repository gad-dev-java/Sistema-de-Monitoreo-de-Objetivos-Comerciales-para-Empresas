package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.StoreDto;
import com.upc.oss.monitoreo.dto.request.CreateStoreRequest;
import com.upc.oss.monitoreo.dto.request.UpdateStoreRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<DataResponse<StoreDto>> createStore(@RequestBody CreateStoreRequest request) {
        StoreDto storeDto = storeService.createStoreAndAssociateWithCompany(request);

        DataResponse<StoreDto> response = DataResponse.<StoreDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Store created successfully")
                .data(storeDto)
                .timestamp(LocalDateTime.now())
                .build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(storeDto.idStore())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataResponse<StoreDto>> updateStore(@PathVariable Long id, @RequestBody UpdateStoreRequest request) {
        StoreDto storeDto = storeService.updateLocal(request, id);

        DataResponse<StoreDto> response = DataResponse.<StoreDto>builder()
                .status(HttpStatus.OK.value())
                .message("Store updated successfully")
                .data(storeDto)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
