import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;

public class StudentView extends JFrame {
    private ClientService clientService;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel;
    private JLabel averageLabel;
    private JLabel statusLabel;
    private JTable gradesTable;
    private JTextArea infoTextArea;
    
    public StudentView(ClientService clientService, Map<String, String> userInfo) {
        this.clientService = clientService;
        this.userInfo = userInfo;
        initializeUI();
        loadStudentData();
    }
    
    private void initializeUI() {
        setTitle("Student Dashboard - " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Create main panel with modern look
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Personal Information", createInfoPanel());
        tabbedPane.addTab("Grades", createGradesPanel());
        tabbedPane.addTab("Statistics", createStatsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        welcomeLabel = new JLabel("Welcome, " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setOpaque(false);
        
        averageLabel = new JLabel("Overall Average: --");
        averageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        averageLabel.setForeground(Color.YELLOW);
        averageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        
        statusLabel = new JLabel("Status: --");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(Color.YELLOW);
        
        statsPanel.add(averageLabel);
        statsPanel.add(statusLabel);
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        infoTextArea = new JTextArea();
        infoTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoTextArea.setEditable(false);
        infoTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Subject", "Exam", "Type", "Coefficient", "Score"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        gradesTable = new JTable(model);
        gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gradesTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea statsTextArea = new JTextArea();
        statsTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsTextArea.setEditable(false);
        
        // Load statistics data
        loadStudentStatistics(statsTextArea);
        
        JScrollPane scrollPane = new JScrollPane(statsTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadStudentData() {
        try {
            int studentId = Integer.parseInt(userInfo.get("studentId"));
            
            // Load personal info from database
            Map<String, String> studentInfo = clientService.getStudentInfo(studentId);
            if (studentInfo != null) {
                StringBuilder info = new StringBuilder();
                info.append("Student ID: ").append(studentInfo.get("studentId")).append("\n");
                info.append("First Name: ").append(studentInfo.get("firstName")).append("\n");
                info.append("Last Name: ").append(studentInfo.get("lastName")).append("\n");
                info.append("School Origin: ").append(studentInfo.get("schoolOrigin")).append("\n");
                info.append("Email: ").append(studentInfo.get("email")).append("\n");
                info.append("Phone: ").append(studentInfo.get("phone")).append("\n");
                info.append("Program: ").append(studentInfo.get("program")).append("\n");
                info.append("Academic Year: ").append(studentInfo.get("academicYear")).append("\n");
                info.append("Registration Date: ").append(studentInfo.get("registrationDate")).append("\n");
                infoTextArea.setText(info.toString());
            }
            
            // Load grades from database
            List<Map<String, String>> grades = clientService.getStudentGrades(studentId);
            if (grades != null && !grades.isEmpty()) {
                DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
                model.setRowCount(0);
                for (Map<String, String> grade : grades) {
                    model.addRow(new Object[]{
                        grade.get("subject"),
                        grade.get("exam"),
                        grade.get("type"),
                        grade.get("coefficient"),
                        grade.get("score")
                    });
                }
            }
            
            // Load overall average from database
            Double average = clientService.getOverallAverage(studentId);
            if (average != null) {
                averageLabel.setText(String.format("Overall Average: %.2f", average));
            }
            
            // Load final status from database
            String status = clientService.getFinalStatus(studentId);
            if (status != null) {
                String statusText = "Status: " + 
                    (status.equals("ADMIS") ? "Admitted" : 
                     status.equals("REDOUBLANT") ? "Repeating" : "Excluded");
                statusLabel.setText(statusText);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadStudentStatistics(JTextArea statsTextArea) {
        try {
            int studentId = Integer.parseInt(userInfo.get("studentId"));
            Map<String, Object> stats = clientService.getStudentStatistics(studentId);
            
            StringBuilder statsText = new StringBuilder();
            statsText.append("Academic Statistics\n\n");
            statsText.append("Overall Performance:\n");
            statsText.append("• Overall Average: ").append(stats.get("overallAverage")).append("/20\n");
            statsText.append("• Rank in Class: ").append(stats.get("classRank")).append("\n");
            statsText.append("• Total Exams: ").append(stats.get("totalExams")).append("\n");
            statsText.append("• Exams Passed: ").append(stats.get("examsPassed")).append("\n");
            statsText.append("• Success Rate: ").append(stats.get("successRate")).append("%\n\n");
            
            statsText.append("Subject-wise Performance:\n");
            List<Map<String, String>> subjectStats = (List<Map<String, String>>) stats.get("subjectStats");
            if (subjectStats != null) {
                for (Map<String, String> subject : subjectStats) {
                    statsText.append("• ").append(subject.get("subject"))
                            .append(": ").append(subject.get("average"))
                            .append("/20 (Rank: ").append(subject.get("rank")).append(")\n");
                }
            }
            
            statsText.append("\nAttendance:\n");
            statsText.append("• Attendance Rate: ").append(stats.get("attendanceRate")).append("%\n");
            statsText.append("• Classes Attended: ").append(stats.get("classesAttended")).append("\n");
            statsText.append("• Total Classes: ").append(stats.get("totalClasses")).append("\n");
            
            statsTextArea.setText(statsText.toString());
        } catch (Exception e) {
            statsTextArea.setText("Error loading statistics: " + e.getMessage());
        }
    }
}