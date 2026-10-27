package com.aicte.grievance.service;

import com.aicte.grievance.dto.*;
import com.aicte.grievance.entity.Complaint;
import com.aicte.grievance.entity.ComplaintCategory;
import com.aicte.grievance.entity.ComplaintStatus;
import com.aicte.grievance.entity.Priority;
import com.aicte.grievance.repository.ComplaintRepository;
import com.aicte.grievance.repository.InstitutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final InstitutionRepository institutionRepository;

    public DashboardService(ComplaintRepository complaintRepository,
                            InstitutionRepository institutionRepository) {
        this.complaintRepository = complaintRepository;
        this.institutionRepository = institutionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getOverallDashboardStats() {
        return getFilteredDashboardStats(null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getFilteredDashboardStats(Long institutionId, Long bureauId, Long cellId, ComplaintStatus status, Priority priority) {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalInstitutions(institutionRepository.count());
        stats.setPotentialDuplicateCount(complaintRepository.countByPotentialDuplicateTrue());

        List<Complaint> all = complaintRepository.findAll().stream()
                .filter(c -> institutionId == null || (c.getInstitution() != null && c.getInstitution().getId().equals(institutionId)))
                .filter(c -> bureauId == null || (c.getBureau() != null && c.getBureau().getId().equals(bureauId)))
                .filter(c -> cellId == null || (c.getCell() != null && c.getCell().getId().equals(cellId)))
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> priority == null || c.getPriority() == priority)
                .toList();

        stats.setTotalComplaints(all.size());

        LocalDate today = LocalDate.now();

        long pending = 0;
        long inProgress = 0;
        long resolved = 0;
        long closed = 0;
        long escalated = 0;
        long overdue = 0;

        Map<ComplaintCategory, Long> categoryCount = new HashMap<>();
        Map<Priority, Long> priorityCount = new HashMap<>();
        Map<ComplaintStatus, Long> statusCount = new HashMap<>();
        Map<String, long[]> monthlyMap = new LinkedHashMap<>(); // month -> [total, resolved]

        // Initialize last 6 months in chronological order
        for (int i = 5; i >= 0; i--) {
            LocalDate m = today.minusMonths(i);
            String monthLabel = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + m.getYear();
            monthlyMap.put(monthLabel, new long[]{0, 0});
        }

        for (Complaint c : all) {
            ComplaintStatus st = c.getStatus();
            statusCount.put(st, statusCount.getOrDefault(st, 0L) + 1);

            ComplaintCategory cat = c.getCategory();
            if (cat != null) {
                categoryCount.put(cat, categoryCount.getOrDefault(cat, 0L) + 1);
            }

            Priority pr = c.getPriority();
            if (pr != null) {
                priorityCount.put(pr, priorityCount.getOrDefault(pr, 0L) + 1);
            }

            if (st == ComplaintStatus.SUBMITTED || st == ComplaintStatus.ASSIGNED || st == ComplaintStatus.UNDER_REVIEW) {
                pending++;
            } else if (st == ComplaintStatus.IN_PROGRESS || st == ComplaintStatus.REOPENED) {
                inProgress++;
            } else if (st == ComplaintStatus.RESOLVED) {
                resolved++;
            } else if (st == ComplaintStatus.CLOSED) {
                closed++;
            } else if (st == ComplaintStatus.ESCALATED) {
                escalated++;
            }

            if (st != ComplaintStatus.RESOLVED && st != ComplaintStatus.CLOSED) {
                if (c.getDueDate() != null && c.getDueDate().isBefore(today)) {
                    overdue++;
                }
            }

            // Monthly Trend
            if (c.getCreatedAt() != null) {
                String monthKey = c.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + c.getCreatedAt().getYear();
                if (monthlyMap.containsKey(monthKey)) {
                    monthlyMap.get(monthKey)[0]++;
                    if (st == ComplaintStatus.RESOLVED || st == ComplaintStatus.CLOSED) {
                        monthlyMap.get(monthKey)[1]++;
                    }
                }
            }
        }

        stats.setPendingComplaints(pending);
        stats.setInProgressComplaints(inProgress);
        stats.setResolvedComplaints(resolved);
        stats.setClosedComplaints(closed);
        stats.setEscalatedComplaints(escalated);
        stats.setOverdueComplaints(overdue);

        long resolvedAndClosed = resolved + closed;
        double rate = all.isEmpty() ? 0.0 : Math.round(((double) resolvedAndClosed / all.size() * 100.0) * 10.0) / 10.0;
        stats.setResolutionRate(rate);

        // Populate Category Stats
        List<CategoryStatDto> catList = new ArrayList<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            long count = categoryCount.getOrDefault(cat, 0L);
            catList.add(new CategoryStatDto(cat.name(), cat.getDisplayName(), count));
        }
        stats.setCategoryStats(catList);

        // Populate Priority Stats
        List<PriorityStatDto> priList = new ArrayList<>();
        for (Priority pr : Priority.values()) {
            long count = priorityCount.getOrDefault(pr, 0L);
            priList.add(new PriorityStatDto(pr.name(), count));
        }
        stats.setPriorityStats(priList);

        // Populate Status Stats
        List<StatusStatDto> stList = new ArrayList<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            long count = statusCount.getOrDefault(s, 0L);
            stList.add(new StatusStatDto(s.name(), count));
        }
        stats.setStatusStats(stList);

        // Populate Monthly Trend
        List<MonthlyTrendDto> trendList = new ArrayList<>();
        monthlyMap.forEach((month, counts) -> {
            trendList.add(new MonthlyTrendDto(month, counts[0], counts[1]));
        });
        stats.setMonthlyTrend(trendList);

        // Populate Institution-wise Stats
        List<Object[]> rawInstStats = complaintRepository.getInstitutionWiseComplaintStats();
        List<InstitutionComplaintStatDto> instList = new ArrayList<>();
        for (Object[] row : rawInstStats) {
            Long instId = ((Number) row[0]).longValue();
            if (institutionId != null && !institutionId.equals(instId)) {
                continue;
            }
            String instName = (String) row[1];
            long total = ((Number) row[2]).longValue();
            long res = ((Number) row[3]).longValue();
            long esc = ((Number) row[4]).longValue();
            double resRate = total > 0 ? Math.round(((double) res / total * 100.0) * 10.0) / 10.0 : 0.0;
            instList.add(new InstitutionComplaintStatDto(instId, instName, total, res, esc, resRate));
        }
        stats.setInstitutionStats(instList);

        // Populate Bureau Stats
        List<BureauStatDto> bureauStats = new ArrayList<>();
        try {
            List<Object[]> rawBureauStats = complaintRepository.getBureauWiseComplaintStats();
            for (Object[] row : rawBureauStats) {
                Long bId = ((Number) row[0]).longValue();
                String bName = (String) row[1];
                String bCode = (String) row[2];
                long bTotal = ((Number) row[3]).longValue();
                long bResolved = ((Number) row[4]).longValue();
                long bPending = ((Number) row[5]).longValue();
                bureauStats.add(new BureauStatDto(bId, bName, bCode, bTotal, bResolved, bPending));
            }
        } catch (Exception ignored) {
        }
        stats.setBureauStats(bureauStats);

        // Populate Cell Stats
        List<CellStatDto> cellStats = new ArrayList<>();
        try {
            List<Object[]> rawCellStats = complaintRepository.getCellWiseComplaintStats();
            for (Object[] row : rawCellStats) {
                Long cId = ((Number) row[0]).longValue();
                String cName = (String) row[1];
                String cCode = (String) row[2];
                long cTotal = ((Number) row[3]).longValue();
                cellStats.add(new CellStatDto(cId, cName, cCode, cTotal));
            }
        } catch (Exception ignored) {
        }
        stats.setCellStats(cellStats);

        return stats;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getInstitutionDashboardStats(Long institutionId) {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalInstitutions(1);
        stats.setPotentialDuplicateCount(0);

        List<Complaint> all = complaintRepository.findAll().stream()
                .filter(c -> c.getInstitution() != null && c.getInstitution().getId().equals(institutionId))
                .toList();

        stats.setTotalComplaints(all.size());
        LocalDate today = LocalDate.now();

        long pending = 0;
        long inProgress = 0;
        long resolved = 0;
        long closed = 0;
        long escalated = 0;
        long overdue = 0;

        Map<ComplaintCategory, Long> categoryCount = new HashMap<>();
        Map<Priority, Long> priorityCount = new HashMap<>();
        Map<ComplaintStatus, Long> statusCount = new HashMap<>();
        Map<String, long[]> monthlyMap = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate m = today.minusMonths(i);
            String monthLabel = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + m.getYear();
            monthlyMap.put(monthLabel, new long[]{0, 0});
        }

        for (Complaint c : all) {
            ComplaintStatus st = c.getStatus();
            statusCount.put(st, statusCount.getOrDefault(st, 0L) + 1);

            ComplaintCategory cat = c.getCategory();
            if (cat != null) categoryCount.put(cat, categoryCount.getOrDefault(cat, 0L) + 1);

            Priority pr = c.getPriority();
            if (pr != null) priorityCount.put(pr, priorityCount.getOrDefault(pr, 0L) + 1);

            if (st == ComplaintStatus.SUBMITTED || st == ComplaintStatus.ASSIGNED || st == ComplaintStatus.UNDER_REVIEW) {
                pending++;
            } else if (st == ComplaintStatus.IN_PROGRESS || st == ComplaintStatus.REOPENED) {
                inProgress++;
            } else if (st == ComplaintStatus.RESOLVED) {
                resolved++;
            } else if (st == ComplaintStatus.CLOSED) {
                closed++;
            } else if (st == ComplaintStatus.ESCALATED) {
                escalated++;
            }

            if (st != ComplaintStatus.RESOLVED && st != ComplaintStatus.CLOSED) {
                if (c.getDueDate() != null && c.getDueDate().isBefore(today)) {
                    overdue++;
                }
            }

            if (c.getCreatedAt() != null) {
                String monthKey = c.getCreatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + c.getCreatedAt().getYear();
                if (monthlyMap.containsKey(monthKey)) {
                    monthlyMap.get(monthKey)[0]++;
                    if (st == ComplaintStatus.RESOLVED || st == ComplaintStatus.CLOSED) {
                        monthlyMap.get(monthKey)[1]++;
                    }
                }
            }
        }

        stats.setPendingComplaints(pending);
        stats.setInProgressComplaints(inProgress);
        stats.setResolvedComplaints(resolved);
        stats.setClosedComplaints(closed);
        stats.setEscalatedComplaints(escalated);
        stats.setOverdueComplaints(overdue);

        long resolvedAndClosed = resolved + closed;
        double rate = all.isEmpty() ? 0.0 : Math.round(((double) resolvedAndClosed / all.size() * 100.0) * 10.0) / 10.0;
        stats.setResolutionRate(rate);

        List<CategoryStatDto> catList = new ArrayList<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            catList.add(new CategoryStatDto(cat.name(), cat.getDisplayName(), categoryCount.getOrDefault(cat, 0L)));
        }
        stats.setCategoryStats(catList);

        List<PriorityStatDto> priList = new ArrayList<>();
        for (Priority pr : Priority.values()) {
            priList.add(new PriorityStatDto(pr.name(), priorityCount.getOrDefault(pr, 0L)));
        }
        stats.setPriorityStats(priList);

        List<StatusStatDto> stList = new ArrayList<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            stList.add(new StatusStatDto(s.name(), statusCount.getOrDefault(s, 0L)));
        }
        stats.setStatusStats(stList);

        List<MonthlyTrendDto> trendList = new ArrayList<>();
        monthlyMap.forEach((month, counts) -> {
            trendList.add(new MonthlyTrendDto(month, counts[0], counts[1]));
        });
        stats.setMonthlyTrend(trendList);

        return stats;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getComplainantDashboardStats(Long complainantId) {
        DashboardStatsDto stats = new DashboardStatsDto();

        List<Complaint> all = complaintRepository.findAll().stream()
                .filter(c -> c.getComplainant() != null && c.getComplainant().getId().equals(complainantId))
                .toList();

        stats.setTotalComplaints(all.size());
        long pending = 0;
        long inProgress = 0;
        long resolved = 0;
        long closed = 0;
        long escalated = 0;

        for (Complaint c : all) {
            ComplaintStatus st = c.getStatus();
            if (st == ComplaintStatus.SUBMITTED || st == ComplaintStatus.ASSIGNED || st == ComplaintStatus.UNDER_REVIEW) {
                pending++;
            } else if (st == ComplaintStatus.IN_PROGRESS || st == ComplaintStatus.REOPENED) {
                inProgress++;
            } else if (st == ComplaintStatus.RESOLVED) {
                resolved++;
            } else if (st == ComplaintStatus.CLOSED) {
                closed++;
            } else if (st == ComplaintStatus.ESCALATED) {
                escalated++;
            }
        }

        stats.setPendingComplaints(pending);
        stats.setInProgressComplaints(inProgress);
        stats.setResolvedComplaints(resolved);
        stats.setClosedComplaints(closed);
        stats.setEscalatedComplaints(escalated);

        long resolvedAndClosed = resolved + closed;
        double rate = all.isEmpty() ? 0.0 : Math.round(((double) resolvedAndClosed / all.size() * 100.0) * 10.0) / 10.0;
        stats.setResolutionRate(rate);

        return stats;
    }
}
