package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;

import java.util.List;

public interface SaleService {
    SaleDto registerSale(CreateSaleRequest request);
    List<SaleDto> getSalesByStoreId(Long storeId);
}
