package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;
import com.upc.oss.monitoreo.entities.Sale;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.SaleRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.SaleService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;

    @Override
    public SaleDto registerSale(CreateSaleRequest request) {
        Store storeFound =  storeRepository.findByNameIgnoreCase(request.storeName())
                .orElseThrow(()-> new StoreNotFoundException("Store not found with name " + request.storeName()));

        Sale saleToSave = Sale.builder()
                .store(storeFound)
                .amount(request.amount())
                .description(request.description())
                .build();

        Sale saleSaved = saleRepository.save(saleToSave);

        return SaleDto.builder()
                .idSale(saleSaved.getIdSale())
                .storeName(saleSaved.getStore().getName())
                .storeStatus(saleSaved.getStore().getStatus())
                .saleDate(saleSaved.getSaleDate())
                .amount(saleSaved.getAmount())
                .description(saleSaved.getDescription())
                .build();
    }
}
