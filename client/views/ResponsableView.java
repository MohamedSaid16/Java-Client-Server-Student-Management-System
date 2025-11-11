import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ResponsableView extends JFrame {
    private ResponsableController responsableController;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JTable studentsTable;
    // Form fields
    private JTextField firstNameField, lastNameField, emailField, phoneField, schoolOriginField;
    private JComboBox<String> programCombo, yearCombo;
    private JButton addStudentButton, registerStudentButton, refreshButton;
    
    public ResponsableView(ResponsableController responsableController, Map<String, String> userInfo) {
        this.responsableController = responsableController;
        this.userInfo = userInfo;
        initializeUI();
        loadData();
    }
   
    private void initializeUI() {
        setTitle("Academic Manager Dashboard - " + userInfo.get("firstName"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Student Registration", createRegistrationTab());
        tabbedPane.addTab("Student Management", createStudentsTab());
        tabbedPane.addTab("Academic Programs", createProgramsTab());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void debugStudentLoading() {
        System.out.println("=== DEBUG: Loading Students ===");
        try {
            List<Map<String, String>> students = responsableController.getAllStudents();
            System.out.println("Number of students found: " + students.size());
            
            for (Map<String, String> student : students) {
                System.out.println("Student: " + student.get("studentId") + " - " + 
                                 student.get("firstName") + " " + student.get("lastName"));
            }
        } catch (Exception e) {
            System.out.println("Error loading students: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, Manager " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(76, 175, 80));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> System.exit(0));
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createRegistrationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Student Registration"));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        firstNameField = new JTextField(20);
        formPanel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        lastNameField = new JTextField(20);
        formPanel.add(lastNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);
        
        // School Origin
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("School Origin:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        schoolOriginField = new JTextField(20);
        formPanel.add(schoolOriginField, gbc);
        
        // Program Selection
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Program:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        programCombo = new JComboBox<>();
        loadProgramsToComboBox();
        formPanel.add(programCombo, gbc);
        
        // Academic Year
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Academic Year:*"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 6;
        yearCombo = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
        formPanel.add(yearCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        addStudentButton = new JButton("Add Student");
        addStudentButton.setBackground(new Color(33, 150, 243));
        addStudentButton.setForeground(Color.WHITE);
        addStudentButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addStudentButton.addActionListener(this::addStudent);
        
        registerStudentButton = new JButton("Register Student");
        registerStudentButton.setBackground(new Color(76, 175, 80));
        registerStudentButton.setForeground(Color.WHITE);
        registerStudentButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerStudentButton.addActionListener(this::registerStudent);
        
        refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(new Color(255, 152, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> loadData());
        
        buttonPanel.add(addStudentButton);
        buttonPanel.add(registerStudentButton);
        buttonPanel.add(refreshButton);
        
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshButton = new JButton("Refresh Students");
        refreshButton.setBackground(new Color(255, 152, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadStudents());
        toolbarPanel.add(refreshButton);
        
        // Students Table
        String[] columns = {"Student ID", "First Name", "Last Name", "Email", "Phone", "School Origin", "Program", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Only actions column is editable
            }
        };
        studentsTable = new JTable(model);
        studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentsTable.setRowHeight(30);
        
        // Add action buttons
        studentsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        studentsTable.getColumn("Actions").setCellEditor(new StudentButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Students"));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProgramsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Programs overview
        JTextArea programsTextArea = new JTextArea();
        programsTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        programsTextArea.setEditable(false);
        
        // Load programs data
        loadProgramsToTextArea(programsTextArea);
        
        JScrollPane scrollPane = new JScrollPane(programsTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Programs Information"));
        
        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Quick Statistics"));
        
        // Load statistics from database
        loadStatistics(statsPanel);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(statsPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setPreferredSize(new Dimension(150, 80));
        
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
    
    private void addStudent(ActionEvent e) {
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String email = emailField.getText().trim();
    String phone = phoneField.getText().trim();
    String schoolOrigin = schoolOriginField.getText().trim();
    
    if (firstName.isEmpty() || lastName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill required fields (First Name, Last Name)", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        System.out.println("🔄 [DEBUG] Adding student: " + firstName + " " + lastName);
        System.out.println("📧 Email: " + email);
        System.out.println("📞 Phone: " + phone);
        System.out.println("🏫 School: " + schoolOrigin);
        
        // Add student to database using controller
        boolean success = responsableController.addStudent(firstName, lastName, schoolOrigin, email, phone);
        
        if (success) {
            System.out.println("✅ [DEBUG] Student added successfully!");
            
            JOptionPane.showMessageDialog(this, 
                "Student added successfully!\n" +
                "Name: " + firstName + " " + lastName + "\n" +
                "Email: " + (email.isEmpty() ? "Not provided" : email) + "\n" +
                "School Origin: " + (schoolOrigin.isEmpty() ? "Not provided" : schoolOrigin),
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
            // Clear form and refresh data
            clearForm();
            loadStudents(); // This refreshes the table
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add student. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error adding student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
    
private void registerStudent(ActionEvent e) {
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String program = (String) programCombo.getSelectedItem();
    String academicYear = (String) yearCombo.getSelectedItem();
    
    if (firstName.isEmpty() || lastName.isEmpty() || program == null || program.equals("No programs available")) {
        JOptionPane.showMessageDialog(this, "Please fill required fields and select a valid program", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        System.out.println("🔄 [DEBUG] Registering student: " + firstName + " " + lastName);
        System.out.println("📚 Program: " + program);
        System.out.println("📅 Academic Year: " + academicYear);
        
        // First, find the student by name
        List<Map<String, String>> allStudents = responsableController.getAllStudents();
        Integer studentId = null;
        String foundStudentName = null;
        
        for (Map<String, String> student : allStudents) {
            if (student.get("firstName").equalsIgnoreCase(firstName) && 
                student.get("lastName").equalsIgnoreCase(lastName)) {
                studentId = Integer.parseInt(student.get("studentId"));
                foundStudentName = student.get("firstName") + " " + student.get("lastName");
                break;
            }
        }
        
        if (studentId == null) {
            JOptionPane.showMessageDialog(this, 
                "Student not found: " + firstName + " " + lastName + "\n" +
                "Please make sure the student exists in the system.\n" +
                "You may need to add the student first using 'Add Student' button.",
                "Student Not Found", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get program ID
        Integer programId = responsableController.getProgramIdByName(program);
        if (programId == null) {
            JOptionPane.showMessageDialog(this, "Invalid program selected: " + program, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("✅ Found student: " + foundStudentName + " (ID: " + studentId + ")");
        System.out.println("✅ Program ID: " + programId);
        
        // Register student to program
        boolean success = responsableController.registerStudent(studentId, programId, academicYear);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Student registered successfully!\n" +
                "Student: " + foundStudentName + "\n" +
                "Program: " + program + "\n" +
                "Academic Year: " + academicYear + "\n" +
                "Student ID: " + studentId,
                "Registration Successful", 
                JOptionPane.INFORMATION_MESSAGE);
                
            // Clear form and refresh data
            clearForm();
            loadStudents(); // Refresh the students table
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to register student.\n" +
                "Possible reasons:\n" +
                "- Student is already registered in this program\n" +
                "- Database error", 
                "Registration Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "Error in registration: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        schoolOriginField.setText("");
        if (programCombo.getItemCount() > 0) {
            programCombo.setSelectedIndex(0);
        }
        yearCombo.setSelectedIndex(0);
    }
    
    private void loadData() {
        loadStudents();
        loadProgramsToComboBox();
    }
    
private void loadStudents() {
    System.out.println("🔄 ========== STARTING LOAD STUDENTS ==========");
    
    try {
        // Get students from database using controller
        System.out.println("🔍 [DEBUG] Calling responsableController.getAllStudents()");
        List<Map<String, String>> students = responsableController.getAllStudents();
        
        System.out.println("📊 [DEBUG] Received students list size: " + students.size());
        
        // Debug the table model
        DefaultTableModel model = (DefaultTableModel) studentsTable.getModel();
        System.out.println("📋 [DEBUG] Table model row count before: " + model.getRowCount());
        
        model.setRowCount(0);
        System.out.println("📋 [DEBUG] Table model row count after clear: " + model.getRowCount());
        
        if (students.isEmpty()) {
            System.out.println("❌ [DEBUG] No students found in the list");
            JOptionPane.showMessageDialog(this, 
                "No students found in the database.\nPlease add students using the registration form.", 
                "No Students", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            System.out.println("✅ [DEBUG] Adding " + students.size() + " students to table");
            
            // Add data from database
            for (int i = 0; i < students.size(); i++) {
                Map<String, String> student = students.get(i);
                System.out.println("➕ [DEBUG] Student " + (i+1) + ": " + 
                    student.get("studentId") + " - " + 
                    student.get("firstName") + " " + student.get("lastName"));
                
                Object[] rowData = new Object[]{
                    student.get("studentId"),
                    student.get("firstName"),
                    student.get("lastName"),
                    student.get("email") != null ? student.get("email") : "",
                    student.get("phone") != null ? student.get("phone") : "",
                    student.get("schoolOrigin") != null ? student.get("schoolOrigin") : "",
                    student.get("program") != null ? student.get("program") : "Not assigned",
                    student.get("status") != null ? student.get("status") : "Active",
                    "Manage"
                };
                
                System.out.println("📝 [DEBUG] Row data: " + java.util.Arrays.toString(rowData));
                model.addRow(rowData);
            }
            
            System.out.println("📋 [DEBUG] Table model row count after adding: " + model.getRowCount());
            
            // Force table refresh
            System.out.println("🔄 [DEBUG] Refreshing table UI");
            studentsTable.revalidate();
            studentsTable.repaint();
            
            // Check if table is visible
            System.out.println("👀 [DEBUG] Table is visible: " + studentsTable.isVisible());
            System.out.println("👀 [DEBUG] Table parent is visible: " + studentsTable.getParent().isVisible());
        }
        
        // Update statistics
        updateStatistics();
        
    } catch (Exception e) {
        System.err.println("❌ [DEBUG] Error loading students: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    System.out.println("🔄 ========== FINISHED LOAD STUDENTS ==========");
}
    
    private void loadProgramsToComboBox() {
        try {
            List<Map<String, String>> programs = responsableController.getAllPrograms();
            programCombo.removeAllItems();
            
            if (programs.isEmpty()) {
                programCombo.addItem("No programs available");
            } else {
                for (Map<String, String> program : programs) {
                    programCombo.addItem(program.get("programName"));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadProgramsToTextArea(JTextArea textArea) {
        try {
            List<Map<String, String>> programs = responsableController.getProgramsWithStats();
            StringBuilder sb = new StringBuilder("Academic Programs Overview\n\n");
            
            if (programs.isEmpty()) {
                sb.append("No programs available in the database.\n");
                sb.append("Please add programs through the administration panel.");
            } else {
                for (Map<String, String> program : programs) {
                    sb.append("📚 ").append(program.get("programName")).append("\n")
                      .append("   ⏱️  Duration: ").append(program.get("duration")).append(" years\n")
                      .append("   📅 Available Years: ").append(program.get("availableYears")).append("\n")
                      .append("   👥 Current Students: ").append(program.get("currentStudents")).append("\n")
                      .append("   📊 Success Rate: ").append(program.get("successRate")).append("%\n\n");
                }
            }
            
            textArea.setText(sb.toString());
        } catch (Exception e) {
            textArea.setText("Error loading programs data: " + e.getMessage());
        }
    }
    
    private void loadStatistics(JPanel statsPanel) {
        updateStatistics();
    }
    
    private void updateStatistics() {
        try {
            Map<String, Object> stats = responsableController.getResponsableStatistics();
            
            // Update the statistics panel in the programs tab
            Component[] components = tabbedPane.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] subComps = panel.getComponents();
                    for (Component subComp : subComps) {
                        if (subComp instanceof JPanel) {
                            JPanel contentPanel = (JPanel) subComp;
                            Component[] innerComps = contentPanel.getComponents();
                            for (Component innerComp : innerComps) {
                                if (innerComp instanceof JPanel) {
                                    JPanel statsPanel = (JPanel) innerComp;
                                    if (statsPanel.getBorder() != null && 
                                        statsPanel.getBorder().toString().contains("Quick Statistics")) {
                                        statsPanel.removeAll();
                                        statsPanel.add(createStatCard("Total Students", 
                                            stats.get("totalStudents").toString(), Color.BLUE));
                                        statsPanel.add(createStatCard("Active Programs", 
                                            stats.get("activePrograms").toString(), Color.GREEN));
                                        statsPanel.add(createStatCard("Registration Rate", 
                                            stats.get("registrationRate").toString() + "%", Color.ORANGE));
                                        statsPanel.add(createStatCard("Average Success", 
                                            stats.get("averageSuccess").toString() + "%", Color.RED));
                                        
                                        statsPanel.revalidate();
                                        statsPanel.repaint();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
    
    // Button renderer for tables
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }
    
    // Button editor for student table
   // Button editor for student table - FIXED WORKING VERSION
class StudentButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String currentStudentId;
    private String currentStudentName;
    private boolean isPushed;
    
    public StudentButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton("Manage");
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.addActionListener(e -> fireEditingStopped());
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentStudentId = table.getValueAt(row, 0).toString();
        currentStudentName = table.getValueAt(row, 1) + " " + table.getValueAt(row, 2);
        isPushed = true;
        return button;
    }
    
    public Object getCellEditorValue() {
        if (isPushed) {
            showStudentManagementDialog(currentStudentId, currentStudentName);
        }
        isPushed = false;
        return "Manage";
    }
    
    private void showStudentManagementDialog(String studentId, String studentName) {
        JDialog dialog = new JDialog(ResponsableView.this, "Manage Student: " + studentName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(ResponsableView.this);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton editButton = new JButton("📝 Edit Student Information");
        JButton viewGradesButton = new JButton("📊 View Grades & Performance");
        JButton changeProgramButton = new JButton("🔄 Change Program");
        JButton viewDetailsButton = new JButton("👤 View Full Details");
        JButton deleteButton = new JButton("🗑️ Delete Student");
        
        // Style buttons
        Color primaryColor = new Color(33, 150, 243);
        Color dangerColor = new Color(244, 67, 54);
        
        editButton.setBackground(primaryColor);
        viewGradesButton.setBackground(primaryColor);
        changeProgramButton.setBackground(primaryColor);
        viewDetailsButton.setBackground(primaryColor);
        deleteButton.setBackground(dangerColor);
        
        for (JButton btn : new JButton[]{editButton, viewGradesButton, changeProgramButton, viewDetailsButton, deleteButton}) {
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
        }
        
        // Add action listeners - NOW WITH REAL FUNCTIONALITY
        editButton.addActionListener(e -> {
            editStudentInformation(studentId, studentName);
            dialog.dispose();
        });
        
        viewGradesButton.addActionListener(e -> {
            viewStudentGrades(studentId, studentName);
            dialog.dispose();
        });
        
        changeProgramButton.addActionListener(e -> {
            changeStudentProgram(studentId, studentName);
            dialog.dispose();
        });
        
        viewDetailsButton.addActionListener(e -> {
            viewStudentDetails(studentId, studentName);
            dialog.dispose();
        });
        
        deleteButton.addActionListener(e -> {
            deleteStudent(studentId, studentName);
            dialog.dispose();
        });
        
        panel.add(editButton);
        panel.add(viewGradesButton);
        panel.add(changeProgramButton);
        panel.add(viewDetailsButton);
        panel.add(deleteButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // REAL EDIT STUDENT FORM
    private void editStudentInformation(String studentId, String studentName) {
        try {
            // Get current student data
            Map<String, String> currentData = responsableController.getStudentDetails(Integer.parseInt(studentId));
            
            if (currentData == null || currentData.isEmpty()) {
                JOptionPane.showMessageDialog(ResponsableView.this, 
                    "Could not load student data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create edit dialog
            JDialog editDialog = new JDialog(ResponsableView.this, "Edit Student: " + studentName, true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(500, 500);
            editDialog.setLocationRelativeTo(ResponsableView.this);
            
            // Create form panel with scroll
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Personal Information
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            JLabel personalLabel = new JLabel("Personal Information");
            personalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            personalLabel.setForeground(new Color(33, 150, 243));
            formPanel.add(personalLabel, gbc);
            
            // First Name
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            formPanel.add(new JLabel("First Name:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            JTextField firstNameField = new JTextField(20);
            firstNameField.setText(currentData.get("firstName"));
            formPanel.add(firstNameField, gbc);
            
            // Last Name
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Last Name:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            JTextField lastNameField = new JTextField(20);
            lastNameField.setText(currentData.get("lastName"));
            formPanel.add(lastNameField, gbc);
            
            // Contact Information
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            JLabel contactLabel = new JLabel("Contact Information");
            contactLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            contactLabel.setForeground(new Color(33, 150, 243));
            formPanel.add(contactLabel, gbc);
            
            // Email
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.gridy = 4;
            JTextField emailField = new JTextField(20);
            emailField.setText(currentData.get("email"));
            formPanel.add(emailField, gbc);
            
            // Phone
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1; gbc.gridy = 5;
            JTextField phoneField = new JTextField(20);
            phoneField.setText(currentData.get("phone"));
            formPanel.add(phoneField, gbc);
            
            // Academic Information
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
            JLabel academicLabel = new JLabel("Academic Information");
            academicLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            academicLabel.setForeground(new Color(33, 150, 243));
            formPanel.add(academicLabel, gbc);
            
            // School Origin
            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
            formPanel.add(new JLabel("School Origin:"), gbc);
            gbc.gridx = 1; gbc.gridy = 7;
            JTextField schoolOriginField = new JTextField(20);
            schoolOriginField.setText(currentData.get("schoolOrigin"));
            formPanel.add(schoolOriginField, gbc);
            
            // Academic Year
            gbc.gridx = 0; gbc.gridy = 8;
            formPanel.add(new JLabel("Academic Year:"), gbc);
            gbc.gridx = 1; gbc.gridy = 8;
            JComboBox<String> yearCombo = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
            String currentYear = currentData.get("academicYear");
            if (currentYear != null) {
                yearCombo.setSelectedItem(currentYear);
            }
            formPanel.add(yearCombo, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton saveButton = new JButton("Save Changes");
            saveButton.setBackground(new Color(76, 175, 80));
            saveButton.setForeground(Color.WHITE);
            saveButton.addActionListener(evt -> {
                // Validate and save
                if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "First Name and Last Name are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Prepare update data
                Map<String, String> updateData = new HashMap<>();
                updateData.put("firstName", firstNameField.getText().trim());
                updateData.put("lastName", lastNameField.getText().trim());
                updateData.put("email", emailField.getText().trim());
                updateData.put("phone", phoneField.getText().trim());
                updateData.put("schoolOrigin", schoolOriginField.getText().trim());
                updateData.put("academicYear", (String) yearCombo.getSelectedItem());
                
                // Update student
                boolean success = responsableController.updateStudent(Integer.parseInt(studentId), updateData);
                
                if (success) {
                    JOptionPane.showMessageDialog(editDialog, "Student information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    editDialog.dispose();
                    loadStudents(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(editDialog, "Failed to update student information.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(244, 67, 54));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.addActionListener(evt -> editDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            formPanel.add(buttonPanel, gbc);
            
            editDialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
            editDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "Error loading edit form: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // REAL CHANGE PROGRAM FORM
    private void changeStudentProgram(String studentId, String studentName) {
        try {
            // Get current student data
            Map<String, String> currentData = responsableController.getStudentDetails(Integer.parseInt(studentId));
            
            JDialog programDialog = new JDialog(ResponsableView.this, "Change Program: " + studentName, true);
            programDialog.setLayout(new BorderLayout());
            programDialog.setSize(400, 300);
            programDialog.setLocationRelativeTo(ResponsableView.this);
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Current Program
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Current Program:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0;
            JLabel currentProgramLabel = new JLabel(currentData.get("programName") != null ? currentData.get("programName") : "Not assigned");
            formPanel.add(currentProgramLabel, gbc);
            
            // New Program
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("New Program:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            JComboBox<String> programCombo = new JComboBox<>();
            List<Map<String, String>> programs = responsableController.getProgramsForDropdown();
            for (Map<String, String> program : programs) {
                programCombo.addItem(program.get("programName"));
            }
            formPanel.add(programCombo, gbc);
            
            // Academic Year
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Academic Year:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            JComboBox<String> yearCombo = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
            String currentYear = currentData.get("academicYear");
            if (currentYear != null) {
                yearCombo.setSelectedItem(currentYear);
            }
            formPanel.add(yearCombo, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton updateButton = new JButton("Update Program");
            updateButton.setBackground(new Color(76, 175, 80));
            updateButton.setForeground(Color.WHITE);
            updateButton.addActionListener(e -> {
                String selectedProgram = (String) programCombo.getSelectedItem();
                String academicYear = (String) yearCombo.getSelectedItem();
                
                if (selectedProgram == null) {
                    JOptionPane.showMessageDialog(programDialog, "Please select a program.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get program ID
                Integer programId = responsableController.getProgramIdByName(selectedProgram);
                if (programId == null) {
                    JOptionPane.showMessageDialog(programDialog, "Invalid program selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update program
                boolean success = responsableController.registerStudent(Integer.parseInt(studentId), programId, academicYear);
                
                if (success) {
                    JOptionPane.showMessageDialog(programDialog, 
                        "Program updated successfully!\n" +
                        "Student: " + studentName + "\n" +
                        "New Program: " + selectedProgram + "\n" +
                        "Academic Year: " + academicYear,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    programDialog.dispose();
                    loadStudents(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(programDialog, "Failed to update program.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(244, 67, 54));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.addActionListener(e -> programDialog.dispose());
            
            buttonPanel.add(updateButton);
            buttonPanel.add(cancelButton);
            formPanel.add(buttonPanel, gbc);
            
            programDialog.add(formPanel, BorderLayout.CENTER);
            programDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "Error loading program change form: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // REAL DELETE STUDENT FUNCTIONALITY
    private void deleteStudent(String studentId, String studentName) {
        int confirm = JOptionPane.showConfirmDialog(ResponsableView.this,
            "Are you sure you want to delete student:\n" +
            studentName + " (ID: " + studentId + ")\n\n" +
            "This action cannot be undone!",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = responsableController.deleteStudent(Integer.parseInt(studentId));
                if (success) {
                    JOptionPane.showMessageDialog(ResponsableView.this,
                        "Student deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStudents(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(ResponsableView.this,
                        "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(ResponsableView.this,
                    "Error deleting student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    // Enhanced view details
    private void viewStudentDetails(String studentId, String studentName) {
        try {
            Map<String, String> studentDetails = responsableController.getStudentDetails(Integer.parseInt(studentId));
            
            StringBuilder details = new StringBuilder();
            details.append("STUDENT DETAILS\n");
            details.append("===============\n\n");
            
            if (studentDetails != null && !studentDetails.isEmpty()) {
                details.append("Personal Information:\n");
                details.append("• ID: ").append(studentDetails.get("studentId")).append("\n");
                details.append("• Name: ").append(studentDetails.get("firstName")).append(" ").append(studentDetails.get("lastName")).append("\n");
                details.append("• School Origin: ").append(studentDetails.get("schoolOrigin")).append("\n\n");
                
                details.append("Contact Information:\n");
                details.append("• Email: ").append(studentDetails.get("email") != null ? studentDetails.get("email") : "Not provided").append("\n");
                details.append("• Phone: ").append(studentDetails.get("phone") != null ? studentDetails.get("phone") : "Not provided").append("\n\n");
                
                details.append("Academic Information:\n");
                details.append("• Program: ").append(studentDetails.get("programName") != null ? studentDetails.get("programName") : "Not assigned").append("\n");
                details.append("• Academic Year: ").append(studentDetails.get("academicYear") != null ? studentDetails.get("academicYear") : "Not set").append("\n");
                details.append("• Registration Date: ").append(studentDetails.get("registrationDate")).append("\n");
                details.append("• Status: ").append(studentDetails.get("finalStatus") != null ? studentDetails.get("finalStatus") : "Active").append("\n");
            } else {
                details.append("No detailed information available.");
            }
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            textArea.setBackground(new Color(240, 240, 240));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(ResponsableView.this, scrollPane, 
                "Student Details: " + studentName, JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "Error loading student details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
 private void viewStudentGrades(String studentId, String studentName) {
    try {
        // Get student grades from the controller
        List<Map<String, String>> grades = responsableController.getStudentGrades(Integer.parseInt(studentId));
        
        if (grades == null || grades.isEmpty()) {
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "No grade records found for student: " + studentName + "\n" +
                "Student ID: " + studentId,
                "No Grades Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create a dialog to display grades
        JDialog gradesDialog = new JDialog(ResponsableView.this, "Grades for: " + studentName, true);
        gradesDialog.setLayout(new BorderLayout());
        gradesDialog.setSize(600, 400);
        gradesDialog.setLocationRelativeTo(ResponsableView.this);
        
        // Create table model for grades
        String[] columns = {"Subject", "Exam", "Type", "Coefficient", "Score", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Populate the table with grade data
        for (Map<String, String> grade : grades) {
            model.addRow(new Object[]{
                grade.get("subject"),
                grade.get("exam"),
                grade.get("type"),
                grade.get("coefficient"),
                grade.get("score"),
                grade.get("gradeDate")
            });
        }
        
        JTable gradesTable = new JTable(model);
        gradesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gradesTable.setRowHeight(25);
        
        JScrollPane tableScroll = new JScrollPane(gradesTable);
        
        // Create summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.add(new JLabel("Total Exams: " + grades.size()));
        
        gradesDialog.add(tableScroll, BorderLayout.CENTER);
        gradesDialog.add(summaryPanel, BorderLayout.SOUTH);
        
        gradesDialog.setVisible(true);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(ResponsableView.this, 
            "Error loading grades: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

// Helper method to convert letter grades to points
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

// Optional: Export functionality
private void exportGradesToPDF(String studentId, String studentName, 
                              List<Map<String, String>> grades, double gpa, double totalCredits) {
    // This would implement PDF export functionality
    // You might use libraries like iText or Apache PDFBox
    
    JOptionPane.showMessageDialog(ResponsableView.this,
        "PDF export functionality would be implemented here.\n\n" +
        "This would generate a transcript for:\n" +
        "Student: " + studentName + "\n" +
        "GPA: " + String.format("%.2f", gpa) + "\n" +
        "Total Credits: " + totalCredits,
        "Export Feature", JOptionPane.INFORMATION_MESSAGE);
}
}
}