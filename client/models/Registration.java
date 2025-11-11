public class Registration {
    private int registrationId;
    private String studentName;
    private String programName;
    private String academicYear;
    private String status;
    private double average;

    public Registration(int registrationId, String studentName, String programName, 
                       String academicYear, String status, double average) {
        this.registrationId = registrationId;
        this.studentName = studentName;
        this.programName = programName;
        this.academicYear = academicYear;
        this.status = status;
        this.average = average;
    }

    // Getters and Setters
    public int getRegistrationId() { return registrationId; }
    public void setRegistrationId(int registrationId) { this.registrationId = registrationId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }
}