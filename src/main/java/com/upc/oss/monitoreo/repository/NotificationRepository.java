package com.upc.oss.monitoreo.repository;

import com.upc.oss.monitoreo.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStoreIdStoreAndIsReadFalseOrderByGeneratedAtDesc(Long storeId);
}
