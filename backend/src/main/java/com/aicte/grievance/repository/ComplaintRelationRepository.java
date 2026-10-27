package com.aicte.grievance.repository;

import com.aicte.grievance.entity.CaseRelationType;
import com.aicte.grievance.entity.ComplaintRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRelationRepository extends JpaRepository<ComplaintRelation, Long> {

    @Query("SELECT r FROM ComplaintRelation r WHERE r.primaryComplaint.id = :primaryId AND r.secondaryComplaint.id = :secondaryId AND r.relationType = :relationType AND r.active = true")
    Optional<ComplaintRelation> findActiveRelation(@Param("primaryId") Long primaryId,
                                                   @Param("secondaryId") Long secondaryId,
                                                   @Param("relationType") CaseRelationType relationType);

    @Query("SELECT r FROM ComplaintRelation r WHERE r.secondaryComplaint.id = :secondaryId AND r.relationType = 'MERGED' AND r.active = true")
    Optional<ComplaintRelation> findActiveMergedRelationBySecondaryId(@Param("secondaryId") Long secondaryId);

    @Query("SELECT r FROM ComplaintRelation r WHERE r.primaryComplaint.id = :primaryId AND r.relationType = 'MERGED' AND r.active = true")
    List<ComplaintRelation> findActiveMergeRelationsByPrimaryId(@Param("primaryId") Long primaryId);

    @Query("SELECT r FROM ComplaintRelation r WHERE (r.primaryComplaint.id = :complaintId OR r.secondaryComplaint.id = :complaintId) AND r.relationType = 'RELATED' AND r.active = true")
    List<ComplaintRelation> findActiveRelatedRelationsByComplaintId(@Param("complaintId") Long complaintId);

    @Query("SELECT r FROM ComplaintRelation r WHERE r.primaryComplaint.id = :complaintId OR r.secondaryComplaint.id = :complaintId ORDER BY r.createdAt DESC")
    List<ComplaintRelation> findAllHistoryByComplaintId(@Param("complaintId") Long complaintId);

    @Query("SELECT r FROM ComplaintRelation r WHERE " +
           "((r.primaryComplaint.id = :id1 AND r.secondaryComplaint.id = :id2) OR " +
           " (r.primaryComplaint.id = :id2 AND r.secondaryComplaint.id = :id1)) AND r.active = true")
    List<ComplaintRelation> findActiveBidirectionalRelations(@Param("id1") Long id1, @Param("id2") Long id2);
}
