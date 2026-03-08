package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.SaleDto;
import com.upc.oss.monitoreo.dto.request.CreateSaleRequest;

public interface SaleService {
    SaleDto registerSale(CreateSaleRequest request);
}
