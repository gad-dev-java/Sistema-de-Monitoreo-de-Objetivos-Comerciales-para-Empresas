package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByRucOrNameIgnoreCase(String ruc, String name);
    Boolean existsByNameIgnoreCase(String name);
    Optional<Company> findByNameIgnoreCase(String name);
}
