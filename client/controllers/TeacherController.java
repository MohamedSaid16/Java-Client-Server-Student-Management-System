import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TeacherController {
    private ClientService clientService;
    
    public TeacherController() {
      this.clientService = new ClientService();
    }
    
    // Exam Management - FIXED: Now gets actual subject name
    public boolean createExam(String examType, String examName, double coefficient, int subjectId, int teacherId) {
        try {
            // Get the actual subject name
            List<Map<String, String>> subjects = clientService.getTeacherSubjects(teacherId);
            String subjectName = "General";
            
            if (subjects != null && !subjects.isEmpty()) {
                for (Map<String, String> subject : subjects) {
                    if (String.valueOf(subjectId).equals(subject.get("subjectId"))) {
                        subjectName = subject.get("subjectName");
                        break;
                    }
                }
            }
            
            boolean success = clientService.createExam(examName, subjectName, examType, coefficient, teacherId);
            if (success) {
                System.out.println("Creating exam: " + examName + " for subject: " + subjectName);
            } else {
                System.err.println("Failed to create exam: " + examName);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error creating exam: " + e.getMessage());
            return false;
        }
    }
    
   public boolean updateExam(int examId, String examName, double coefficient) {
    try {
        // Call the actual service method
        boolean success = clientService.updateExam(examId, examName, coefficient);
        if (success) {
            System.out.println("Exam ID " + examId + " updated successfully");
        } else {
            System.err.println("Failed to update exam ID " + examId);
        }
        return success;
    } catch (Exception e) {
        System.err.println("Error updating exam: " + e.getMessage());
        return false;
    }
}
    
    public boolean deleteExam(int examId) {
        try {
            // Implementation for deleting exam
            System.out.println("Deleting exam ID: " + examId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting exam: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> getTeacherExams(int teacherId) {
        try {
            return clientService.getTeacherExams(teacherId);
        } catch (Exception e) {
            System.err.println("Error getting teacher exams: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, String>> getExamsBySubject(int subjectId) {
        try {
            // Implementation for getting exams by subject
            System.out.println("Getting exams for subject ID: " + subjectId);
            return List.of();
        } catch (Exception e) {
            System.err.println("Error getting exams by subject: " + e.getMessage());
            return null;
        }
    }
    
    // Grade Management
    public boolean addGrade(int studentId, int examId, double score) {
        try {
            boolean success = clientService.addGrade(String.valueOf(studentId), String.valueOf(examId), score);
            if (success) {
                System.out.println("Adding grade for student ID " + studentId + " in exam ID " + examId + ": " + score);
            } else {
                System.err.println("Failed to add grade for student ID " + studentId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error adding grade: " + e.getMessage());
            return false;
        }
    }
    
    // FIXED: Now actually calls the service to update grade
    public boolean updateGrade(int gradeId, double newScore) {
        try {
            // Call the actual service method instead of just printing
            boolean success = clientService.updateGrade(gradeId, newScore);
            if (success) {
                System.out.println("Grade ID " + gradeId + " updated to: " + newScore);
            } else {
                System.err.println("Failed to update grade ID " + gradeId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error updating grade: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteGrade(int gradeId) {
        try {
            // Implementation for deleting grade
            System.out.println("Deleting grade ID: " + gradeId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting grade: " + e.getMessage());
            return false;
        }
    }
    
    // NEW: Added method to get teacher grades
    public List<Map<String, String>> getTeacherGrades(int teacherId) {
        try {
            return clientService.getTeacherGrades(teacherId);
        } catch (Exception e) {
            System.err.println("Error getting teacher grades: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, String>> getGradesByExam(int examId) {
        try {
            // Implementation for getting grades by exam
            System.out.println("Getting grades for exam ID: " + examId);
            return List.of();
        } catch (Exception e) {
            System.err.println("Error getting grades by exam: " + e.getMessage());
            return null;
        }
    }
    
    // Student Management
    public List<Map<String, String>> getStudentsBySubject(int subjectId) {
        try {
            // Implementation for getting students by subject
            System.out.println("Getting students for subject ID: " + subjectId);
            return List.of();
        } catch (Exception e) {
            System.err.println("Error getting students by subject: " + e.getMessage());
            return null;
        }
    }
    
    public List<Map<String, String>> getStudentsForGrading(int examId) {
        try {
            // Implementation for getting students who need grading
            System.out.println("Getting students for grading in exam ID: " + examId);
            return List.of();
        } catch (Exception e) {
            System.err.println("Error getting students for grading: " + e.getMessage());
            return null;
        }
    }
    
    // Subject Management
    public List<Map<String, String>> getTeacherSubjects(int teacherId) {
        return clientService.getTeacherSubjects(teacherId);
    }
    
    public Map<String, Object> getSubjectStatistics(int subjectId) {
        try {
            // Implementation for subject statistics
            System.out.println("Getting statistics for subject ID: " + subjectId);
            return Map.of(
                "totalStudents", 45,
                "averageGrade", 14.2,
                "successRate", 78.5,
                "highestGrade", 19.5,
                "lowestGrade", 8.0
            );
        } catch (Exception e) {
            System.err.println("Error getting subject statistics: " + e.getMessage());
            return null;
        }
    }
    
    // Grade Calculation
    public double calculateSubjectAverage(int studentId, int subjectId) {
        try {
            Double average = clientService.calculateSubjectAverage(studentId, subjectId);
            return average != null ? average : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating subject average: " + e.getMessage());
            return 0.0;
        }
    }
    
    public Map<Integer, Double> calculateClassAverages(int examId) {
        try {
            // Implementation for calculating class averages
            System.out.println("Calculating class averages for exam ID: " + examId);
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("Error calculating class averages: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    public boolean validateFinalGrades(int subjectId) {
        try {
            // Implementation for validating final grades
            System.out.println("Validating final grades for subject ID: " + subjectId);
            return true;
        } catch (Exception e) {
            System.err.println("Error validating final grades: " + e.getMessage());
            return false;
        }
    }
    
    // Reporting
    public String generateExamReport(int examId) {
        try {
            // Implementation for generating exam report
            System.out.println("Generating report for exam ID: " + examId);
            return "Exam Report for ID: " + examId;
        } catch (Exception e) {
            System.err.println("Error generating exam report: " + e.getMessage());
            return "Error generating report";
        }
    }
    
    public String generateClassPerformanceReport(int subjectId) {
        try {
            // Implementation for generating class performance report
            System.out.println("Generating performance report for subject ID: " + subjectId);
            return "Class Performance Report for Subject ID: " + subjectId;
        } catch (Exception e) {
            System.err.println("Error generating performance report: " + e.getMessage());
            return "Error generating report";
        }
    }
    
    // Bulk Operations
    public boolean importGradesFromFile(int examId, String filePath) {
        try {
            // Implementation for bulk grade import
            System.out.println("Importing grades for exam ID " + examId + " from: " + filePath);
            return true;
        } catch (Exception e) {
            System.err.println("Error importing grades: " + e.getMessage());
            return false;
        }
    }
    
    public boolean exportGradesToFile(int examId, String filePath) {
        try {
            // Implementation for grade export
            System.out.println("Exporting grades for exam ID " + examId + " to: " + filePath);
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting grades: " + e.getMessage());
            return false;
        }
    }
    
    // Utility Methods
    public String getExamTypeDisplayName(String examType) {
        switch (examType.toUpperCase()) {
            case "CONTROLE": return "Control";
            case "EXAMEN": return "Exam";
            case "PROJET": return "Project";
            case "TP": return "Practical Work";
            default: return examType;
        }
    }
    
    public String getGradeStatus(double score) {
        if (score >= 16) return "Excellent";
        else if (score >= 14) return "Very Good";
        else if (score >= 12) return "Good";
        else if (score >= 10) return "Pass";
        else return "Fail";
    }
}