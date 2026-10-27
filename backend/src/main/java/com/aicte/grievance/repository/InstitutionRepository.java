package com.aicte.grievance.repository;

import com.aicte.grievance.entity.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByAicteReferenceCode(String aicteReferenceCode);
    List<Institution> findByActiveTrue();

    @Query("SELECT i FROM Institution i WHERE " +
           "(:query IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(i.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(i.district, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(i.aicteReferenceCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.type) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Institution> searchInstitutions(@Param("query") String query, Pageable pageable);
}
