import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.util.HashMap;
public class ClientService {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected = false;
    
    public ClientService() {
        // Regular constructor
    }
    
    // Connection Management
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Connected to server " + host + ":" + port);
            return true;
        } catch (Exception e) {
            System.err.println("Cannot connect to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void disconnect() {
        try {
            if (output != null) {
                output.writeObject("EXIT");
                output.flush();
            }
            if (socket != null) socket.close();
            connected = false;
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    // Database Connection Helper
    private Connection getConnection() throws SQLException {
    System.out.println("🔗 Attempting database connection...");
    
    try {
        // Explicitly load the MySQL JDBC driver
        System.out.println("📦 Loading MySQL JDBC driver...");
        Class.forName("com.mysql.cj.jdbc.Driver");
        System.out.println("✅ MySQL JDBC Driver loaded successfully!");
        
        String url = "jdbc:mysql://localhost:3306/gestion_scolarite";
        String user = "root";
        String pass = ""; // your password here
        
        System.out.println("🔗 Connecting to: " + url);
        System.out.println("👤 Username: " + user);
        
        // Test the connection
        Connection conn = DriverManager.getConnection(url, user, pass);
        System.out.println("✅ Database connection successful!");
        
        return conn;
        
    } catch (ClassNotFoundException e) {
        System.err.println("❌ MySQL JDBC Driver not found in classpath!");
        System.err.println("💡 Current classpath: " + System.getProperty("java.class.path"));
        System.err.println("💡 Make sure ../lib/mysql-connector-java-8.0.33.jar exists and is accessible");
        throw new SQLException("MySQL JDBC Driver not found", e);
    } catch (SQLException e) {
        System.err.println("❌ Database connection failed!");
        System.err.println("💡 Error: " + e.getMessage());
        throw e;
    }
}
    
    // Database Authentication Method
   public Map<String, String> authenticateWithDatabase(String username, String password) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    // Add debug output
    System.out.println("🔐 Attempting authentication for: " + username);
    
    try {
        conn = getConnection();
        System.out.println("✅ Database connection established");
        
        // Updated query to match your actual database schema
        String sql = "SELECT u.*, " +
                    "s.first_name as s_first_name, s.last_name as s_last_name, s.student_id, " +
                    "t.first_name as t_first_name, t.last_name as t_last_name, t.teacher_id " +
                    "FROM users u " +
                    "LEFT JOIN students s ON u.student_id = s.student_id " +
                    "LEFT JOIN teachers t ON u.teacher_id = t.teacher_id " +
                    "WHERE u.username = ? AND u.password_hash = ? AND u.is_active = 1";
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        
        System.out.println("📊 Executing query with username: " + username);
        
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("✅ User found in database!");
            
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userType", rs.getString("user_type"));
            userInfo.put("userId", rs.getString("user_id"));
            
            // Debug: Print all available columns
            System.out.println("User type: " + userInfo.get("userType"));
            System.out.println("User ID: " + userInfo.get("userId"));
            
            // Set user-specific information
            String userType = userInfo.get("userType");
            switch (userType) {
                case "STUDENT":
                    userInfo.put("firstName", rs.getString("s_first_name"));
                    userInfo.put("lastName", rs.getString("s_last_name"));
                    userInfo.put("studentId", rs.getString("student_id"));
                    System.out.println("👨‍🎓 Student: " + rs.getString("s_first_name") + " " + rs.getString("s_last_name"));
                    break;
                case "TEACHER":
                    userInfo.put("firstName", rs.getString("t_first_name"));
                    userInfo.put("lastName", rs.getString("t_last_name"));
                    userInfo.put("teacherId", rs.getString("teacher_id"));
                    System.out.println("👨‍🏫 Teacher: " + rs.getString("t_first_name") + " " + rs.getString("t_last_name"));
                    break;
                case "ADMIN":
                    userInfo.put("firstName", "System");
                    userInfo.put("lastName", "Administrator");
                    System.out.println("👨‍💼 Admin user");
                    break;
                case "RESPONSABLE":
                    userInfo.put("firstName", "Academic");
                    userInfo.put("lastName", "Responsable");
                    System.out.println("👨‍💼 Responsable user");
                    break;
                default:
                    System.out.println("❓ Unknown user type: " + userType);
                    return null;
            }
            
            System.out.println("🎉 Authentication successful!");
            return userInfo;
        } else {
            System.out.println("❌ No user found with these credentials");
            System.out.println("💡 Try these test accounts:");
            System.out.println("   - admin / password");
            System.out.println("   - prof1 / password"); 
            System.out.println("   - ahmed / password");
            System.out.println("   - resp / password");
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Database authentication error: " + e.getMessage());
        e.printStackTrace();
        return null;
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    System.out.println("❌ Authentication failed");
    return null;
}
    
    // Server Authentication
    public Map<String, String> authenticate(String username, String password) {
        try {
            output.writeObject("LOGIN");
            output.writeObject(username);
            output.writeObject(password);
            output.flush();
            
            String response = (String) input.readObject();
            if ("SUCCESS".equals(response)) {
                return (Map<String, String>) input.readObject();
            }
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            // Fall back to database authentication
            return authenticateWithDatabase(username, password);
        }
        return null;
    }
    
    // Student Methods - Database Only
    public Map<String, String> getStudentInfo(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT s.*, p.program_name, u.email " +
                        "FROM students s " +
                        "LEFT JOIN programs p ON s.program_id = p.program_id " +
                        "LEFT JOIN users u ON s.student_id = u.student_id " +
                        "WHERE s.student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> studentInfo = new HashMap<>();
                studentInfo.put("studentId", String.valueOf(studentId));
                studentInfo.put("firstName", rs.getString("first_name"));
                studentInfo.put("lastName", rs.getString("last_name"));
                studentInfo.put("schoolOrigin", rs.getString("school_origin"));
                studentInfo.put("email", rs.getString("email"));
                studentInfo.put("phone", rs.getString("phone"));
                studentInfo.put("program", rs.getString("program_name"));
                studentInfo.put("academicYear", rs.getString("academic_year"));
                studentInfo.put("registrationDate", rs.getString("registration_date"));
                return studentInfo;
            }
        } catch (SQLException e) {
            System.err.println("Error getting student info from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public List<Map<String, String>> getStudentGrades(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> grades = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT g.*, e.exam_name, e.exam_type, e.coefficient, s.subject_name " +
                        "FROM grades g " +
                        "JOIN exams e ON g.exam_id = e.exam_id " +
                        "JOIN subjects s ON e.subject_id = s.subject_id " +
                        "WHERE g.student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("subject", rs.getString("subject_name"));
                grade.put("exam", rs.getString("exam_name"));
                grade.put("type", rs.getString("exam_type"));
                grade.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                grade.put("score", String.valueOf(rs.getDouble("score")));
                grades.add(grade);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student grades from database: " + e.getMessage());
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
    
    public Double getOverallAverage(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT AVG(g.score) as average " +
                        "FROM grades g " +
                        "WHERE g.student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("average");
            }
        } catch (SQLException e) {
            System.err.println("Error getting overall average from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public String getFinalStatus(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT final_status FROM students WHERE student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("final_status");
            }
        } catch (SQLException e) {
            System.err.println("Error getting final status from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    // Student Statistics Method - Database Only
    public Map<String, Object> getStudentStatistics(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> stats = new HashMap<>();
        
        try {
            conn = getConnection();
            
            // Get overall average
            String avgSql = "SELECT AVG(score) as average FROM grades WHERE student_id = ?";
            stmt = conn.prepareStatement(avgSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("overallAverage", rs.getDouble("average"));
            }
            
            // Get total exams
            String countSql = "SELECT COUNT(*) as total FROM grades WHERE student_id = ?";
            stmt = conn.prepareStatement(countSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("totalExams", rs.getInt("total"));
            }
            
            // Get passed exams (assuming pass is 10 or above)
            String passedSql = "SELECT COUNT(*) as passed FROM grades WHERE student_id = ? AND score >= 10";
            stmt = conn.prepareStatement(passedSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int passed = rs.getInt("passed");
                int total = (Integer) stats.get("totalExams");
                stats.put("examsPassed", passed);
                stats.put("successRate", total > 0 ? (passed * 100.0 / total) : 0);
            }
            
            // Get subject-wise statistics
            String subjectSql = "SELECT s.subject_name, AVG(g.score) as average " +
                               "FROM grades g " +
                               "JOIN exams e ON g.exam_id = e.exam_id " +
                               "JOIN subjects s ON e.subject_id = s.subject_id " +
                               "WHERE g.student_id = ? " +
                               "GROUP BY s.subject_name";
            stmt = conn.prepareStatement(subjectSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, String>> subjectStats = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> subject = new HashMap<>();
                subject.put("subject", rs.getString("subject_name"));
                subject.put("average", String.format("%.1f", rs.getDouble("average")));
                subjectStats.add(subject);
            }
            stats.put("subjectStats", subjectStats);
            
        } catch (SQLException e) {
            System.err.println("Error getting student statistics from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return stats;
    }
    
    // Teacher Methods - Database Only
    public boolean createExam(String examName, String subject, String examType, double coefficient, int teacherId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            // First get subject_id from subject name
            String subjectSql = "SELECT subject_id FROM subjects WHERE subject_name = ?";
            stmt = conn.prepareStatement(subjectSql);
            stmt.setString(1, subject);
            ResultSet rs = stmt.executeQuery();
            
            int subjectId = -1;
            if (rs.next()) {
                subjectId = rs.getInt("subject_id");
            }
            rs.close();
            
            if (subjectId == -1) {
                System.err.println("Subject not found: " + subject);
                return false;
            }
            
            String sql = "INSERT INTO exams (exam_name, subject_id, exam_type, coefficient, teacher_id, creation_date) " +
                        "VALUES (?, ?, ?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, examName);
            stmt.setInt(2, subjectId);
            stmt.setString(3, examType);
            stmt.setDouble(4, coefficient);
            stmt.setInt(5, teacherId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating exam in database: " + e.getMessage());
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
    
    public boolean addGrade(String studentId, String examId, double score) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "INSERT INTO grades (student_id, exam_id, score, grade_date) " +
                        "VALUES (?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(studentId));
            stmt.setInt(2, Integer.parseInt(examId));
            stmt.setDouble(3, score);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding grade to database: " + e.getMessage());
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
    
    public boolean updateGrade(int gradeId, double newScore) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "UPDATE grades SET score = ?, grade_date = NOW() WHERE grade_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newScore);
            stmt.setInt(2, gradeId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating grade in database: " + e.getMessage());
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
    
    public Double calculateSubjectAverage(int studentId, int subjectId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT AVG(g.score) as average " +
                        "FROM grades g " +
                        "JOIN exams e ON g.exam_id = e.exam_id " +
                        "WHERE g.student_id = ? AND e.subject_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("average");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating subject average from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public List<Map<String, String>> getTeacherSubjects(int teacherId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> subjects = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT DISTINCT s.subject_id, s.subject_name " +
                        "FROM teacher_subjects ts " +
                        "JOIN subjects s ON ts.subject_id = s.subject_id " +
                        "WHERE ts.teacher_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> subject = new HashMap<>();
                subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
                subject.put("subjectName", rs.getString("subject_name"));
                subjects.add(subject);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting teacher subjects from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return subjects;
    }
    
    public List<Map<String, String>> getTeacherExams(int teacherId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> exams = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT e.exam_id, s.subject_name, e.exam_name, e.exam_type, e.coefficient, e.creation_date " +
                        "FROM exams e " +
                        "JOIN subjects s ON e.subject_id = s.subject_id " +
                        "WHERE e.teacher_id = ? " +
                        "ORDER BY e.creation_date DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> exam = new HashMap<>();
                exam.put("examId", String.valueOf(rs.getInt("exam_id")));
                exam.put("subject", rs.getString("subject_name"));
                exam.put("examName", rs.getString("exam_name"));
                exam.put("examType", rs.getString("exam_type"));
                exam.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                exam.put("creationDate", rs.getString("creation_date"));
                exams.add(exam);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting teacher exams from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return exams;
    }
    
    public List<Map<String, String>> getTeacherGrades(int teacherId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> grades = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT g.grade_id, g.student_id, s.first_name, s.last_name, " +
                        "sub.subject_name, e.exam_name, g.score, g.grade_date " +
                        "FROM grades g " +
                        "JOIN exams e ON g.exam_id = e.exam_id " +
                        "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                        "JOIN students s ON g.student_id = s.student_id " +
                        "WHERE e.teacher_id = ? " +
                        "ORDER BY g.grade_date DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("gradeId", String.valueOf(rs.getInt("grade_id")));
                grade.put("studentId", String.valueOf(rs.getInt("student_id")));
                grade.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
                grade.put("subject", rs.getString("subject_name"));
                grade.put("examName", rs.getString("exam_name"));
                grade.put("grade", String.valueOf(rs.getDouble("score")));
                grade.put("gradeDate", rs.getString("grade_date"));
                grades.add(grade);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting teacher grades from database: " + e.getMessage());
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
    
    public boolean deleteExam(String examId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            // First delete related grades
            String deleteGradesSql = "DELETE FROM grades WHERE exam_id = ?";
            stmt = conn.prepareStatement(deleteGradesSql);
            stmt.setInt(1, Integer.parseInt(examId));
            stmt.executeUpdate();
            stmt.close();
            
            // Then delete the exam
            String deleteExamSql = "DELETE FROM exams WHERE exam_id = ?";
            stmt = conn.prepareStatement(deleteExamSql);
            stmt.setInt(1, Integer.parseInt(examId));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting exam from database: " + e.getMessage());
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
    
    // Admin Methods - Database Only
    public boolean addProgram(String programName, int programYear, String description) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "INSERT INTO programs (program_name, program_year, description) VALUES (?, ?, ?)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, programName);
            stmt.setInt(2, programYear);
            stmt.setString(3, description);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding program to database: " + e.getMessage());
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
    
    public List<Map<String, String>> getAllPrograms() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> programs = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT program_id, program_name, program_year, description FROM programs ORDER BY program_name";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> program = new HashMap<>();
                program.put("programId", String.valueOf(rs.getInt("program_id")));
                program.put("programName", rs.getString("program_name"));
                program.put("programYear", String.valueOf(rs.getInt("program_year")));
                program.put("description", rs.getString("description"));
                programs.add(program);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting programs from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return programs;
    }
    
    // Responsable Methods - Database Only
    public boolean addStudent(String firstName, String lastName, String email, String phone, String schoolOrigin) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "INSERT INTO students (first_name, last_name, school_origin, phone, registration_date) " +
                        "VALUES (?, ?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, schoolOrigin);
            stmt.setString(4, phone);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated student ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int studentId = generatedKeys.getInt(1);
                    
                    // Also create a user account for the student
                    String userSql = "INSERT INTO users (username, password_hash, user_type, student_id, is_active) " +
                                   "VALUES (?, ?, 'STUDENT', ?, 1)";
                    stmt = conn.prepareStatement(userSql);
                    stmt.setString(1, firstName.toLowerCase() + "." + lastName.toLowerCase());
                    stmt.setString(2, "password"); // Default password
                    stmt.setInt(3, studentId);
                    stmt.executeUpdate();
                }
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding student to database: " + e.getMessage());
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
    
    public boolean registerStudent(int studentId, int programId, int yearId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "UPDATE students SET program_id = ?, academic_year = ? WHERE student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, programId);
            stmt.setString(2, getAcademicYearName(yearId));
            stmt.setInt(3, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registering student in database: " + e.getMessage());
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
    
    private String getAcademicYearName(int yearId) {
        switch (yearId) {
            case 1: return "2024-2025";
            case 2: return "2025-2026";
            case 3: return "2026-2027";
            default: return "2024-2025";
        }
    }
    
    public List<Map<String, String>> getAllStudents() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> students = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT s.student_id, s.first_name, s.last_name, s.school_origin, " +
                        "s.phone, p.program_name, s.academic_year, s.registration_date, " +
                        "u.email, s.final_status " +
                        "FROM students s " +
                        "LEFT JOIN programs p ON s.program_id = p.program_id " +
                        "LEFT JOIN users u ON s.student_id = u.student_id " +
                        "ORDER BY s.student_id";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("studentId", String.valueOf(rs.getInt("student_id")));
                student.put("firstName", rs.getString("first_name"));
                student.put("lastName", rs.getString("last_name"));
                student.put("schoolOrigin", rs.getString("school_origin"));
                student.put("phone", rs.getString("phone"));
                student.put("email", rs.getString("email"));
                student.put("program", rs.getString("program_name"));
                student.put("academicYear", rs.getString("academic_year"));
                student.put("registrationDate", rs.getString("registration_date"));
                student.put("status", rs.getString("final_status") != null ? rs.getString("final_status") : "Active");
                students.add(student);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return students;
    }
    
    public List<Map<String, String>> getProgramsWithStats() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> programs = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT p.program_id, p.program_name, p.program_year as duration, " +
                        "COUNT(s.student_id) as current_students, " +
                        "ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                        "FROM programs p " +
                        "LEFT JOIN students s ON p.program_id = s.program_id " +
                        "LEFT JOIN grades g ON s.student_id = g.student_id " +
                        "GROUP BY p.program_id, p.program_name, p.program_year " +
                        "ORDER BY p.program_name";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> program = new HashMap<>();
                program.put("programName", rs.getString("program_name"));
                program.put("duration", String.valueOf(rs.getInt("duration")));
                program.put("availableYears", "1, 2, 3"); // Assuming all programs have 3 years
                program.put("currentStudents", String.valueOf(rs.getInt("current_students")));
                program.put("successRate", String.valueOf(rs.getDouble("success_rate")));
                programs.add(program);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting programs with stats from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return programs;
    }
    // Add these imports at the top if not already present


// Add these methods to your ClientService class:

// Admin Methods
public List<Map<String, String>> getAllUsers() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> users = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT u.user_id, u.username, u.user_type, u.is_active, " +
                    "COALESCE(s.first_name, t.first_name, 'System') as first_name, " +
                    "COALESCE(s.last_name, t.last_name, 'Administrator') as last_name " +
                    "FROM users u " +
                    "LEFT JOIN students s ON u.student_id = s.student_id " +
                    "LEFT JOIN teachers t ON u.teacher_id = t.teacher_id " +
                    "ORDER BY u.user_id";
        
        stmt = conn.prepareStatement(sql);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> user = new HashMap<>();
            user.put("userId", String.valueOf(rs.getInt("user_id")));
            user.put("username", rs.getString("username"));
            user.put("userType", rs.getString("user_type"));
            user.put("firstName", rs.getString("first_name"));
            user.put("lastName", rs.getString("last_name"));
            user.put("status", rs.getBoolean("is_active") ? "Active" : "Inactive");
            users.add(user);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting users from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return users;
}

public Map<String, Object> getSystemStatistics() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Object> stats = new HashMap<>();
    
    try {
        conn = getConnection();
        
        // Total Students
        String studentsSql = "SELECT COUNT(*) as total FROM students";
        stmt = conn.prepareStatement(studentsSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("totalStudents", rs.getInt("total"));
        }
        
        // Total Teachers
        String teachersSql = "SELECT COUNT(*) as total FROM teachers";
        stmt = conn.prepareStatement(teachersSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("totalTeachers", rs.getInt("total"));
        }
        
        // Total Programs
        String programsSql = "SELECT COUNT(*) as total FROM programs";
        stmt = conn.prepareStatement(programsSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("totalPrograms", rs.getInt("total"));
        }
        
        // Active Users
        String activeUsersSql = "SELECT COUNT(*) as total FROM users WHERE is_active = 1";
        stmt = conn.prepareStatement(activeUsersSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("activeUsers", rs.getInt("total"));
        }
        
        // Success Rate (students with average >= 10)
        String successRateSql = "SELECT ROUND(AVG(CASE WHEN student_avg >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                               "FROM (SELECT student_id, AVG(score) as student_avg FROM grades GROUP BY student_id) as student_averages";
        stmt = conn.prepareStatement(successRateSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            double successRate = rs.getDouble("success_rate");
            stats.put("successRate", successRate);
            stats.put("failureRate", 100 - successRate);
        }
        
        // Average Grade
        String avgGradeSql = "SELECT ROUND(AVG(score), 2) as average FROM grades";
        stmt = conn.prepareStatement(avgGradeSql);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("averageGrade", rs.getDouble("average"));
        }
        
        // System Uptime (dummy value for now)
        stats.put("systemUptime", 99.9);
        
        // Detailed Analysis
        StringBuilder analysis = new StringBuilder();
        analysis.append("System Performance Overview:\n");
        analysis.append("- Database connections: Stable\n");
        analysis.append("- User activity: Normal\n");
        analysis.append("- Grade entries: Consistent\n");
        analysis.append("- System resources: Optimal\n");
        stats.put("detailedAnalysis", analysis.toString());
        
    } catch (SQLException e) {
        System.err.println("Error getting system statistics from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return stats;
}

// Teacher Methods
public List<Map<String, String>> getStudentResults(int teacherId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> results = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT s.student_id, s.first_name, s.last_name, " +
                    "sub.subject_name, " +
                    "ROUND(AVG(g.score), 2) as average, " +
                    "CASE WHEN AVG(g.score) >= 10 THEN 'Passed' ELSE 'Failed' END as status, " +
                    "RANK() OVER (PARTITION BY sub.subject_id ORDER BY AVG(g.score) DESC) as rank " +
                    "FROM students s " +
                    "JOIN grades g ON s.student_id = g.student_id " +
                    "JOIN exams e ON g.exam_id = e.exam_id " +
                    "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                    "JOIN teacher_subjects ts ON sub.subject_id = ts.subject_id " +
                    "WHERE ts.teacher_id = ? " +
                    "GROUP BY s.student_id, s.first_name, s.last_name, sub.subject_name, sub.subject_id " +
                    "ORDER BY sub.subject_name, average DESC";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> result = new HashMap<>();
            result.put("studentId", String.valueOf(rs.getInt("student_id")));
            result.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
            result.put("subject", rs.getString("subject_name"));
            result.put("finalGrade", String.valueOf(rs.getDouble("average")));
            result.put("average", String.valueOf(rs.getDouble("average")));
            result.put("status", rs.getString("status"));
            result.put("rank", String.valueOf(rs.getInt("rank")));
            results.add(result);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting student results from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return results;
}

public List<Map<String, String>> getTeacherStudents(int teacherId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> students = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT DISTINCT s.student_id, s.first_name, s.last_name " +
                    "FROM students s " +
                    "JOIN grades g ON s.student_id = g.student_id " +
                    "JOIN exams e ON g.exam_id = e.exam_id " +
                    "JOIN teacher_subjects ts ON e.subject_id = ts.subject_id " +
                    "WHERE ts.teacher_id = ? " +
                    "ORDER BY s.first_name, s.last_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> student = new HashMap<>();
            student.put("studentId", String.valueOf(rs.getInt("student_id")));
            student.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
            students.add(student);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting teacher students from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return students;
}

public Map<String, Object> getTeacherStatistics(int teacherId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Object> stats = new HashMap<>();
    
    try {
        conn = getConnection();
        
        // Total Students
        String studentsSql = "SELECT COUNT(DISTINCT s.student_id) as total " +
                           "FROM students s " +
                           "JOIN grades g ON s.student_id = g.student_id " +
                           "JOIN exams e ON g.exam_id = e.exam_id " +
                           "WHERE e.teacher_id = ?";
        stmt = conn.prepareStatement(studentsSql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("totalStudents", rs.getInt("total"));
        }
        
        // Average Grade
        String avgGradeSql = "SELECT ROUND(AVG(g.score), 2) as average " +
                           "FROM grades g " +
                           "JOIN exams e ON g.exam_id = e.exam_id " +
                           "WHERE e.teacher_id = ?";
        stmt = conn.prepareStatement(avgGradeSql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("averageGrade", rs.getDouble("average"));
        }
        
        // Success Rate
        String successRateSql = "SELECT ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                              "FROM grades g " +
                              "JOIN exams e ON g.exam_id = e.exam_id " +
                              "WHERE e.teacher_id = ?";
        stmt = conn.prepareStatement(successRateSql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("successRate", rs.getDouble("success_rate"));
        }
        
        // Exams Created
        String examsSql = "SELECT COUNT(*) as total FROM exams WHERE teacher_id = ?";
        stmt = conn.prepareStatement(examsSql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("examsCreated", rs.getInt("total"));
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting teacher statistics from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return stats;
}
    public Map<String, Object> getResponsableStatistics() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> stats = new HashMap<>();
        
        try {
            conn = getConnection();
            
            // Total students
            String totalStudentsSql = "SELECT COUNT(*) as total FROM students";
            stmt = conn.prepareStatement(totalStudentsSql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("totalStudents", rs.getInt("total"));
            }
            
            // Active programs
            String activeProgramsSql = "SELECT COUNT(DISTINCT program_id) as active FROM students WHERE program_id IS NOT NULL";
            stmt = conn.prepareStatement(activeProgramsSql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("activePrograms", rs.getInt("active"));
            }
            
            // Registration rate (students with programs vs total students)
            int totalStudents = (Integer) stats.get("totalStudents");
            int studentsWithPrograms = (Integer) stats.get("activePrograms");
            double registrationRate = totalStudents > 0 ? (studentsWithPrograms * 100.0 / totalStudents) : 0;
            stats.put("registrationRate", Math.round(registrationRate));
            
            // Average success rate
            String successRateSql = "SELECT ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as avg_success " +
                                   "FROM grades g";
            stmt = conn.prepareStatement(successRateSql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("averageSuccess", rs.getDouble("avg_success"));
            } else {
                stats.put("averageSuccess", 0);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting responsable statistics from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return stats;
    }
    
    // Remove all sample data methods and server communication methods
    // Only keep database operations
    
    // Remove authenticateForTesting method as we only use database authentication
    // Remove all output.writeObject and input.readObject calls
    // Remove sample data returns
}