package com.notifyhub.integrity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IntegrityViolationRepository extends JpaRepository<IntegrityViolationEntity, Long>, JpaSpecificationExecutor<IntegrityViolationEntity> {
    List<IntegrityViolationEntity> findByStatusAndTargetSchema(String status, String targetSchema);
}
