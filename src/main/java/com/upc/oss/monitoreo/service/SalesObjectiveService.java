package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;

public interface SalesObjectiveService {
    SalesObjectiveDto recordMonthlyGoalAndAssociateWithStore(CreateSalesObjectiveRequest request);
}
