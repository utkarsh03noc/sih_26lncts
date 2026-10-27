package com.aicte.grievance.repository;

import com.aicte.grievance.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);
    Page<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId, Pageable pageable);
    List<AuditLog> findTop50ByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);
}
