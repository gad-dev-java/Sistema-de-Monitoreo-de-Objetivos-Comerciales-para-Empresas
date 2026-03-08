package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.SalesObjectiveRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.SalesObjectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;

@Service
@RequiredArgsConstructor
public class SalesObjectiveServiceImpl implements SalesObjectiveService {
    private final SalesObjectiveRepository salesObjectiveRepository;
    private final StoreRepository storeRepository;

    @Override
    public SalesObjectiveDto recordMonthlyGoalAndAssociateWithStore(CreateSalesObjectiveRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new DateTimeException("Date range not valid");
        }

        Store storeSaved = storeRepository.findByNameIgnoreCase(request.nameStore())
                .orElseThrow(() -> new StoreNotFoundException("Store not found with name " + request.nameStore()));

        SalesObjective salesObjectiveToSave = SalesObjective.builder()
                .store(storeSaved)
                .targetAmount(request.targetAmount())
                .periodType(request.periodType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        SalesObjective salesObjectiveSaved = salesObjectiveRepository.save(salesObjectiveToSave);

        return SalesObjectiveDto.builder()
                .idObjective(salesObjectiveSaved.getIdObjective())
                .nameStore(salesObjectiveSaved.getStore().getName())
                .statusStore(salesObjectiveSaved.getStore().getStatus())
                .targetAmount(salesObjectiveSaved.getTargetAmount())
                .periodType(salesObjectiveSaved.getPeriodType())
                .startDate(salesObjectiveSaved.getStartDate())
                .endDate(salesObjectiveSaved.getEndDate())
                .build();
    }
}
