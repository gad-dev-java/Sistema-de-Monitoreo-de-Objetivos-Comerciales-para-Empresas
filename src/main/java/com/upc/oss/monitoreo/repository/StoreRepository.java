package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByNameIgnoreCase(String name);
    List<Store> findByCompanyIdCompany(Long idCompany);
    List<Store> findByStatusTrue();
    List<Store> findByCompanyIdCompanyAndStatusTrue(Long companyId);
}
