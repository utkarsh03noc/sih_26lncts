package com.aicte.grievance.repository;

import com.aicte.grievance.entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {
    Optional<Cell> findByCode(String code);
    boolean existsByCode(String code);
    List<Cell> findByActiveTrueOrderByNameAsc();
    List<Cell> findByBureauIdAndActiveTrue(Long bureauId);
}
