import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.List;

public class ResponsableView extends JFrame {
    private ClientService clientService;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JTable studentsTable;
    private JTable registrationsTable;
    
    // Form fields
    private JTextField firstNameField, lastNameField, emailField, phoneField, schoolOriginField;
    private JComboBox<String> programCombo, yearCombo;
    private JButton addStudentButton, registerStudentButton, refreshButton;
    
    public ResponsableView(ClientService clientService, Map<String, String> userInfo) {
        this.clientService = clientService;
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
        
        buttonPanel.add(addStudentButton);
        buttonPanel.add(registerStudentButton);
        
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
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String schoolOrigin = schoolOriginField.getText();
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields (First Name, Last Name)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Add student to database
            boolean success = clientService.addStudent(firstName, lastName, email, phone, schoolOrigin);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Student added successfully!\n" +
                    "Name: " + firstName + " " + lastName + "\n" +
                    "Email: " + email + "\n" +
                    "School Origin: " + schoolOrigin,
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add student", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registerStudent(ActionEvent e) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String program = (String) programCombo.getSelectedItem();
        String academicYear = (String) yearCombo.getSelectedItem();
        
        if (firstName.isEmpty() || lastName.isEmpty() || program == null) {
            JOptionPane.showMessageDialog(this, "Please fill required fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // For now, use dummy IDs since we don't have the actual student and program IDs
            // In a real implementation, you would need to get these from the database
            int studentId = 1; // This should be the actual student ID from the database
            int programId = 1; // This should be the actual program ID from the database
            int yearId = 1;    // This should be determined based on academicYear
            
            // Register student in database
            boolean success = clientService.registerStudent(studentId, programId, yearId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Student registered successfully!\n" +
                    "Student: " + firstName + " " + lastName + "\n" +
                    "Program: " + program + "\n" +
                    "Academic Year: " + academicYear,
                    "Registration Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear form and refresh data
                clearForm();
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register student", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error registering student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        schoolOriginField.setText("");
        programCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);
    }
    
    private void loadData() {
        loadStudents();
    }
    
    private void loadStudents() {
        try {
            // Get students from database
            List<Map<String, String>> students = clientService.getAllStudents();
            DefaultTableModel model = (DefaultTableModel) studentsTable.getModel();
            model.setRowCount(0);
            
            // Add data from database
            for (Map<String, String> student : students) {
                model.addRow(new Object[]{
                    student.get("studentId"),
                    student.get("firstName"),
                    student.get("lastName"),
                    student.get("email"),
                    student.get("phone"),
                    student.get("schoolOrigin"),
                    student.get("program"),
                    student.get("status"),
                    "Manage"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadProgramsToComboBox() {
        try {
            List<Map<String, String>> programs = clientService.getAllPrograms();
            programCombo.removeAllItems();
            for (Map<String, String> program : programs) {
                programCombo.addItem(program.get("programName"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadProgramsToTextArea(JTextArea textArea) {
        try {
            List<Map<String, String>> programs = clientService.getProgramsWithStats();
            StringBuilder sb = new StringBuilder("Academic Programs Overview\n\n");
            
            for (Map<String, String> program : programs) {
                sb.append(program.get("programName")).append("\n")
                  .append("   - Duration: ").append(program.get("duration")).append(" years\n")
                  .append("   - Available Years: ").append(program.get("availableYears")).append("\n")
                  .append("   - Current Students: ").append(program.get("currentStudents")).append("\n")
                  .append("   - Success Rate: ").append(program.get("successRate")).append("%\n\n");
            }
            
            textArea.setText(sb.toString());
        } catch (Exception e) {
            textArea.setText("Error loading programs data: " + e.getMessage());
        }
    }
    
    private void loadStatistics(JPanel statsPanel) {
        try {
            Map<String, Object> stats = clientService.getResponsableStatistics();
            
            statsPanel.removeAll();
            statsPanel.add(createStatCard("Total Students", stats.get("totalStudents").toString(), Color.BLUE));
            statsPanel.add(createStatCard("Active Programs", stats.get("activePrograms").toString(), Color.GREEN));
            statsPanel.add(createStatCard("Registration Rate", stats.get("registrationRate").toString() + "%", Color.ORANGE));
            statsPanel.add(createStatCard("Average Success", stats.get("averageSuccess").toString() + "%", Color.RED));
            
            statsPanel.revalidate();
            statsPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            return this;
        }
    }
    
    // Button editor for student table
    class StudentButtonEditor extends DefaultCellEditor {
        private JButton button;
        
        public StudentButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Manage");
            button.setBackground(new Color(33, 150, 243));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            return button;
        }
        
        public Object getCellEditorValue() {
            int row = studentsTable.getSelectedRow();
            if (row != -1) {
                String studentId = studentsTable.getValueAt(row, 0).toString();
                String studentName = studentsTable.getValueAt(row, 1) + " " + studentsTable.getValueAt(row, 2);
                showStudentManagementDialog(studentId, studentName, row);
            }
            return "Manage";
        }
        
        private void showStudentManagementDialog(String studentId, String studentName, int row) {
            JDialog dialog = new JDialog(ResponsableView.this, "Manage Student: " + studentName, true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(ResponsableView.this);
            
            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JButton editButton = new JButton("Edit Student Information");
            JButton viewGradesButton = new JButton("View Grades");
            JButton changeProgramButton = new JButton("Change Program");
            JButton deactivateButton = new JButton("Deactivate Student");
            
            // Add action listeners
            editButton.addActionListener(e -> {
                editStudentInformation(studentId);
                dialog.dispose();
            });
            
            viewGradesButton.addActionListener(e -> {
                viewStudentGrades(studentId);
                dialog.dispose();
            });
            
            panel.add(editButton);
            panel.add(viewGradesButton);
            panel.add(changeProgramButton);
            panel.add(deactivateButton);
            
            dialog.add(panel, BorderLayout.CENTER);
            dialog.setVisible(true);
        }
        
        private void editStudentInformation(String studentId) {
            // Implement edit student functionality
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "Edit student: " + studentId + "\nThis would open an edit form in a real implementation.",
                "Edit Student", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        private void viewStudentGrades(String studentId) {
            // Implement view grades functionality
            JOptionPane.showMessageDialog(ResponsableView.this, 
                "View grades for student: " + studentId + "\nThis would show grade details in a real implementation.",
                "Student Grades", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}