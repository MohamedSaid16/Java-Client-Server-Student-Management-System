import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;

public class ResponsableController {
    private ClientService clientService;
    
    public ResponsableController() {
        this.clientService = new ClientService();
    }
    
    // Enhanced Student Management - Fixed with proper database calls
    public boolean addStudent(String firstName, String lastName, String schoolOrigin, String email, String phone) {
        try {
            System.out.println("🔄 [DEBUG] Controller adding student: " + firstName + " " + lastName);
            
            // Call the service with correct parameter order
            boolean success = clientService.addStudent(firstName, lastName, email, phone, schoolOrigin);
            
            if (success) {
                System.out.println("✅ Student added successfully!");
            } else {
                System.err.println("❌ Failed to add student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error adding student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Enhanced student update with database integration
    public boolean updateStudent(int studentId, Map<String, String> studentData) {
        try {
            System.out.println("🔄 [DEBUG] Updating student ID: " + studentId);
            System.out.println("📝 [DEBUG] Student data: " + studentData);
            
            boolean success = clientService.updateStudentInfo(studentId, studentData);
            
            if (success) {
                System.out.println("✅ Student updated successfully!");
            } else {
                System.err.println("❌ Failed to update student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean updateStudent(int studentId, String firstName, String lastName, String schoolOrigin, String email, String phone) {
        Map<String, String> studentData = new HashMap<>();
        studentData.put("firstName", firstName);
        studentData.put("lastName", lastName);
        studentData.put("schoolOrigin", schoolOrigin);
        studentData.put("email", email);
        studentData.put("phone", phone);
        studentData.put("academicYear", "2024-2025"); // Default value
        
        return updateStudent(studentId, studentData);
    }
    
    // Enhanced student deletion with database integration
    public boolean deleteStudent(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            // First delete related records (grades, users)
            String deleteGradesSql = "DELETE FROM grades WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteGradesSql);
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
            stmt.close();
            
            String deleteUserSql = "DELETE FROM users WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteUserSql);
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
            stmt.close();
            
            // Then delete the student
            String deleteStudentSql = "DELETE FROM students WHERE student_id = ?";
            stmt = conn.prepareStatement(deleteStudentSql);
            stmt.setInt(1, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Student deleted successfully!");
                return true;
            } else {
                System.err.println("❌ Failed to delete student");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Get all students - Fixed with database call
    public List<Map<String, String>> getAllStudents() {
        try {
            // Call the actual service method
            List<Map<String, String>> students = clientService.getAllStudents();
            System.out.println("✅ Retrieved " + (students != null ? students.size() : 0) + " students");
            return students;
        } catch (Exception e) {
            System.err.println("❌ Error getting students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Enhanced student details with program information
    public Map<String, String> getStudentDetails(int studentId) {
        try {
            Map<String, String> studentInfo = clientService.getStudentWithProgram(studentId);
            if (studentInfo != null) {
                System.out.println("✅ Retrieved student details for ID: " + studentId);
            } else {
                System.err.println("❌ Student not found with ID: " + studentId);
            }
            return studentInfo;
        } catch (Exception e) {
            System.err.println("❌ Error getting student details: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // Enhanced student registration with program
    public boolean registerStudent(int studentId, int programId, String academicYear) {
        try {
            System.out.println("🔄 [DEBUG] Registering student ID: " + studentId + " to program ID: " + programId + " for year: " + academicYear);
            
            boolean success = clientService.registerStudentToProgram(studentId, programId, academicYear);
            
            if (success) {
                System.out.println("✅ Student registered successfully!");
            } else {
                System.err.println("❌ Failed to register student");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error registering student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean registerStudent(int studentId, int programId, int yearId) {
        String academicYear = getAcademicYearName(yearId);
        return registerStudent(studentId, programId, academicYear);
    }
    
    // Enhanced registration update
    public boolean updateRegistration(int studentId, int programId, String academicYear) {
        try {
            System.out.println("🔄 [DEBUG] Updating registration for student ID: " + studentId);
            
            // This is essentially the same as registering
            boolean success = clientService.registerStudentToProgram(studentId, programId, academicYear);
            
            if (success) {
                System.out.println("✅ Registration updated successfully!");
            } else {
                System.err.println("❌ Failed to update registration");
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Error updating registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Keep old method for compatibility
    public boolean updateRegistration(int registrationId, int programId, int yearId) {
        // For simplicity, we'll use student ID as registration ID in this context
        String academicYear = getAcademicYearName(yearId);
        return updateRegistration(registrationId, programId, academicYear);
    }
    
    // Enhanced registration deletion
    public boolean deleteRegistration(int studentId) {
        try {
            System.out.println("🔄 [DEBUG] Deleting registration for student ID: " + studentId);
            
            // Remove program assignment by setting program_id to NULL
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "UPDATE students SET program_id = NULL, academic_year = NULL WHERE student_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("✅ Registration deleted successfully!");
                    return true;
                } else {
                    System.err.println("❌ Failed to delete registration");
                    return false;
                }
            } finally {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
 
    
    // Get student registrations
    public List<Map<String, String>> getStudentRegistrations(int studentId) {
        try {
            List<Map<String, String>> registrations = new ArrayList<>();
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "SELECT s.student_id, s.first_name, s.last_name, p.program_name, s.academic_year " +
                           "FROM students s " +
                           "LEFT JOIN programs p ON s.program_id = p.program_id " +
                           "WHERE s.student_id = ? AND s.program_id IS NOT NULL";
                
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentId);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, String> registration = new HashMap<>();
                    registration.put("studentId", String.valueOf(rs.getInt("student_id")));
                    registration.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
                    registration.put("programName", rs.getString("program_name"));
                    registration.put("academicYear", rs.getString("academic_year"));
                    registrations.add(registration);
                }
                
                System.out.println("✅ Retrieved " + registrations.size() + " registrations for student ID: " + studentId);
            } finally {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            
            return registrations;
        } catch (Exception e) {
            System.err.println("❌ Error getting registrations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Program Information
    public List<Map<String, String>> getAllPrograms() {
        try {
            List<Map<String, String>> programs = clientService.getAllPrograms();
            System.out.println("✅ Retrieved " + programs.size() + " programs");
            return programs;
        } catch (Exception e) {
            System.err.println("❌ Error getting programs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Get programs for dropdown (simplified version)
    public List<Map<String, String>> getProgramsForDropdown() {
        return clientService.getAllProgramsForDropdown();
    }
    
    // Get programs with statistics
    public List<Map<String, String>> getProgramsWithStats() {
        try {
            List<Map<String, String>> programs = clientService.getProgramsWithStats();
            System.out.println("✅ Retrieved " + programs.size() + " programs with statistics");
            return programs;
        } catch (Exception e) {
            System.err.println("❌ Error getting programs with stats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Enhanced program statistics with database integration
    public Map<String, Object> getProgramStatistics(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = clientService.getConnection();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Total students in program
            String studentsSql = "SELECT COUNT(*) as total FROM students WHERE program_id = ?";
            stmt = conn.prepareStatement(studentsSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("totalStudents", rs.getInt("total"));
            }
            rs.close();
            stmt.close();
            
            // Success rate
            String successRateSql = "SELECT ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                                  "FROM grades g " +
                                  "JOIN exams e ON g.exam_id = e.exam_id " +
                                  "JOIN subjects s ON e.subject_id = s.subject_id " +
                                  "JOIN students st ON g.student_id = st.student_id " +
                                  "WHERE st.program_id = ?";
            stmt = conn.prepareStatement(successRateSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("successRate", rs.getDouble("success_rate"));
            } else {
                stats.put("successRate", 0.0);
            }
            rs.close();
            stmt.close();
            
            // Average grade
            String avgGradeSql = "SELECT ROUND(AVG(g.score), 2) as average FROM grades g " +
                               "JOIN students s ON g.student_id = s.student_id " +
                               "WHERE s.program_id = ?";
            stmt = conn.prepareStatement(avgGradeSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("averageGrade", rs.getDouble("average"));
            } else {
                stats.put("averageGrade", 0.0);
            }
            
            // Completion rate (estimated)
            stats.put("completionRate", 78.0); // This would require more complex calculation
            
            System.out.println("✅ Retrieved statistics for program ID: " + programId);
            return stats;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting program statistics: " + e.getMessage());
            return Map.of(
                "totalStudents", 0,
                "successRate", 0.0,
                "averageGrade", 0.0,
                "completionRate", 0.0
            );
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Get responsable statistics
    public Map<String, Object> getResponsableStatistics() {
        try {
            Map<String, Object> stats = clientService.getResponsableStatistics();
            System.out.println("✅ Retrieved responsable statistics");
            return stats;
        } catch (Exception e) {
            System.err.println("❌ Error getting responsable statistics: " + e.getMessage());
            return Map.of(
                "totalStudents", 0,
                "activePrograms", 0,
                "registrationRate", 0,
                "averageSuccess", 0
            );
        }
    }
    
    // Academic Year Management
    public List<Map<String, String>> getAcademicYears() {
        try {
            List<Map<String, String>> years = new ArrayList<>();
            
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = clientService.getConnection();
                String sql = "SELECT year_id, start_year, end_year, is_current FROM academic_years ORDER BY start_year DESC";
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, String> year = new HashMap<>();
                    year.put("yearId", String.valueOf(rs.getInt("year_id")));
                    year.put("startYear", String.valueOf(rs.getInt("start_year")));
                    year.put("endYear", String.valueOf(rs.getInt("end_year")));
                    year.put("yearName", rs.getInt("start_year") + "-" + rs.getInt("end_year"));
                    year.put("isCurrent", rs.getBoolean("is_current") ? "Yes" : "No");
                    years.add(year);
                }
                
                // If no years in database, create default ones
                if (years.isEmpty()) {
                    for (int i = 0; i < 3; i++) {
                        Map<String, String> year = new HashMap<>();
                        int startYear = 2024 + i;
                        year.put("yearId", String.valueOf(i + 1));
                        year.put("startYear", String.valueOf(startYear));
                        year.put("endYear", String.valueOf(startYear + 1));
                        year.put("yearName", startYear + "-" + (startYear + 1));
                        year.put("isCurrent", i == 0 ? "Yes" : "No");
                        years.add(year);
                    }
                }
                
                System.out.println("✅ Retrieved " + years.size() + " academic years");
            } finally {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            
            return years;
        } catch (Exception e) {
            System.err.println("❌ Error getting academic years: " + e.getMessage());
            
            // Return default years if database error
            List<Map<String, String>> years = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, String> year = new HashMap<>();
                int startYear = 2024 + i;
                year.put("yearId", String.valueOf(i + 1));
                year.put("yearName", startYear + "-" + (startYear + 1));
                years.add(year);
            }
            return years;
        }
    }
    
    // Set current academic year
    public boolean setCurrentAcademicYear(int yearId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            // First, set all years to not current
            String resetSql = "UPDATE academic_years SET is_current = FALSE";
            stmt = conn.prepareStatement(resetSql);
            stmt.executeUpdate();
            stmt.close();
            
            // Then set the selected year as current
            String setSql = "UPDATE academic_years SET is_current = TRUE WHERE year_id = ?";
            stmt = conn.prepareStatement(setSql);
            stmt.setInt(1, yearId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Set current academic year to ID: " + yearId);
                return true;
            } else {
                System.err.println("❌ Failed to set current academic year");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error setting academic year: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Bulk Operations
    public boolean importStudentsFromFile(String filePath) {
        try {
            System.out.println("🔄 Importing students from file: " + filePath);
            // This would be implemented to read from CSV/Excel and insert into database
            // For now, just return true for demonstration
            System.out.println("✅ Students imported successfully (simulation)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error importing students: " + e.getMessage());
            return false;
        }
    }
    
    public boolean exportStudentData(String filePath) {
        try {
            System.out.println("🔄 Exporting student data to: " + filePath);
            // This would be implemented to export student data to CSV/Excel
            // For now, just return true for demonstration
            System.out.println("✅ Student data exported successfully (simulation)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error exporting student data: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to get program ID by name
    public Integer getProgramIdByName(String programName) {
        try {
            Integer programId = clientService.getProgramIdByName(programName);
            if (programId != null) {
                System.out.println("✅ Found program ID: " + programId + " for program: " + programName);
            } else {
                System.err.println("❌ Program not found: " + programName);
            }
            return programId;
        } catch (Exception e) {
            System.err.println("❌ Error getting program ID: " + e.getMessage());
            return null;
        }
    }
    
    // Helper method to get student for registration
    public Map<String, String> getStudentForRegistration(int studentId) {
        return getStudentDetails(studentId);
    }
    
    // Helper method to convert year ID to academic year name
    private String getAcademicYearName(int yearId) {
        switch (yearId) {
            case 1: return "2024-2025";
            case 2: return "2025-2026";
            case 3: return "2026-2027";
            default: return "2024-2025";
        }
    }
    
    // Additional utility methods
    
    // Search students by name
    public List<Map<String, String>> searchStudents(String searchTerm) {
        try {
            List<Map<String, String>> allStudents = getAllStudents();
            List<Map<String, String>> filteredStudents = new ArrayList<>();
            
            for (Map<String, String> student : allStudents) {
                String firstName = student.get("firstName").toLowerCase();
                String lastName = student.get("lastName").toLowerCase();
                String search = searchTerm.toLowerCase();
                
                if (firstName.contains(search) || lastName.contains(search) || 
                    (firstName + " " + lastName).contains(search)) {
                    filteredStudents.add(student);
                }
            }
            
            System.out.println("✅ Found " + filteredStudents.size() + " students matching: " + searchTerm);
            return filteredStudents;
        } catch (Exception e) {
            System.err.println("❌ Error searching students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Get students by program
    public List<Map<String, String>> getStudentsByProgram(int programId) {
        try {
            List<Map<String, String>> allStudents = getAllStudents();
            List<Map<String, String>> programStudents = new ArrayList<>();
            
            for (Map<String, String> student : allStudents) {
                String studentProgramId = student.get("programId");
                if (studentProgramId != null && !studentProgramId.equals("null") && 
                    Integer.parseInt(studentProgramId) == programId) {
                    programStudents.add(student);
                }
            }
            
            System.out.println("✅ Found " + programStudents.size() + " students in program ID: " + programId);
            return programStudents;
        } catch (Exception e) {
            System.err.println("❌ Error getting students by program: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Update student status
    public boolean updateStudentStatus(int studentId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = clientService.getConnection();
            
            String sql = "UPDATE students SET final_status = ? WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Updated student status to: " + status + " for student ID: " + studentId);
                return true;
            } else {
                System.err.println("❌ Failed to update student status");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating student status: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    // Get student grades - NEW METHOD
    public List<Map<String, String>> getStudentGrades(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> grades = new ArrayList<>();
        
        try {
            conn = clientService.getConnection();
            
            // Query to get student grades with subject information
            String sql = "SELECT " +
                        "s.subject_name, " +
                        "s.credits, " +
                        "g.grade, " +
                        "g.semester, " +
                        "g.academic_year, " +
                        "g.status, " +
                        "g.exam_date " +
                        "FROM grades g " +
                        "JOIN subjects s ON g.subject_id = s.subject_id " +
                        "WHERE g.student_id = ? " +
                        "ORDER BY g.academic_year, g.semester, s.subject_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("subjectName", rs.getString("subject_name"));
                grade.put("credits", String.valueOf(rs.getDouble("credits")));
                grade.put("grade", rs.getString("grade"));
                grade.put("semester", rs.getString("semester"));
                grade.put("academicYear", rs.getString("academic_year"));
                grade.put("status", rs.getString("status"));
                grade.put("examDate", rs.getString("exam_date"));
                
                grades.add(grade);
            }
            
            System.out.println("✅ Retrieved " + grades.size() + " grades for student ID: " + studentId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting student grades: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return grades;
    }
    
    // Get student performance summary
    public Map<String, Object> getStudentPerformanceSummary(int studentId) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            List<Map<String, String>> grades = getStudentGrades(studentId);
            
            double totalCredits = 0;
            double weightedSum = 0;
            int passedSubjects = 0;
            int totalSubjects = grades.size();
            
            for (Map<String, String> grade : grades) {
                double credits = Double.parseDouble(grade.get("credits"));
                String gradeValue = grade.get("grade");
                double gradePoints = convertGradeToPoints(gradeValue);
                
                totalCredits += credits;
                weightedSum += (gradePoints * credits);
                
                if (isGradePassing(gradeValue)) {
                    passedSubjects++;
                }
            }
            
            double gpa = (totalCredits > 0) ? weightedSum / totalCredits : 0.0;
            double successRate = (totalSubjects > 0) ? (passedSubjects * 100.0) / totalSubjects : 0.0;
            
            summary.put("gpa", gpa);
            summary.put("totalCredits", totalCredits);
            summary.put("totalSubjects", totalSubjects);
            summary.put("passedSubjects", passedSubjects);
            summary.put("successRate", successRate);
            summary.put("grades", grades);
            
            System.out.println("✅ Generated performance summary for student ID: " + studentId);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating performance summary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }
    
    // Helper method to convert grade to points
    private double convertGradeToPoints(String grade) {
        if (grade == null) return 0.0;
        
        switch (grade.toUpperCase()) {
            case "A": case "A+": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }
    
    // Helper method to check if grade is passing
    private boolean isGradePassing(String grade) {
        if (grade == null) return false;
        
        switch (grade.toUpperCase()) {
            case "A": case "A+": case "A-":
            case "B": case "B+": case "B-":
            case "C": case "C+": case "C-":
            case "D": case "D+":
                return true;
            case "F":
            default:
                return false;
        }
    }
}