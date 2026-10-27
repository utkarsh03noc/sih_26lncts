package com.aicte.grievance.entity;

public enum ComplaintCategory {
    ACADEMIC("Academic"),
    EXAMINATION("Examination"),
    ADMISSION("Admission"),
    FEES("Fees"),
    SCHOLARSHIP("Scholarship"),
    HOSTEL("Hostel"),
    INFRASTRUCTURE("Infrastructure"),
    FACULTY_STAFF("Faculty/Staff"),
    HARASSMENT("Harassment"),
    ADMINISTRATION("Administration"),
    OTHER("Other");

    private final String displayName;

    ComplaintCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
