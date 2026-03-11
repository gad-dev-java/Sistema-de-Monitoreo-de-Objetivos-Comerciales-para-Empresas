package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.StoreDto;
import com.upc.oss.monitoreo.dto.request.CreateStoreRequest;
import com.upc.oss.monitoreo.dto.request.UpdateStoreRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.exception.StoreNotFoundException;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final CompanyRepository companyRepository;

    @Override
    public StoreDto createStoreAndAssociateWithCompany(CreateStoreRequest request) {
        Company companySaved = companyRepository.findByNameIgnoreCase(request.companyName())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with name " + request.name()));

        Store storeToSave = Store.builder()
                .name(request.name())
                .status(true)
                .address(request.address())
                .city(request.city())
                .company(companySaved)
                .build();

        Store storeSaved = storeRepository.save(storeToSave);

        return StoreDto.builder()
                .idStore(storeSaved.getIdStore())
                .name(storeSaved.getName())
                .address(storeSaved.getAddress())
                .city(storeSaved.getCity())
                .companyName(storeSaved.getCompany().getName())
                .companyRuc(storeSaved.getCompany().getRuc())
                .companyStatus(storeSaved.getCompany().getStatus())
                .build();
    }

    @Override
    public StoreDto updateLocal(UpdateStoreRequest request, Long idStore) {
        Store storeSaved = storeRepository.findById(idStore)
                .orElseThrow(() -> new StoreNotFoundException("Store not found with id " + idStore));

        Company companySaved = companyRepository.findByNameIgnoreCase(request.companyName())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with name " + request.name()));

        storeSaved.setName(request.name());
        storeSaved.setAddress(request.address());
        storeSaved.setCity(request.city());
        storeSaved.setCompany(companySaved);

        Store storeUpdated = storeRepository.save(storeSaved);

        return StoreDto.builder()
                .idStore(storeUpdated.getIdStore())
                .name(storeUpdated.getName())
                .address(storeUpdated.getAddress())
                .city(storeSaved.getCity())
                .companyName(storeSaved.getCompany().getName())
                .companyRuc(storeSaved.getCompany().getRuc())
                .companyStatus(storeSaved.getCompany().getStatus())
                .build();
    }

    @Override
    public List<StoreDto> getStoresByCompanyId(Long idCompany) {
        return storeRepository.findByCompanyIdCompany(idCompany)
                .stream()
                .map(store -> StoreDto.builder()
                        .idStore(store.getIdStore())
                        .name(store.getName())
                        .address(store.getAddress())
                        .city(store.getCity())
                        .companyName(store.getCompany().getName())
                        .companyRuc(store.getCompany().getRuc())
                        .companyStatus(store.getCompany().getStatus())
                        .build())
                .toList();
    }
}
