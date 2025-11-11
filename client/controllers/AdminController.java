import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

public class AdminController {
    private ClientService clientService;
    
    public AdminController() {
        this.clientService = new ClientService();
    }
    
    // Program Management
    public boolean addProgram(String programName, int programYear, String description) {
        try {
            return clientService.addProgram(programName, programYear, description);
        } catch (Exception e) {
            System.err.println("Error adding program: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateProgram(int programId, String programName, int programYear, String description) {
        try {
            return clientService.updateProgram(programId, programName, programYear, description);
        } catch (Exception e) {
            System.err.println("Error updating program: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteProgram(int programId) {
        try {
            return clientService.deleteProgram(programId);
        } catch (Exception e) {
            System.err.println("Error deleting program: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> getAllPrograms() {
        return clientService.getAllPrograms();
    }
    
    // User Management
    public List<Map<String, String>> getAllUsers() {
        return clientService.getAllUsers();
    }
    
   // Update the createUser method in AdminController to include debug
public boolean createUser(String username, String password, String userType, 
                        Integer studentId, Integer teacherId, String email) {
    try {
        // Debug output
        System.out.println("🎯 Creating user:");
        System.out.println("   Username: " + username);
        System.out.println("   Type: " + userType);
        System.out.println("   Email: " + email);
        System.out.println("   Student ID: " + studentId);
        System.out.println("   Teacher ID: " + teacherId);
        
        boolean success = clientService.createUser(username, password, userType, studentId, teacherId, email);
        
        // Debug teacher creation
        if ("TEACHER".equals(userType)) {
            clientService.debugTeacherCreation(username, userType, teacherId);
        }
        
        return success;
    } catch (Exception e) {
        System.err.println("Error creating user: " + e.getMessage());
        return false;
    }
}
    
    public boolean updateUserStatus(int userId, boolean isActive) {
        try {
            return clientService.updateUserStatus(userId, isActive);
        } catch (Exception e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }
    
    public boolean resetUserPassword(int userId, String newPassword) {
        try {
            return clientService.resetUserPassword(userId, newPassword);
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }
    
    // System Management
    public boolean backupDatabase() {
        try {
            // TODO: Implement actual backup in ClientService
            System.out.println("Initiating database backup");
            return true;
        } catch (Exception e) {
            System.err.println("Error backing up database: " + e.getMessage());
            return false;
        }
    }
    
    public boolean restoreDatabase(String backupFile) {
        try {
            // TODO: Implement actual restore in ClientService
            System.out.println("Restoring database from: " + backupFile);
            return true;
        } catch (Exception e) {
            System.err.println("Error restoring database: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getSystemStatistics() {
        try {
            return clientService.getSystemStatistics();
        } catch (Exception e) {
            System.err.println("Error getting system statistics: " + e.getMessage());
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalStudents", 0);
            errorStats.put("totalTeachers", 0);
            errorStats.put("totalPrograms", 0);
            errorStats.put("successRate", 0.0);
            errorStats.put("systemUptime", 0.0);
            return errorStats;
        }
    }
    
    // Subject/Topic Management
    public boolean addSubject(String subjectName, int programId, int semester, double coefficient, String objectives) {
        try {
            return clientService.addSimpleSubjectToProgram(programId, subjectName, objectives, semester, coefficient);
        } catch (Exception e) {
            System.err.println("Error adding subject: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateSubject(int subjectId, String subjectName, double coefficient, String objectives) {
        try {
            // TODO: Implement actual subject update in ClientService
            System.out.println("Updating subject ID " + subjectId + ": " + subjectName);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating subject: " + e.getMessage());
            return false;
        }
    }
    
    // Teacher Management
    public List<Map<String, String>> getAllTeachers() {
        return clientService.getAllTeachers();
    }
    
    public List<Map<String, String>> getSubjectsByProgram(int programId) {
        return clientService.getSubjectsByProgram(programId);
    }
    
    public boolean assignTeacherToProgram(int programId, int teacherId, int subjectId, String academicYear) {
        try {
            return clientService.assignTeacherToProgram(programId, teacherId, subjectId, academicYear);
        } catch (Exception e) {
            System.err.println("Error assigning teacher to program: " + e.getMessage());
            return false;
        }
    }
    
    public boolean addProgramWithTeachers(String programName, int programYear, String description, 
                                     Map<Integer, Integer> teacherAssignments, String academicYear) {
        try {
            return clientService.addProgramWithTeachers(programName, programYear, description, 
                                                   teacherAssignments, academicYear);
        } catch (Exception e) {
            System.err.println("Error adding program with teachers: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> getProgramTeachers(int programId) {
        try {
            return clientService.getProgramTeachers(programId);
        } catch (Exception e) {
            System.err.println("Error getting program teachers: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Enhanced Topic Management Methods
    public List<Map<String, String>> getProgramSubjects(int programId) {
        try {
            return clientService.getProgramSubjectsWithDetails(programId);
        } catch (Exception e) {
            System.err.println("Error getting program subjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean addSubjectToProgram(int programId, String subjectName, String objectives, 
                                     int semester, double coefficient) {
        try {
            return clientService.addSimpleSubjectToProgram(programId, subjectName, objectives, semester, coefficient);
        } catch (Exception e) {
            System.err.println("Error adding subject to program: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean assignTeacherToSubject(int teacherId, int subjectId) {
        try {
            return clientService.assignTeacherToSubject(teacherId, subjectId, "2024-2025");
        } catch (Exception e) {
            System.err.println("Error assigning teacher to subject: " + e.getMessage());
            return false;
        }
    }
    
    public boolean removeTeacherFromSubject(int subjectId) {
        try {
            return clientService.removeTeacherFromSubject(subjectId);
        } catch (Exception e) {
            System.err.println("Error removing teacher from subject: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> getUnassignedSubjects(int programId) {
        try {
            return clientService.getUnassignedSubjects(programId);
        } catch (Exception e) {
            System.err.println("Error getting unassigned subjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, String>> getAllTeachersForDropdown() {
        try {
            List<Map<String, String>> teachers = clientService.getAllTeachers();
            List<Map<String, String>> dropdownTeachers = new ArrayList<>();
            
            for (Map<String, String> teacher : teachers) {
                Map<String, String> dropdownTeacher = new HashMap<>();
                dropdownTeacher.put("teacherId", teacher.get("teacherId"));
                dropdownTeacher.put("firstName", teacher.get("firstName"));
                dropdownTeacher.put("lastName", teacher.get("lastName"));
                dropdownTeacher.put("specialty", teacher.get("specialty"));
                dropdownTeacher.put("displayName", teacher.get("firstName") + " " + teacher.get("lastName") + 
                                  " (" + teacher.get("specialty") + ")");
                dropdownTeachers.add(dropdownTeacher);
            }
            
            return dropdownTeachers;
        } catch (Exception e) {
            System.err.println("Error getting teachers for dropdown: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Utility Methods
    public List<String> getProgramTypes() {
        return Arrays.asList("LICENCE", "MASTER", "DUT", "INGENIEUR");
    }
    
    public List<String> getDepartments() {
        return Arrays.asList("Informatique", "Mathématiques", "Physique", "Électronique", 
                           "Génie Civil", "Génie Mécanique", "Commerce", "Droit");
    }
    
    public List<String> getSemesters() {
        return Arrays.asList("1", "2", "3", "4", "5", "6");
    }
    
    // Enhanced program creation with structure
    public boolean createProgramWithStructure(String programName, String programType, int durationYears, 
                                           int totalCredits, String department, String description) {
        try {
            return clientService.createProgramWithStructure(programName, programType, durationYears, 
                                                          totalCredits, department, description);
        } catch (Exception e) {
            System.err.println("Error creating program with structure: " + e.getMessage());
            return false;
        }
    }
    
    // Get program structure
    public Map<String, Object> getProgramStructure(int programId) {
        try {
            return clientService.getProgramStructure(programId);
        } catch (Exception e) {
            System.err.println("Error getting program structure: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // Get all programs with detailed information
    public List<Map<String, String>> getAllProgramsDetailed() {
        try {
            List<Map<String, String>> programs = clientService.getAllPrograms();
            List<Map<String, String>> detailedPrograms = new ArrayList<>();
            
            for (Map<String, String> program : programs) {
                int programId = Integer.parseInt(program.get("programId"));
                Map<String, Object> structure = getProgramStructure(programId);
                
                Map<String, String> detailedProgram = new HashMap<>(program);
                detailedProgram.put("semesterCount", "6"); // Default value
                detailedProgram.put("totalCredits", "180"); // Default value
                detailedPrograms.add(detailedProgram);
            }
            
            return detailedPrograms;
        } catch (Exception e) {
            System.err.println("Error getting detailed programs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean doesProgramExist(int programId) {
        try {
            return clientService.doesProgramExist(programId);
        } catch (Exception e) {
            System.err.println("Error checking program existence: " + e.getMessage());
            return false;
        }
    }
    
    public void checkDatabaseStructure() {
        try {
            clientService.checkDatabaseStructure();
        } catch (Exception e) {
            System.err.println("Error checking database structure: " + e.getMessage());
        }
    }
    
    // Program Data Methods
    public Map<String, String> getProgramData(int programId) {
        try {
            List<Map<String, String>> programs = getAllPrograms();
            for (Map<String, String> program : programs) {
                if (Integer.parseInt(program.get("programId")) == programId) {
                    return program;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting program data: " + e.getMessage());
        }
        
        // Return default data if not found
        Map<String, String> defaultData = new HashMap<>();
        defaultData.put("programName", "");
        defaultData.put("programYear", "");
        defaultData.put("description", "");
        return defaultData;
    }
    
    public List<Map<String, String>> getProgramStudents(int programId) {
        try {
            return clientService.getStudentsByProgram(programId);
        } catch (Exception e) {
            System.err.println("Error getting program students: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Map<String, Object> getProgramStatistics(int programId) {
        try {
            return clientService.getProgramStatistics(programId);
        } catch (Exception e) {
            System.err.println("Error getting program statistics: " + e.getMessage());
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalStudents", 0);
            errorStats.put("successRate", 0.0);
            errorStats.put("averageGrade", 0.0);
            errorStats.put("totalSubjects", 0);
            return errorStats;
        }
    }
    // Add these methods to AdminController class

// Replace the getUserData method in AdminController with this improved version:
public Map<String, String> getUserData(int userId) {
    try {
        // First try to get detailed user information from database
        Map<String, String> userDetails = clientService.getUserDetails(userId);
        if (userDetails != null) {
            System.out.println("✅ Found detailed user data for ID: " + userId);
            return userDetails;
        }
    } catch (Exception e) {
        System.err.println("Error getting detailed user data: " + e.getMessage());
    }
    
    // Fallback: try to get basic info from the users list
    try {
        List<Map<String, String>> users = getAllUsers();
        for (Map<String, String> user : users) {
            if (Integer.parseInt(user.get("userId")) == userId) {
                System.out.println("✅ Found user in list for ID: " + userId);
                return user;
            }
        }
    } catch (Exception e) {
        System.err.println("Error getting user from list: " + e.getMessage());
    }
    
    // Return default data if not found
    System.out.println("❌ User not found, returning default data for ID: " + userId);
    Map<String, String> defaultData = new HashMap<>();
    defaultData.put("userId", String.valueOf(userId));
    defaultData.put("username", "Unknown");
    defaultData.put("userType", "UNKNOWN");
    defaultData.put("firstName", "Unknown");
    defaultData.put("lastName", "User");
    defaultData.put("status", "Active");
    return defaultData;
}
// Add this method to your AdminController class
public void debugProgramTeachers(int programId) {
    System.out.println("🔍 [DEBUG PROGRAM TEACHERS]");
    System.out.println("   Program ID: " + programId);
    
    try {
        // Call the ClientService debug method
        clientService.debugProgramTeachers(programId);
    } catch (Exception e) {
        System.err.println("❌ Error in debugProgramTeachers: " + e.getMessage());
        e.printStackTrace();
    }
}

}