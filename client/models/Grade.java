public class Grade {
    private String subject;
    private String exam;
    private String type;
    private double coefficient;
    private double score;
    
    public Grade(String subject, String exam, String type, double coefficient, double score) {
        this.subject = subject;
        this.exam = exam;
        this.type = type;
        this.coefficient = coefficient;
        this.score = score;
    }
    
    // Getters and Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getExam() { return exam; }
    public void setExam(String exam) { this.exam = exam; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}