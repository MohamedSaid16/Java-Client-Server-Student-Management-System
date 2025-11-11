public class Exam {
    private String examName;
    private String type;
    private double coefficient;
    
    public Exam(String examName, String type, double coefficient) {
        this.examName = examName;
        this.type = type;
        this.coefficient = coefficient;
    }
    
    // Getters
    public String getExamName() { return examName; }
    public String getType() { return type; }
    public double getCoefficient() { return coefficient; }
    
    // Setters
    public void setExamName(String examName) { this.examName = examName; }
    public void setType(String type) { this.type = type; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
}