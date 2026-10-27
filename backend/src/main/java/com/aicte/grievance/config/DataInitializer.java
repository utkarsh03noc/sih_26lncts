package com.aicte.grievance.config;

import com.aicte.grievance.entity.*;
import com.aicte.grievance.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintUpdateRepository complaintUpdateRepository;
    private final FeedbackRepository feedbackRepository;
    private final AuditLogRepository auditLogRepository;
    private final AttachmentRepository attachmentRepository;
    private final BureauRepository bureauRepository;
    private final CellRepository cellRepository;
    private final ComplaintTransferRepository complaintTransferRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataInitializer(UserRepository userRepository,
                           InstitutionRepository institutionRepository,
                           ComplaintRepository complaintRepository,
                           ComplaintUpdateRepository complaintUpdateRepository,
                           FeedbackRepository feedbackRepository,
                           AuditLogRepository auditLogRepository,
                           AttachmentRepository attachmentRepository,
                           BureauRepository bureauRepository,
                           CellRepository cellRepository,
                           ComplaintTransferRepository complaintTransferRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.complaintRepository = complaintRepository;
        this.complaintUpdateRepository = complaintUpdateRepository;
        this.feedbackRepository = feedbackRepository;
        this.auditLogRepository = auditLogRepository;
        this.attachmentRepository = attachmentRepository;
        this.bureauRepository = bureauRepository;
        this.cellRepository = cellRepository;
        this.complaintTransferRepository = complaintTransferRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            logger.info("Database seeding is disabled by configuration.");
            return;
        }

        logger.info("Verifying and seeding AICTE Grievance System data...");

        // 1. Seed Bureaus and Cells (Idempotent)
        Map<String, Bureau> bureaus = seedBureaus();
        Map<String, Cell> cells = seedCells(bureaus);

        // 2. Seed / Enhance Institutions (16 institutions total, with the 8 requested ones first)
        List<Institution> institutions = seedInstitutions();

        // 3. Seed Users (1 Central Admin, 8 Institution Officers, 5 Complainants)
        Map<String, User> users = seedUsers(institutions);

        // 4. Seed Complaints, Transfers & Duplicates (Idempotent)
        seedComplaints(institutions, users, bureaus, cells);

        logger.info("Seed data verification and initialization complete!");
    }

    private Map<String, Bureau> seedBureaus() {
        record BureauData(String code, String name, String desc, String email) {}
        List<BureauData> defs = List.of(
            new BureauData("RB", "Regulation Bureau", "Responsible for formulation, revision, and implementation of regulations governing technical institutions.", "regulation@aicte-demo.gov.in"),
            new BureauData("AB", "Approval Bureau", "Processes applications for approval of new institutions, introduction of new programs, and expansion of intake capacity.", "approval@aicte-demo.gov.in"),
            new BureauData("PAPB", "Policy and Academic Planning Bureau", "Formulates national policies for technical education and designs curriculum frameworks aligned with NEP 2020.", "policy@aicte-demo.gov.in"),
            new BureauData("AFB", "Administration and Finance Bureau", "Manages overall administration, legal coordination, institutional finance, and scholarship disbursements.", "finance@aicte-demo.gov.in")
        );

        Map<String, Bureau> map = new HashMap<>();
        for (BureauData d : defs) {
            Bureau b = bureauRepository.findByCode(d.code).orElse(null);
            if (b == null) {
                b = new Bureau(d.code, d.name, d.desc, d.email);
            } else {
                b.setName(d.name);
                b.setDescription(d.desc);
                b.setContactEmail(d.email);
            }
            map.put(d.code, bureauRepository.save(b));
        }
        return map;
    }

    private Map<String, Cell> seedCells(Map<String, Bureau> bureaus) {
        Bureau rb = bureaus.get("RB");
        Bureau ab = bureaus.get("AB");
        Bureau papb = bureaus.get("PAPB");

        record CellData(String code, String name, String desc, Bureau parent, String email) {}
        List<CellData> defs = List.of(
            new CellData("PGRC", "Public Grievance Redressal Cell", "Monitors student, faculty, and stakeholder grievances, coordinating rapid resolution across regional offices.", rb, "pgrc@aicte-demo.gov.in"),
            new CellData("SDC", "Student Development Cell", "Oversees student welfare, fee complaints, hostel amenities, and scholarship eligibility issues.", rb, "sdc@aicte-demo.gov.in"),
            new CellData("ASC", "Approval Support Cell", "Handles complaints regarding unauthorized courses, affiliation irregularities, and compliance verification.", ab, "scrutiny@aicte-demo.gov.in"),
            new CellData("CNAC", "Curriculum and NEP Alignment Cell", "Evaluates grievances regarding examination schemes, academic calendar discrepancies, and grading policies.", papb, "curriculum@aicte-demo.gov.in"),
            new CellData("ICC", "Internal Complaints Committee", "Statutory cell handling harassment, discrimination, and code of conduct inquiries independently.", null, "icc@aicte-demo.gov.in"),
            new CellData("ARMC", "Academic Review and Monitoring Cell", "Academic review and monitoring cell coordinating compliance, curriculum adherence, and institutional standards.", null, "armc@aicte-demo.gov.in")
        );

        Map<String, Cell> map = new HashMap<>();
        for (CellData d : defs) {
            Cell c = cellRepository.findByCode(d.code).orElse(null);
            if (c == null) {
                c = new Cell(d.code, d.name, d.desc, d.parent, d.email);
            } else {
                c.setName(d.name);
                c.setDescription(d.desc);
                c.setBureau(d.parent);
                c.setContactEmail(d.email);
            }
            map.put(d.code, cellRepository.save(c));
        }
        return map;
    }

    private List<Institution> seedInstitutions() {
        record InstData(String name, String code, String location, String state, String district, String address, String aicteRef, String type, String email, String phone) {}

        List<InstData> instDefs = List.of(
            // 8 Primary Requested Institutions
            new InstData("Indian Institute of Technology Delhi", "AICTE-IIT-001", "Hauz Khas, New Delhi", "Delhi", "South Delhi", "Hauz Khas, New Delhi 110016", "AICTE-ND-1-1001928", "Engineering & Technology", "director@iitd.ac.in", "011-26591000"),
            new InstData("Visvesvaraya National Institute of Technology", "AICTE-VNIT-013", "South Ambazari Road, Nagpur", "Maharashtra", "Nagpur", "South Ambazari Road, Nagpur 440010", "AICTE-MH-1-1305890", "National Institute", "director@vnit.ac.in", "0712-2801363"),
            new InstData("Veermata Jijabai Technological Institute", "AICTE-VJTI-004", "Matunga, Mumbai, Maharashtra", "Maharashtra", "Mumbai City", "H R Mahajani Marg, Matunga East, Mumbai 400019", "AICTE-MH-1-4005829", "State Autonomous College", "director@vjti.ac.in", "022-24198101"),
            new InstData("Birla Institute of Technology and Science, Pilani", "AICTE-BITS-003", "Pilani, Rajasthan", "Rajasthan", "Jhunjhunu", "Vidya Vihar Campus, Pilani 333031", "AICTE-RJ-1-3004958", "Deemed University", "registrar@pilani.bits-pilani.ac.in", "01596-242210"),
            new InstData("National Institute of Technology Tiruchirappalli", "AICTE-NITT-014", "Tanjore Main Road, Tiruchirappalli", "Tamil Nadu", "Tiruchirappalli", "Tanjore Main Road, National Highway 67, Tiruchirappalli 620015", "AICTE-TN-1-1406901", "National Institute", "director@nitt.edu", "0431-2503000"),
            new InstData("Indian Institute of Technology Indore", "AICTE-IITI-015", "Khandwa Road, Simrol, Indore", "Madhya Pradesh", "Indore", "Khandwa Road, Simrol, Indore 453552", "AICTE-MP-1-1507012", "Engineering & Technology", "dean.aa@iiti.ac.in", "0731-6603555"),
            new InstData("Government Engineering College, Bhopal", "AICTE-GECB-016", "Airport Bypass Road, Bhopal", "Madhya Pradesh", "Bhopal", "Airport Bypass Road, Gandhi Nagar, Bhopal 462036", "AICTE-MP-1-1608123", "Government Engineering", "principal@gecbhopal.ac.in", "0755-2734567"),
            new InstData("National Institute of Technology Karnataka", "AICTE-NIT-002", "Surathkal, Karnataka", "Karnataka", "Dakshina Kannada", "NH 66, Srinivasnagar, Surathkal, Mangaluru 575025", "AICTE-KA-1-2003847", "Engineering & Technology", "dean.academic@nitk.edu.in", "0824-2474000"),

            // Additional 8 Institutions for Regional & Institutional Diversity
            new InstData("College of Engineering Pune", "AICTE-COEP-005", "Shivajinagar, Pune, Maharashtra", "Maharashtra", "Pune", "Wellesley Rd, Shivajinagar, Pune 411005", "AICTE-MH-1-5006930", "Technological University", "contact@coeptech.ac.in", "020-25507000"),
            new InstData("Delhi Technological University", "AICTE-DTU-006", "Shahbad Daulatpur, Delhi", "Delhi", "North West Delhi", "Shahbad Daulatpur, Main Bawana Road, Delhi 110042", "AICTE-ND-1-6007123", "State University", "info@dtu.ac.in", "011-27871018"),
            new InstData("PSG College of Technology", "AICTE-PSG-007", "Peelamedu, Coimbatore, Tamil Nadu", "Tamil Nadu", "Coimbatore", "Post Box No. 1611, Peelamedu, Coimbatore 641004", "AICTE-TN-1-7008234", "Autonomous Engineering", "principal@psgtech.edu", "0422-2572177"),
            new InstData("Thapar Institute of Engineering and Technology", "AICTE-TIET-008", "Bhadson Road, Patiala, Punjab", "Punjab", "Patiala", "Bhadson Road, Patiala 147004", "AICTE-PB-1-8009345", "Deemed University", "registrar@thapar.edu", "0175-2393021"),
            new InstData("RV College of Engineering", "AICTE-RVCE-009", "Mysore Road, Bengaluru, Karnataka", "Karnataka", "Bengaluru Urban", "Mysore Road, RV Vidyaniketan Post, Bengaluru 560059", "AICTE-KA-1-9001456", "Autonomous Engineering", "principal@rvce.edu.in", "080-68188110"),
            new InstData("Malaviya National Institute of Technology", "AICTE-MNIT-010", "JLN Marg, Jaipur, Rajasthan", "Rajasthan", "Jaipur", "Jawahar Lal Nehru Marg, Malviya Nagar, Jaipur 302017", "AICTE-RJ-1-1002567", "National Institute", "academics@mnit.ac.in", "0141-2529087"),
            new InstData("Jadavpur University Faculty of Engineering", "AICTE-JU-011", "Kolkata, West Bengal", "West Bengal", "Kolkata", "188 Raja S.C. Mallick Road, Kolkata 700032", "AICTE-WB-1-1103678", "State University", "dean.fet@jadavpuruniversity.in", "033-24572227"),
            new InstData("Government College of Technology", "AICTE-GCT-012", "Thadagam Road, Coimbatore, Tamil Nadu", "Tamil Nadu", "Coimbatore", "Thadagam Road, Coimbatore 641013", "AICTE-TN-1-1204789", "Government College", "gctcbe@gct.ac.in", "0422-2432221")
        );

        List<Institution> result = new ArrayList<>();
        for (InstData d : instDefs) {
            Institution inst = institutionRepository.findByCode(d.code).orElse(null);
            if (inst == null) {
                inst = new Institution(null, d.name, d.code, d.location, d.type, d.email, d.phone, true);
            } else {
                inst.setName(d.name);
            }
            inst.setState(d.state);
            inst.setDistrict(d.district);
            inst.setAddress(d.address);
            inst.setAicteReferenceCode(d.aicteRef);
            inst.setType(d.type);
            inst.setContactEmail(d.email);
            inst.setContactPhone(d.phone);
            result.add(institutionRepository.save(inst));
        }

        return result;
    }

    private Map<String, User> seedUsers(List<Institution> institutions) {
        Map<String, User> userMap = new HashMap<>();
        Map<String, Institution> instMap = institutions.stream()
                .collect(Collectors.toMap(Institution::getCode, i -> i, (a, b) -> a));

        // 1 Admin
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User("admin", "admin@aicte-demo.gov.in", passwordEncoder.encode("Password@123"),
                    "AICTE Central Administrator", "9876543210", Role.ADMIN, null);
        } else {
            admin.setFullName("AICTE Central Administrator");
            admin.setEmail("admin@aicte-demo.gov.in");
            admin.setRole(Role.ADMIN);
        }
        userMap.put("admin", userRepository.save(admin));

        // 8 Institution Officers
        record OfficerDef(String username, String instCode, String name, String email, String phone) {}
        List<OfficerDef> officerDefs = List.of(
            new OfficerDef("officer_iit", "AICTE-IIT-001", "Dr. Rajesh Verma (IIT Delhi Nodal Officer)", "officer.delhi@institution.edu", "9811223344"),
            new OfficerDef("officer_vnit", "AICTE-VNIT-013", "Dr. Sunita Deshmukh (VNIT Grievance Redressal Officer)", "officer.vnit@institution.edu", "9811223355"),
            new OfficerDef("officer_vjti", "AICTE-VJTI-004", "Dr. Anil Kulkarni (VJTI Grievance Chair)", "officer.vjti@institution.edu", "9833445566"),
            new OfficerDef("officer_bits", "AICTE-BITS-003", "Prof. Meera Sharma (BITS Pilani Ombudsman)", "officer.bits@institution.edu", "9822334455"),
            new OfficerDef("officer_nitt", "AICTE-NITT-014", "Dr. S. Ramanathan (NIT Trichy Nodal Officer)", "officer.nitt@institution.edu", "9844556677"),
            new OfficerDef("officer_iiti", "AICTE-IITI-015", "Prof. Arvind Kashyap (IIT Indore Dean Student Affairs)", "officer.iiti@institution.edu", "9855667788"),
            new OfficerDef("officer_gecb", "AICTE-GECB-016", "Prof. Rakesh Sharma (GEC Bhopal Grievance Officer)", "officer.gecb@institution.edu", "9866778899"),
            new OfficerDef("officer_nitk", "AICTE-NIT-002", "Dr. K. Manjunath (NITK Nodal Officer)", "officer.nitk@institution.edu", "9877889900")
        );

        for (OfficerDef od : officerDefs) {
            Institution inst = instMap.get(od.instCode);
            User off = userRepository.findByUsername(od.username).orElse(null);
            if (off == null) {
                off = new User(od.username, od.email, passwordEncoder.encode("Password@123"), od.name, od.phone, Role.INSTITUTION_OFFICER, inst);
            } else {
                off.setFullName(od.name);
                off.setEmail(od.email);
                off.setInstitution(inst);
                off.setRole(Role.INSTITUTION_OFFICER);
            }
            userMap.put(od.username, userRepository.save(off));
        }

        // 5 Complainants
        String[][] complainantData = {
                {"student_rohit", "rohit.sharma@gmail.com", "Rohit Sharma", "9900112233"},
                {"student_ananya", "ananya.verma@gmail.com", "Ananya Verma", "9911223344"},
                {"student_priya", "priya.nair@gmail.com", "Priya Nair", "9922334455"},
                {"parent_rajesh", "rajesh.gupta@yahoo.com", "Rajesh Kumar Gupta (Parent)", "9933445566"},
                {"faculty_suresh", "suresh.kumar@gmail.com", "Dr. Suresh Chand (Ad-hoc Faculty)", "9944556677"}
        };

        for (String[] cd : complainantData) {
            final String u = cd[0];
            final String e = cd[1];
            final String fn = cd[2];
            final String p = cd[3];
            User cUser = userRepository.findByUsername(u).orElse(null);
            if (cUser == null) {
                cUser = new User(u, e, passwordEncoder.encode("Password@123"), fn, p, Role.COMPLAINANT, null);
            } else {
                cUser.setEmail(e);
                cUser.setFullName(fn);
                cUser.setPhone(p);
                cUser.setRole(Role.COMPLAINANT);
            }
            userMap.put(u, userRepository.save(cUser));
        }

        return userMap;
    }

    private void seedComplaints(List<Institution> institutions, Map<String, User> users, Map<String, Bureau> bureaus, Map<String, Cell> cells) {
        User admin = users.get("admin");

        Map<String, User> instOfficerMap = Map.of(
                "AICTE-IIT-001", users.get("officer_iit"),
                "AICTE-VNIT-013", users.get("officer_vnit"),
                "AICTE-VJTI-004", users.get("officer_vjti"),
                "AICTE-BITS-003", users.get("officer_bits"),
                "AICTE-NITT-014", users.get("officer_nitt"),
                "AICTE-IITI-015", users.get("officer_iiti"),
                "AICTE-GECB-016", users.get("officer_gecb"),
                "AICTE-NIT-002", users.get("officer_nitk")
        );
        List<User> allOfficers = new ArrayList<>(instOfficerMap.values());

        List<User> complainants = List.of(
                users.get("student_rohit"),
                users.get("student_ananya"),
                users.get("student_priya"),
                users.get("parent_rajesh"),
                users.get("faculty_suresh")
        );

        record ComplaintSeed(
                String title,
                String desc,
                ComplaintCategory category,
                Priority priority,
                ComplaintStatus status,
                int instIndex,
                int complainantIndex,
                int officerIndex,
                int daysAgoCreated,
                int slaDaysOffset,
                String updateRemarks,
                Integer feedbackRating,
                String feedbackComments
        ) {}

        List<ComplaintSeed> seeds = List.of(
            // 1-10
            new ComplaintSeed(
                "Delay in AICTE Pragati Scholarship Disbursement for 2nd Year",
                "Application was verified by the institution in August, but stipend credit has not appeared in bank account despite portal showing approval.",
                ComplaintCategory.SCHOLARSHIP, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 0, 1, 0, 20, 5,
                "Under review by finance department and verified scholarship cell.", null, null
            ),
            new ComplaintSeed(
                "Unauthorized Capitation & Development Fees Demanded for Branch Change",
                "College administration has issued an unofficial notice demanding additional non-refundable infrastructure fee for branch transfer despite clear AICTE guidelines.",
                ComplaintCategory.FEES, Priority.CRITICAL, ComplaintStatus.ESCALATED, 2, 0, 2, 28, -5,
                "AICTE Ombudsman flagged this case for urgent physical inspection. Violation of fee norms.", null, null
            ),
            new ComplaintSeed(
                "Unannounced Rescheduling of 4th Semester End-Term Examinations",
                "Examination department changed practical exam timetable with less than 12 hours notice, causing clash with national competitive examinations.",
                ComplaintCategory.EXAMINATION, Priority.MEDIUM, ComplaintStatus.RESOLVED, 1, 2, 1, 35, -10,
                "Examination controller reconvened board meeting and provided re-examination slot for affected students.", 5, "Prompt action taken by the college grievance officer. Appreciated!"
            ),
            new ComplaintSeed(
                "Inadequate Sanitation & Severe Water Shortage in Girls Hostel Block 3",
                "Running water has been discontinued in morning hours. Hygiene conditions in washrooms have degraded, creating health hazards.",
                ComplaintCategory.HOSTEL, Priority.HIGH, ComplaintStatus.UNDER_REVIEW, 0, 1, 0, 8, 7,
                "Hostel warden and campus maintenance engineer have scheduled tank cleaning and pump replacement.", null, null
            ),
            new ComplaintSeed(
                "Outdated GPU Workstations & Faulty Internet in AI/ML Research Lab",
                "40% of computers in the laboratory fail to boot and lack GPUs necessary for prescribed AICTE syllabus assignments.",
                ComplaintCategory.INFRASTRUCTURE, Priority.MEDIUM, ComplaintStatus.SUBMITTED, 4, 0, -1, 3, 12,
                "Grievance officially submitted into AICTE monitoring system.", null, null
            ),
            new ComplaintSeed(
                "Discriminatory Admission Merit List Publication in Institutional Quota",
                "Merit cutoff ranks were suppressed and admissions granted to candidates with lower CET percentile without publishing merit lists.",
                ComplaintCategory.ADMISSION, Priority.CRITICAL, ComplaintStatus.UNDER_REVIEW, 5, 3, 5, 14, 1,
                "Notice issued to admission director seeking complete rank list disclosure within 48 hours.", null, null
            ),
            new ComplaintSeed(
                "Non-Payment of Approved Salary Scales to Ad-hoc Teaching Faculty",
                "Seventh Pay Commission pay scales are not being adhered to despite college collecting complete fee structure from enrolled batches.",
                ComplaintCategory.FACULTY_STAFF, Priority.HIGH, ComplaintStatus.ASSIGNED, 6, 4, 6, 12, 3,
                "Assigned to regional AICTE vigilance desk for payroll verification.", null, null
            ),
            new ComplaintSeed(
                "Verbal Harassment and Coercive Intimidation by Senior Lab Instructor",
                "A formal complaint was lodged with Internal Complaints Committee (ICC) two months ago but no hearing has taken place so far.",
                ComplaintCategory.HARASSMENT, Priority.CRITICAL, ComplaintStatus.ESCALATED, 7, 2, 7, 40, -15,
                "SLA breached by college ICC. Escalated to Central AICTE Grievance Oversight Panel.", null, null
            ),
            new ComplaintSeed(
                "Arbitrary Withholding of Original Educational Certificates Upon Graduation",
                "Student completed degree in July 2026. Placement cell is withholding original 10th and 12th certificates citing pending alumni association dues.",
                ComplaintCategory.ADMINISTRATION, Priority.HIGH, ComplaintStatus.RESOLVED, 8, 0, 0, 25, -2,
                "Certificates handed over to student without illegal retention. Warning issued to registrar.", 4, "Received all original documents safely. Thank you AICTE portal."
            ),
            new ComplaintSeed(
                "Refusal of College to Disclose Continuous Evaluation & Internal Marks",
                "Faculty refused to show evaluated mid-term answer booklets and arbitrarily allotted zero in internal assessments without explanation.",
                ComplaintCategory.ACADEMIC, Priority.MEDIUM, ComplaintStatus.CLOSED, 9, 1, 1, 45, -20,
                "Internal evaluation sheets shown to student; re-evaluated score updated in ERP portal.", 4, "Issue successfully resolved."
            ),

            // 11-20
            new ComplaintSeed(
                "Non-Refund of Caution Deposit After Successful Completion of Degree",
                "Submitted clearance certificate 6 months ago. Institute accountant insists on indefinite processing delay.",
                ComplaintCategory.FEES, Priority.LOW, ComplaintStatus.RESOLVED, 0, 0, 0, 30, -5,
                "Caution deposit of Rs. 15,000 credited to complainant's bank account via NEFT.", 5, "Amount credited promptly after lodging grievance here."
            ),
            new ComplaintSeed(
                "Inadequate Fire Safety Equipment in Chemistry and Chemical Engg Labs",
                "Fire extinguishers in chemical storage area are expired since 2024. Absence of emergency eye-wash station.",
                ComplaintCategory.INFRASTRUCTURE, Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, 10, 4, 2, 10, 5,
                "Safety audit conducted. Procurement orders placed for fire suppression systems.", null, null
            ),
            new ComplaintSeed(
                "Denial of Exam Hall Ticket Over Unpaid Optional Hostel Mess Dues",
                "Day scholar student wrongfully blocked from downloading semester examination admit card over erroneous hostel fee charge.",
                ComplaintCategory.EXAMINATION, Priority.HIGH, ComplaintStatus.RESOLVED, 11, 2, 3, 15, -2,
                "Admit card immediately generated; day-scholar status re-validated in system.", 5, "I was able to take the exam without losing an academic year!"
            ),
            new ComplaintSeed(
                "Delay in Providing AICTE Swanath Scholarship Verification Documents",
                "Orphan student's application verification delayed by administrative office beyond the National Scholarship Portal deadline.",
                ComplaintCategory.SCHOLARSHIP, Priority.CRITICAL, ComplaintStatus.REOPENED, 2, 1, 2, 22, -1,
                "Complainant reopened case as college nodal officer uploaded incorrect income certificate.", null, null
            ),
            new ComplaintSeed(
                "Substandard Food Quality and Recurring Food Poisoning in College Canteen",
                "Multiple students fell ill due to unhygienic conditions in campus canteen. No food safety license displayed.",
                ComplaintCategory.OTHER, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 3, 0, 3, 11, 4,
                "Canteen vendor issued showcause notice. Hygiene inspection by FSSAI requested.", null, null
            ),
            new ComplaintSeed(
                "Compulsory Purchase of Uniform & Stationeries From Specific Private Vendor",
                "Institute mandates purchasing generic lab coats and drawing kits at 300% inflated price from on-campus shop.",
                ComplaintCategory.FEES, Priority.MEDIUM, ComplaintStatus.UNDER_REVIEW, 5, 3, 5, 9, 6,
                "Investigation initiated regarding commercial agreements with private vendors.", null, null
            ),
            new ComplaintSeed(
                "Biased Ph.D. Research Guide Assignment & Coercive Delay of Synopsis Defense",
                "Doctoral candidate's synopsis defense stalled for over 18 months despite meeting all AICTE publication requirements.",
                ComplaintCategory.ACADEMIC, Priority.HIGH, ComplaintStatus.ASSIGNED, 0, 4, 0, 16, -1,
                "Assigned to Research Advisory Committee chairman for hearing.", null, null
            ),
            new ComplaintSeed(
                "Severe Shortage of Qualified Faculty in Artificial Intelligence & Data Science",
                "Syllabus for 3 subjects not covered due to no faculty appointed. Guest lectures conducted irregularly.",
                ComplaintCategory.FACULTY_STAFF, Priority.HIGH, ComplaintStatus.ESCALATED, 7, 0, 7, 32, -8,
                "Overdue compliance. Showcause notice dispatched to governing body.", null, null
            ),
            new ComplaintSeed(
                "Non-Functional Disabled Ramps & Elevators in New Academic Complex",
                "Differently-abled students unable to access upper floor lecture halls due to locked lifts and broken ramps.",
                ComplaintCategory.INFRASTRUCTURE, Priority.HIGH, ComplaintStatus.RESOLVED, 8, 2, 0, 26, -4,
                "Ramps renovated with non-slip flooring and lift access keys provided to disabled students.", 4, "Physical accessibility improved significantly."
            ),
            new ComplaintSeed(
                "Illegal Penalty Charged for Delayed Tuition Fee Despite Bank Loan Sanction Letter",
                "Late fine of Rs 500/day charged even after submission of Nationalized Bank loan sanction disbursement certificate.",
                ComplaintCategory.FEES, Priority.MEDIUM, ComplaintStatus.RESOLVED, 4, 3, 4, 18, -3,
                "Late fee penalty waived completely; student ledger adjusted.", 5, "Grateful for AICTE's swift intervention."
            ),

            // 21-30
            new ComplaintSeed(
                "Ragging Incident by Senior Batches Near College Transit Gate",
                "First-year students subjected to psychological humiliation and forced nocturnal tasks outside campus premises.",
                ComplaintCategory.HARASSMENT, Priority.CRITICAL, ComplaintStatus.UNDER_REVIEW, 1, 0, 1, 5, 2,
                "Anti-ragging squad activated, CCTV recordings seized, and police liaison established.", null, null
            ),
            new ComplaintSeed(
                "Unfair Disqualification from Campus Recruitment Placements",
                "Placement cell banned candidate from all campus drives after candidate opted out of an unrelated BPO role.",
                ComplaintCategory.ADMINISTRATION, Priority.MEDIUM, ComplaintStatus.CLOSED, 6, 1, 6, 50, -25,
                "Placement guidelines clarified. Student allowed to participate in core engineering drives.", 4, "Fair placement policy restored."
            ),
            new ComplaintSeed(
                "Absence of Sanitary Napkin Vending Machines & Disposal Incinerators",
                "AICTE circular regarding menstrual hygiene facilities on campus not implemented across women common rooms.",
                ComplaintCategory.INFRASTRUCTURE, Priority.LOW, ComplaintStatus.RESOLVED, 9, 2, 1, 40, -10,
                "Vending machines and incinerator units installed in 4 campus washroom blocks.", 5, "Well implemented."
            ),
            new ComplaintSeed(
                "Failure to Issue Semester Transcript for Foreign University Application",
                "Applied for official sealed transcripts for MS admission in October. Examination wing has not dispatched documents.",
                ComplaintCategory.EXAMINATION, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 10, 0, 2, 14, 1,
                "Verification completed by registrar. Digital signed transcripts being emailed.", null, null
            ),
            new ComplaintSeed(
                "Cancellation of Approved Sports Quota Admission Without Hearing",
                "National-level badminton player's admission cancelled arbitrarily alleging certificate mismatch without opportunity for explanation.",
                ComplaintCategory.ADMISSION, Priority.CRITICAL, ComplaintStatus.ASSIGNED, 11, 3, 3, 6, 1,
                "Assigned to Directorate of Technical Education ombudsman.", null, null
            ),
            new ComplaintSeed(
                "Mandatory Laptop Purchase at Exorbitant Rate Under Disguise of BYOD Policy",
                "College forced students to purchase a specific brand of laptop through campus distributor at 40% markup.",
                ComplaintCategory.FEES, Priority.HIGH, ComplaintStatus.UNDER_REVIEW, 2, 4, 2, 13, 2,
                "College accounts audited; notice served against bundled hardware mandates.", null, null
            ),
            new ComplaintSeed(
                "Unauthorized Substitution of Elective Subjects Without Student Consent",
                "Enrolled for Cloud Computing elective but department head arbitrarily reassigned entire section to generic ERP course.",
                ComplaintCategory.ACADEMIC, Priority.MEDIUM, ComplaintStatus.RESOLVED, 5, 1, 5, 28, -6,
                "Academic council restored Cloud Computing track with guest faculty from industry.", 4, "Elective choices respected."
            ),
            new ComplaintSeed(
                "Overcrowded Hostel Rooms Beyond AICTE Prescribed Floor Area Norms",
                "Four students accommodated in double-sharing rooms without adequate study tables or ventilation.",
                ComplaintCategory.HOSTEL, Priority.HIGH, ComplaintStatus.ESCALATED, 7, 2, 7, 36, -12,
                "AICTE infrastructure compliance team conducted physical surprise audit.", null, null
            ),
            new ComplaintSeed(
                "Stipend Deduction for GATE Qualified M.Tech Students During Internship",
                "College withheld AICTE PG scholarship allowance during the mandatory 6-month industry research internship.",
                ComplaintCategory.SCHOLARSHIP, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 0, 0, 0, 17, 3,
                "Clarification issued: AICTE PG stipend continues during approved curriculum internships.", null, null
            ),
            new ComplaintSeed(
                "Severe Delays in Revaluation Results Causing Loss of Higher Study Opportunities",
                "Revaluation results of 7th semester pending for over 90 days, rendering students ineligible for government exams.",
                ComplaintCategory.EXAMINATION, Priority.HIGH, ComplaintStatus.RESOLVED, 8, 3, 0, 42, -14,
                "Fast-track revaluation completed and consolidated marksheets distributed.", 4, "Helped save my career."
            ),

            // 31-40
            new ComplaintSeed(
                "Unsafe Electrical Wiring and Frequent Short Circuits in Physics Laboratory",
                "Open live wires dangling near student workbenches; two minor shock incidents reported last week.",
                ComplaintCategory.INFRASTRUCTURE, Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, 1, 1, 1, 7, 3,
                "PWD electrical inspection ongoing; lab closed until rewiring clearance.", null, null
            ),
            new ComplaintSeed(
                "Forced Resignation of Senior Associate Professor Over Whistleblowing",
                "Faculty member coerced into resigning after raising concerns over false student-faculty ratios submitted to NBA.",
                ComplaintCategory.FACULTY_STAFF, Priority.CRITICAL, ComplaintStatus.UNDER_REVIEW, 3, 4, 3, 21, 0,
                "AICTE Vigilance Division seized recruitment register and biometric logs.", null, null
            ),
            new ComplaintSeed(
                "Discriminatory Dress Code Enforcement Exclusively for Female Students",
                "Security guards turning back female students from library citing subjective attire guidelines not present in college rulebook.",
                ComplaintCategory.HARASSMENT, Priority.MEDIUM, ComplaintStatus.RESOLVED, 4, 2, 4, 19, -4,
                "Administration issued formal apology and rescinded unauthorized dress code circular.", 5, "Thank you AICTE for protecting our dignity."
            ),
            new ComplaintSeed(
                "Collection of Capitation Fee Under the Pretext of Alumni Endowment Fund",
                "Candidates forced to pay Rs. 2,00,000 cash receipt into an unverified trust before issuing admission slip.",
                ComplaintCategory.FEES, Priority.CRITICAL, ComplaintStatus.ESCALATED, 6, 3, 6, 38, -18,
                "Ombudsman filed report recommending suspension of approval for next academic year.", null, null
            ),
            new ComplaintSeed(
                "Absence of Emergency First-Aid and Medical Staff on Campus",
                "Health centre remains locked on weekends and after 4 PM; student injured during sports tournament had to wait 2 hours for transport.",
                ComplaintCategory.INFRASTRUCTURE, Priority.HIGH, ComplaintStatus.ASSIGNED, 9, 0, 0, 9, 6,
                "Assigned to campus amenities nodal officer for 24x7 medical attendant staffing.", null, null
            ),
            new ComplaintSeed(
                "Refusal to Grant Attendance Waiver for University-Sponsored Hackathon",
                "Students representing institute at Smart India Hackathon marked absent and detained from semester examinations.",
                ComplaintCategory.ACADEMIC, Priority.HIGH, ComplaintStatus.RESOLVED, 11, 1, 1, 14, -2,
                "Duty leave sanctioned as per AICTE Hackathon participation policy; detention revoked.", 5, "Quick and fair resolution!"
            ),
            new ComplaintSeed(
                "Non-Payment of Maternity Benefits to Female Assistant Professors",
                "Contractual women faculty denied 26-week paid maternity leave in contravention of statutory Maternity Benefit Act.",
                ComplaintCategory.FACULTY_STAFF, Priority.HIGH, ComplaintStatus.UNDER_REVIEW, 5, 4, 5, 15, 2,
                "Governing body directed to disburse complete back-pay within 15 days.", null, null
            ),
            new ComplaintSeed(
                "Inadequate Lighting and Broken Street Lamps on Campus Perimeter Road",
                "Complete blackout along the 1.5 km road connecting girls hostel to the main academic block.",
                ComplaintCategory.INFRASTRUCTURE, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 10, 2, 2, 8, 7,
                "Procurement approved for 35 solar LED poles along perimeter route.", null, null
            ),
            new ComplaintSeed(
                "Unexplained Deductions from SC/ST Post-Matric Scholarship Grants",
                "College withheld tuition fee portion directly disbursed by state social welfare department into college account.",
                ComplaintCategory.SCHOLARSHIP, Priority.CRITICAL, ComplaintStatus.ESCALATED, 7, 0, 7, 33, -9,
                "State liaison officer notified; direct audit of scholarship disbursement ledger.", null, null
            ),
            new ComplaintSeed(
                "Arbitrary Mass-Fines Levied on Entire Mechanical Batch for Minor Lab Breakage",
                "Rs. 5,000 fine imposed on each of the 120 students for a faulty vernier caliper without identifying responsible individual.",
                ComplaintCategory.ADMINISTRATION, Priority.MEDIUM, ComplaintStatus.RESOLVED, 2, 3, 2, 24, -6,
                "Mass fine revoked; individual accountability protocol instituted.", 4, "Fair treatment restored."
            ),

            // 41-50
            new ComplaintSeed(
                "Biased Internal Assessment Marks Allotted by Guest Lecturer",
                "Significant marks disparity between local and outstation students in continuous internal evaluations.",
                ComplaintCategory.ACADEMIC, Priority.MEDIUM, ComplaintStatus.UNDER_REVIEW, 8, 1, 0, 11, 4,
                "External evaluator reviewing sample answer scripts across sections.", null, null
            ),
            new ComplaintSeed(
                "Refusal to Provide Bonafide Certificate for Passport & Visa Applications",
                "Administrative office demands clearance from 6 separate departments for issuing a simple bonafide letter.",
                ComplaintCategory.ADMINISTRATION, Priority.LOW, ComplaintStatus.RESOLVED, 1, 0, 1, 29, -8,
                "Digitized single-click bonafide certificate generator deployed on student portal.", 5, "Seamless now!"
            ),
            new ComplaintSeed(
                "Clogged Stormwater Drains Causing Waterlogging in Ground Floor Classrooms",
                "Monsoon rains led to 2 feet water stagnation inside classrooms and computer lab with active electrical cables.",
                ComplaintCategory.INFRASTRUCTURE, Priority.HIGH, ComplaintStatus.RESOLVED, 6, 2, 6, 34, -10,
                "Drainage canal desilted and protective flood embankments constructed.", 4, "Safety measures implemented."
            ),
            new ComplaintSeed(
                "Denial of AICTE Fee-Waiver (TFWS) Seat Allotment Despite State Rank",
                "College claimed TFWS quota was filled and pressured candidate to accept high-fee management quota seat.",
                ComplaintCategory.ADMISSION, Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, 3, 3, 3, 10, 5,
                "Admission scrutiny committee validating CET TFWS seat allotment records.", null, null
            ),
            new ComplaintSeed(
                "Unfair Ban on Student Technical Societies and Coding Club Activities",
                "Dean of student affairs dissolved student ACM and IEEE chapters without citing violation of university statutes.",
                ComplaintCategory.ADMINISTRATION, Priority.LOW, ComplaintStatus.RESOLVED, 0, 1, 0, 31, -11,
                "Student club charters reinstated with student-elected executive bodies.", 5, "Club activities are back on track!"
            ),
            new ComplaintSeed(
                "Substandard Laboratory Equipment in Geotechnical & Civil Engineering Labs",
                "Triaxial compression testing machine calibration expired in 2021; inaccurate test readings for student projects.",
                ComplaintCategory.INFRASTRUCTURE, Priority.MEDIUM, ComplaintStatus.ASSIGNED, 4, 4, 4, 12, 3,
                "Assigned to civil engineering laboratory coordinator for NABL calibration.", null, null
            ),
            new ComplaintSeed(
                "Unaddressed Cyber-Bullying & Unauthorized Photographs on Social Media",
                "Anonymous social media handle posting derogatory remarks against junior students; college anti-ragging cell inactive.",
                ComplaintCategory.HARASSMENT, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 9, 2, 2, 6, 9,
                "Cyber cell complaint filed and legal takedown notice served to social platform.", null, null
            ),
            new ComplaintSeed(
                "Non-Issuance of Migration Certificate After Course Withdrawal",
                "Student withdrew admission within AICTE cut-off deadline; college refuses migration certificate citing bond rules.",
                ComplaintCategory.ADMISSION, Priority.HIGH, ComplaintStatus.RESOLVED, 5, 0, 5, 27, -5,
                "Migration certificate and full tuition fee refund issued to candidate.", 5, "Refund received in full."
            ),
            new ComplaintSeed(
                "Lack of Potable RO Drinking Water Dispensers in Architecture Block",
                "Water coolers dispensing yellow turbid water; students forced to purchase commercial packaged water daily.",
                ComplaintCategory.INFRASTRUCTURE, Priority.MEDIUM, ComplaintStatus.UNDER_REVIEW, 11, 1, 0, 13, 2,
                "Maintenance vendor replacing RO filtration membranes and UV disinfection chambers.", null, null
            ),
            new ComplaintSeed(
                "Arbitrary Cancellation of College Bus Route Causing Exam Absenteeism",
                "Transit service cancelled on the morning of final semester examination without backup transport arrangements.",
                ComplaintCategory.OTHER, Priority.HIGH, ComplaintStatus.IN_PROGRESS, 0, 0, 0, 16, -1,
                "Transportation committee issued contract warning and scheduled dedicated backup buses.", null, null
            )
        );

        logger.info("Seeding / verifying {} complaints across Bureaus and Cells...", seeds.size());
        List<Complaint> savedComplaints = new ArrayList<>();

        for (int i = 0; i < seeds.size(); i++) {
            ComplaintSeed s = seeds.get(i);
            Institution inst = institutions.get(s.instIndex % institutions.size());
            User comp = complainants.get(s.complainantIndex % complainants.size());
            User officer = s.officerIndex >= 0 ? instOfficerMap.getOrDefault(inst.getCode(), allOfficers.get(s.officerIndex % allOfficers.size())) : null;

            String compNum = String.format("CMP-2026-%04d", 1001 + i);
            Complaint complaint = complaintRepository.findByComplaintNumber(compNum).orElse(null);
            boolean isNew = (complaint == null);
            if (isNew) {
                complaint = new Complaint();
                complaint.setComplaintNumber(compNum);
            }

            complaint.setTitle(s.title);
            complaint.setDescription(s.desc);
            complaint.setCategory(s.category);
            complaint.setPriority(s.priority);
            complaint.setStatus(s.status);
            complaint.setComplainant(comp);
            complaint.setInstitution(inst);
            complaint.setAssignedOfficer(officer);

            // Bureau and Cell Assignment
            Bureau b = resolveBureauForCategory(s.category, bureaus);
            Cell cl = resolveCellForCategory(s.category, cells);
            complaint.setBureau(b);
            complaint.setCell(cl);

            LocalDateTime createdTime = LocalDateTime.now().minusDays(s.daysAgoCreated);
            complaint.setCreatedAt(createdTime);
            complaint.setUpdatedAt(createdTime.plusDays(Math.max(1, s.daysAgoCreated / 2)));
            complaint.setDueDate(LocalDate.now().plusDays(s.slaDaysOffset));
            if (officer != null) {
                complaint.setAssignmentDate(createdTime.plusDays(1));
                complaint.setAssignmentRemarks("Case officially assigned to " + officer.getFullName());
            }

            if (s.status == ComplaintStatus.RESOLVED || s.status == ComplaintStatus.CLOSED) {
                complaint.setResolvedAt(createdTime.plusDays(Math.max(2, s.daysAgoCreated - 5)));
            }

            Complaint saved = complaintRepository.save(complaint);
            savedComplaints.add(saved);

            if (isNew) {
                // Timeline updates
                ComplaintUpdate initUpdate = new ComplaintUpdate(
                        saved,
                        null,
                        ComplaintStatus.SUBMITTED,
                        "Grievance officially submitted into AICTE monitoring system.",
                        comp
                );
                initUpdate.setCreatedAt(createdTime);
                complaintUpdateRepository.save(initUpdate);

                if (s.status != ComplaintStatus.SUBMITTED) {
                    ComplaintUpdate step2 = new ComplaintUpdate(
                            saved,
                            ComplaintStatus.SUBMITTED,
                            s.status,
                            s.updateRemarks,
                            officer != null ? officer : admin
                    );
                    step2.setCreatedAt(createdTime.plusDays(1));
                    complaintUpdateRepository.save(step2);
                }

                // Feedback
                if (s.feedbackRating != null) {
                    Feedback feedback = new Feedback(saved, comp, s.feedbackRating, s.feedbackComments);
                    feedback.setCreatedAt(createdTime.plusDays(Math.max(3, s.daysAgoCreated - 3)));
                    feedbackRepository.save(feedback);
                }

                // Seed sample attachments for realism
                if (compNum.equals("CMP-2026-1050") || i % 4 == 0) {
                    String fileName = compNum.equals("CMP-2026-1050")
                            ? "Exam_Day_Bus_Service_Disruption_Notice.pdf"
                            : switch (s.category) {
                                case FEES, SCHOLARSHIP -> "Fee_Payment_Challan_Receipt.pdf";
                                case EXAMINATION, ADMISSION -> "Hall_Ticket_Exam_Entry_Pass.pdf";
                                case INFRASTRUCTURE, HOSTEL -> "Site_Inspection_Evidence_Photo.png";
                                default -> "Grievance_Supporting_Document.pdf";
                            };
                    String fileType = fileName.endsWith(".png") ? "image/png" : "application/pdf";
                    Long fileSize = fileName.endsWith(".png") ? 2450120L : 1245180L;
                    Attachment attachment = new Attachment(
                            saved,
                            fileName,
                            fileType,
                            "/uploads/complaints/" + compNum + "/" + fileName,
                            fileSize,
                            comp
                    );
                    attachment.setUploadedAt(createdTime);
                    attachmentRepository.save(attachment);
                }

                // Audit Log
                AuditLog log = new AuditLog(
                        "COMPLAINT_REGISTERED",
                        comp,
                        "Complaint",
                        saved.getId(),
                        "Complaint " + saved.getComplaintNumber() + " registered for " + inst.getName() + " under Bureau [" + (b != null ? b.getCode() : "N/A") + "]",
                        "127.0.0.1"
                );
                log.setTimestamp(createdTime);
                auditLogRepository.save(log);
            }
        }

        // Seed Duplicate & Overlap Cases
        seedDuplicateCases(institutions, users, bureaus, cells);

        // Ensure Transfers Exist
        ensureSeedTransfers(savedComplaints, users, bureaus, cells);
    }

    private Bureau resolveBureauForCategory(ComplaintCategory cat, Map<String, Bureau> bureaus) {
        if (cat == null) return bureaus.get("RB");
        return switch (cat) {
            case SCHOLARSHIP, FEES -> bureaus.get("AFB");
            case EXAMINATION, ACADEMIC -> bureaus.get("PAPB");
            case ADMISSION, INFRASTRUCTURE -> bureaus.get("AB");
            default -> bureaus.get("RB");
        };
    }

    private Cell resolveCellForCategory(ComplaintCategory cat, Map<String, Cell> cells) {
        if (cat == null) return cells.get("PGRC");
        return switch (cat) {
            case SCHOLARSHIP, FEES -> cells.get("SDC");
            case EXAMINATION, ACADEMIC -> cells.get("CNAC");
            case HARASSMENT -> cells.get("ICC");
            case ADMISSION, INFRASTRUCTURE -> cells.get("ASC");
            case FACULTY_STAFF -> cells.get("ARMC");
            default -> cells.get("PGRC");
        };
    }

    private void seedDuplicateCases(List<Institution> institutions, Map<String, User> users, Map<String, Bureau> bureaus, Map<String, Cell> cells) {
        Map<String, Institution> instMap = institutions.stream()
                .collect(Collectors.toMap(Institution::getCode, i -> i, (a, b) -> a));

        Institution iit = instMap.get("AICTE-IIT-001");
        Institution vnit = instMap.get("AICTE-VNIT-013");
        Institution vjti = instMap.get("AICTE-VJTI-004");

        User complainantPriya = users.get("student_priya");
        User complainantRohit = users.get("student_rohit");
        User complainantAnanya = users.get("student_ananya");

        if (complaintRepository.findByComplaintNumber("CMP-2026-1051").isEmpty()) {
            Complaint dup1 = new Complaint();
            dup1.setComplaintNumber("CMP-2026-1051");
            dup1.setTitle("Severe Water Shortage and Maintenance in Girls Hostel Block 3");
            dup1.setDescription("Water shortage issue persists in Girls Hostel Block 3; mornings have zero pressure and washrooms are unusable.");
            dup1.setCategory(ComplaintCategory.HOSTEL);
            dup1.setPriority(Priority.HIGH);
            dup1.setStatus(ComplaintStatus.SUBMITTED);
            dup1.setComplainant(complainantPriya);
            dup1.setInstitution(iit);
            dup1.setBureau(bureaus.get("RB"));
            dup1.setCell(cells.get("PGRC"));
            dup1.setPotentialDuplicate(true);
            dup1.setDuplicateAcknowledged(true);
            dup1.setDuplicateRemarks("Separate wing issue in Block 3; lodging on behalf of 2nd year hostel floor.");
            dup1.setDueDate(LocalDate.now().plusDays(7));
            Complaint saved = complaintRepository.save(dup1);

            ComplaintUpdate u = new ComplaintUpdate(saved, null, ComplaintStatus.SUBMITTED,
                    "Grievance officially submitted into AICTE monitoring system. [Potential overlap/duplicate detected (1 match)] (Complainant confirmed submission with remarks: Separate wing issue in Block 3)",
                    complainantPriya);
            complaintUpdateRepository.save(u);

            AuditLog log = new AuditLog("COMPLAINT_REGISTERED", complainantPriya, "Complaint", saved.getId(),
                    "Complaint CMP-2026-1051 registered for " + iit.getName() + " [Potential Overlap Flagged]", "127.0.0.1");
            auditLogRepository.save(log);
        }

        if (complaintRepository.findByComplaintNumber("CMP-2026-1052").isEmpty()) {
            Complaint dup2 = new Complaint();
            dup2.setComplaintNumber("CMP-2026-1052");
            dup2.setTitle("Unauthorized Capitation and Extra Development Fees for Stream Change");
            dup2.setDescription("College administration is still demanding additional development fee for branch transfer despite warnings.");
            dup2.setCategory(ComplaintCategory.FEES);
            dup2.setPriority(Priority.CRITICAL);
            dup2.setStatus(ComplaintStatus.UNDER_REVIEW);
            dup2.setComplainant(complainantRohit);
            dup2.setInstitution(vjti);
            dup2.setBureau(bureaus.get("AFB"));
            dup2.setCell(cells.get("SDC"));
            dup2.setPotentialDuplicate(true);
            dup2.setDuplicateAcknowledged(false);
            dup2.setDueDate(LocalDate.now().plusDays(3));
            Complaint saved = complaintRepository.save(dup2);

            ComplaintUpdate u = new ComplaintUpdate(saved, null, ComplaintStatus.SUBMITTED,
                    "Grievance officially submitted into AICTE monitoring system. [Potential overlap/duplicate detected]",
                    complainantRohit);
            complaintUpdateRepository.save(u);

            AuditLog log = new AuditLog("COMPLAINT_REGISTERED", complainantRohit, "Complaint", saved.getId(),
                    "Complaint CMP-2026-1052 registered for " + vjti.getName() + " [Potential Overlap Flagged]", "127.0.0.1");
            auditLogRepository.save(log);
        }

        if (complaintRepository.findByComplaintNumber("CMP-2026-1053").isEmpty()) {
            Complaint dup3 = new Complaint();
            dup3.setComplaintNumber("CMP-2026-1053");
            dup3.setTitle("Rescheduling of End-Semester Examination Timing Conflict");
            dup3.setDescription("Examination section rescheduled 4th semester timetable creating direct overlap with competitive examinations.");
            dup3.setCategory(ComplaintCategory.EXAMINATION);
            dup3.setPriority(Priority.HIGH);
            dup3.setStatus(ComplaintStatus.SUBMITTED);
            dup3.setComplainant(complainantAnanya);
            dup3.setInstitution(vnit);
            dup3.setBureau(bureaus.get("PAPB"));
            dup3.setCell(cells.get("CNAC"));
            dup3.setPotentialDuplicate(true);
            dup3.setDuplicateAcknowledged(true);
            dup3.setDuplicateRemarks("Clash affecting 15 outstation students preparing for national competitive examinations.");
            dup3.setDueDate(LocalDate.now().plusDays(5));
            Complaint saved = complaintRepository.save(dup3);

            ComplaintUpdate u = new ComplaintUpdate(saved, null, ComplaintStatus.SUBMITTED,
                    "Grievance officially submitted into AICTE monitoring system. [Potential overlap/duplicate detected (1 match)] (Complainant confirmed submission with remarks: Clash affecting 15 outstation students)",
                    complainantAnanya);
            complaintUpdateRepository.save(u);

            AuditLog log = new AuditLog("COMPLAINT_REGISTERED", complainantAnanya, "Complaint", saved.getId(),
                    "Complaint CMP-2026-1053 registered for " + vnit.getName() + " [Potential Overlap Flagged]", "127.0.0.1");
            auditLogRepository.save(log);
        }
    }

    private void ensureSeedTransfers(List<Complaint> complaints, Map<String, User> users, Map<String, Bureau> bureaus, Map<String, Cell> cells) {
        User admin = users.get("admin");
        User officerIIT = users.get("officer_iit");
        User officerBITS = users.get("officer_bits");
        User officerVNIT = users.get("officer_vnit");

        Bureau rb = bureaus.get("RB");
        Bureau ab = bureaus.get("AB");
        Bureau afb = bureaus.get("AFB");

        Cell pgrc = cells.get("PGRC");
        Cell sdc = cells.get("SDC");
        Cell asc = cells.get("ASC");

        // Case CMP-2026-1050 (Case 50)
        complaintRepository.findByComplaintNumber("CMP-2026-1050").ifPresent(c -> {
            if (complaintTransferRepository.countByComplaintId(c.getId()) == 0) {
                ComplaintTransfer ct = new ComplaintTransfer(
                        c,
                        rb, ab,
                        pgrc, asc,
                        null, officerIIT,
                        "Transferred from Public Grievance Redressal Cell to Approval Support Cell for institution-level transport infrastructure verification.",
                        admin
                );
                complaintTransferRepository.save(ct);

                c.setBureau(ab);
                c.setCell(asc);
                c.setAssignedOfficer(officerIIT);
                c.setAssignmentDate(LocalDateTime.now().minusDays(1));
                c.setAssignmentRemarks("Transferred to Approval Support Cell for verification");
                complaintRepository.save(c);

                ComplaintUpdate cu = new ComplaintUpdate(
                        c,
                        c.getStatus(),
                        c.getStatus(),
                        "Case transferred from Bureau [Regulation Bureau] to [Approval Bureau] / Cell [Public Grievance Redressal Cell] to [Approval Support Cell]. Reason: Transferred from Public Grievance Redressal Cell to Approval Support Cell for institution-level transport infrastructure verification.",
                        admin
                );
                complaintUpdateRepository.save(cu);

                AuditLog log = new AuditLog("COMPLAINT_TRANSFERRED", admin, "Complaint", c.getId(),
                        "Complaint " + c.getComplaintNumber() + " transferred from RB/PGRC to AB/ASC", "127.0.0.1");
                auditLogRepository.save(log);
            }
        });

        // Case CMP-2026-1002 (Capitation Fee)
        complaintRepository.findByComplaintNumber("CMP-2026-1002").ifPresent(c -> {
            if (complaintTransferRepository.countByComplaintId(c.getId()) == 0) {
                ComplaintTransfer ct = new ComplaintTransfer(
                        c,
                        afb, rb,
                        sdc, pgrc,
                        null, admin,
                        "Transferred from Student Development Cell to Regulation Bureau for Ombudsman inquiry into capitation fee policy violations.",
                        admin
                );
                complaintTransferRepository.save(ct);

                c.setBureau(rb);
                c.setCell(pgrc);
                complaintRepository.save(c);

                ComplaintUpdate cu = new ComplaintUpdate(
                        c,
                        c.getStatus(),
                        c.getStatus(),
                        "Case transferred from Bureau [Administration and Finance Bureau] to [Regulation Bureau] / Cell [Student Development Cell] to [Public Grievance Redressal Cell]. Reason: Transferred from Student Development Cell to Regulation Bureau for Ombudsman inquiry into capitation fee policy violations.",
                        admin
                );
                complaintUpdateRepository.save(cu);

                AuditLog log = new AuditLog("COMPLAINT_TRANSFERRED", admin, "Complaint", c.getId(),
                        "Complaint " + c.getComplaintNumber() + " transferred from AFB/SDC to RB/PGRC", "127.0.0.1");
                auditLogRepository.save(log);
            }
        });

        // Case CMP-2026-1014 (Scholarship Verification)
        complaintRepository.findByComplaintNumber("CMP-2026-1014").ifPresent(c -> {
            if (complaintTransferRepository.countByComplaintId(c.getId()) == 0) {
                ComplaintTransfer ct = new ComplaintTransfer(
                        c,
                        rb, afb,
                        sdc, sdc,
                        null, officerBITS,
                        "Re-assigned to Administration and Finance Bureau for direct National Scholarship Portal disbursement clearance.",
                        admin
                );
                complaintTransferRepository.save(ct);

                c.setBureau(afb);
                c.setCell(sdc);
                c.setAssignedOfficer(officerBITS);
                complaintRepository.save(c);

                ComplaintUpdate cu = new ComplaintUpdate(
                        c,
                        c.getStatus(),
                        c.getStatus(),
                        "Case transferred from Bureau [Regulation Bureau] to [Administration and Finance Bureau]. Reason: Re-assigned to Administration and Finance Bureau for direct National Scholarship Portal disbursement clearance.",
                        admin
                );
                complaintUpdateRepository.save(cu);

                AuditLog log = new AuditLog("COMPLAINT_TRANSFERRED", admin, "Complaint", c.getId(),
                        "Complaint " + c.getComplaintNumber() + " transferred from RB to AFB", "127.0.0.1");
                auditLogRepository.save(log);
            }
        });

        // Case CMP-2026-1031 (VNIT Nagpur Electrical Safety)
        complaintRepository.findByComplaintNumber("CMP-2026-1031").ifPresent(c -> {
            if (complaintTransferRepository.countByComplaintId(c.getId()) == 0) {
                ComplaintTransfer ct = new ComplaintTransfer(
                        c,
                        ab, rb,
                        asc, pgrc,
                        null, officerVNIT,
                        "Transferred to Regulation Bureau PGRC following student safety committee recommendations regarding laboratory electrical infrastructure.",
                        admin
                );
                complaintTransferRepository.save(ct);

                c.setBureau(rb);
                c.setCell(pgrc);
                c.setAssignedOfficer(officerVNIT);
                complaintRepository.save(c);

                ComplaintUpdate cu = new ComplaintUpdate(
                        c,
                        c.getStatus(),
                        c.getStatus(),
                        "Case transferred from Bureau [Approval Bureau] to [Regulation Bureau] / Cell [Approval Support Cell] to [Public Grievance Redressal Cell]. Reason: Transferred to Regulation Bureau PGRC following student safety committee recommendations regarding laboratory electrical infrastructure.",
                        admin
                );
                complaintUpdateRepository.save(cu);

                AuditLog log = new AuditLog("COMPLAINT_TRANSFERRED", admin, "Complaint", c.getId(),
                        "Complaint " + c.getComplaintNumber() + " transferred from AB/ASC to RB/PGRC", "127.0.0.1");
                auditLogRepository.save(log);
            }
        });
    }
}
