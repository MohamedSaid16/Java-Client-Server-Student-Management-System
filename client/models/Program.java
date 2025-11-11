public class Program {
    private int programId;
    private String programName;
    private int programYear;
    private String description;

    public Program(int programId, String programName, int programYear, String description) {
        this.programId = programId;
        this.programName = programName;
        this.programYear = programYear;
        this.description = description;
    }

    // Getters and Setters
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public int getProgramYear() { return programYear; }
    public void setProgramYear(int programYear) { this.programYear = programYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}