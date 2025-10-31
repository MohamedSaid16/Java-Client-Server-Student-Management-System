import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Server {
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        System.out.println("Starting the server...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(" Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Connection conn;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gestion_scolarite", "root", "");
        } catch (SQLException e) {
            System.err.println(" Database connection error: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {
            
            while (true) {
                String action = (String) input.readObject();
                System.out.println(" Action: " + action);
                
                switch (action) {
                    case "LOGIN":
                        handleLogin(input, output);
                        break;
                    case "GET_STUDENT_INFO":
                        handleGetStudentInfo(input, output);
                        break;
                    case "GET_STUDENT_GRADES":
                        handleGetStudentGrades(input, output);
                        break;
                    case "GET_OVERALL_AVERAGE":
                        handleGetOverallAverage(input, output);
                        break;
                    case "GET_FINAL_STATUS":
                        handleGetFinalStatus(input, output);
                        break;
                    case "CREATE_EXAM":
                        handleCreateExam(input, output);
                        break;
                    case "ADD_GRADE":
                        handleAddGrade(input, output);
                        break;
                    case "CALCULATE_SUBJECT_AVERAGE":
                        handleCalculateSubjectAverage(input, output);
                        break;
                    case "ADD_STUDENT":
                        handleAddStudent(input, output);
                        break;
                    case "ADD_PROGRAM":
                        handleAddProgram(input, output);
                        break;
                    case "EXIT":
                        return;
                    default:
                        output.writeObject("ERROR: Unknown action");
                }
            }
        } catch (Exception e) {
            System.err.println(" Client error: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    private void handleLogin(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        String username = (String) input.readObject();
        String password = (String) input.readObject();
        
        String sql = "SELECT u.*, s.first_name, s.last_name, s.student_id, " +
                    "t.first_name as t_first_name, t.last_name as t_last_name " +
                    "FROM users u " +
                    "LEFT JOIN students s ON u.student_id = s.student_id " +
                    "LEFT JOIN teachers t ON u.teacher_id = t.teacher_id " +
                    "WHERE u.username = ? AND u.password_hash = ? AND u.is_active = 1";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userType", rs.getString("user_type"));
            userInfo.put("userId", rs.getString("user_id"));
            
            if ("STUDENT".equals(userInfo.get("userType"))) {
                userInfo.put("firstName", rs.getString("first_name"));
                userInfo.put("lastName", rs.getString("last_name"));
                userInfo.put("studentId", rs.getString("student_id"));
            } else if ("TEACHER".equals(userInfo.get("userType"))) {
                userInfo.put("firstName", rs.getString("t_first_name"));
                userInfo.put("lastName", rs.getString("t_last_name"));
                userInfo.put("teacherId", rs.getString("teacher_id"));
            }
            
            output.writeObject("SUCCESS");
            output.writeObject(userInfo);
            System.out.println("Login successful: " + username);
        } else {
            output.writeObject("ERROR: Invalid login credentials");
        }
    }
    
    private void handleGetStudentInfo(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        
        String sql = "SELECT s.*, p.program_name, ay.start_year, ay.end_year " +
                    "FROM students s " +
                    "JOIN registrations r ON s.student_id = r.student_id " +
                    "JOIN programs p ON r.program_id = p.program_id " +
                    "JOIN academic_years ay ON r.year_id = ay.year_id " +
                    "WHERE s.student_id = ? AND ay.is_current = 1";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Map<String, String> studentInfo = new HashMap<>();
            studentInfo.put("firstName", rs.getString("first_name"));
            studentInfo.put("lastName", rs.getString("last_name"));
            studentInfo.put("schoolOrigin", rs.getString("school_origin"));
            studentInfo.put("email", rs.getString("email"));
            studentInfo.put("phone", rs.getString("phone"));
            studentInfo.put("program", rs.getString("program_name"));
            studentInfo.put("academicYear", rs.getInt("start_year") + "-" + rs.getInt("end_year"));
            
            output.writeObject("SUCCESS");
            output.writeObject(studentInfo);
        } else {
            output.writeObject("ERROR: Student not found");
        }
    }
    
    private void handleGetStudentGrades(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        
        String sql = "SELECT s.subject_name, e.exam_name, e.exam_type, e.coefficient, g.score " +
                    "FROM grades g " +
                    "JOIN exams e ON g.exam_id = e.exam_id " +
                    "JOIN subjects s ON e.subject_id = s.subject_id " +
                    "WHERE g.student_id = ? " +
                    "ORDER BY s.subject_name, e.exam_date";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        
        ResultSet rs = stmt.executeQuery();
        List<Map<String, String>> grades = new ArrayList<>();
        
        while (rs.next()) {
            Map<String, String> grade = new HashMap<>();
            grade.put("subject", rs.getString("subject_name"));
            grade.put("exam", rs.getString("exam_name"));
            grade.put("type", rs.getString("exam_type"));
            grade.put("coefficient", rs.getString("coefficient"));
            grade.put("score", rs.getString("score"));
            grades.add(grade);
        }
        
        output.writeObject("SUCCESS");
        output.writeObject(grades);
    }
    
    private void handleGetOverallAverage(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        
        // حساب المعدل العام
        String sql = "SELECT AVG(subject_avg * s.coefficient) / AVG(s.coefficient) as overall_avg " +
                    "FROM ( " +
                    "    SELECT e.subject_id, AVG(g.score * e.coefficient) as subject_avg " +
                    "    FROM grades g " +
                    "    JOIN exams e ON g.exam_id = e.exam_id " +
                    "    WHERE g.student_id = ? " +
                    "    GROUP BY e.subject_id " +
                    ") sa " +
                    "JOIN subjects s ON sa.subject_id = s.subject_id";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            double average = rs.getDouble("overall_avg");
            output.writeObject("SUCCESS");
            output.writeDouble(average);
        } else {
            output.writeObject("ERROR: No grades available to calculate average");
        }
    }
    
    private void handleGetFinalStatus(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        
        // الحصول على المعدل وتحديد الحالة
        String sql = "SELECT overall_average FROM registrations " +
                    "WHERE student_id = ? AND year_id = (SELECT year_id FROM academic_years WHERE is_current = 1)";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            Double average = rs.getDouble("overall_average");
            if (rs.wasNull()) {
                output.writeObject("ERROR: لم يتم حساب المعدل بعد");
            } else {
                String status = average >= 10 ? "ADMIS" : (average >= 8 ? "REDOUBLANT" : "EXCLU");
                output.writeObject("SUCCESS");
                output.writeObject(status);
                output.writeDouble(average);
            }
        } else {
            output.writeObject("ERROR: لا توجد تسجيلات للطالب");
        }
    }
    
    private void handleCreateExam(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        String examType = (String) input.readObject();
        String examName = (String) input.readObject();
        double coefficient = input.readDouble();
        int subjectId = input.readInt();
        int teacherId = input.readInt();
        
        String sql = "INSERT INTO exams (exam_type, exam_name, coefficient, exam_date, subject_id, teacher_id) " +
                    "VALUES (?, ?, ?, CURDATE(), ?, ?)";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, examType);
        stmt.setString(2, examName);
        stmt.setDouble(3, coefficient);
        stmt.setInt(4, subjectId);
        stmt.setInt(5, teacherId);
        
        int affected = stmt.executeUpdate();
        
        if (affected > 0) {
            output.writeObject("SUCCESS: تم إنشاء الاختبار بنجاح");
        } else {
            output.writeObject("ERROR: فشل في إنشاء الاختبار");
        }
    }
    
    private void handleAddGrade(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        int examId = input.readInt();
        double score = input.readDouble();
        
        String sql = "INSERT INTO grades (student_id, exam_id, score, entry_date) VALUES (?, ?, ?, CURDATE()) " +
                    "ON DUPLICATE KEY UPDATE score = ?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setInt(2, examId);
        stmt.setDouble(3, score);
        stmt.setDouble(4, score);
        
        int affected = stmt.executeUpdate();
        
        if (affected > 0) {
            output.writeObject("SUCCESS: تم إضافة/تعديل الدرجة");
        } else {
            output.writeObject("ERROR: فشل في إضافة الدرجة");
        }
    }
    
    private void handleCalculateSubjectAverage(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        int studentId = input.readInt();
        int subjectId = input.readInt();
        
        String sql = "SELECT AVG(g.score * e.coefficient) / SUM(e.coefficient) as subject_avg " +
                    "FROM grades g " +
                    "JOIN exams e ON g.exam_id = e.exam_id " +
                    "WHERE g.student_id = ? AND e.subject_id = ?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, studentId);
        stmt.setInt(2, subjectId);
        
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            double average = rs.getDouble("subject_avg");
            output.writeObject("SUCCESS");
            output.writeDouble(average);
        } else {
            output.writeObject("ERROR: لا توجد درجات لهذه المادة");
        }
    }
    
    private void handleAddStudent(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        String firstName = (String) input.readObject();
        String lastName = (String) input.readObject();
        String schoolOrigin = (String) input.readObject();
        String email = (String) input.readObject();
        String phone = (String) input.readObject();
        
        String sql = "INSERT INTO students (first_name, last_name, school_origin, email, phone) VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, schoolOrigin);
        stmt.setString(4, email);
        stmt.setString(5, phone);
        
        int affected = stmt.executeUpdate();
        
        if (affected > 0) {
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int studentId = keys.getInt(1);
                output.writeObject("SUCCESS: تم إضافة الطالب برقم: " + studentId);
            }
        } else {
            output.writeObject("ERROR: فشل في إضافة الطالب");
        }
    }
    
    private void handleAddProgram(ObjectInputStream input, ObjectOutputStream output) throws Exception {
        String programName = (String) input.readObject();
        int programYear = input.readInt();
        String description = (String) input.readObject();
        
        String sql = "INSERT INTO programs (program_name, program_year, description) VALUES (?, ?, ?)";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, programName);
        stmt.setInt(2, programYear);
        stmt.setString(3, description);
        
        int affected = stmt.executeUpdate();
        
        if (affected > 0) {
            output.writeObject("SUCCESS: تم إضافة البرنامج بنجاح");
        } else {
            output.writeObject("ERROR: فشل في إضافة البرنامج");
        }
    }
}