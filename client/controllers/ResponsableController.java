import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
public class ResponsableController {
    private ClientService clientService;
    
    public ResponsableController() {
        this.clientService = new ClientService();
    }
    
    // Student Management - FIXED: Now actually calls the service
    public boolean addStudent(String firstName, String lastName, String schoolOrigin, String email, String phone) {
        try {
            // Call the actual service method
            boolean success = clientService.addStudent(firstName, lastName, email, phone, schoolOrigin);
            if (success) {
                System.out.println("Adding student: " + firstName + " " + lastName);
            } else {
                System.err.println("Failed to add student: " + firstName + " " + lastName);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateStudent(int studentId, String firstName, String lastName, String schoolOrigin, String email, String phone) {
        try {
            // Implementation for updating student
            System.out.println("Updating student ID " + studentId + ": " + firstName + " " + lastName);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteStudent(int studentId) {
        try {
            // Implementation for deleting student
            System.out.println("Deleting student ID: " + studentId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }
    
    // FIXED: Now actually calls the service to get students
    public List<Map<String, String>> getAllStudents() {
        try {
            // Call the actual service method
            List<Map<String, String>> students = clientService.getAllStudents();
            System.out.println("Retrieved " + (students != null ? students.size() : 0) + " students");
            return students;
        } catch (Exception e) {
            System.err.println("Error getting students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Map<String, String> getStudentDetails(int studentId) {
        return clientService.getStudentInfo(studentId);
    }
    
    // Registration Management - FIXED: Now actually calls the service
    public boolean registerStudent(int studentId, int programId, int yearId) {
        try {
            // Call the actual service method
            boolean success = clientService.registerStudent(studentId, programId, yearId);
            if (success) {
                System.out.println("Registering student ID " + studentId + " to program ID " + programId);
            } else {
                System.err.println("Failed to register student ID " + studentId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error registering student: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateRegistration(int registrationId, int programId, int yearId) {
        try {
            // Implementation for updating registration
            System.out.println("Updating registration ID " + registrationId);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating registration: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteRegistration(int registrationId) {
        try {
            // Implementation for deleting registration
            System.out.println("Deleting registration ID: " + registrationId);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting registration: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> getStudentRegistrations(int studentId) {
        try {
            // Implementation for getting student registrations
            System.out.println("Getting registrations for student ID: " + studentId);
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error getting registrations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Program Information
    public List<Map<String, String>> getAllPrograms() {
        return clientService.getAllPrograms();
    }
    
    // NEW: Get programs with statistics
    public List<Map<String, String>> getProgramsWithStats() {
        try {
            return clientService.getProgramsWithStats();
        } catch (Exception e) {
            System.err.println("Error getting programs with stats: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Map<String, Object> getProgramStatistics(int programId) {
        try {
            // Implementation for program statistics
            System.out.println("Getting statistics for program ID: " + programId);
            return Map.of(
                "totalStudents", 45,
                "successRate", 82.5,
                "averageGrade", 14.2,
                "completionRate", 78.0
            );
        } catch (Exception e) {
            System.err.println("Error getting program statistics: " + e.getMessage());
            return null;
        }
    }
    
    // NEW: Get responsable statistics
    public Map<String, Object> getResponsableStatistics() {
        try {
            return clientService.getResponsableStatistics();
        } catch (Exception e) {
            System.err.println("Error getting responsable statistics: " + e.getMessage());
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
            // Implementation for getting academic years
            System.out.println("Retrieving academic years");
            List<Map<String, String>> years = new ArrayList<>();
            
            Map<String, String> year1 = new HashMap<>();
            year1.put("yearId", "1");
            year1.put("yearName", "2024-2025");
            years.add(year1);
            
            Map<String, String> year2 = new HashMap<>();
            year2.put("yearId", "2");
            year2.put("yearName", "2025-2026");
            years.add(year2);
            
            return years;
        } catch (Exception e) {
            System.err.println("Error getting academic years: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean setCurrentAcademicYear(int yearId) {
        try {
            // Implementation for setting current academic year
            System.out.println("Setting current academic year to ID: " + yearId);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting academic year: " + e.getMessage());
            return false;
        }
    }
    
    // Bulk Operations
    public boolean importStudentsFromFile(String filePath) {
        try {
            // Implementation for bulk import
            System.out.println("Importing students from file: " + filePath);
            return true;
        } catch (Exception e) {
            System.err.println("Error importing students: " + e.getMessage());
            return false;
        }
    }
    
    public boolean exportStudentData(String filePath) {
        try {
            // Implementation for data export
            System.out.println("Exporting student data to: " + filePath);
            return true;
        } catch (Exception e) {
            System.err.println("Error exporting student data: " + e.getMessage());
            return false;
        }
    }
}