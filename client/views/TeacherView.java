import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.List;

public class TeacherView extends JFrame {
    private ClientService clientService;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JTable examsTable;
    private JTable gradesTable;
    private JTable studentsTable;
    
    // Form fields
    private JComboBox<String> subjectCombo, examTypeCombo;
    private JTextField examNameField, coefficientField;
    private JButton createExamButton, addGradeButton, refreshButton;
    
    public TeacherView(ClientService clientService, Map<String, String> userInfo) {
    this.clientService = clientService;
    this.userInfo = userInfo;
    initializeUI();
    loadData();
    
    // ADD DEBUG HERE - Right after initialization
    debugTeacherSetup();
}

private void debugTeacherSetup() {
    try {
        int teacherId = Integer.parseInt(userInfo.get("teacherId"));
        System.out.println("🔍 [TEACHER VIEW DEBUG] Teacher ID: " + teacherId);
        
        // Get teacher's subjects to debug
        List<Map<String, String>> subjects = clientService.getTeacherSubjects(teacherId);
        System.out.println("📚 Teacher subjects: " + subjects.size());
        
        for (Map<String, String> subject : subjects) {
            int subjectId = Integer.parseInt(subject.get("subjectId"));
            String subjectName = subject.get("subjectName");
            System.out.println("   - Subject: " + subjectName + " (ID: " + subjectId + ")");
            
            // Debug each subject
            clientService.debugTeacherStudentVisibility(teacherId, subjectId);
        }
        
    } catch (Exception e) {
        System.err.println("Error in teacher debug: " + e.getMessage());
    }
}
    
    private void initializeUI() {
        setTitle("Teacher Dashboard - " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Exam Management", createExamsTab());
        tabbedPane.addTab("Grade Management", createGradesTab());
        tabbedPane.addTab("Student Results", createResultsTab());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 98, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, Teacher " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        // Load teacher's subjects from database
        String subjects = loadTeacherSubjects();
        JLabel subjectLabel = new JLabel("Subjects: " + subjects);
        subjectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectLabel.setForeground(Color.YELLOW);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(welcomeLabel, BorderLayout.NORTH);
        infoPanel.add(subjectLabel, BorderLayout.SOUTH);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(41, 98, 255));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> System.exit(0));
        
        headerPanel.add(infoPanel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createExamsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create exam form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Exam"));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Subject
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Subject:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        subjectCombo = new JComboBox<>();
        loadTeacherSubjectsToComboBox();
        formPanel.add(subjectCombo, gbc);
        
        // Exam Type
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Exam Type:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        examTypeCombo = new JComboBox<>(new String[]{"CONTROLE", "EXAMEN", "PROJET", "TP"});
        formPanel.add(examTypeCombo, gbc);
        
        // Exam Name
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Exam Name:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        examNameField = new JTextField(20);
        formPanel.add(examNameField, gbc);
        
        // Coefficient
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Coefficient:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        coefficientField = new JTextField(20);
        coefficientField.setText("1.0");
        formPanel.add(coefficientField, gbc);
        
        // Create Exam Button
        gbc.gridx = 1; gbc.gridy = 4;
        createExamButton = new JButton("Create Exam");
        createExamButton.setBackground(new Color(76, 175, 80));
        createExamButton.setForeground(Color.WHITE);
        createExamButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createExamButton.addActionListener(this::createExam);
        formPanel.add(createExamButton, gbc);
        
        // Exams Table
        String[] columns = {"Exam ID", "Subject", "Exam Name", "Type", "Coefficient", "Date", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };
        
        examsTable = new JTable(model);
        examsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        examsTable.setRowHeight(30);
        
        // Add action buttons
        examsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        examsTable.getColumn("Actions").setCellEditor(new ExamButtonEditor(new JCheckBox()));
        
        JScrollPane tableScrollPane = new JScrollPane(examsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Existing Exams"));
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
  private JPanel createGradesTab() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Grade management form
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Grades"));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    
    // Student selection
    gbc.gridx = 0; gbc.gridy = 0;
    formPanel.add(new JLabel("Student:*"), gbc);
    
    gbc.gridx = 1; gbc.gridy = 0;
    JComboBox<String> studentCombo = new JComboBox<>();
    loadStudentsToComboBox(studentCombo);
    formPanel.add(studentCombo, gbc);
    
    // Exam selection
    gbc.gridx = 0; gbc.gridy = 1;
    formPanel.add(new JLabel("Exam:*"), gbc);
    
    gbc.gridx = 1; gbc.gridy = 1;
    JComboBox<String> examCombo = new JComboBox<>();
    loadExamsToComboBox(examCombo);
    formPanel.add(examCombo, gbc);
    
    // Grade input
    gbc.gridx = 0; gbc.gridy = 2;
    formPanel.add(new JLabel("Grade (0-20):*"), gbc);
    
    gbc.gridx = 1; gbc.gridy = 2;
    JTextField gradeField = new JTextField(10);
    formPanel.add(gradeField, gbc);
    
    // Add Grade Button
    gbc.gridx = 1; gbc.gridy = 3;
    addGradeButton = new JButton("Add/Update Grade");
    addGradeButton.setBackground(new Color(33, 150, 243));
    addGradeButton.setForeground(Color.WHITE);
    addGradeButton.addActionListener(e -> addGrade(studentCombo, examCombo, gradeField));
    formPanel.add(addGradeButton, gbc);
    
    // Refresh button for grades table
    gbc.gridx = 0; gbc.gridy = 3;
    JButton refreshGradesButton = new JButton("Refresh Grades");
    refreshGradesButton.setBackground(new Color(255, 152, 0));
    refreshGradesButton.setForeground(Color.WHITE);
    refreshGradesButton.addActionListener(e -> loadGrades());
    formPanel.add(refreshGradesButton, gbc);
    
    // Grades Table - FIXED: Now with proper button column
    String[] columns = {"Grade ID", "Student", "Subject", "Exam", "Grade", "Date", "Actions"};
    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 6; // Only actions column is editable
        }
    };
    
