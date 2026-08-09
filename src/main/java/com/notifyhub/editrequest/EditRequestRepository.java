package com.notifyhub.editrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EditRequestRepository extends JpaRepository<EditRequestEntity, Long>, JpaSpecificationExecutor<EditRequestEntity> {
}
