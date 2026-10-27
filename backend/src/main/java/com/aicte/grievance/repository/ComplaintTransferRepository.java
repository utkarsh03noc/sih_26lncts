package com.aicte.grievance.repository;

import com.aicte.grievance.entity.ComplaintTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintTransferRepository extends JpaRepository<ComplaintTransfer, Long> {
    List<ComplaintTransfer> findByComplaintIdOrderByTransferredAtDesc(Long complaintId);
    List<ComplaintTransfer> findByComplaintIdOrderByTransferredAtAsc(Long complaintId);
    long countByComplaintId(Long complaintId);
}
