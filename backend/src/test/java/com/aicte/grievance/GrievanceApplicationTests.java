package com.aicte.grievance;

import com.aicte.grievance.dto.*;
import com.aicte.grievance.entity.*;
import com.aicte.grievance.repository.*;
import com.aicte.grievance.security.UserPrincipal;
import com.aicte.grievance.service.BureauService;
import com.aicte.grievance.service.ComplaintService;
import com.aicte.grievance.service.InstitutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class GrievanceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private BureauService bureauService;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private BureauRepository bureauRepository;

    @Autowired
    private CellRepository cellRepository;

    @Autowired
    private ComplaintTransferRepository complaintTransferRepository;

    @Autowired
    private ComplaintRelationRepository complaintRelationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void testComplaintDetailsByIdAndNumber() {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // 1. Fetch by complaint number CMP-2026-1050
        ComplaintResponseDto comp1050 = complaintService.getComplaintById("CMP-2026-1050", adminPrincipal);
        assertNotNull(comp1050);
        assertEquals("CMP-2026-1050", comp1050.getComplaintNumber());
        assertNotNull(comp1050.getTitle());
        assertNotNull(comp1050.getDescription());
        assertNotNull(comp1050.getComplainantName());
        assertNotNull(comp1050.getInstitutionName());

        // 2. Fetch updates for CMP-2026-1050
        List<ComplaintUpdateDto> updates = complaintService.getComplaintUpdates("CMP-2026-1050", adminPrincipal);
        assertNotNull(updates);
        assertFalse(updates.isEmpty());

        // 3. Fetch attachments for CMP-2026-1050
        List<AttachmentDto> attachments = complaintService.getComplaintAttachments("CMP-2026-1050", adminPrincipal);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty(), "CMP-2026-1050 should have seeded attachments");
        assertEquals("Exam_Day_Bus_Service_Disruption_Notice.pdf", attachments.get(0).getFileName());

        // 4. Dynamic test with another complaint number CMP-2026-1001
        ComplaintResponseDto comp1001 = complaintService.getComplaintById("CMP-2026-1001", adminPrincipal);
        assertNotNull(comp1001);
        assertEquals("CMP-2026-1001", comp1001.getComplaintNumber());

        // 5. Dynamic test with another complaint number CMP-2026-1025
        ComplaintResponseDto comp1025 = complaintService.getComplaintById("CMP-2026-1025", adminPrincipal);
        assertNotNull(comp1025);
        assertEquals("CMP-2026-1025", comp1025.getComplaintNumber());

        // 6. Test numeric ID lookup
        ComplaintResponseDto compNumeric = complaintService.getComplaintById(String.valueOf(comp1050.getId()), adminPrincipal);
        assertNotNull(compNumeric);
        assertEquals("CMP-2026-1050", compNumeric.getComplaintNumber());
    }

    @Test
    void testComplaintDetailsRestEndpoints() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Test GET /api/complaints/CMP-2026-1050
        mockMvc.perform(get("/api/complaints/CMP-2026-1050")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CMP-2026-1050"))
                .andExpect(jsonPath("$.data.title").isNotEmpty())
                .andExpect(jsonPath("$.data.institutionName").isNotEmpty());

        // Test GET /api/complaints/CMP-2026-1050/updates
        mockMvc.perform(get("/api/complaints/CMP-2026-1050/updates")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // Test GET /api/complaints/CMP-2026-1050/attachments
        mockMvc.perform(get("/api/complaints/CMP-2026-1050/attachments")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("Exam_Day_Bus_Service_Disruption_Notice.pdf"));

        // Dynamic test GET /api/complaints/CMP-2026-1001
        mockMvc.perform(get("/api/complaints/CMP-2026-1001")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CMP-2026-1001"));
    }

    @Test
    void testBureauAndCellEndpoints() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Test GET /api/bureaus
        mockMvc.perform(get("/api/bureaus")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").isNotEmpty());

        // Test GET /api/cells
        mockMvc.perform(get("/api/cells")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").isNotEmpty());
    }

    @Test
    void testInstitutionMonitoringSummary() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Test GET /api/institutions/monitoring
        mockMvc.perform(get("/api/institutions/monitoring")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].institutionName").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].involvedBureausCount").isNumber());
    }

    @Test
    void testDuplicateCheckEndpoint() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        DuplicateCheckRequestDto req = new DuplicateCheckRequestDto();
        req.setTitle("Water Shortage and Low Pressure in Girls Hostel Block 3");
        req.setDescription("There is no running water in morning hours in Girls Hostel 3.");
        req.setInstitutionId(1L);
        req.setCategory(ComplaintCategory.HOSTEL);

        mockMvc.perform(post("/api/complaints/check-duplicate")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testComplaintTransferWorkflow() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        List<BureauDto> bureaus = bureauService.getActiveBureaus();
        assertFalse(bureaus.isEmpty());
        Long targetBureauId = bureaus.get(0).getId();

        ComplaintTransferRequestDto transferReq = new ComplaintTransferRequestDto();
        transferReq.setTargetBureauId(targetBureauId);
        transferReq.setTransferReason("Test Cross-Bureau Reassignment for verification");
        transferReq.setRemarks("Special assignment by admin");

        mockMvc.perform(post("/api/complaints/CMP-2026-1001/transfer")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CMP-2026-1001"));

        // Verify transfer history
        mockMvc.perform(get("/api/complaints/CMP-2026-1001/transfers")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].transferReason").value("Test Cross-Bureau Reassignment for verification"));
    }

    @Test
    void testSeededDemoDataEntitiesAndCounts() {
        // 1. Verify Institutions (16 seeded, including all 8 requested)
        assertTrue(institutionRepository.count() >= 16, "Should have at least 16 institutions seeded");

        String[] requiredCodes = {
                "AICTE-IIT-001", "AICTE-VNIT-013", "AICTE-VJTI-004", "AICTE-BITS-003",
                "AICTE-NITT-014", "AICTE-IITI-015", "AICTE-GECB-016", "AICTE-NIT-002"
        };
        for (String code : requiredCodes) {
            assertTrue(institutionRepository.findByCode(code).isPresent(), "Required institution missing: " + code);
        }

        // 2. Verify Bureaus (4 seeded: RB, AB, PAPB, AFB)
        assertEquals(4, bureauRepository.count(), "Should have exactly 4 bureaus");
        assertTrue(bureauRepository.findByCode("RB").isPresent());
        assertTrue(bureauRepository.findByCode("AB").isPresent());
        assertTrue(bureauRepository.findByCode("PAPB").isPresent());
        assertTrue(bureauRepository.findByCode("AFB").isPresent());

        // 3. Verify Cells (6 seeded: PGRC, SDC, ASC, CNAC, ICC, ARMC)
        assertEquals(6, cellRepository.count(), "Should have exactly 6 cells");
        assertTrue(cellRepository.findByCode("PGRC").isPresent());
        assertTrue(cellRepository.findByCode("SDC").isPresent());
        assertTrue(cellRepository.findByCode("ASC").isPresent());
        assertTrue(cellRepository.findByCode("CNAC").isPresent());
        assertTrue(cellRepository.findByCode("ICC").isPresent());
        assertTrue(cellRepository.findByCode("ARMC").isPresent());

        // 4. Verify Officers (8 officers seeded for the 8 primary institutions)
        String[] requiredOfficers = {
                "officer_iit", "officer_vnit", "officer_vjti", "officer_bits",
                "officer_nitt", "officer_iiti", "officer_gecb", "officer_nitk"
        };
        for (String off : requiredOfficers) {
            User officerUser = userRepository.findByUsername(off).orElse(null);
            assertNotNull(officerUser, "Officer account missing: " + off);
            assertEquals(Role.INSTITUTION_OFFICER, officerUser.getRole());
            assertNotNull(officerUser.getInstitution(), "Officer must be linked to institution: " + off);
        }

        // 5. Verify Complaints (53 total: 50 primary + 3 potential duplicates)
        assertTrue(complaintRepository.count() >= 53, "Should have at least 53 complaints seeded");

        // 6. Verify Overlap & Duplicates (at least 3 flagged)
        assertTrue(complaintRepository.countByPotentialDuplicateTrue() >= 3, "Should have at least 3 duplicate flagged complaints");

        // 7. Verify Cross-Bureau Transfers
        assertTrue(complaintTransferRepository.count() >= 4, "Should have at least 4 transfer records seeded");
    }

    @Test
    void testSeededDuplicateAndTransferAlerts() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // 1. Test GET /api/complaints/duplicates
        mockMvc.perform(get("/api/complaints/duplicates")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));

        // 2. Test GET /api/complaints/CMP-2026-1050/transfers (Transferred from RB/PGRC to AB/ASC)
        mockMvc.perform(get("/api/complaints/CMP-2026-1050/transfers")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].toBureauCode").value("AB"));

        // 3. Test GET /api/institutions/monitoring returns active bureau and cell counts
        mockMvc.perform(get("/api/institutions/monitoring")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].involvedBureausCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].totalComplaints").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void testDashboardStatsEndpoints() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Test GET /api/dashboard/stats
        mockMvc.perform(get("/api/dashboard/stats")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInstitutions").value(org.hamcrest.Matchers.greaterThanOrEqualTo(16)))
                .andExpect(jsonPath("$.data.totalComplaints").value(org.hamcrest.Matchers.greaterThanOrEqualTo(50)))
                .andExpect(jsonPath("$.data.potentialDuplicateCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.categoryStats").isArray())
                .andExpect(jsonPath("$.data.bureauStats").isArray())
                .andExpect(jsonPath("$.data.cellStats").isArray())
                .andExpect(jsonPath("$.data.monthlyTrend").isArray());
    }

    @Test
    void testSemanticDuplicateDetectionDifferentWording() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // IIT Delhi ID = 1L. It has seeded complaint:
        // "Inadequate Sanitation & Severe Water Shortage in Girls Hostel Block 3"
        // Test paraphrased query with completely different wording:
        // "Students are facing a severe shortage of drinking water in their hostel"
        DuplicateCheckRequestDto req = new DuplicateCheckRequestDto();
        req.setTitle("Students are facing a severe shortage of drinking water in their hostel");
        req.setDescription("Taps have run dry since yesterday morning in the campus residence hall.");
        req.setInstitutionId(1L);
        req.setCategory(ComplaintCategory.HOSTEL);

        mockMvc.perform(post("/api/complaints/check-duplicate")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].similarityScore").value(org.hamcrest.Matchers.greaterThanOrEqualTo(50.0)))
                .andExpect(jsonPath("$.data[0].matchType").isNotEmpty())
                .andExpect(jsonPath("$.data[0].matchedConcepts").isArray())
                .andExpect(jsonPath("$.data[0].matchExplanation").isNotEmpty());
    }

    @Test
    void testDuplicateDetectionUnrelatedGrievance() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Completely unrelated query for IIT Delhi
        DuplicateCheckRequestDto req = new DuplicateCheckRequestDto();
        req.setTitle("Annual cultural symposium acoustic microphone setup repair");
        req.setDescription("Stage sound system requires wire fixing before morning keynote.");
        req.setInstitutionId(1L);
        req.setCategory(ComplaintCategory.OTHER);

        mockMvc.perform(post("/api/complaints/check-duplicate")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testDuplicateCheckUnauthorizedRequest() throws Exception {
        DuplicateCheckRequestDto req = new DuplicateCheckRequestDto();
        req.setTitle("Water supply cut in hostel");
        req.setInstitutionId(1L);

        // Missing authentication post should be rejected
        mockMvc.perform(post("/api/complaints/check-duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testComplaintClassificationScholarship() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        ComplaintClassificationRequestDto req = new ComplaintClassificationRequestDto();
        req.setTitle("Delay in Pragati scholarship stipend disbursement and DBT portal linking");
        req.setDescription("My national scholarship portal application was approved but monthly stipend fellowship grant has not been credited.");

        mockMvc.perform(post("/api/complaints/classify")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.suggestedCategory").value("SCHOLARSHIP"))
                .andExpect(jsonPath("$.data.suggestedBureauCode").value("AFB"))
                .andExpect(jsonPath("$.data.manualReviewRequired").value(false))
                .andExpect(jsonPath("$.data.confidence").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0.40)))
                .andExpect(jsonPath("$.data.extractedKeywords").isArray())
                .andExpect(jsonPath("$.data.explanation").isNotEmpty());
    }

    @Test
    void testComplaintClassificationCriticalRagging() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        ComplaintClassificationRequestDto req = new ComplaintClassificationRequestDto();
        req.setTitle("Severe physical harassment and ragging incident in junior hostel wing");
        req.setDescription("First-year students are subjected to threats and midnight ragging. Urgent disciplinary action required immediately.");

        mockMvc.perform(post("/api/complaints/classify")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.suggestedCategory").value("HARASSMENT"))
                .andExpect(jsonPath("$.data.suggestedPriority").value("CRITICAL"))
                .andExpect(jsonPath("$.data.suggestedBureauCode").value("RB"))
                .andExpect(jsonPath("$.data.suggestedCellCode").value("ICC"))
                .andExpect(jsonPath("$.data.manualReviewRequired").value(false));
    }

    @Test
    void testComplaintClassificationAmbiguousRequiresManualReview() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        ComplaintClassificationRequestDto req = new ComplaintClassificationRequestDto();
        req.setTitle("Issue");
        req.setDescription("pls fix");

        mockMvc.perform(post("/api/complaints/classify")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.manualReviewRequired").value(true))
                .andExpect(jsonPath("$.data.confidenceLevel").value("MANUAL_REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.data.explanation").value(org.hamcrest.Matchers.containsString("Manual review required")));
    }

    @Test
    void testComplaintClassificationUnauthorized() throws Exception {
        ComplaintClassificationRequestDto req = new ComplaintClassificationRequestDto();
        req.setTitle("Ragging incident");
        req.setDescription("Students threatened.");

        mockMvc.perform(post("/api/complaints/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMergeUnauthorizedRoleComplainant() throws Exception {
        User studentUser = userRepository.findByUsername("student_rohit")
                .orElseThrow(() -> new RuntimeException("Student user not found"));
        UserPrincipal studentPrincipal = UserPrincipal.create(studentUser);

        ComplaintMergeRequestDto req = new ComplaintMergeRequestDto(1L, 4L, "Attempt unauthorized merge");

        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMergeSelfMergePrevention() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        ComplaintMergeRequestDto req = new ComplaintMergeRequestDto(1L, 1L, "Merging case into itself should be blocked");

        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Self-merge is not allowed")));
    }

    @Test
    void testMergeSameInstitutionPidValidation() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // Find two complaints from different institutions
        Complaint compIITD = complaintRepository.findByComplaintNumber("CMP-2026-1001").orElseThrow();
        Complaint compBITS = complaintRepository.findByComplaintNumber("CMP-2026-1002").orElseThrow();
        assertNotEquals(compIITD.getInstitution().getId(), compBITS.getInstitution().getId());

        ComplaintMergeRequestDto req = new ComplaintMergeRequestDto(
                compIITD.getId(), compBITS.getId(),
                "Merge across different universities without override");

        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cross-institution merge is not permitted")));
    }

    @Test
    void testMergeSuccessOriginalPreservationAndUnifiedFile() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        Complaint primary = complaintRepository.findByComplaintNumber("CMP-2026-1001").orElseThrow();
        Complaint secondary = complaintRepository.findByComplaintNumber("CMP-2026-1004").orElseThrow();
        assertEquals(primary.getInstitution().getId(), secondary.getInstitution().getId());

        String originalSecondaryDesc = secondary.getDescription();
        String originalSecondaryComplainant = secondary.getComplainant().getFullName();

        ComplaintMergeRequestDto req = new ComplaintMergeRequestDto(
                primary.getId(), secondary.getId(),
                "Confirmed duplicate reporting of the same institutional campus grievance. Consolidation approved.");
        req.setSimilarityScore(55.0);

        // 1. Approve merge
        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unifiedCaseNumber").value("UCF-CMP-2026-1001"))
                .andExpect(jsonPath("$.data.mergedComplaints").isArray())
                .andExpect(jsonPath("$.data.totalMergedCases").value(1));

        // 2. Verify secondary complaint in DB
        Complaint secondaryReloaded = complaintRepository.findById(secondary.getId()).orElseThrow();
        assertEquals(ComplaintStatus.MERGED, secondaryReloaded.getStatus());
        // Verify original record preservation
        assertEquals(originalSecondaryDesc, secondaryReloaded.getDescription());
        assertEquals(originalSecondaryComplainant, secondaryReloaded.getComplainant().getFullName());
        assertEquals(primary.getInstitution().getId(), secondaryReloaded.getInstitution().getId());

        // 3. Verify audit log entry
        boolean auditExists = auditLogRepository.findAll().stream()
                .anyMatch(a -> "COMPLAINT_MERGED".equals(a.getAction()) &&
                               a.getDetails().contains("CMP-2026-1004"));
        assertTrue(auditExists, "Audit log for COMPLAINT_MERGED must exist");

        // 4. Verify Unified File GET endpoint
        mockMvc.perform(get("/api/complaints/" + primary.getId() + "/unified-file")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.primaryComplaint.complaintNumber").value("CMP-2026-1001"))
                .andExpect(jsonPath("$.data.mergedComplaints[0].complaintNumber").value("CMP-2026-1004"))
                .andExpect(jsonPath("$.data.consolidatedTimeline").isArray())
                .andExpect(jsonPath("$.data.crossBureauCoordination").isArray());

        // 5. Test Repeated & Circular merge prevention
        ComplaintMergeRequestDto repeatedReq = new ComplaintMergeRequestDto(
                primary.getId(), secondary.getId(), "Attempting repeated merge");
        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repeatedReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already merged")));

        // Circular merge (secondary as primary, primary as secondary)
        ComplaintMergeRequestDto circularReq = new ComplaintMergeRequestDto(
                secondary.getId(), primary.getId(), "Attempting circular merge");
        mockMvc.perform(post("/api/complaints/merge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(circularReq)))
                .andExpect(status().isBadRequest());

        // 6. Test Unmerge / Reversal Workflow
        ComplaintUnmergeRequestDto unmergeReq = new ComplaintUnmergeRequestDto(
                primary.getId(), secondary.getId(),
                "Reopening independent inquiry following submission of verified distinct evidence.");

        mockMvc.perform(post("/api/complaints/unmerge")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unmergeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMergedCases").value(0));

        // Verify status restored
        Complaint secondaryUnmerged = complaintRepository.findById(secondary.getId()).orElseThrow();
        assertNotEquals(ComplaintStatus.MERGED, secondaryUnmerged.getStatus());

        boolean unmergeAuditExists = auditLogRepository.findAll().stream()
                .anyMatch(a -> "COMPLAINT_UNMERGED".equals(a.getAction()) &&
                               a.getDetails().contains("CMP-2026-1004"));
        assertTrue(unmergeAuditExists, "Audit log for COMPLAINT_UNMERGED must exist");
    }

    @Test
    void testLinkAsRelatedAlternative() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        Complaint caseA = complaintRepository.findByComplaintNumber("CMP-2026-1005").orElseThrow();
        Complaint caseB = complaintRepository.findByComplaintNumber("CMP-2026-1006").orElseThrow();

        ComplaintStatus statusBeforeA = caseA.getStatus();
        ComplaintStatus statusBeforeB = caseB.getStatus();

        CaseLinkRequestDto req = new CaseLinkRequestDto(
                caseA.getId(), caseB.getId(),
                "Related department admission procedure inquiries for cross-referencing.");

        mockMvc.perform(post("/api/complaints/link-related")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.relationType").value("RELATED"));

        // Both complaints must remain in their original status! Neither becomes MERGED.
        Complaint reloadedA = complaintRepository.findById(caseA.getId()).orElseThrow();
        Complaint reloadedB = complaintRepository.findById(caseB.getId()).orElseThrow();
        assertEquals(statusBeforeA, reloadedA.getStatus());
        assertEquals(statusBeforeB, reloadedB.getStatus());
    }

    @Test
    void testCompareComplaintsEndpoint() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        Complaint comp1 = complaintRepository.findByComplaintNumber("CMP-2026-1001").orElseThrow();
        Complaint comp4 = complaintRepository.findByComplaintNumber("CMP-2026-1004").orElseThrow();

        // Test with caseA & caseB parameters
        mockMvc.perform(get("/api/complaints/compare")
                        .param("caseA", comp1.getId().toString())
                        .param("caseB", comp4.getId().toString())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sameInstitution").value(true))
                .andExpect(jsonPath("$.data.institutionPidA").isNotEmpty())
                .andExpect(jsonPath("$.data.institutionPidB").isNotEmpty())
                .andExpect(jsonPath("$.data.caseA.complaintNumber").value("CMP-2026-1001"))
                .andExpect(jsonPath("$.data.caseB.complaintNumber").value("CMP-2026-1004"))
                .andExpect(jsonPath("$.data.canMerge").isBoolean());

        // Test with caseAId & caseBId parameters (and complaint number strings)
        mockMvc.perform(get("/api/complaints/compare")
                        .param("caseAId", comp1.getComplaintNumber())
                        .param("caseBId", comp4.getComplaintNumber())
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sameInstitution").value(true))
                .andExpect(jsonPath("$.data.caseA.complaintNumber").value("CMP-2026-1001"))
                .andExpect(jsonPath("$.data.caseB.complaintNumber").value("CMP-2026-1004"));
    }

    @Test
    void testMergeCandidatesEndpoint() throws Exception {
        User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        Complaint comp1051 = complaintRepository.findByComplaintNumber("CMP-2026-1051").orElseThrow();

        // 1. Test with Long ID
        mockMvc.perform(get("/api/complaints/" + comp1051.getId() + "/merge-candidates")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].complaintNumber").value("CMP-2026-1004"));

        // 2. Test with complaintNumber String (e.g. CMP-2026-1051)
        mockMvc.perform(get("/api/complaints/" + comp1051.getComplaintNumber() + "/merge-candidates")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].complaintNumber").value("CMP-2026-1004"));

        // 3. Test Unified Case File with complaintNumber String
        mockMvc.perform(get("/api/complaints/" + comp1051.getComplaintNumber() + "/unified-file")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryComplaint.complaintNumber").value("CMP-2026-1051"));

        // 4. Test with Complaint 52 (CMP-2026-1052, VJTI Mumbai Fees complaint)
        Complaint comp1052 = complaintRepository.findByComplaintNumber("CMP-2026-1052").orElseThrow();
        mockMvc.perform(get("/api/complaints/" + comp1052.getId() + "/merge-candidates")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].complaintNumber").value("CMP-2026-1002"));
    }
}