    gradesTable = new JTable(model);
    gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    gradesTable.setRowHeight(30);
    
    // Add action buttons to grades table
    gradesTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
    gradesTable.getColumn("Actions").setCellEditor(new GradeButtonEditor(new JCheckBox()));
    
    JScrollPane tableScrollPane = new JScrollPane(gradesTable);
    tableScrollPane.setBorder(BorderFactory.createTitledBorder("Entered Grades"));
    
    panel.add(formPanel, BorderLayout.NORTH);
    panel.add(tableScrollPane, BorderLayout.CENTER);
    
    return panel;
}
    
    private JPanel createResultsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("Refresh Results");
        refreshButton.setBackground(new Color(255, 152, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadResults());
        toolbarPanel.add(refreshButton);
        
        // Results Table
        String[] columns = {"Student ID", "Student Name", "Subject", "Final Grade", "Average", "Status", "Rank"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        studentsTable = new JTable(model);
        studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentsTable.setRowHeight(30);
        
        JScrollPane tableScrollPane = new JScrollPane(studentsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Student Results"));
        
        // Statistics panel
        JPanel statsPanel = createTeacherStatsPanel();
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTeacherStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Teaching Statistics"));
        statsPanel.setPreferredSize(new Dimension(0, 100));
        
        // Load teacher statistics from database
        loadTeacherStatistics(statsPanel);
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void createExam(ActionEvent e) {
        String subject = (String) subjectCombo.getSelectedItem();
        String examType = (String) examTypeCombo.getSelectedItem();
        String examName = examNameField.getText();
        String coefficientText = coefficientField.getText();
        
        if (examName.isEmpty() || coefficientText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double coefficient = Double.parseDouble(coefficientText);
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            
            // Create exam in database
            boolean success = clientService.createExam(examName, subject, examType, coefficient, teacherId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Exam created successfully!\n" +
                    "Subject: " + subject + "\n" +
                    "Exam: " + examName + "\n" +
                    "Type: " + examType + "\n" +
                    "Coefficient: " + coefficient,
                    "Exam Created", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear form and refresh table
                examNameField.setText("");
                coefficientField.setText("1.0");
                loadExams();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create exam", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid coefficient", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating exam: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
 private void addGrade(JComboBox<String> studentCombo, JComboBox<String> examCombo, JTextField gradeField) {
    String gradeText = gradeField.getText().trim();
    String studentSelection = (String) studentCombo.getSelectedItem();
    String examSelection = (String) examCombo.getSelectedItem();
    
    if (gradeText.isEmpty() || studentSelection == null || examSelection == null) {
        JOptionPane.showMessageDialog(this, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        double grade = Double.parseDouble(gradeText);
        if (grade < 0 || grade > 20) {
            JOptionPane.showMessageDialog(this, "Grade must be between 0 and 20", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Extract student ID and exam ID from selections
        String studentId = extractIdFromSelection(studentSelection);
        String examId = extractIdFromSelection(examSelection);
        
        if (studentId == null || examId == null) {
            JOptionPane.showMessageDialog(this, "Invalid student or exam selection", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("🔄 Adding grade - Student ID: " + studentId + ", Exam ID: " + examId + ", Grade: " + grade);
        
        // Add grade to database
        boolean success = clientService.addGrade(studentId, examId, grade);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Grade added successfully!\n" +
                "Student: " + studentSelection + "\n" +
                "Exam: " + examSelection + "\n" +
                "Grade: " + grade,
                "Grade Added", 
                JOptionPane.INFORMATION_MESSAGE);
            
            gradeField.setText("");
            loadGrades(); // Refresh the table
            
            // Refresh comboboxes
            loadStudentsToComboBox(studentCombo);
            loadExamsToComboBox(examCombo);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to add grade. Possible reasons:\n" +
                "- Grade already exists for this student and exam\n" +
                "- Database error", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Please enter a valid number for grade", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error adding grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
    
    private void loadData() {
        loadExams();
        loadGrades();
        loadResults();
    }
    
    private void loadExams() {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> exams = clientService.getTeacherExams(teacherId);
            DefaultTableModel model = (DefaultTableModel) examsTable.getModel();
            model.setRowCount(0);
            
            for (Map<String, String> exam : exams) {
                model.addRow(new Object[]{
                    exam.get("examId"),
                    exam.get("subject"),
                    exam.get("examName"),
                    exam.get("examType"),
                    exam.get("coefficient"),
                    exam.get("creationDate"),
                    "Manage"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
  private void loadGrades() {
    try {
        int teacherId = Integer.parseInt(userInfo.get("teacherId"));
        List<Map<String, String>> grades = clientService.getTeacherGrades(teacherId);
        DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
        model.setRowCount(0);
        
        for (Map<String, String> grade : grades) {
            model.addRow(new Object[]{
                grade.get("gradeId"),  // Make sure this is included
                grade.get("studentName"),
                grade.get("subject"),
                grade.get("examName"),
                grade.get("grade"),
                grade.get("gradeDate"),
                "Manage"  // This will now be a clickable button
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    private void loadResults() {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> results = clientService.getStudentResults(teacherId);
            DefaultTableModel model = (DefaultTableModel) studentsTable.getModel();
            model.setRowCount(0);
            
            for (Map<String, String> result : results) {
                model.addRow(new Object[]{
                    result.get("studentId"),
                    result.get("studentName"),
                    result.get("subject"),
                    result.get("finalGrade"),
                    result.get("average"),
                    result.get("status"),
                    result.get("rank")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading results: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String loadTeacherSubjects() {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> subjects = clientService.getTeacherSubjects(teacherId);
            List<String> subjectNames = new ArrayList<>();
            for (Map<String, String> subject : subjects) {
                subjectNames.add(subject.get("subjectName"));
            }
            return String.join(", ", subjectNames);
        } catch (Exception e) {
            return "Error loading subjects";
        }
    }
    
    private void loadTeacherSubjectsToComboBox() {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> subjects = clientService.getTeacherSubjects(teacherId);
            subjectCombo.removeAllItems();
            for (Map<String, String> subject : subjects) {
                subjectCombo.addItem(subject.get("subjectName"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
private void loadStudentsToComboBox(JComboBox<String> comboBox) {
    try {
        int teacherId = Integer.parseInt(userInfo.get("teacherId"));
        
        // DEBUG: Check what's happening
        System.out.println("🎯 [DEBUG] Loading students for teacher: " + teacherId);
        
        List<Map<String, String>> students = clientService.getTeacherStudents(teacherId);
        comboBox.removeAllItems();
        
        if (students.isEmpty()) {
            System.out.println("❌ No students found for teacher!");
            
            // Debug why no students are visible
            List<Map<String, String>> teacherSubjects = clientService.getTeacherSubjects(teacherId);
            if (teacherSubjects.isEmpty()) {
                System.out.println("❌ Teacher has no subjects assigned!");
                JOptionPane.showMessageDialog(this, 
                    "You are not assigned to any subjects. Please contact administrator.", 
                    "No Subjects", JOptionPane.WARNING_MESSAGE);
            } else {
                System.out.println("✅ Teacher has " + teacherSubjects.size() + " subjects, but no students found.");
                
                // Debug each subject
                for (Map<String, String> subject : teacherSubjects) {
                    int subjectId = Integer.parseInt(subject.get("subjectId"));
                    String subjectName = subject.get("subjectName");
                    System.out.println("🔍 Debugging subject: " + subjectName);
                    clientService.debugTeacherStudentVisibility(teacherId, subjectId);
                }
            }
        }
        
        for (Map<String, String> student : students) {
            String displayText = student.get("studentName") + " (" + student.get("studentId") + ")";
            if (student.containsKey("program")) {
                displayText += " - " + student.get("program");
            }
            comboBox.addItem(displayText);
        }
        
        System.out.println("✅ Loaded " + students.size() + " students for teacher");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void loadExamsToComboBox(JComboBox<String> comboBox) {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> exams = clientService.getTeacherExams(teacherId);
            comboBox.removeAllItems();
            for (Map<String, String> exam : exams) {
                comboBox.addItem(exam.get("examName") + " - " + exam.get("subject") + " (" + exam.get("examId") + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTeacherStatistics(JPanel statsPanel) {
        try {
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            Map<String, Object> stats = clientService.getTeacherStatistics(teacherId);
            
            statsPanel.removeAll();
            statsPanel.add(createStatCard("Total Students", stats.get("totalStudents").toString(), Color.BLUE));
            statsPanel.add(createStatCard("Average Grade", stats.get("averageGrade").toString(), Color.GREEN));
            statsPanel.add(createStatCard("Success Rate", stats.get("successRate").toString() + "%", Color.ORANGE));
            statsPanel.add(createStatCard("Exams Created", stats.get("examsCreated").toString(), Color.RED));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String extractIdFromSelection(String selection) {
        // Extract ID from format "Name (ID)"
        if (selection != null && selection.contains("(")) {
            return selection.substring(selection.lastIndexOf("(") + 1, selection.lastIndexOf(")"));
        }
        return selection;
    }
    
    // Button renderer for tables
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    // Button editor for exam table
    class ExamButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String currentExamId;
    private String currentExamName;
    private boolean isPushed;
    
    public ExamButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton("Manage");
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.addActionListener(e -> fireEditingStopped());
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentExamId = table.getValueAt(row, 0).toString();
        currentExamName = (String) table.getValueAt(row, 2);
        isPushed = true;
        return button;
    }
    
    public Object getCellEditorValue() {
        if (isPushed) {
            showExamManagementDialog(currentExamId, currentExamName);
        }
        isPushed = false;
        return "Manage";
    }
    
    private void showExamManagementDialog(String examId, String examName) {
        JDialog dialog = new JDialog(TeacherView.this, "Manage Exam: " + examName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(TeacherView.this);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton editButton = new JButton("📝 Edit Exam");
        JButton deleteButton = new JButton("🗑️ Delete Exam");
        JButton viewGradesButton = new JButton("📊 View Grades");
        
        // Style buttons
        Color primaryColor = new Color(33, 150, 243);
        Color dangerColor = new Color(244, 67, 54);
        
        editButton.setBackground(primaryColor);
        viewGradesButton.setBackground(primaryColor);
        deleteButton.setBackground(dangerColor);
        
        for (JButton btn : new JButton[]{editButton, viewGradesButton, deleteButton}) {
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
        }
        
        // Add REAL action listeners
        editButton.addActionListener(e -> {
            editExam(examId, examName);
            dialog.dispose();
        });
        
        deleteButton.addActionListener(e -> {
            deleteExam(examId, examName);
            dialog.dispose();
        });
        
        viewGradesButton.addActionListener(e -> {
            viewExamGrades(examId, examName);
            dialog.dispose();
        });
        
        panel.add(editButton);
        panel.add(viewGradesButton);
        panel.add(deleteButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // REAL EDIT EXAM FUNCTIONALITY
    private void editExam(String examId, String examName) {
        try {
            // Get current exam data
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> exams = clientService.getTeacherExams(teacherId);
            Map<String, String> currentExam = null;
            
            for (Map<String, String> exam : exams) {
                if (exam.get("examId").equals(examId)) {
                    currentExam = exam;
                    break;
                }
            }
            
            if (currentExam == null) {
                JOptionPane.showMessageDialog(TeacherView.this, 
                    "Could not load exam data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create edit dialog
            JDialog editDialog = new JDialog(TeacherView.this, "Edit Exam: " + examName, true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(400, 300);
            editDialog.setLocationRelativeTo(TeacherView.this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Exam Name
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Exam Name:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 0;
            JTextField nameField = new JTextField(20);
            nameField.setText(currentExam.get("examName"));
            formPanel.add(nameField, gbc);
            
            // Coefficient
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Coefficient:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            JTextField coeffField = new JTextField(20);
            coeffField.setText(currentExam.get("coefficient"));
            formPanel.add(coeffField, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton saveButton = new JButton("Save Changes");
            saveButton.setBackground(new Color(76, 175, 80));
            saveButton.setForeground(Color.WHITE);
            saveButton.addActionListener(evt -> {
                try {
                    String newName = nameField.getText().trim();
                    double newCoefficient = Double.parseDouble(coeffField.getText().trim());
                    
                    if (newName.isEmpty()) {
                        JOptionPane.showMessageDialog(editDialog, "Exam name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Update exam in database
                    boolean success = clientService.updateExam(Integer.parseInt(examId), newName, newCoefficient);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(editDialog, "Exam updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        editDialog.dispose();
                        loadExams(); // Refresh table
                    } else {
                        JOptionPane.showMessageDialog(editDialog, "Failed to update exam.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Please enter a valid coefficient.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(244, 67, 54));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.addActionListener(evt -> editDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            formPanel.add(buttonPanel, gbc);
            
            editDialog.add(formPanel, BorderLayout.CENTER);
            editDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TeacherView.this, 
                "Error loading edit form: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // REAL DELETE EXAM FUNCTIONALITY
    private void deleteExam(String examId, String examName) {
        int confirm = JOptionPane.showConfirmDialog(TeacherView.this,
            "Are you sure you want to delete exam:\n" +
            examName + " (ID: " + examId + ")\n\n" +
            "This will also delete all associated grades!",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = clientService.deleteExam(examId);
                if (success) {
                    JOptionPane.showMessageDialog(TeacherView.this,
                        "Exam deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadExams(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(TeacherView.this,
                        "Failed to delete exam.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TeacherView.this,
                    "Error deleting exam: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void viewExamGrades(String examId, String examName) {
        try {
            // Get grades for this exam
            int teacherId = Integer.parseInt(userInfo.get("teacherId"));
            List<Map<String, String>> allGrades = clientService.getTeacherGrades(teacherId);
            List<Map<String, String>> examGrades = new ArrayList<>();
            
            for (Map<String, String> grade : allGrades) {
                // Extract exam ID from the grade record and compare
                if (grade.get("examId") != null && grade.get("examId").equals(examId)) {
                    examGrades.add(grade);
                }
            }
            
            if (examGrades.isEmpty()) {
                JOptionPane.showMessageDialog(TeacherView.this,
                    "No grades found for exam: " + examName,
                    "No Grades", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Display grades in a dialog
            JDialog gradesDialog = new JDialog(TeacherView.this, "Grades for: " + examName, true);
            gradesDialog.setLayout(new BorderLayout());
            gradesDialog.setSize(500, 300);
            gradesDialog.setLocationRelativeTo(TeacherView.this);
            
            String[] columns = {"Student", "Grade", "Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Map<String, String> grade : examGrades) {
                model.addRow(new Object[]{
                    grade.get("studentName"),
                    grade.get("grade"),
                    grade.get("gradeDate")
                });
            }
            
            JTable gradesTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(gradesTable);
            
            gradesDialog.add(scrollPane, BorderLayout.CENTER);
            gradesDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TeacherView.this,
                "Error loading exam grades: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
// Button editor for grade table
class GradeButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String currentGradeId;
    private String currentStudentName;
    private boolean isPushed;
    
    public GradeButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton("Manage");
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.addActionListener(e -> fireEditingStopped());
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentGradeId = table.getValueAt(row, 0).toString();
        currentStudentName = (String) table.getValueAt(row, 1);
        isPushed = true;
        return button;
    }
    
    public Object getCellEditorValue() {
        if (isPushed) {
            showGradeManagementDialog(currentGradeId, currentStudentName);
        }
        isPushed = false;
        return "Manage";
    }
    
    private void showGradeManagementDialog(String gradeId, String studentName) {
        JDialog dialog = new JDialog(TeacherView.this, "Manage Grade: " + studentName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(TeacherView.this);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton editButton = new JButton("📝 Edit Grade");
        JButton deleteButton = new JButton("🗑️ Delete Grade");
        
        // Style buttons
        Color primaryColor = new Color(33, 150, 243);
        Color dangerColor = new Color(244, 67, 54);
        
        editButton.setBackground(primaryColor);
        deleteButton.setBackground(dangerColor);
        
        for (JButton btn : new JButton[]{editButton, deleteButton}) {
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
        }
        
        // Add action listeners
        editButton.addActionListener(e -> {
            editGrade(gradeId, studentName);
            dialog.dispose();
        });
        
        deleteButton.addActionListener(e -> {
            deleteGrade(gradeId, studentName);
            dialog.dispose();
        });
        
        panel.add(editButton);
        panel.add(deleteButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void editGrade(String gradeId, String studentName) {
        try {
            // Get current grade details
            Map<String, String> gradeDetails = clientService.getGradeDetails(Integer.parseInt(gradeId));
            
            if (gradeDetails == null) {
                JOptionPane.showMessageDialog(TeacherView.this, 
                    "Could not load grade details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create edit dialog
            JDialog editDialog = new JDialog(TeacherView.this, "Edit Grade: " + studentName, true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(400, 200);
            editDialog.setLocationRelativeTo(TeacherView.this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Current info
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            JLabel infoLabel = new JLabel("Student: " + gradeDetails.get("studentName") + 
                                         " | Exam: " + gradeDetails.get("examName"));
            formPanel.add(infoLabel, gbc);
            
            // New grade
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            formPanel.add(new JLabel("New Grade (0-20):*"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            JTextField gradeField = new JTextField(10);
            gradeField.setText(gradeDetails.get("currentScore"));
            formPanel.add(gradeField, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton saveButton = new JButton("Save Changes");
            saveButton.setBackground(new Color(76, 175, 80));
            saveButton.setForeground(Color.WHITE);
            saveButton.addActionListener(e -> {
                try {
                    double newGrade = Double.parseDouble(gradeField.getText().trim());
                    if (newGrade < 0 || newGrade > 20) {
                        JOptionPane.showMessageDialog(editDialog, "Grade must be between 0 and 20", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int teacherId = Integer.parseInt(userInfo.get("teacherId"));
                    boolean success = clientService.updateStudentGrade(Integer.parseInt(gradeId), teacherId, newGrade);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(editDialog, "Grade updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        editDialog.dispose();
                        loadGrades(); // Refresh table
                    } else {
                        JOptionPane.showMessageDialog(editDialog, "Failed to update grade", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(244, 67, 54));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.addActionListener(e -> editDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            formPanel.add(buttonPanel, gbc);
            
            editDialog.add(formPanel, BorderLayout.CENTER);
            editDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TeacherView.this, 
                "Error loading edit form: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteGrade(String gradeId, String studentName) {
        int confirm = JOptionPane.showConfirmDialog(TeacherView.this,
            "Are you sure you want to delete grade for:\n" +
            studentName + " (Grade ID: " + gradeId + ")",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Implementation for deleting grade
                // You'll need to add a deleteGrade method to ClientService
                JOptionPane.showMessageDialog(TeacherView.this,
                    "Grade deletion would be implemented here.\n" +
                    "Grade ID: " + gradeId + " for student: " + studentName,
                    "Delete Feature", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TeacherView.this,
                    "Error deleting grade: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
}