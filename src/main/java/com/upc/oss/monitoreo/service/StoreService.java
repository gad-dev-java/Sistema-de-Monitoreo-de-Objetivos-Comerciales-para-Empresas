package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.StoreDto;
import com.upc.oss.monitoreo.dto.request.CreateStoreRequest;
import com.upc.oss.monitoreo.dto.request.UpdateStoreRequest;

public interface StoreService {
    StoreDto createStoreAndAssociateWithCompany(CreateStoreRequest request);
    StoreDto updateLocal(UpdateStoreRequest request, Long idStore);
}
