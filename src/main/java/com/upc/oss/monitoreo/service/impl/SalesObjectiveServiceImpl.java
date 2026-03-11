package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.SalesObjectiveDto;
import com.upc.oss.monitoreo.dto.request.CreateSalesObjectiveRequest;
import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import com.upc.oss.monitoreo.exception.InvalidDateRangeException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.SalesObjectiveRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.SalesObjectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesObjectiveServiceImpl implements SalesObjectiveService {
    private final SalesObjectiveRepository salesObjectiveRepository;
    private final StoreRepository storeRepository;

    @Override
    public SalesObjectiveDto recordMonthlyGoalAndAssociateWithStore(CreateSalesObjectiveRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidDateRangeException("End date must be after start date");
        }

        Store storeSaved = storeRepository.findByNameIgnoreCase(request.nameStore())
                .orElseThrow(() -> new StoreNotFoundException("Store not found with name " + request.nameStore()));

        salesObjectiveRepository.findActiveObjectiveByStoreId(storeSaved.getIdStore(), SalesObjectiveStatus.ACTIVO)
                .ifPresent(oldObjective -> {
                    oldObjective.setStatus(SalesObjectiveStatus.FINALIZADO);
                    salesObjectiveRepository.save(oldObjective);
                });

        SalesObjective newObjective = SalesObjective.builder()
                .store(storeSaved)
                .status(SalesObjectiveStatus.ACTIVO)
                .targetAmount(request.targetAmount())
                .periodType(request.periodType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        SalesObjective salesObjectiveSaved = salesObjectiveRepository.save(newObjective);

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

    @Override
    public List<SalesObjectiveDto> getByStoreId(Long idStore) {
        return salesObjectiveRepository.findByStoreIdStore(idStore)
                .stream()
                .map(objective -> SalesObjectiveDto.builder()
                        .idObjective(objective.getIdObjective())
                        .nameStore(objective.getStore().getName())
                        .statusStore(objective.getStore().getStatus())
                        .targetAmount(objective.getTargetAmount())
                        .periodType(objective.getPeriodType())
                        .startDate(objective.getStartDate())
                        .endDate(objective.getEndDate())
                        .build())
                .toList();
    }
}
