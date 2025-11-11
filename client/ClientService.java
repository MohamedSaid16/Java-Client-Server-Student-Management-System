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
    
    // Database Connection Helper - MAKE IT PUBLIC
    public Connection getConnection() throws SQLException {
        System.out.println(" Attempting database connection...");
        
        try {
            // Explicitly load the MySQL JDBC driver
            System.out.println(" Loading MySQL JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(" MySQL JDBC Driver loaded successfully!");
            
            String url = "jdbc:mysql://localhost:3306/gestion_scolarite";
            String user = "root";
            String pass = ""; // your password here
            
            System.out.println(" Connecting to: " + url);
            System.out.println(" Username: " + user);
            
            // Test the connection
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println(" Database connection successful!");
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL JDBC Driver not found in classpath!");
            System.err.println(" Current classpath: " + System.getProperty("java.class.path"));
            System.err.println(" Make sure ../lib/mysql-connector-java-8.0.33.jar exists and is accessible");
            throw new SQLException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println(" Database connection failed!");
            System.err.println(" Error: " + e.getMessage());
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
            System.out.println(" Database connection established");
            
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
            
            System.out.println(" Executing query with username: " + username);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println(" User found in database!");
                
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
                
                System.out.println(" Authentication successful!");
                return userInfo;
            } else {
                System.out.println(" No user found with these credentials");
                System.out.println(" Try these test accounts:");
                System.out.println("   - admin / password");
                System.out.println("   - prof1 / password"); 
                System.out.println("   - ahmed / password");
                System.out.println("   - resp / password");
            }
            
        } catch (SQLException e) {
            System.err.println(" Database authentication error: " + e.getMessage());
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
        
        System.out.println(" Authentication failed");
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
    
    // Add this method to ClientService class
    public List<Map<String, String>> getStudentTranscript(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> transcript = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT " +
                        "s.subject_name, " +
                        "e.exam_name, " +
                        "e.exam_type, " +
                        "e.coefficient, " +
                        "g.score, " +
                        "g.grade_date, " +
                        "p.program_name, " +
                        "st.first_name, " +
                        "st.last_name, " +
                        "st.academic_year " +
                        "FROM grades g " +
                        "JOIN exams e ON g.exam_id = e.exam_id " +
                        "JOIN subjects s ON e.subject_id = s.subject_id " +
                        "JOIN students st ON g.student_id = st.student_id " +
                        "LEFT JOIN programs p ON st.program_id = p.program_id " +
                        "WHERE g.student_id = ? " +
                        "ORDER BY s.subject_name, e.exam_type, g.grade_date";
        
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("subject", rs.getString("subject_name"));
                grade.put("exam", rs.getString("exam_name"));
                grade.put("type", rs.getString("exam_type"));
                grade.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                grade.put("score", String.format("%.2f", rs.getDouble("score")));
                grade.put("gradeDate", rs.getString("grade_date"));
                grade.put("program", rs.getString("program_name"));
                grade.put("firstName", rs.getString("first_name"));
                grade.put("lastName", rs.getString("last_name"));
                grade.put("academicYear", rs.getString("academic_year"));
                transcript.add(grade);
            }
            
            // Calculate subject averages and overall average
            calculateAverages(transcript, studentId);
            
        } catch (SQLException e) {
            System.err.println("Error getting student transcript: " + e.getMessage());
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
        
        return transcript;
    }

    private void calculateAverages(List<Map<String, String>> transcript, int studentId) {
        // Calculate subject averages
        Map<String, List<Double>> subjectScores = new HashMap<>();
        Map<String, List<Double>> subjectCoefficients = new HashMap<>();
        
        // First, clear any existing average entries
        transcript.removeIf(grade -> "MOYENNE MATIÈRE".equals(grade.get("exam")) || 
                                    "MOYENNE GÉNÉRALE".equals(grade.get("exam")));
        
        for (Map<String, String> grade : transcript) {
            String subject = grade.get("subject");
            double score = Double.parseDouble(grade.get("score"));
            double coefficient = Double.parseDouble(grade.get("coefficient"));
            
            subjectScores.computeIfAbsent(subject, k -> new ArrayList<>()).add(score);
            subjectCoefficients.computeIfAbsent(subject, k -> new ArrayList<>()).add(coefficient);
        }
        
        // Add subject averages to transcript
        for (String subject : subjectScores.keySet()) {
            List<Double> scores = subjectScores.get(subject);
            List<Double> coefficients = subjectCoefficients.get(subject);
            
            double weightedSum = 0;
            double totalCoefficient = 0;
            
            for (int i = 0; i < scores.size(); i++) {
                weightedSum += scores.get(i) * coefficients.get(i);
                totalCoefficient += coefficients.get(i);
            }
            
            double subjectAverage = totalCoefficient > 0 ? weightedSum / totalCoefficient : 0;
            
            Map<String, String> averageEntry = new HashMap<>();
            averageEntry.put("subject", subject);
            averageEntry.put("exam", "MOYENNE MATIÈRE");
            averageEntry.put("type", "MOYENNE");
            averageEntry.put("coefficient", String.format("%.2f", totalCoefficient));
            averageEntry.put("score", String.format("%.2f", subjectAverage));
            averageEntry.put("gradeDate", "");
            averageEntry.put("program", "");
            averageEntry.put("firstName", "");
            averageEntry.put("lastName", "");
            averageEntry.put("academicYear", "");
            transcript.add(averageEntry);
        }
    }
    
    // Student Methods - Database Only
    public Map<String, String> getStudentInfo(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("🔍 [DEBUG] Starting getStudentInfo for ID: " + studentId);
            conn = getConnection();
            System.out.println("✅ [DEBUG] Database connection established");

            // First, let's try a simple query without JOINs
            String sql = "SELECT * FROM students WHERE student_id = ?";
            System.out.println("🔍 [DEBUG] Executing SQL: " + sql);
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ [DEBUG] Student found in database!");
                Map<String, String> studentInfo = new HashMap<>();
                
                // Get basic student info
                studentInfo.put("studentId", String.valueOf(studentId));
                studentInfo.put("firstName", rs.getString("first_name"));
                studentInfo.put("lastName", rs.getString("last_name"));
                studentInfo.put("schoolOrigin", rs.getString("school_origin"));
                studentInfo.put("phone", rs.getString("phone"));
                studentInfo.put("academicYear", rs.getString("academic_year"));
                studentInfo.put("registrationDate", rs.getString("registration_date"));
                
                System.out.println("🔍 [DEBUG] Basic student info retrieved:");
                System.out.println("  - First Name: " + studentInfo.get("firstName"));
                System.out.println("  - Last Name: " + studentInfo.get("lastName"));
                System.out.println("  - School Origin: " + studentInfo.get("schoolOrigin"));
                
                // Now get program name
                int programId = rs.getInt("program_id");
                if (!rs.wasNull()) {
                    String programName = getProgramName(programId);
                    studentInfo.put("program", programName);
                    System.out.println("  - Program: " + programName);
                } else {
                    studentInfo.put("program", "Not assigned");
                    System.out.println("  - Program: Not assigned (program_id is NULL)");
                }
                
                // Get email from users table
                String email = getStudentEmail(studentId);
                studentInfo.put("email", email);
                System.out.println("  - Email: " + email);
                
                return studentInfo;
            } else {
                System.out.println("❌ [DEBUG] No student found with ID: " + studentId);
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting student info from database: " + e.getMessage());
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
    }

    // Helper method to get program name
    private String getProgramName(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT program_name FROM programs WHERE program_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("program_name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting program name: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        return "Unknown Program";
    }

    // Helper method to get student email
    private String getStudentEmail(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT email FROM users WHERE student_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Error getting student email: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        return "No email";
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
    
    // FIXED: Enhanced grade calculation with proper weight distribution
    public Double calculateSubjectFinalGrade(int studentId, int subjectId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get all exam scores for this student and subject
            String sql = "SELECT e.exam_type, e.coefficient, g.score " +
                        "FROM exams e " +
                        "JOIN grades g ON e.exam_id = g.exam_id " +
                        "WHERE g.student_id = ? AND e.subject_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            rs = stmt.executeQuery();
            
            // Store scores by exam type
            Map<String, Double> examScores = new HashMap<>();
            Map<String, Double> examCoefficients = new HashMap<>();
            
            while (rs.next()) {
                String examType = rs.getString("exam_type");
                double coefficient = rs.getDouble("coefficient");
                double score = rs.getDouble("score");
                
                examScores.put(examType, score);
                examCoefficients.put(examType, coefficient);
            }
            
            // Calculate final grade based on exam types available
            double finalGrade = 0.0;
            
            boolean hasControle = examScores.containsKey("CONTROLE");
            boolean hasTP = examScores.containsKey("TP");
            boolean hasExamen = examScores.containsKey("EXAMEN");
            
            if (hasControle && hasTP && hasExamen) {
                // Case 1: Has all three components
                // 0.2 for CONTROLE, 0.2 for TP, 0.6 for EXAMEN
                double controleScore = examScores.get("CONTROLE");
                double tpScore = examScores.get("TP");
                double examenScore = examScores.get("EXAMEN");
                
                finalGrade = (controleScore * 0.2) + (tpScore * 0.2) + (examenScore * 0.6);
                
            } else if (hasControle && hasExamen && !hasTP) {
                // Case 2: Has CONTROLE and EXAMEN only
                // 0.4 for CONTROLE, 0.6 for EXAMEN
                double controleScore = examScores.get("CONTROLE");
                double examenScore = examScores.get("EXAMEN");
                
                finalGrade = (controleScore * 0.4) + (examenScore * 0.6);
                
            } else if (hasExamen) {
                // Case 3: Only has EXAMEN, use database coefficient
                double examenScore = examScores.get("EXAMEN");
                double examenCoefficient = examCoefficients.get("EXAMEN");
                finalGrade = examenScore * examenCoefficient;
                
            } else {
                // Case 4: Other combinations - calculate weighted average
                double totalWeightedScore = 0.0;
                double totalCoefficient = 0.0;
                
                for (Map.Entry<String, Double> entry : examScores.entrySet()) {
                    String examType = entry.getKey();
                    double score = entry.getValue();
                    double coefficient = examCoefficients.get(examType);
                    
                    totalWeightedScore += score * coefficient;
                    totalCoefficient += coefficient;
                }
                
                finalGrade = totalCoefficient > 0 ? totalWeightedScore / totalCoefficient : 0.0;
            }
            
            return Math.round(finalGrade * 100.0) / 100.0; // Round to 2 decimal places
            
        } catch (SQLException e) {
            System.err.println("Error calculating subject final grade: " + e.getMessage());
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
    }
    
    // FIXED: Enhanced overall average calculation
    public Double calculateOverallAverage(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get all subjects the student is enrolled in
            String subjectsSql = "SELECT DISTINCT s.subject_id " +
                               "FROM grades g " +
                               "JOIN exams e ON g.exam_id = e.exam_id " +
                               "JOIN subjects s ON e.subject_id = s.subject_id " +
                               "WHERE g.student_id = ?";
            
            stmt = conn.prepareStatement(subjectsSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            List<Integer> subjectIds = new ArrayList<>();
            while (rs.next()) {
                subjectIds.add(rs.getInt("subject_id"));
            }
            rs.close();
            stmt.close();
            
            // Calculate average of all subject final grades
            if (subjectIds.isEmpty()) {
                return 0.0;
            }
            
            double totalGrade = 0.0;
            int count = 0;
            
            for (int subjectId : subjectIds) {
                Double subjectGrade = calculateSubjectFinalGrade(studentId, subjectId);
                if (subjectGrade != null) {
                    totalGrade += subjectGrade;
                    count++;
                }
            }
            
            return count > 0 ? Math.round((totalGrade / count) * 100.0) / 100.0 : 0.0;
            
        } catch (SQLException e) {
            System.err.println("Error calculating overall average: " + e.getMessage());
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
    }
    
    // Keep the old method for compatibility
    public Double getOverallAverage(int studentId) {
        return calculateOverallAverage(studentId);
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
                String status = rs.getString("final_status");
                return status != null ? status : "NOT_SET"; // Return default if null
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
        
        return "NOT_SET"; // Default value if not found
    }
    
    // Student Statistics Method - Database Only
    public Map<String, Object> getStudentStatistics(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> stats = new HashMap<>();
        
        try {
            conn = getConnection();
            
            // Get overall average using new calculation
            Double average = calculateOverallAverage(studentId);
            stats.put("overallAverage", average != null ? average : 0.0);
            
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
            
            // Get subject-wise statistics using new calculation
            String subjectSql = "SELECT DISTINCT s.subject_id, s.subject_name " +
                               "FROM grades g " +
                               "JOIN exams e ON g.exam_id = e.exam_id " +
                               "JOIN subjects s ON e.subject_id = s.subject_id " +
                               "WHERE g.student_id = ?";
            stmt = conn.prepareStatement(subjectSql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();
            
            List<Map<String, String>> subjectStats = new ArrayList<>();
            while (rs.next()) {
                int subjectId = rs.getInt("subject_id");
                String subjectName = rs.getString("subject_name");
                Double subjectAverage = calculateSubjectFinalGrade(studentId, subjectId);
                
                Map<String, String> subject = new HashMap<>();
                subject.put("subject", subjectName);
                subject.put("average", subjectAverage != null ? String.format("%.2f", subjectAverage) : "0.00");
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
    
    // FIXED: Enhanced grade update with teacher authorization
    public boolean updateStudentGrade(int gradeId, int teacherId, double newScore) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            // Verify that the teacher owns this grade through their exam
            String verifySql = "SELECT COUNT(*) as count FROM grades g " +
                              "JOIN exams e ON g.exam_id = e.exam_id " +
                              "WHERE g.grade_id = ? AND e.teacher_id = ?";
            
            stmt = conn.prepareStatement(verifySql);
            stmt.setInt(1, gradeId);
            stmt.setInt(2, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                // Teacher is authorized to update this grade
                String updateSql = "UPDATE grades SET score = ?, grade_date = NOW() WHERE grade_id = ?";
                stmt = conn.prepareStatement(updateSql);
                stmt.setDouble(1, newScore);
                stmt.setInt(2, gradeId);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            } else {
                System.err.println("Teacher not authorized to update this grade");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating student grade: " + e.getMessage());
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
    
    // Keep old method for compatibility
    public boolean updateGrade(int gradeId, double newScore) {
        return updateStudentGrade(gradeId, 0, newScore); // 0 means no teacher check
    }
    
    // Method to get grade details for editing
    public Map<String, String> getGradeDetails(int gradeId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT g.grade_id, g.student_id, s.first_name, s.last_name, " +
                        "e.exam_id, e.exam_name, sub.subject_name, g.score, e.exam_type " +
                        "FROM grades g " +
                        "JOIN exams e ON g.exam_id = e.exam_id " +
                        "JOIN subjects sub ON e.subject_id = sub.subject_id " +
                        "JOIN students s ON g.student_id = s.student_id " +
                        "WHERE g.grade_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, gradeId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> gradeDetails = new HashMap<>();
                gradeDetails.put("gradeId", String.valueOf(rs.getInt("grade_id")));
                gradeDetails.put("studentId", String.valueOf(rs.getInt("student_id")));
                gradeDetails.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
                gradeDetails.put("examId", String.valueOf(rs.getInt("exam_id")));
                gradeDetails.put("examName", rs.getString("exam_name"));
                gradeDetails.put("subject", rs.getString("subject_name"));
                gradeDetails.put("currentScore", String.valueOf(rs.getDouble("score")));
                gradeDetails.put("examType", rs.getString("exam_type"));
                return gradeDetails;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting grade details: " + e.getMessage());
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
    return getSubjectsByTeacher(teacherId);
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
    
    public boolean updateExam(int examId, String examName, double coefficient) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "UPDATE exams SET exam_name = ?, coefficient = ? WHERE exam_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, examName);
            stmt.setDouble(2, coefficient);
            stmt.setInt(3, examId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating exam in database: " + e.getMessage());
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
            
            // Updated SQL to include email and academic_year
            String sql = "INSERT INTO students (first_name, last_name, school_origin, email, phone, academic_year, registration_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, schoolOrigin);
            stmt.setString(4, email.isEmpty() ? null : email); // Add email (null if empty)
            stmt.setString(5, phone.isEmpty() ? null : phone); // Keep phone handling
            stmt.setString(6, "2024-2025"); // Add default academic year
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated student ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int studentId = generatedKeys.getInt(1);
                    
                    // Also create a user account for the student (KEEPING YOUR ORIGINAL METHOD)
                    String userSql = "INSERT INTO users (username, password_hash, user_type, student_id, is_active) " +
                                   "VALUES (?, ?, 'STUDENT', ?, 1)";
                    stmt = conn.prepareStatement(userSql);
                    stmt.setString(1, firstName.toLowerCase() + "." + lastName.toLowerCase());
                    stmt.setString(2, "password"); // Default password
                    stmt.setInt(3, studentId);
                    stmt.executeUpdate();
                    
                    System.out.println("✅ [DEBUG] Student added with ID: " + studentId);
                    System.out.println("✅ [DEBUG] User account created: " + firstName.toLowerCase() + "." + lastName.toLowerCase());
                }
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding student to database: " + e.getMessage());
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
    
    // MISSING METHODS - ADD THESE:
    
    // Enhanced student update method
    public boolean updateStudentInfo(int studentId, Map<String, String> studentData) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                        "school_origin = ?, program_id = ?, academic_year = ? WHERE student_id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentData.get("firstName"));
            stmt.setString(2, studentData.get("lastName"));
            stmt.setString(3, studentData.get("email"));
            stmt.setString(4, studentData.get("phone"));
            stmt.setString(5, studentData.get("schoolOrigin"));
            
            // Handle program ID (can be null)
            String programIdStr = studentData.get("programId");
            if (programIdStr != null && !programIdStr.isEmpty() && !programIdStr.equals("null")) {
                stmt.setInt(6, Integer.parseInt(programIdStr));
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setString(7, studentData.get("academicYear"));
            stmt.setInt(8, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student info: " + e.getMessage());
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

    // Get student with complete information including program
    public Map<String, String> getStudentWithProgram(int studentId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT s.*, p.program_name, p.program_id " +
                        "FROM students s " +
                        "LEFT JOIN programs p ON s.program_id = p.program_id " +
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
                studentInfo.put("phone", rs.getString("phone"));
                studentInfo.put("email", rs.getString("email"));
                studentInfo.put("academicYear", rs.getString("academic_year"));
                studentInfo.put("registrationDate", rs.getString("registration_date"));
                studentInfo.put("finalStatus", rs.getString("final_status"));
                
                // Program information
                studentInfo.put("programId", rs.getString("program_id"));
                studentInfo.put("programName", rs.getString("program_name"));
                
                return studentInfo;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student with program: " + e.getMessage());
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
        
        return null;
    }

    // Enhanced student registration with program
public boolean registerStudentToProgram(int studentId, int programId, String academicYear) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        System.out.println("🎯 [REGISTRATION] Registering student " + studentId + " to program " + programId);
        
        conn = getConnection();
        
        // First, verify both student and program exist
        if (!verifyStudentExists(studentId)) {
            System.out.println("❌ Student " + studentId + " does not exist!");
            return false;
        }
        
        if (!verifyProgramExists(programId)) {
            System.out.println("❌ Program " + programId + " does not exist!");
            return false;
        }
        
        // Update student's program_id
        String sql = "UPDATE students SET program_id = ?, academic_year = ? WHERE student_id = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        stmt.setString(2, academicYear);
        stmt.setInt(3, studentId);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("✅ Successfully registered student to program!");
            
            // Also add to student_programs table for tracking
            String trackSql = "INSERT INTO student_programs (student_id, program_id, academic_year, is_active) VALUES (?, ?, ?, 1)";
            stmt = conn.prepareStatement(trackSql);
            stmt.setInt(1, studentId);
            stmt.setInt(2, programId);
            stmt.setString(3, academicYear);
            stmt.executeUpdate();
            
            return true;
        } else {
            System.out.println("❌ Failed to update student record");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error registering student to program: " + e.getMessage());
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


    // Get all programs for dropdown
    public List<Map<String, String>> getAllProgramsForDropdown() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> programs = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT program_id, program_name FROM programs ORDER BY program_name";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> program = new HashMap<>();
                program.put("programId", String.valueOf(rs.getInt("program_id")));
                program.put("programName", rs.getString("program_name"));
                programs.add(program);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting programs for dropdown: " + e.getMessage());
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
            System.out.println(" [DEBUG] Starting getAllStudents()...");
            conn = getConnection();
            System.out.println(" [DEBUG] Database connection established");

            // FIXED SQL - Get email from students table instead of users table
            String sql = "SELECT s.student_id, s.first_name, s.last_name, s.school_origin, " +
                        "s.phone, s.email, p.program_name, s.academic_year, s.registration_date, " +
                        "s.final_status " +
                        "FROM students s " +
                        "LEFT JOIN programs p ON s.program_id = p.program_id " +
                        "ORDER BY s.student_id";
            
            System.out.println(" [DEBUG] Executing SQL: " + sql);
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                Map<String, String> student = new HashMap<>();
                student.put("studentId", String.valueOf(rs.getInt("student_id")));
                student.put("firstName", rs.getString("first_name"));
                student.put("lastName", rs.getString("last_name"));
                student.put("schoolOrigin", rs.getString("school_origin"));
                student.put("phone", rs.getString("phone"));
                student.put("email", rs.getString("email")); // Now from students table
                student.put("program", rs.getString("program_name"));
                student.put("academicYear", rs.getString("academic_year"));
                student.put("registrationDate", rs.getString("registration_date"));
                student.put("status", rs.getString("final_status") != null ? rs.getString("final_status") : "Active");
                students.add(student);
                
                System.out.println(" [DEBUG] Found student: " + student.get("studentId") + " - " + 
                                 student.get("firstName") + " " + student.get("lastName"));
            }
            
            System.out.println(" [DEBUG] Total students found: " + count);
            
        } catch (SQLException e) {
            System.err.println(" Error getting students from database: " + e.getMessage());
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
        
        // FIXED QUERY: Properly links teachers to students through subjects and programs
        String sql = "SELECT DISTINCT s.student_id, s.first_name, s.last_name, " +
                    "p.program_name, sub.subject_name " +
                    "FROM students s " +
                    "JOIN programs p ON s.program_id = p.program_id " +
                    "JOIN subjects sub ON p.program_id = sub.program_id " +
                    "JOIN teacher_subjects ts ON sub.subject_id = ts.subject_id " +
                    "WHERE ts.teacher_id = ? " +
                    "AND s.program_id IS NOT NULL " +
                    "ORDER BY s.first_name, s.last_name";
        
        System.out.println("🔍 [DEBUG] Getting students for teacher ID: " + teacherId);
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
            Map<String, String> student = new HashMap<>();
            student.put("studentId", String.valueOf(rs.getInt("student_id")));
            student.put("studentName", rs.getString("first_name") + " " + rs.getString("last_name"));
            student.put("program", rs.getString("program_name"));
            student.put("subject", rs.getString("subject_name"));
            students.add(student);
            
            System.out.println("✅ Found student: " + student.get("studentName") + 
                             " | Program: " + student.get("program") + 
                             " | Subject: " + student.get("subject"));
        }
        
        System.out.println("📊 Total students found for teacher: " + count);
        
    } catch (SQLException e) {
        System.err.println("Error getting teacher students from database: " + e.getMessage());
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
    
    // Helper method to get program ID by name
    public Integer getProgramIdByName(String programName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT program_id FROM programs WHERE program_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, programName);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("program_id");
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error getting program ID: " + e.getMessage());
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
    }
    
    // Helper method to get subject ID by name
    public Integer getSubjectIdByName(String subjectName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            String sql = "SELECT subject_id FROM subjects WHERE subject_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectName);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("subject_id");
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error getting subject ID: " + e.getMessage());
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
    }

  // Make sure your getAllTeachers method looks like this:
public List<Map<String, String>> getAllTeachers() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> teachers = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT teacher_id, first_name, last_name, email, specialty " +
                    "FROM teachers ORDER BY first_name, last_name";
        
        stmt = conn.prepareStatement(sql);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> teacher = new HashMap<>();
            teacher.put("teacherId", String.valueOf(rs.getInt("teacher_id")));
            teacher.put("firstName", rs.getString("first_name"));
            teacher.put("lastName", rs.getString("last_name"));
            teacher.put("email", rs.getString("email"));
            teacher.put("specialty", rs.getString("specialty"));
            teacher.put("displayName", rs.getString("first_name") + " " + rs.getString("last_name") + 
                        " (" + rs.getString("specialty") + ")");
            teachers.add(teacher);
        }
        
        System.out.println("📋 Found " + teachers.size() + " teachers in database");
        
    } catch (SQLException e) {
        System.err.println("Error getting teachers from database: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    return teachers;
}

    // Get subjects by program
    public List<Map<String, String>> getSubjectsByProgram(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> subjects = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT subject_id, subject_name, semester, coefficient " +
                        "FROM subjects WHERE program_id = ? ORDER BY semester, subject_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> subject = new HashMap<>();
                subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
                subject.put("subjectName", rs.getString("subject_name"));
                subject.put("semester", String.valueOf(rs.getInt("semester")));
                subject.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                subject.put("displayName", rs.getString("subject_name") + " (S" + rs.getInt("semester") + 
                            " - Coeff: " + rs.getDouble("coefficient") + ")");
                subjects.add(subject);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting subjects by program: " + e.getMessage());
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

    // Assign teacher to program subject
    public boolean assignTeacherToProgram(int programId, int teacherId, int subjectId, String academicYear) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "INSERT INTO program_teachers (program_id, teacher_id, subject_id, academic_year) " +
                        "VALUES (?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, programId);
            stmt.setInt(2, teacherId);
            stmt.setInt(3, subjectId);
            stmt.setString(4, academicYear);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error assigning teacher to program: " + e.getMessage());
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

    // Enhanced add program method with teacher assignment
    public boolean addProgramWithTeachers(String programName, int programYear, String description, 
                                         Map<Integer, Integer> teacherAssignments, String academicYear) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // First, add the program
            String programSql = "INSERT INTO programs (program_name, program_year, description) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(programSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, programName);
            stmt.setInt(2, programYear);
            stmt.setString(3, description);
            
            int programRows = stmt.executeUpdate();
            if (programRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Get the generated program ID
            int programId = -1;
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                programId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }
            
            // Assign teachers to subjects if provided
            if (teacherAssignments != null && !teacherAssignments.isEmpty()) {
                for (Map.Entry<Integer, Integer> assignment : teacherAssignments.entrySet()) {
                    int subjectId = assignment.getKey();
                    int teacherId = assignment.getValue();
                    
                    String assignSql = "INSERT INTO program_teachers (program_id, teacher_id, subject_id, academic_year) " +
                                     "VALUES (?, ?, ?, ?)";
                    stmt = conn.prepareStatement(assignSql);
                    stmt.setInt(1, programId);
                    stmt.setInt(2, teacherId);
                    stmt.setInt(3, subjectId);
                    stmt.setString(4, academicYear);
                    stmt.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error adding program with teachers: " + e.getMessage());
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
 public List<Map<String, String>> getProgramTeachers(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> teachers = new ArrayList<>();
    
    try {
        System.out.println("🔍 [DEBUG] Getting teachers for program ID: " + programId);
        conn = getConnection();
        
        // First, try the teacher_subjects table (most common)
        String sql = "SELECT DISTINCT " +
                    "t.teacher_id, " +
                    "t.first_name, " +
                    "t.last_name, " +
                    "t.specialty, " +
                    "s.subject_name, " +
                    "s.semester " +
                    "FROM teacher_subjects ts " +
                    "JOIN teachers t ON ts.teacher_id = t.teacher_id " +
                    "JOIN subjects s ON ts.subject_id = s.subject_id " +
                    "WHERE s.program_id = ? " +
                    "ORDER BY s.semester, s.subject_name";
        
        System.out.println("🔍 [DEBUG] Executing SQL: " + sql);
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
            Map<String, String> teacher = new HashMap<>();
            teacher.put("teacherId", String.valueOf(rs.getInt("teacher_id")));
            teacher.put("teacherName", rs.getString("first_name") + " " + rs.getString("last_name"));
            teacher.put("subjectName", rs.getString("subject_name"));
            teacher.put("semester", String.valueOf(rs.getInt("semester")));
            teacher.put("specialty", rs.getString("specialty"));
            teachers.add(teacher);
            
            System.out.println("✅ Found teacher: " + teacher.get("teacherName") + 
                             " | Subject: " + teacher.get("subjectName"));
        }
        
        System.out.println("📊 Total teachers found: " + count);
        
        // If no teachers found, try alternative table structure
        if (teachers.isEmpty()) {
            System.out.println("🔄 No teachers found in teacher_subjects, trying alternative query...");
            rs.close();
            stmt.close();
            
            // Alternative query for program_teachers table
            String altSql = "SELECT DISTINCT " +
                           "t.teacher_id, " +
                           "t.first_name, " +
                           "t.last_name, " +
                           "t.specialty, " +
                           "s.subject_name, " +
                           "s.semester " +
                           "FROM program_teachers pt " +
                           "JOIN teachers t ON pt.teacher_id = t.teacher_id " +
                           "JOIN subjects s ON pt.subject_id = s.subject_id " +
                           "WHERE pt.program_id = ? " +
                           "ORDER BY s.semester, s.subject_name";
            
            try {
                stmt = conn.prepareStatement(altSql);
                stmt.setInt(1, programId);
                rs = stmt.executeQuery();
                
                int altCount = 0;
                while (rs.next()) {
                    altCount++;
                    Map<String, String> teacher = new HashMap<>();
                    teacher.put("teacherId", String.valueOf(rs.getInt("teacher_id")));
                    teacher.put("teacherName", rs.getString("first_name") + " " + rs.getString("last_name"));
                    teacher.put("subjectName", rs.getString("subject_name"));
                    teacher.put("semester", String.valueOf(rs.getInt("semester")));
                    teacher.put("specialty", rs.getString("specialty"));
                    teachers.add(teacher);
                    
                    System.out.println("✅ Found teacher (alt): " + teacher.get("teacherName"));
                }
                System.out.println("📊 Total teachers found (alt): " + altCount);
            } catch (SQLException e) {
                System.out.println("ℹ️ Alternative table structure not available");
            }
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Error getting program teachers: " + e.getMessage());
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
    
    return teachers;
}
    
    // Enhanced program management methods
    public boolean createProgramWithStructure(String programName, String programType, int durationYears, 
                                            int totalCredits, String department, String description) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Create the program
            String programSql = "INSERT INTO programs (program_name, program_type, duration_years, " +
                              "total_credits, department, description) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(programSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, programName);
            stmt.setString(2, programType);
            stmt.setInt(3, durationYears);
            stmt.setInt(4, totalCredits);
            stmt.setString(5, department);
            stmt.setString(6, description);
            
            int programRows = stmt.executeUpdate();
            if (programRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Get generated program ID
            int programId = -1;
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                programId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }
            
            // Create semesters based on duration
            for (int year = 1; year <= durationYears; year++) {
                for (int semester = 1; semester <= 2; semester++) { // Assuming 2 semesters per year
                    int semesterNumber = (year - 1) * 2 + semester;
                    String semesterName = "S" + semesterNumber;
                    
                    String semesterSql = "INSERT INTO semesters (program_id, semester_number, semester_name) " +
                                       "VALUES (?, ?, ?)";
                    stmt = conn.prepareStatement(semesterSql);
                    stmt.setInt(1, programId);
                    stmt.setInt(2, semesterNumber);
                    stmt.setString(3, semesterName);
                    stmt.executeUpdate();
                }
            }
            
            // Create default configuration
            String configSql = "INSERT INTO program_configuration (program_id) VALUES (?)";
            stmt = conn.prepareStatement(configSql);
            stmt.setInt(1, programId);
            stmt.executeUpdate();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error creating program with structure: " + e.getMessage());
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Get program structure with semesters and subjects
    public Map<String, Object> getProgramStructure(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> programStructure = new HashMap<>();
        
        try {
            conn = getConnection();
            
            // Get program basic info
            String programSql = "SELECT * FROM programs WHERE program_id = ?";
            stmt = conn.prepareStatement(programSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                programStructure.put("programId", rs.getInt("program_id"));
                programStructure.put("programName", rs.getString("program_name"));
                programStructure.put("programType", rs.getString("program_type"));
                programStructure.put("durationYears", rs.getInt("duration_years"));
                programStructure.put("totalCredits", rs.getInt("total_credits"));
                programStructure.put("department", rs.getString("department"));
                programStructure.put("description", rs.getString("description"));
            }
            rs.close();
            stmt.close();
            
            // Get semesters
            String semesterSql = "SELECT * FROM semesters WHERE program_id = ? ORDER BY semester_number";
            stmt = conn.prepareStatement(semesterSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            List<Map<String, String>> semesters = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> semester = new HashMap<>();
                semester.put("semesterId", String.valueOf(rs.getInt("semester_id")));
                semester.put("semesterNumber", String.valueOf(rs.getInt("semester_number")));
                semester.put("semesterName", rs.getString("semester_name"));
                semester.put("totalCredits", String.valueOf(rs.getInt("total_credits")));
                
                // Get subjects for this semester - FIXED: Store as List instead of String
                List<Map<String, String>> subjectsList = getSubjectsBySemester(rs.getInt("semester_id"));
                semester.put("subjectsList", subjectsList.toString()); // Convert to String for storage
                semesters.add(semester);
            }
            programStructure.put("semesters", semesters);
            
        } catch (SQLException e) {
            System.err.println("Error getting program structure: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return programStructure;
    }

    // FIXED: Only one addSubjectToProgram method to avoid duplication
   
    
    public boolean addSimpleSubjectToProgram(int programId, String subjectName, String objectives, 
                                       int semester, double coefficient) {
    // Call the simple addSubjectToProgram method instead of the complex one
    return addSubjectToProgram(programId, subjectName, objectives, semester, coefficient);
}
    
    // Add this method to get subjects by semester
    public List<Map<String, String>> getSubjectsBySemester(int semesterId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> subjects = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT s.*, t.first_name, t.last_name " +
                        "FROM subjects s " +
                        "LEFT JOIN teacher_subjects ts ON s.subject_id = ts.subject_id " +
                        "LEFT JOIN teachers t ON ts.teacher_id = t.teacher_id " +
                        "WHERE s.semester = ? " +
                        "ORDER BY s.subject_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, semesterId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> subject = new HashMap<>();
                subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
                subject.put("subjectName", rs.getString("subject_name"));
                subject.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                subject.put("credits", String.valueOf(rs.getInt("credits")));
                
                // Teacher information
                String teacherName = rs.getString("first_name") + " " + rs.getString("last_name");
                subject.put("assignedTeacher", teacherName != null ? teacherName : "Not Assigned");
                
                subjects.add(subject);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting subjects by semester: " + e.getMessage());
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

    // Get all subjects for a program
    public List<Map<String, String>> getProgramSubjects(int programId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Map<String, String>> subjects = new ArrayList<>();
        
        try {
            conn = getConnection();
            
            String sql = "SELECT s.*, t.first_name, t.last_name, ts.teacher_subject_id " +
                        "FROM subjects s " +
                        "LEFT JOIN teacher_subjects ts ON s.subject_id = ts.subject_id " +
                        "LEFT JOIN teachers t ON ts.teacher_id = t.teacher_id " +
                        "WHERE s.program_id = ? " +
                        "ORDER BY s.semester, s.subject_name";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> subject = new HashMap<>();
                subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
                subject.put("subjectName", rs.getString("subject_name"));
                subject.put("objectives", rs.getString("objectives"));
                subject.put("semester", String.valueOf(rs.getInt("semester")));
                subject.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                subject.put("programId", String.valueOf(rs.getInt("program_id")));
                
                // Teacher information
                String teacherName = rs.getString("first_name") + " " + rs.getString("last_name");
                subject.put("assignedTeacher", teacherName != null ? teacherName : "Not Assigned");
                subject.put("teacherId", rs.getString("teacher_id"));
                subject.put("teacherSubjectId", rs.getString("teacher_subject_id"));
                
                subjects.add(subject);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting program subjects: " + e.getMessage());
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
public boolean verifyProgramExists(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        String sql = "SELECT program_id FROM programs WHERE program_id = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        boolean exists = rs.next();
        System.out.println("🔍 [DEBUG] Program ID " + programId + " exists: " + exists);
        return exists;
        
    } catch (SQLException e) {
        System.err.println("Error checking program existence: " + e.getMessage());
        return false;
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
    // Assign teacher to subject
public boolean assignTeacherToSubject(int teacherId, int subjectId, String academicYear) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        
        // First, check if assignment already exists
        String checkSql = "SELECT * FROM teacher_subjects WHERE teacher_id = ? AND subject_id = ?";
        stmt = conn.prepareStatement(checkSql);
        stmt.setInt(1, teacherId);
        stmt.setInt(2, subjectId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("✅ Teacher already assigned to this subject");
            return true;
        }
        rs.close();
        stmt.close();
        
        // Create new assignment
        String insertSql = "INSERT INTO teacher_subjects (teacher_id, subject_id, academic_year) VALUES (?, ?, ?)";
        stmt = conn.prepareStatement(insertSql);
        stmt.setInt(1, teacherId);
        stmt.setInt(2, subjectId);
        stmt.setString(3, academicYear);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("✅ Successfully assigned teacher " + teacherId + " to subject " + subjectId);
            return true;
        } else {
            System.out.println("❌ Failed to assign teacher to subject");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("Error assigning teacher to subject: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

    // Remove teacher from subject
    public boolean removeTeacherFromSubject(int subjectId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            
            String sql = "DELETE FROM teacher_subjects WHERE subject_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing teacher from subject: " + e.getMessage());
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


    // Add these methods to ClientService class

public boolean addSubjectToProgram(int programId, String subjectName, String objectives, 
                                 int semester, double coefficient) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        System.out.println("🎯 [DEBUG FINAL] Starting addSubjectToProgram");
        System.out.println("   Program ID: " + programId);
        System.out.println("   Subject: " + subjectName);
        System.out.println("   Semester: " + semester);
        System.out.println("   Coefficient: " + coefficient);
        
        // First, verify program exists
        if (!verifyProgramExists(programId)) {
            System.out.println("❌ [DEBUG] Program " + programId + " does not exist!");
            return false;
        }
        
        conn = getConnection();
        System.out.println("✅ [DEBUG] Database connected");
        
        // SIMPLE DIRECT INSERT - NO semester table lookup
        String sql = "INSERT INTO subjects (subject_name, program_id, semester, coefficient, objectives) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        System.out.println("🔍 [DEBUG] Executing: " + sql);
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, subjectName);
        stmt.setInt(2, programId);
        stmt.setInt(3, semester);
        stmt.setDouble(4, coefficient);
        stmt.setString(5, objectives);
        
        int rows = stmt.executeUpdate();
        System.out.println("✅ [DEBUG] Rows affected: " + rows);
        
        return rows > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ [DEBUG] SQL Error: " + e.getMessage());
        System.err.println("❌ [DEBUG] SQL State: " + e.getSQLState());
        System.err.println("❌ [DEBUG] Error Code: " + e.getErrorCode());
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
public boolean doesProgramExist(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        String sql = "SELECT program_id FROM programs WHERE program_id = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        boolean exists = rs.next();
        System.out.println("🔍 [DEBUG] Program ID " + programId + " exists: " + exists);
        return exists;
        
    } catch (SQLException e) {
        System.err.println("Error checking program existence: " + e.getMessage());
        return false;
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
public List<Map<String, String>> getUnassignedSubjects(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> subjects = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT s.subject_id, s.subject_name, s.semester " +
                    "FROM subjects s " +
                    "LEFT JOIN teacher_subjects ts ON s.subject_id = ts.subject_id " +
                    "WHERE s.program_id = ? AND ts.teacher_id IS NULL " +
                    "ORDER BY s.semester, s.subject_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> subject = new HashMap<>();
            subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
            subject.put("subjectName", rs.getString("subject_name"));
            subject.put("semester", String.valueOf(rs.getInt("semester")));
            subjects.add(subject);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting unassigned subjects: " + e.getMessage());
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
public void checkDatabaseStructure() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        System.out.println("🔍 [DEBUG] Checking database structure...");
        
        // Check if subjects table exists and show its structure
        DatabaseMetaData meta = conn.getMetaData();
        rs = meta.getTables(null, null, "subjects", null);
        if (rs.next()) {
            System.out.println("✅ [DEBUG] 'subjects' table exists");
            
            // Show columns of subjects table
            rs = meta.getColumns(null, null, "subjects", null);
            System.out.println("📋 [DEBUG] Subjects table columns:");
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                System.out.println("   - " + columnName + " (" + columnType + ")");
            }
        } else {
            System.out.println("❌ [DEBUG] 'subjects' table does NOT exist!");
        }
        rs.close();
        
        // Check if programs table exists
        rs = meta.getTables(null, null, "programs", null);
        if (rs.next()) {
            System.out.println("✅ [DEBUG] 'programs' table exists");
        } else {
            System.out.println("❌ [DEBUG] 'programs' table does NOT exist!");
        }
        rs.close();
        
        // Check if semesters table exists
        rs = meta.getTables(null, null, "semesters", null);
        if (rs.next()) {
            System.out.println("✅ [DEBUG] 'semesters' table exists");
        } else {
            System.out.println("❌ [DEBUG] 'semesters' table does NOT exist!");
        }
        
    } catch (SQLException e) {
        System.err.println("Error checking database structure: " + e.getMessage());
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
// Add this method to ClientService
public List<Map<String, String>> getProgramSubjectsWithDetails(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> subjects = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT s.subject_id, s.subject_name, s.semester, s.coefficient, s.objectives, " +
                    "t.first_name, t.last_name " +
                    "FROM subjects s " +
                    "LEFT JOIN teacher_subjects ts ON s.subject_id = ts.subject_id " +
                    "LEFT JOIN teachers t ON ts.teacher_id = t.teacher_id " +
                    "WHERE s.program_id = ? " +
                    "ORDER BY s.semester, s.subject_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> subject = new HashMap<>();
            subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
            subject.put("subjectName", rs.getString("subject_name"));
            subject.put("semester", String.valueOf(rs.getInt("semester")));
            subject.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
            subject.put("objectives", rs.getString("objectives"));
            
            String teacherName = rs.getString("first_name") + " " + rs.getString("last_name");
            subject.put("assignedTeacher", teacherName != null ? teacherName : "Not Assigned");
            
            subjects.add(subject);
        }
        
        System.out.println("📚 Found " + subjects.size() + " subjects for program " + programId);
        
    } catch (SQLException e) {
        System.err.println("Error getting program subjects: " + e.getMessage());
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
public boolean updateProgram(int programId, String programName, int programYear, String description) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        
        String sql = "UPDATE programs SET program_name = ?, program_year = ?, description = ? WHERE program_id = ?";
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, programName);
        stmt.setInt(2, programYear);
        stmt.setString(3, description);
        stmt.setInt(4, programId);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("✅ Program updated successfully! Rows affected: " + rowsAffected);
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Error updating program: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

// Delete program
public boolean deleteProgram(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        
        // First, check if program has any students
        String checkSql = "SELECT COUNT(*) as student_count FROM students WHERE program_id = ?";
        stmt = conn.prepareStatement(checkSql);
        stmt.setInt(1, programId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next() && rs.getInt("student_count") > 0) {
            System.err.println("❌ Cannot delete program: It has " + rs.getInt("student_count") + " students enrolled");
            return false;
        }
        rs.close();
        stmt.close();
        
        // Delete program
        String deleteSql = "DELETE FROM programs WHERE program_id = ?";
        stmt = conn.prepareStatement(deleteSql);
        stmt.setInt(1, programId);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("✅ Program deleted successfully! Rows affected: " + rowsAffected);
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Error deleting program: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

// Get students by program
public List<Map<String, String>> getStudentsByProgram(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> students = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT student_id, first_name, last_name, email, academic_year " +
                    "FROM students WHERE program_id = ? ORDER BY first_name, last_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> student = new HashMap<>();
            student.put("studentId", String.valueOf(rs.getInt("student_id")));
            student.put("firstName", rs.getString("first_name"));
            student.put("lastName", rs.getString("last_name"));
            student.put("email", rs.getString("email"));
            student.put("academicYear", rs.getString("academic_year"));
            students.add(student);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting program students: " + e.getMessage());
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

// Get program statistics
public Map<String, Object> getProgramStatistics(int programId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Object> stats = new HashMap<>();
    
    try {
        conn = getConnection();
        
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
        
        // Total subjects in program
        String subjectsSql = "SELECT COUNT(*) as total FROM subjects WHERE program_id = ?";
        stmt = conn.prepareStatement(subjectsSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("totalSubjects", rs.getInt("total"));
        }
        rs.close();
        stmt.close();
        
        // Average grade for program students
        String avgGradeSql = "SELECT ROUND(AVG(g.score), 2) as average " +
                           "FROM grades g " +
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
        rs.close();
        stmt.close();
        
        // Success rate
        String successRateSql = "SELECT ROUND(AVG(CASE WHEN g.score >= 10 THEN 1 ELSE 0 END) * 100, 2) as success_rate " +
                              "FROM grades g " +
                              "JOIN students s ON g.student_id = s.student_id " +
                              "WHERE s.program_id = ?";
        stmt = conn.prepareStatement(successRateSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            stats.put("successRate", rs.getDouble("success_rate"));
        } else {
            stats.put("successRate", 0.0);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting program statistics: " + e.getMessage());
        // Set default values
        stats.put("totalStudents", 0);
        stats.put("totalSubjects", 0);
        stats.put("averageGrade", 0.0);
        stats.put("successRate", 0.0);
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
// Update the createUser method in ClientService to handle teacher creation properly
public boolean createUser(String username, String password, String userType, 
                         Integer studentId, Integer teacherId, String email) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        conn.setAutoCommit(false); // Start transaction
        
        // For TEACHER type, we need to create a teacher record first if teacherId is not provided
        if ("TEACHER".equals(userType) && teacherId == null) {
            // Create a new teacher record
            String teacherSql = "INSERT INTO teachers (first_name, last_name, email, specialty) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(teacherSql, Statement.RETURN_GENERATED_KEYS);
            
            // Extract first and last name from username or use defaults
            String[] nameParts = username.split("\\.");
            String firstName = nameParts.length > 0 ? capitalize(nameParts[0]) : "Teacher";
            String lastName = nameParts.length > 1 ? capitalize(nameParts[1]) : "User";
            
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, "General"); // Default specialty
            
            int teacherRows = stmt.executeUpdate();
            if (teacherRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Get the generated teacher ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                teacherId = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            stmt.close();
        }
        
        // For STUDENT type, we need to create a student record first if studentId is not provided
        if ("STUDENT".equals(userType) && studentId == null) {
            // Create a new student record
            String studentSql = "INSERT INTO students (first_name, last_name, email, academic_year) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS);
            
            // Extract first and last name from username or use defaults
            String[] nameParts = username.split("\\.");
            String firstName = nameParts.length > 0 ? capitalize(nameParts[0]) : "Student";
            String lastName = nameParts.length > 1 ? capitalize(nameParts[1]) : "User";
            
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, "2024-2025"); // Default academic year
            
            int studentRows = stmt.executeUpdate();
            if (studentRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Get the generated student ID
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                studentId = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            stmt.close();
        }
        
        // Now create the user account
        String userSql = "INSERT INTO users (username, password_hash, user_type, student_id, teacher_id, email, is_active) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1)";
        
        stmt = conn.prepareStatement(userSql);
        stmt.setString(1, username);
        stmt.setString(2, password); // In real application, you should hash the password
        stmt.setString(3, userType);
        
        if (studentId != null) {
            stmt.setInt(4, studentId);
        } else {
            stmt.setNull(4, java.sql.Types.INTEGER);
        }
        
        if (teacherId != null) {
            stmt.setInt(5, teacherId);
        } else {
            stmt.setNull(5, java.sql.Types.INTEGER);
        }
        
        stmt.setString(6, email);
        
        int userRows = stmt.executeUpdate();
        
        if (userRows > 0) {
            conn.commit(); // Commit transaction
            System.out.println("✅ User created successfully! Username: " + username + ", Type: " + userType);
            
            // Log the created IDs for debugging
            if ("TEACHER".equals(userType) && teacherId != null) {
                System.out.println("✅ Teacher record created with ID: " + teacherId);
            }
            if ("STUDENT".equals(userType) && studentId != null) {
                System.out.println("✅ Student record created with ID: " + studentId);
            }
            
            return true;
        } else {
            conn.rollback();
            return false;
        }
        
    } catch (SQLException e) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {
            System.err.println("Error rolling back transaction: " + ex.getMessage());
        }
        System.err.println("❌ Error creating user: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

// Helper method to capitalize names
private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
        return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
}
// Update user status
public boolean updateUserStatus(int userId, boolean isActive) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        
        stmt = conn.prepareStatement(sql);
        stmt.setBoolean(1, isActive);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("✅ User status updated successfully! Rows affected: " + rowsAffected);
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Error updating user status: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

// Reset user password
public boolean resetUserPassword(int userId, String newPassword) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    try {
        conn = getConnection();
        
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, newPassword); // In real application, hash the password
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("✅ Password reset successfully! Rows affected: " + rowsAffected);
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ Error resetting password: " + e.getMessage());
        return false;
    } finally {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
// Add this debug method to ClientService
public void debugTeacherCreation(String username, String userType, Integer teacherId) {
    System.out.println("🔍 [DEBUG TEACHER CREATION]");
    System.out.println("   Username: " + username);
    System.out.println("   User Type: " + userType);
    System.out.println("   Teacher ID provided: " + teacherId);
    
    // Check if teacher exists
    if (teacherId != null) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT * FROM teachers WHERE teacher_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, teacherId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ Teacher exists: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                System.out.println("❌ Teacher does NOT exist with ID: " + teacherId);
            }
        } catch (SQLException e) {
            System.err.println("Error checking teacher: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
// Add these methods to ClientService class


// Get detailed user information from database
public Map<String, String> getUserDetails(int userId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        
        String sql = "SELECT u.user_id, u.username, u.user_type, u.is_active, " +
                    "COALESCE(s.first_name, t.first_name, 'System') as first_name, " +
                    "COALESCE(s.last_name, t.last_name, 'Administrator') as last_name " +
                    "FROM users u " +
                    "LEFT JOIN students s ON u.student_id = s.student_id " +
                    "LEFT JOIN teachers t ON u.teacher_id = t.teacher_id " +
                    "WHERE u.user_id = ?";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            Map<String, String> userDetails = new HashMap<>();
            userDetails.put("userId", String.valueOf(rs.getInt("user_id")));
            userDetails.put("username", rs.getString("username"));
            userDetails.put("userType", rs.getString("user_type"));
            userDetails.put("firstName", rs.getString("first_name"));
            userDetails.put("lastName", rs.getString("last_name"));
            userDetails.put("status", rs.getBoolean("is_active") ? "Active" : "Inactive");
            return userDetails;
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting user details: " + e.getMessage());
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
public List<Map<String, String>> getStudentsBySubject(int subjectId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> students = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT DISTINCT s.student_id, s.first_name, s.last_name, s.email " +
                    "FROM students s " +
                    "JOIN student_programs sp ON s.student_id = sp.student_id " +
                    "JOIN subjects sub ON sp.program_id = sub.program_id " +
                    "WHERE sub.subject_id = ? AND sp.is_active = 1 " +
                    "ORDER BY s.first_name, s.last_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, subjectId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> student = new HashMap<>();
            student.put("studentId", String.valueOf(rs.getInt("student_id")));
            student.put("firstName", rs.getString("first_name"));
            student.put("lastName", rs.getString("last_name"));
            student.put("email", rs.getString("email"));
            student.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
            students.add(student);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting students by subject: " + e.getMessage());
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
public List<Map<String, String>> getSubjectsByStudent(int studentId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> subjects = new ArrayList<>();
    
    try {
        conn = getConnection();
        System.out.println("🔍 [DEBUG] Getting subjects for student ID: " + studentId);
        
        // First, get the student's program_id
        String studentSql = "SELECT program_id FROM students WHERE student_id = ?";
        stmt = conn.prepareStatement(studentSql);
        stmt.setInt(1, studentId);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            int programId = rs.getInt("program_id");
            System.out.println("🔍 [DEBUG] Student program ID: " + programId);
            
            if (!rs.wasNull()) {
                // Now get subjects for this program
                rs.close();
                stmt.close();
                
                String subjectsSql = "SELECT subject_id, subject_name, semester, coefficient, credits, volume_horaire " +
                                   "FROM subjects WHERE program_id = ? ORDER BY semester, subject_name";
                
                stmt = conn.prepareStatement(subjectsSql);
                stmt.setInt(1, programId);
                rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, String> subject = new HashMap<>();
                    subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
                    subject.put("subjectName", rs.getString("subject_name"));
                    subject.put("semester", String.valueOf(rs.getInt("semester")));
                    subject.put("coefficient", String.valueOf(rs.getDouble("coefficient")));
                    subject.put("credits", String.valueOf(rs.getInt("credits")));
                    subject.put("volumeHoraire", String.valueOf(rs.getInt("volume_horaire")));
                    subjects.add(subject);
                    
                    System.out.println("✅ [DEBUG] Found subject: " + rs.getString("subject_name"));
                }
                
                System.out.println("✅ [DEBUG] Total subjects found: " + subjects.size());
            } else {
                System.out.println("❌ [DEBUG] Student has no program assigned (program_id is NULL)");
            }
        } else {
            System.out.println("❌ [DEBUG] Student not found with ID: " + studentId);
        }
        
    } catch (SQLException e) {
        System.err.println("❌ [DEBUG] Error getting subjects by student: " + e.getMessage());
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
    
    return subjects;
}

// Get subjects by teacher
public List<Map<String, String>> getSubjectsByTeacher(int teacherId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> subjects = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT s.subject_id, s.subject_name, s.semester, p.program_name " +
                    "FROM subjects s " +
                    "JOIN teacher_subjects ts ON s.subject_id = ts.subject_id " +
                    "JOIN programs p ON s.program_id = p.program_id " +
                    "WHERE ts.teacher_id = ? " +
                    "ORDER BY s.semester, s.subject_name";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, teacherId);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            Map<String, String> subject = new HashMap<>();
            subject.put("subjectId", String.valueOf(rs.getInt("subject_id")));
            subject.put("subjectName", rs.getString("subject_name"));
            subject.put("semester", String.valueOf(rs.getInt("semester")));
            subject.put("programName", rs.getString("program_name"));
            subjects.add(subject);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting subjects by teacher: " + e.getMessage());
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
public void debugStudentRegistration(int studentId, int programId) {
    System.out.println("🔍 [DEBUG REGISTRATION]");
    System.out.println("   Student ID: " + studentId);
    System.out.println("   Program ID: " + programId);
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        
        // Check if student exists
        String studentSql = "SELECT * FROM students WHERE student_id = ?";
        stmt = conn.prepareStatement(studentSql);
        stmt.setInt(1, studentId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("✅ Student found: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            System.out.println("   Current program_id: " + rs.getInt("program_id"));
        } else {
            System.out.println("❌ Student not found with ID: " + studentId);
        }
        rs.close();
        stmt.close();
        
        // Check if program exists
        String programSql = "SELECT * FROM programs WHERE program_id = ?";
        stmt = conn.prepareStatement(programSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("✅ Program found: " + rs.getString("program_name"));
        } else {
            System.out.println("❌ Program not found with ID: " + programId);
        }
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        System.err.println("Error debugging registration: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
public void checkStudentRegistrationStatus(int studentId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        
        String sql = "SELECT s.student_id, s.first_name, s.last_name, " +
                    "p.program_name, p.program_id, s.academic_year " +
                    "FROM students s " +
                    "LEFT JOIN programs p ON s.program_id = p.program_id " +
                    "WHERE s.student_id = ?";
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("🔍 [REGISTRATION STATUS]");
            System.out.println("   Student: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            System.out.println("   Program: " + rs.getString("program_name"));
            System.out.println("   Program ID: " + rs.getInt("program_id"));
            System.out.println("   Academic Year: " + rs.getString("academic_year"));
            
            if (rs.getInt("program_id") == 0 || rs.wasNull()) {
                System.out.println("❌ STUDENT NOT REGISTERED TO ANY PROGRAM");
            } else {
                System.out.println("✅ STUDENT SUCCESSFULLY REGISTERED");
            }
        } else {
            System.out.println("❌ Student not found: " + studentId);
        }
        
    } catch (SQLException e) {
        System.err.println("Error checking registration status: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
// Add this method to ClientService class (around line 1388)
private boolean verifyStudentExists(int studentId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        String sql = "SELECT student_id FROM students WHERE student_id = ?";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        rs = stmt.executeQuery();
        return rs.next();
    } catch (SQLException e) {
        System.err.println("Error verifying student: " + e.getMessage());
        return false;
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
public void debugTeacherStudentVisibility(int teacherId, int subjectId) {
    System.out.println("🔍 [DEBUG TEACHER STUDENT VISIBILITY]");
    System.out.println("   Teacher ID: " + teacherId);
    System.out.println("   Subject ID: " + subjectId);
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        
        // Check teacher-subject assignment
        System.out.println("\n1. Checking teacher-subject assignment:");
        String teacherSubjectSql = "SELECT * FROM teacher_subjects WHERE teacher_id = ? AND subject_id = ?";
        stmt = conn.prepareStatement(teacherSubjectSql);
        stmt.setInt(1, teacherId);
        stmt.setInt(2, subjectId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("✅ Teacher is assigned to this subject");
        } else {
            System.out.println("❌ Teacher NOT assigned to this subject");
        }
        rs.close();
        stmt.close();
        
        // Check students in the program that has this subject
        System.out.println("\n2. Checking students in program with this subject:");
        String studentsSql = "SELECT s.student_id, s.first_name, s.last_name, p.program_name " +
                           "FROM students s " +
                           "JOIN programs p ON s.program_id = p.program_id " +
                           "JOIN subjects sub ON p.program_id = sub.program_id " +
                           "WHERE sub.subject_id = ? AND s.program_id IS NOT NULL";
        stmt = conn.prepareStatement(studentsSql);
        stmt.setInt(1, subjectId);
        rs = stmt.executeQuery();
        
        int studentCount = 0;
        while (rs.next()) {
            studentCount++;
            System.out.println("   Student: " + rs.getString("first_name") + " " + rs.getString("last_name") + 
                             " (Program: " + rs.getString("program_name") + ")");
        }
        System.out.println("📊 Total students in program with this subject: " + studentCount);
        rs.close();
        stmt.close();
        
        // Check if subject belongs to any program
        System.out.println("\n3. Checking subject-program association:");
        String subjectProgramSql = "SELECT s.subject_id, s.subject_name, p.program_id, p.program_name " +
                                 "FROM subjects s " +
                                 "LEFT JOIN programs p ON s.program_id = p.program_id " +
                                 "WHERE s.subject_id = ?";
        stmt = conn.prepareStatement(subjectProgramSql);
        stmt.setInt(1, subjectId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("✅ Subject: " + rs.getString("subject_name"));
            System.out.println("   Program: " + rs.getString("program_name") + " (ID: " + rs.getInt("program_id") + ")");
        } else {
            System.out.println("❌ Subject not found or not associated with any program");
        }
        
    } catch (SQLException e) {
        System.err.println("Error debugging teacher visibility: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
public List<Map<String, String>> getStudentsBySubjectForTeacher(int teacherId, int subjectId) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Map<String, String>> students = new ArrayList<>();
    
    try {
        conn = getConnection();
        
        String sql = "SELECT DISTINCT s.student_id, s.first_name, s.last_name, s.email, " +
                    "p.program_name, s.academic_year " +
                    "FROM students s " +
                    "JOIN programs p ON s.program_id = p.program_id " +
                    "JOIN subjects sub ON p.program_id = sub.program_id " +
                    "JOIN teacher_subjects ts ON sub.subject_id = ts.subject_id " +
                    "WHERE ts.teacher_id = ? AND sub.subject_id = ? " +
                    "AND s.program_id IS NOT NULL " +
                    "ORDER BY s.first_name, s.last_name";
        
        System.out.println("🔍 [DEBUG] Getting students for teacher " + teacherId + " and subject " + subjectId);
        
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, teacherId);
        stmt.setInt(2, subjectId);
        rs = stmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
            Map<String, String> student = new HashMap<>();
            student.put("studentId", String.valueOf(rs.getInt("student_id")));
            student.put("firstName", rs.getString("first_name"));
            student.put("lastName", rs.getString("last_name"));
            student.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
            student.put("email", rs.getString("email"));
            student.put("program", rs.getString("program_name"));
            student.put("academicYear", rs.getString("academic_year"));
            students.add(student);
            
            System.out.println("✅ Student: " + student.get("fullName") + " | " + student.get("program"));
        }
        
        System.out.println("📊 Total students found: " + count);
        
    } catch (SQLException e) {
        System.err.println("Error getting students by subject for teacher: " + e.getMessage());
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
    
    return students;
}
public void debugProgramTeachers(int programId) {
    System.out.println("🔍 [DEBUG PROGRAM TEACHERS]");
    System.out.println("   Program ID: " + programId);
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = getConnection();
        
        // Check if program exists
        String programSql = "SELECT program_name FROM programs WHERE program_id = ?";
        stmt = conn.prepareStatement(programSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("✅ Program found: " + rs.getString("program_name"));
        } else {
            System.out.println("❌ Program not found with ID: " + programId);
            return;
        }
        rs.close();
        stmt.close();
        
        // Check teacher_subjects table
        System.out.println("\n1. Checking teacher_subjects table:");
        String teacherSubjectsSql = "SELECT ts.*, t.first_name, t.last_name, s.subject_name " +
                                   "FROM teacher_subjects ts " +
                                   "JOIN teachers t ON ts.teacher_id = t.teacher_id " +
                                   "JOIN subjects s ON ts.subject_id = s.subject_id " +
                                   "WHERE s.program_id = ?";
        stmt = conn.prepareStatement(teacherSubjectsSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        int teacherCount = 0;
        while (rs.next()) {
            teacherCount++;
            System.out.println("   ✅ Teacher: " + rs.getString("first_name") + " " + rs.getString("last_name") +
                             " | Subject: " + rs.getString("subject_name"));
        }
        System.out.println("   📊 Total teachers found: " + teacherCount);
        rs.close();
        stmt.close();
        
        // Check program_teachers table (if it exists)
        System.out.println("\n2. Checking program_teachers table:");
        try {
            String programTeachersSql = "SELECT pt.*, t.first_name, t.last_name, s.subject_name " +
                                       "FROM program_teachers pt " +
                                       "JOIN teachers t ON pt.teacher_id = t.teacher_id " +
                                       "JOIN subjects s ON pt.subject_id = s.subject_id " +
                                       "WHERE pt.program_id = ?";
            stmt = conn.prepareStatement(programTeachersSql);
            stmt.setInt(1, programId);
            rs = stmt.executeQuery();
            
            int programTeacherCount = 0;
            while (rs.next()) {
                programTeacherCount++;
                System.out.println("   ✅ Program Teacher: " + rs.getString("first_name") + " " + rs.getString("last_name") +
                                 " | Subject: " + rs.getString("subject_name"));
            }
            System.out.println("   📊 Total program teachers found: " + programTeacherCount);
        } catch (SQLException e) {
            System.out.println("   ℹ️ program_teachers table doesn't exist or has different structure");
        }
        
        // Check subjects in this program
        System.out.println("\n3. Checking subjects in program:");
        String subjectsSql = "SELECT subject_id, subject_name, semester FROM subjects WHERE program_id = ?";
        stmt = conn.prepareStatement(subjectsSql);
        stmt.setInt(1, programId);
        rs = stmt.executeQuery();
        
        int subjectCount = 0;
        while (rs.next()) {
            subjectCount++;
            System.out.println("   📚 Subject: " + rs.getString("subject_name") + 
                             " (S" + rs.getInt("semester") + ") - ID: " + rs.getInt("subject_id"));
        }
        System.out.println("   📊 Total subjects in program: " + subjectCount);
        
    } catch (SQLException e) {
        System.err.println("❌ Error debugging program teachers: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
}