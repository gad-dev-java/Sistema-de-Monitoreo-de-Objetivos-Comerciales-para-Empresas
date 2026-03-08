package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("""
            SELECT COALESCE(SUM(s.amount), 0)
            FROM Sale s
            WHERE s.store.idStore = :storeId
            AND s.saleDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumSalesByStoreAndPeriod(Long storeId, LocalDate startDate, LocalDate endDate);
}
