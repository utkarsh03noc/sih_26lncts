package com.aicte.grievance.repository;

import com.aicte.grievance.entity.Bureau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BureauRepository extends JpaRepository<Bureau, Long> {
    Optional<Bureau> findByCode(String code);
    boolean existsByCode(String code);
    List<Bureau> findByActiveTrueOrderByNameAsc();
}
