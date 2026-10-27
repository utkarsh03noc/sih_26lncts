package com.aicte.grievance.repository;

import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    long countByStatus(ComplaintStatus status);

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndStatus(Long institutionId, ComplaintStatus status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.dueDate < :today AND c.status NOT IN ('RESOLVED', 'CLOSED')")
    long countOverdueComplaints(@Param("today") LocalDate today);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.institution.id = :institutionId AND c.dueDate < :today AND c.status NOT IN ('RESOLVED', 'CLOSED')")
    long countInstitutionOverdueComplaints(@Param("institutionId") Long institutionId, @Param("today") LocalDate today);

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countGroupedByCategory();

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countGroupedByPriority();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c WHERE c.institution.id = :institutionId GROUP BY c.status")
    List<Object[]> countGroupedByStatusForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c WHERE c.institution.id = :institutionId GROUP BY c.category")
    List<Object[]> countGroupedByCategoryForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c WHERE c.institution.id = :institutionId GROUP BY c.priority")
    List<Object[]> countGroupedByPriorityForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT c.institution.id, c.institution.name, COUNT(c), " +
           "SUM(CASE WHEN c.status = 'RESOLVED' OR c.status = 'CLOSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status = 'ESCALATED' THEN 1 ELSE 0 END) " +
           "FROM Complaint c GROUP BY c.institution.id, c.institution.name")
    List<Object[]> getInstitutionWiseComplaintStats();

    @Query("SELECT c.institution.id, c.institution.name, c.institution.code, " +
           "COALESCE(c.institution.state, 'N/A'), COALESCE(c.institution.district, 'N/A'), COUNT(c), " +
           "SUM(CASE WHEN c.status = 'RESOLVED' OR c.status = 'CLOSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.dueDate < :today AND c.status NOT IN ('RESOLVED', 'CLOSED') THEN 1 ELSE 0 END), " +
           "COUNT(DISTINCT c.bureau.id), COUNT(DISTINCT c.cell.id) " +
           "FROM Complaint c GROUP BY c.institution.id, c.institution.name, c.institution.code, c.institution.state, c.institution.district")
    List<Object[]> getInstitutionMonitoringStats(@Param("today") LocalDate today);

    @Query("SELECT c.bureau.id, c.bureau.name, c.bureau.code, COUNT(c), " +
           "SUM(CASE WHEN c.status = 'RESOLVED' OR c.status = 'CLOSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN c.status NOT IN ('RESOLVED', 'CLOSED') THEN 1 ELSE 0 END) " +
           "FROM Complaint c WHERE c.bureau IS NOT NULL GROUP BY c.bureau.id, c.bureau.name, c.bureau.code")
    List<Object[]> getBureauWiseComplaintStats();

    @Query("SELECT c.cell.id, c.cell.name, c.cell.code, COUNT(c) " +
           "FROM Complaint c WHERE c.cell IS NOT NULL GROUP BY c.cell.id, c.cell.name, c.cell.code")
    List<Object[]> getCellWiseComplaintStats();

    long countByPotentialDuplicateTrue();

    Page<Complaint> findByPotentialDuplicateTrueOrderByCreatedAtDesc(Pageable pageable);

    List<Complaint> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);

    List<Complaint> findByInstitutionId(Long institutionId);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:institutionId IS NULL OR c.institution.id = :institutionId) AND " +
           "(:bureauId IS NULL OR c.bureau.id = :bureauId) AND " +
           "(:cellId IS NULL OR c.cell.id = :cellId) AND " +
           "(:complainantId IS NULL OR c.complainant.id = :complainantId) AND " +
           "(:officerId IS NULL OR c.assignedOfficer.id = :officerId) AND " +
           "(:search IS NULL OR LOWER(c.complaintNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Complaint> findFilteredComplaints(
            @Param("status") ComplaintStatus status,
            @Param("category") ComplaintCategory category,
            @Param("priority") Priority priority,
            @Param("institutionId") Long institutionId,
            @Param("bureauId") Long bureauId,
            @Param("cellId") Long cellId,
            @Param("complainantId") Long complainantId,
            @Param("officerId") Long officerId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT c FROM Complaint c WHERE c.status = 'ESCALATED' OR (c.dueDate < :today AND c.status NOT IN ('RESOLVED', 'CLOSED'))")
    Page<Complaint> findEscalatedAndOverdueComplaints(@Param("today") LocalDate today, Pageable pageable);
}
