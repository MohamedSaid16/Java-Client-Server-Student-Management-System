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
        formPanel.add(new JLabel("Student:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JComboBox<String> studentCombo = new JComboBox<>();
        loadStudentsToComboBox(studentCombo);
        formPanel.add(studentCombo, gbc);
        
        // Exam selection
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Exam:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JComboBox<String> examCombo = new JComboBox<>();
        loadExamsToComboBox(examCombo);
        formPanel.add(examCombo, gbc);
        
        // Grade input
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Grade (0-20):"), gbc);
        
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
        
        // Grades Table
        String[] columns = {"Student ID", "Student Name", "Subject", "Exam", "Grade", "Date", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        gradesTable = new JTable(model);
        gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gradesTable.setRowHeight(30);
        
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
        String gradeText = gradeField.getText();
        String studentSelection = (String) studentCombo.getSelectedItem();
        String examSelection = (String) examCombo.getSelectedItem();
        
        if (gradeText.isEmpty() || studentSelection == null || examSelection == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
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
                loadGrades();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add grade", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for grade", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding grade: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    "Edit/Delete"
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
                    grade.get("studentId"),
                    grade.get("studentName"),
                    grade.get("subject"),
                    grade.get("examName"),
                    grade.get("grade"),
                    grade.get("gradeDate"),
                    "Edit"
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
            List<Map<String, String>> students = clientService.getTeacherStudents(teacherId);
            comboBox.removeAllItems();
            for (Map<String, String> student : students) {
                comboBox.addItem(student.get("studentName") + " (" + student.get("studentId") + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        
        public ExamButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            return button;
        }
        
        public Object getCellEditorValue() {
            int row = examsTable.getSelectedRow();
            if (row != -1) {
                String examId = examsTable.getValueAt(row, 0).toString();
                String examName = (String) examsTable.getValueAt(row, 2);
                String action = button.getText();
                
                if (action.contains("Edit")) {
                    editExam(examId, examName);
                } else if (action.contains("Delete")) {
                    deleteExam(examId, examName);
                }
            }
            return button.getText();
        }
        
        private void editExam(String examId, String examName) {
            JOptionPane.showMessageDialog(TeacherView.this, 
                "Editing exam: " + examName + " (ID: " + examId + ")\n" +
                "This would open an edit form in a real implementation.",
                "Edit Exam", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void deleteExam(String examId, String examName) {
            int confirm = JOptionPane.showConfirmDialog(TeacherView.this,
                "Are you sure you want to delete exam: " + examName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = clientService.deleteExam(examId);
                    if (success) {
                        JOptionPane.showMessageDialog(TeacherView.this, "Exam deleted successfully!");
                        loadExams();
                    } else {
                        JOptionPane.showMessageDialog(TeacherView.this, "Failed to delete exam");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TeacherView.this, "Error deleting exam: " + e.getMessage());
                }
            }
        }
    }
}