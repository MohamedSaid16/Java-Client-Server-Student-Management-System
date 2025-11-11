import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class StudentController {
    private ClientService clientService;
    
    public StudentController() {
       this.clientService = new ClientService();
    }
    
    // Student Information
    public Map<String, String> getStudentInfo(int studentId) {
        Map<String, String> info = clientService.getStudentInfo(studentId);
        if (info != null) {
            return info;
        }
        return new HashMap<>();
    }
    
    // Grades Management
    public List<Map<String, String>> getStudentGrades(int studentId) {
        try {
            return clientService.getStudentGrades(studentId);
        } catch (Exception e) {
            System.err.println("Error getting student grades: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, String>> getGradesBySubject(int studentId, String subject) {
        try {
            List<Map<String, String>> allGrades = getStudentGrades(studentId);
            if (allGrades != null) {
                List<Map<String, String>> filtered = new ArrayList<>();
                for (Map<String, String> grade : allGrades) {
                    if (subject.equals(grade.get("subject"))) {
                        filtered.add(grade);
                    }
                }
                return filtered;
            }
        } catch (Exception e) {
            System.err.println("Error getting grades by subject: " + e.getMessage());
        }
        return new ArrayList<>();
    }
    
    public Map<String, Double> getSubjectAverages(int studentId) {
        try {
            List<Map<String, String>> grades = getStudentGrades(studentId);
            Map<String, Double> subjectAverages = new HashMap<>();
            Map<String, Double> subjectTotals = new HashMap<>();
            Map<String, Double> subjectWeights = new HashMap<>();
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    String subject = grade.get("subject");
                    double score = Double.parseDouble(grade.get("score"));
                    double coefficient = Double.parseDouble(grade.get("coefficient"));
                    
                    // Update totals and weights
                    double currentTotal = subjectTotals.getOrDefault(subject, 0.0);
                    double currentWeight = subjectWeights.getOrDefault(subject, 0.0);
                    
                    subjectTotals.put(subject, currentTotal + (score * coefficient));
                    subjectWeights.put(subject, currentWeight + coefficient);
                }
                
                // Calculate averages
                for (String subject : subjectTotals.keySet()) {
                    double total = subjectTotals.get(subject);
                    double weight = subjectWeights.get(subject);
                    double average = total / weight;
                    subjectAverages.put(subject, Math.round(average * 100.0) / 100.0);
                }
            }
            
            return subjectAverages;
        } catch (Exception e) {
            System.err.println("Error calculating subject averages: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // Academic Performance
    public double getOverallAverage(int studentId) {
        try {
            Double average = clientService.getOverallAverage(studentId);
            return average != null ? average : 0.0;
        } catch (Exception e) {
            System.err.println("Error getting overall average: " + e.getMessage());
            return 0.0;
        }
    }
    
    // FIXED: Changed return type to String to match ClientService
    public String getFinalStatus(int studentId) {
        try {
            return clientService.getFinalStatus(studentId);
        } catch (Exception e) {
            System.err.println("Error getting final status: " + e.getMessage());
            return "UNKNOWN";
        }
    }
    
    // New method that returns detailed status info
    public Map<String, Object> getFinalStatusDetailed(int studentId) {
        try {
            String status = getFinalStatus(studentId);
            double average = getOverallAverage(studentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("average", average);
            result.put("statusArabic", convertStatusToArabic(status));
            result.put("statusEnglish", convertStatusToEnglish(status));
            result.put("canProceed", !"EXCLU".equals(status) && average >= 10.0);
            
            return result;
        } catch (Exception e) {
            System.err.println("Error getting final status: " + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "UNKNOWN");
            errorResult.put("average", 0.0);
            errorResult.put("statusArabic", "غير معروف");
            errorResult.put("statusEnglish", "Unknown");
            errorResult.put("canProceed", false);
            return errorResult;
        }
    }
    
    public Map<String, Object> getAcademicProgress(int studentId) {
        try {
            double average = getOverallAverage(studentId);
            List<Map<String, String>> grades = getStudentGrades(studentId);
            int totalExams = grades != null ? grades.size() : 0;
            int passedExams = 0;
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    double score = Double.parseDouble(grade.get("score"));
                    if (score >= 10.0) {
                        passedExams++;
                    }
                }
            }
            
            double progressPercentage = totalExams > 0 ? (passedExams * 100.0 / totalExams) : 0.0;
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("overallAverage", average);
            progress.put("totalExams", totalExams);
            progress.put("passedExams", passedExams);
            progress.put("progressPercentage", Math.round(progressPercentage * 100.0) / 100.0);
            progress.put("academicLevel", getAcademicLevel(average));
            
            return progress;
        } catch (Exception e) {
            System.err.println("Error getting academic progress: " + e.getMessage());
            Map<String, Object> errorProgress = new HashMap<>();
            errorProgress.put("overallAverage", 0.0);
            errorProgress.put("totalExams", 0);
            errorProgress.put("passedExams", 0);
            errorProgress.put("progressPercentage", 0.0);
            errorProgress.put("academicLevel", "غير معروف");
            return errorProgress;
        }
    }
    
    // Utility Methods
    public String convertStatusToArabic(String status) {
        if (status == null) return "غير معروف";
        
        switch (status.toUpperCase()) {
            case "ADMIS": return "ناجح";
            case "REDOUBLANT": return "راسب";
            case "EXCLU": return "مطرود";
            case "IN_PROGRESS": return "قيد التقدم";
            default: return "غير معروف";
        }
    }
    
    public String convertStatusToEnglish(String status) {
        if (status == null) return "Unknown";
        
        switch (status.toUpperCase()) {
            case "ADMIS": return "Admitted";
            case "REDOUBLANT": return "Repeating";
            case "EXCLU": return "Excluded";
            case "IN_PROGRESS": return "In Progress";
            default: return "Unknown";
        }
    }
    
    private String getAcademicLevel(double average) {
        if (average >= 16) return "Excellent";
        else if (average >= 14) return "Very Good";
        else if (average >= 12) return "Good";
        else if (average >= 10) return "Acceptable";
        else return "Weak";
    }
    
    // Personal Information Management
    public boolean updateContactInfo(int studentId, String email, String phone) {
        try {
            // Implementation for updating contact info
            System.out.println("Updating contact info for student ID: " + studentId);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating contact info: " + e.getMessage());
            return false;
        }
    }
    // Add this method to StudentController class
public List<Map<String, String>> getStudentTranscript(int studentId) {
    return clientService.getStudentTranscript(studentId);
}
    // Document Generation
    public String generateGradeReport(int studentId) {
        try {
            Map<String, String> studentInfo = getStudentInfo(studentId);
            List<Map<String, String>> grades = getStudentGrades(studentId);
            double average = getOverallAverage(studentId);
            Map<String, Object> status = getFinalStatusDetailed(studentId);
            
            StringBuilder report = new StringBuilder();
            report.append("Student Grade Report\n");
            report.append("====================\n");
            report.append("Name: ").append(studentInfo.get("firstName")).append(" ").append(studentInfo.get("lastName")).append("\n");
            report.append("Program: ").append(studentInfo.get("program")).append("\n");
            report.append("Overall Average: ").append(String.format("%.2f", average)).append("\n");
            report.append("Final Status: ").append(status.get("statusEnglish")).append("\n\n");
            report.append("Grades:\n");
            
            if (grades != null && !grades.isEmpty()) {
                for (Map<String, String> grade : grades) {
                    report.append("- ").append(grade.get("subject"))
                          .append(" | ").append(grade.get("exam"))
                          .append(" | ").append(grade.get("score"))
                          .append("/20\n");
                }
            } else {
                report.append("No grades available.\n");
            }
            
            return report.toString();
        } catch (Exception e) {
            System.err.println("Error generating grade report: " + e.getMessage());
            return "Error generating report";
        }
    }
    
    // Add these missing methods that might be called from views
    public Double getOverallAverageAsDouble(int studentId) {
        return getOverallAverage(studentId);
    }
    
    // Sample data for testing
    public List<Map<String, String>> getSampleGrades() {
        ArrayList<Map<String, String>> sampleGrades = new ArrayList<>();
        
        Map<String, String> grade1 = new HashMap<>();
        grade1.put("subject", "Mathematics");
        grade1.put("exam", "Midterm Exam");
        grade1.put("score", "16.5");
        grade1.put("coefficient", "2.0");
        sampleGrades.add(grade1);
        
        Map<String, String> grade2 = new HashMap<>();
        grade2.put("subject", "Mathematics");
        grade2.put("exam", "Final Exam");
        grade2.put("score", "15.0");
        grade2.put("coefficient", "3.0");
        sampleGrades.add(grade2);
        
        Map<String, String> grade3 = new HashMap<>();
        grade3.put("subject", "Computer Science");
        grade3.put("exam", "Project");
        grade3.put("score", "18.0");
        grade3.put("coefficient", "2.5");
        sampleGrades.add(grade3);
        
        return sampleGrades;
    }
    // ADD this method to your StudentController class:
public List<Map<String, String>> getStudentSubjects(int studentId) {
    try {
        return clientService.getSubjectsByStudent(studentId);
    } catch (Exception e) {
        System.err.println("Error getting student subjects: " + e.getMessage());
        return new ArrayList<>();
    }
}
}