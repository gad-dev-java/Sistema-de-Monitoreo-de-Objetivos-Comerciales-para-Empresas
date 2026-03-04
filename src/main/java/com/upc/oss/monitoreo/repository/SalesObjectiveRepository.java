package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.SalesObjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesObjectiveRepository extends JpaRepository<SalesObjective, Long> {
}
