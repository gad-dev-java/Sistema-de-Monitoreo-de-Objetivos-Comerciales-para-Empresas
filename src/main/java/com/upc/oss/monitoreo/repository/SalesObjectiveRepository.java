package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.SalesObjective;
import com.upc.oss.monitoreo.enums.SalesObjectiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesObjectiveRepository extends JpaRepository<SalesObjective, Long> {
    @Query("""
            SELECT so
            FROM SalesObjective so
            WHERE so.store.idStore = :storeId
            AND so.status = :status
            """)
    Optional<SalesObjective> findActiveObjectiveByStoreId(Long storeId, SalesObjectiveStatus status);

    List<SalesObjective> findByStoreIdStore(Long idStore);
}
