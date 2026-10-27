package com.aicte.grievance.repository;

import com.aicte.grievance.entity.ComplaintUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintUpdateRepository extends JpaRepository<ComplaintUpdate, Long> {
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
}
