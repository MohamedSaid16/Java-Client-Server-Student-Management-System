import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.Map;
import java.util.List;
import java.util.HashMap; 
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AdminView extends JFrame {
    private AdminController adminController;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JTable usersTable;
    private JTable programsTable;
    private JTextArea statsTextArea;
    
    // Form fields
    private JTextField programNameField, programYearField, programDescField;
    private JButton addProgramButton, refreshUsersButton, generateStatsButton;
    
    // Color scheme
    private final Color PRIMARY_BLUE = new Color(41, 98, 255);
    private final Color LIGHT_BLUE = new Color(33, 150, 243);
    private final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private final Color WARNING_ORANGE = new Color(255, 152, 0);
    private final Color DANGER_RED = new Color(244, 67, 54);
    private final Color PURPLE = new Color(156, 39, 176);
    
    public AdminView(AdminController adminController, Map<String, String> userInfo) {
        this.adminController = adminController;
        this.userInfo = userInfo;
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setTitle("Admin Dashboard - " + userInfo.get("firstName"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📚 Program Management", createProgramsTab());
        tabbedPane.addTab("👥 User Management", createUsersTab());
        tabbedPane.addTab("📊 Statistics & Reports", createStatsTab());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("👨‍💼 Welcome, Admin " + userInfo.get("firstName"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JButton logoutButton = createStyledButton("🚪 Logout", Color.WHITE, LIGHT_BLUE);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        userPanel.add(logoutButton);
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProgramsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create a split pane for better organization
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        
        // Top: Program creation form
        JPanel formPanel = createProgramFormPanel();
        
        // Bottom: Programs table
        JPanel tablePanel = createProgramsTablePanel();
        
        splitPane.setTopComponent(formPanel);
        splitPane.setBottomComponent(tablePanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProgramFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("➕ Add New Program"));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Program Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Program Name:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        programNameField = new JTextField(25);
        programNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(programNameField, gbc);
        
        // Program Year
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Program Year:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        programYearField = new JTextField(25);
        programYearField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(programYearField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        programDescField = new JTextField(25);
        programDescField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(programDescField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        addProgramButton = createStyledButton("💾 Add Program", Color.WHITE, LIGHT_BLUE);
        addProgramButton.setPreferredSize(new Dimension(150, 35));
        addProgramButton.addActionListener(this::addProgram);
        
        JButton clearButton = createStyledButton("🗑️ Clear Form", Color.WHITE, LIGHT_BLUE);
        clearButton.setPreferredSize(new Dimension(150, 35));
        clearButton.addActionListener(e -> clearProgramForm());
        
        buttonPanel.add(addProgramButton);
        buttonPanel.add(clearButton);
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createProgramsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📋 Existing Programs"));
        
        // Programs Table
        String[] programColumns = {"Program ID", "Program Name", "Year", "Description", "Actions"};
        DefaultTableModel programModel = new DefaultTableModel(programColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        programsTable = new JTable(programModel);
        programsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        programsTable.setRowHeight(35);
        programsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Center align all columns except Actions
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < programsTable.getColumnCount() - 1; i++) {
            programsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Add action buttons to programs table
        programsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        programsTable.getColumn("Actions").setCellEditor(new ProgramButtonEditor(new JCheckBox()));
        
        JScrollPane tableScrollPane = new JScrollPane(programsTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 250));
        
        // Refresh button
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = createStyledButton("🔄 Refresh Programs", Color.WHITE, LIGHT_BLUE);
        refreshButton.addActionListener(e -> loadPrograms());
        toolbarPanel.add(refreshButton);
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshUsersButton = createStyledButton("🔄 Refresh Users", Color.WHITE, LIGHT_BLUE);
        refreshUsersButton.addActionListener(e -> loadUsers());
        leftToolbar.add(refreshUsersButton);
        
        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addUserButton = createStyledButton("👤 Add New User", Color.WHITE, LIGHT_BLUE);
        addUserButton.addActionListener(e -> showAddUserDialog());
        rightToolbar.add(addUserButton);
        
        toolbarPanel.add(leftToolbar, BorderLayout.WEST);
        toolbarPanel.add(rightToolbar, BorderLayout.EAST);
        
        // Users Table
        String[] userColumns = {"User ID", "Username", "User Type", "First Name", "Last Name", "Status", "Actions"};
        DefaultTableModel userModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };
        
        usersTable = new JTable(userModel);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.setRowHeight(35);
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Add action buttons to users table
        usersTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        usersTable.getColumn("Actions").setCellEditor(new UserButtonEditor(new JCheckBox()));
        
        JScrollPane tableScrollPane = new JScrollPane(usersTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("👥 User Management"));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateStatsButton = createStyledButton("📈 Generate Statistics Report", Color.WHITE, LIGHT_BLUE);
        generateStatsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateStatsButton.addActionListener(e -> generateStatistics());
        toolbarPanel.add(generateStatsButton);
        
        // Statistics Text Area
        statsTextArea = new JTextArea();
        statsTextArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        statsTextArea.setEditable(false);
        statsTextArea.setBackground(new Color(245, 245, 245));
        statsTextArea.setText("System Statistics Report\n\n" +
                "• Total Students: --\n" +
                "• Total Teachers: --\n" +
                "• Total Programs: --\n" +
                "• Active Users: --\n" +
                "• Success Rate: --\n" +
                "• Failure Rate: --\n" +
                "• Average Grade: --/20\n" +
                "• System Uptime: --%\n\n" +
                "Click 'Generate Statistics Report' to load current data.");
        
        JScrollPane scrollPane = new JScrollPane(statsTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📊 System Statistics"));
        scrollPane.setPreferredSize(new Dimension(0, 400));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Helper method to create styled buttons - ALL BLUE
    private JButton createStyledButton(String text, Color foreground, Color background) {
        JButton button = new JButton(text);
        button.setBackground(LIGHT_BLUE); // Always use light blue
        button.setForeground(Color.WHITE); // Always white text
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void addProgram(ActionEvent e) {
        String name = programNameField.getText().trim();
        String year = programYearField.getText().trim();
        String desc = programDescField.getText().trim();
        
        if (name.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill all required fields (Program Name and Program Year)", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int programYear = Integer.parseInt(year);
            boolean success = adminController.addProgram(name, programYear, desc);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Program added successfully!\n\n" +
                    "Program: " + name + "\n" +
                    "Year: " + programYear + "\n" +
                    "Description: " + (desc.isEmpty() ? "No description" : desc),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                clearProgramForm();
                loadPrograms();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "❌ Failed to add program. Please try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid number for Program Year", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error adding program: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearProgramForm() {
        programNameField.setText("");
        programYearField.setText("");
        programDescField.setText("");
    }
    
    private void loadData() {
        loadPrograms();
        loadUsers();
    }
    
    private void loadPrograms() {
        try {
            List<Map<String, String>> programs = adminController.getAllPrograms();
            DefaultTableModel model = (DefaultTableModel) programsTable.getModel();
            model.setRowCount(0);
            
            for (Map<String, String> program : programs) {
                model.addRow(new Object[]{
                    program.get("programId"),
                    program.get("programName"),
                    program.get("programYear"),
                    program.get("description"),
                    "Manage"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading programs: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUsers() {
        try {
            List<Map<String, String>> users = adminController.getAllUsers();
            DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
            model.setRowCount(0);
            
            for (Map<String, String> user : users) {
                model.addRow(new Object[]{
                    user.get("userId"),
                    user.get("username"),
                    user.get("userType"),
                    user.get("firstName"),
                    user.get("lastName"),
                    user.get("status"),
                    "Manage"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading users: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateStatistics() {
        try {
            Map<String, Object> stats = adminController.getSystemStatistics();
            
            StringBuilder statsText = new StringBuilder();
            statsText.append("📊 System Statistics Report\n");
            statsText.append("Generated on: ").append(new java.util.Date()).append("\n\n");
            statsText.append("• Total Students: ").append(stats.get("totalStudents")).append("\n");
            statsText.append("• Total Teachers: ").append(stats.get("totalTeachers")).append("\n");
            statsText.append("• Total Programs: ").append(stats.get("totalPrograms")).append("\n");
            statsText.append("• Active Users: ").append(stats.get("activeUsers")).append("\n");
            statsText.append("• Success Rate: ").append(stats.get("successRate")).append("%\n");
            statsText.append("• Failure Rate: ").append(stats.get("failureRate")).append("%\n");
            statsText.append("• Average Grade: ").append(stats.get("averageGrade")).append("/20\n");
            statsText.append("• System Uptime: ").append(stats.get("systemUptime")).append("%\n\n");
            
            if (stats.containsKey("detailedAnalysis")) {
                statsText.append("📈 Detailed Analysis:\n").append(stats.get("detailedAnalysis"));
            }
            
            statsTextArea.setText(statsText.toString());
            
            JOptionPane.showMessageDialog(this,
                "Statistics generated successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating statistics: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
private void showAddUserDialog() {
    JDialog addUserDialog = new JDialog(this, "Add New User", true);
    addUserDialog.setLayout(new BorderLayout());
    addUserDialog.setSize(500, 550);
    addUserDialog.setLocationRelativeTo(this);
    
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(8, 8, 8, 8);
    
    // Username
    gbc.gridx = 0; gbc.gridy = 0;
    mainPanel.add(new JLabel("Username:*"), gbc);
    gbc.gridx = 1; gbc.gridy = 0;
    JTextField usernameField = new JTextField(20);
    mainPanel.add(usernameField, gbc);
    
    // Password
    gbc.gridx = 0; gbc.gridy = 1;
    mainPanel.add(new JLabel("Password:*"), gbc);
    gbc.gridx = 1; gbc.gridy = 1;
    JPasswordField passwordField = new JPasswordField(20);
    mainPanel.add(passwordField, gbc);
    
    // User Type
    gbc.gridx = 0; gbc.gridy = 2;
    mainPanel.add(new JLabel("User Type:*"), gbc);
    gbc.gridx = 1; gbc.gridy = 2;
    JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "ADMIN", "RESPONSABLE"});
    mainPanel.add(userTypeCombo, gbc);
    
    // Email
    gbc.gridx = 0; gbc.gridy = 3;
    mainPanel.add(new JLabel("Email:*"), gbc);
    gbc.gridx = 1; gbc.gridy = 3;
    JTextField emailField = new JTextField(20);
    mainPanel.add(emailField, gbc);
    
    // Info label for teacher/student creation
    gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
    JLabel infoLabel = new JLabel("For TEACHER/STUDENT: Leave IDs empty to create new record", JLabel.CENTER);
    infoLabel.setForeground(Color.BLUE);
    infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    mainPanel.add(infoLabel, gbc);
    
    // Student ID (optional)
    gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
    mainPanel.add(new JLabel("Student ID (optional):"), gbc);
    gbc.gridx = 1; gbc.gridy = 5;
    JTextField studentIdField = new JTextField(20);
    studentIdField.setToolTipText("Leave empty to create a new student record");
    mainPanel.add(studentIdField, gbc);
    
    // Teacher ID (optional)
    gbc.gridx = 0; gbc.gridy = 6;
    mainPanel.add(new JLabel("Teacher ID (optional):"), gbc);
    gbc.gridx = 1; gbc.gridy = 6;
    JTextField teacherIdField = new JTextField(20);
    teacherIdField.setToolTipText("Leave empty to create a new teacher record");
    mainPanel.add(teacherIdField, gbc);
    
    // Buttons
    gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
    JPanel buttonPanel = new JPanel(new FlowLayout());
    
    JButton createButton = createStyledButton("💾 Create User", Color.WHITE, LIGHT_BLUE);
    createButton.addActionListener(e -> {
        createNewUser(usernameField, passwordField, userTypeCombo, emailField, 
                     studentIdField, teacherIdField, addUserDialog);
    });
    
    JButton cancelButton = createStyledButton("❌ Cancel", Color.WHITE, LIGHT_BLUE);
    cancelButton.addActionListener(e -> addUserDialog.dispose());
    
    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    mainPanel.add(buttonPanel, gbc);
    
    addUserDialog.add(mainPanel);
    addUserDialog.setVisible(true);
}

private void createNewUser(JTextField usernameField, JPasswordField passwordField, 
                         JComboBox<String> userTypeCombo, JTextField emailField,
                         JTextField studentIdField, JTextField teacherIdField, JDialog dialog) {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    String userType = (String) userTypeCombo.getSelectedItem();
    String email = emailField.getText().trim();
    String studentIdText = studentIdField.getText().trim();
    String teacherIdText = teacherIdField.getText().trim();
    
    if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Please fill all required fields (Username, Password, Email)", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        Integer studentId = studentIdText.isEmpty() ? null : Integer.parseInt(studentIdText);
        Integer teacherId = teacherIdText.isEmpty() ? null : Integer.parseInt(teacherIdText);
        
        boolean success = adminController.createUser(username, password, userType, studentId, teacherId, email);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "✅ User created successfully!\n\n" +
                "Username: " + username + "\n" +
                "User Type: " + userType + "\n" +
                "Email: " + email,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
            loadUsers(); // Refresh users table
        } else {
            JOptionPane.showMessageDialog(this, 
                "❌ Failed to create user. Username might already exist.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, 
            "Please enter valid numbers for Student ID and Teacher ID", 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    // Enhanced Program Management Methods
    public void showProgramTopicsManagement(int programId, String programName) {
        JDialog topicsDialog = new JDialog(this, "📚 Manage Topics - " + programName, true);
        topicsDialog.setLayout(new BorderLayout());
        topicsDialog.setSize(900, 600);
        topicsDialog.setLocationRelativeTo(this);
        
        JTabbedPane topicsTabbedPane = new JTabbedPane();
        
        // Tab 1: View and Manage Existing Topics
        topicsTabbedPane.addTab("📋 Current Topics", createTopicsManagementTab(programId));
        
        // Tab 2: Add New Topic
        topicsTabbedPane.addTab("➕ Add New Topic", createAddTopicTab(programId));
        
        // Tab 3: Teacher Assignments
        topicsTabbedPane.addTab("👨‍🏫 Teacher Assignments", createTeacherAssignmentTab(programId));
        
        topicsDialog.add(topicsTabbedPane, BorderLayout.CENTER);
        topicsDialog.setVisible(true);
    }
    
    private JPanel createTopicsManagementTab(int programId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = createStyledButton("🔄 Refresh Topics", Color.WHITE, LIGHT_BLUE);
        refreshButton.addActionListener(e -> loadProgramTopics(programId));
        toolbarPanel.add(refreshButton);
        
        // Topics table
        String[] columns = {"Subject ID", "Subject Name", "Semester", "Coefficient", "Assigned Teacher", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        JTable topicsTable = new JTable(model);
        topicsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topicsTable.setRowHeight(30);
        
        // Load initial data
        loadProgramTopicsData(programId, topicsTable);
        
        JScrollPane tableScrollPane = new JScrollPane(topicsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Program Topics"));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadProgramTopicsData(int programId, JTable topicsTable) {
        try {
            List<Map<String, String>> subjects = adminController.getProgramSubjects(programId);
            DefaultTableModel model = (DefaultTableModel) topicsTable.getModel();
            model.setRowCount(0);
            
            for (Map<String, String> subject : subjects) {
                model.addRow(new Object[]{
                    subject.get("subjectId"),
                    subject.get("subjectName"),
                    subject.get("semester"),
                    subject.get("coefficient"),
                    subject.get("assignedTeacher"),
                    "Edit"
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading program topics: " + e.getMessage());
        }
    }
    
    private JPanel createAddTopicTab(int programId) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Subject Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Subject Name:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField subjectNameField = new JTextField(25);
        panel.add(subjectNameField, gbc);
        
        // Semester
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Semester:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JComboBox<String> semesterCombo = new JComboBox<>();
        for (String semester : adminController.getSemesters()) {
            semesterCombo.addItem(semester);
        }
        panel.add(semesterCombo, gbc);
        
        // Coefficient
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Coefficient:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField coefficientField = new JTextField(10);
        coefficientField.setText("1.0");
        panel.add(coefficientField, gbc);
        
        // Objectives
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Objectives:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JTextArea objectivesArea = new JTextArea(4, 25);
        JScrollPane objectivesScroll = new JScrollPane(objectivesArea);
        panel.add(objectivesScroll, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton addTopicButton = createStyledButton("💾 Add Topic", Color.WHITE, LIGHT_BLUE);
        addTopicButton.addActionListener(e -> {
            addNewTopic(programId, subjectNameField, semesterCombo, coefficientField, objectivesArea);
        });
        
        JButton clearButton = createStyledButton("🗑️ Clear", Color.WHITE, LIGHT_BLUE);
        clearButton.addActionListener(e -> {
            subjectNameField.setText("");
            objectivesArea.setText("");
            coefficientField.setText("1.0");
        });
        
        buttonPanel.add(addTopicButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTeacherAssignmentTab(int programId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Assign Teacher to Subject"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Subject selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Subject:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        JComboBox<String> subjectCombo = new JComboBox<>();
        loadUnassignedSubjects(programId, subjectCombo);
        formPanel.add(subjectCombo, gbc);
        
        // Teacher selection
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Teacher:*"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JComboBox<String> teacherCombo = new JComboBox<>();
        loadTeachersForAssignment(teacherCombo);
        formPanel.add(teacherCombo, gbc);
        
        // Assign button
        gbc.gridx = 1; gbc.gridy = 2;
        JButton assignButton = createStyledButton("👨‍🏫 Assign Teacher", Color.WHITE, LIGHT_BLUE);
        assignButton.addActionListener(e -> {
            assignTeacherToSubject(programId, subjectCombo, teacherCombo);
        });
        formPanel.add(assignButton, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private void loadProgramTopics(int programId) {
        // This method is called when refreshing topics
        System.out.println("Refreshing topics for program: " + programId);
    }
    
    private void addNewTopic(int programId, JTextField nameField, JComboBox<String> semesterCombo, 
                           JTextField coefficientField, JTextArea objectivesArea) {
        String name = nameField.getText().trim();
        String semesterStr = (String) semesterCombo.getSelectedItem();
        String coefficientText = coefficientField.getText().trim();
        String objectives = objectivesArea.getText().trim();
        
        System.out.println("🔍 [DEBUG] ===== STARTING TOPIC ADDITION =====");
        
        // First, check database structure
        adminController.checkDatabaseStructure();
        
        System.out.println("   Program ID: " + programId);
        System.out.println("   Subject Name: '" + name + "'");
        System.out.println("   Semester: '" + semesterStr + "'");
        System.out.println("   Coefficient: '" + coefficientText + "'");
        
        if (name.isEmpty() || coefficientText.isEmpty() || semesterStr == null) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int semester = Integer.parseInt(semesterStr);
            double coefficient = Double.parseDouble(coefficientText);
            
            System.out.println("🔍 [DEBUG] Calling addSubjectToProgram...");
            boolean success = adminController.addSubjectToProgram(programId, name, objectives, semester, coefficient);
            
            System.out.println("🔍 [DEBUG] Result: " + success);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Topic added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                objectivesArea.setText("");
                coefficientField.setText("1.0");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add topic - check console for details", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for semester and coefficient", "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("🔍 [DEBUG] ===== TOPIC ADDITION COMPLETE =====");
    }
    
    private void loadUnassignedSubjects(int programId, JComboBox<String> subjectCombo) {
        try {
            subjectCombo.removeAllItems();
            List<Map<String, String>> subjects = adminController.getUnassignedSubjects(programId);
            
            for (Map<String, String> subject : subjects) {
                subjectCombo.addItem(subject.get("subjectName") + " (S" + subject.get("semester") + ") - ID: " + subject.get("subjectId"));
            }
            
            if (subjects.isEmpty()) {
                subjectCombo.addItem("No unassigned subjects available");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTeachersForAssignment(JComboBox<String> teacherCombo) {
        try {
            teacherCombo.removeAllItems();
            List<Map<String, String>> teachers = adminController.getAllTeachersForDropdown();
            
            for (Map<String, String> teacher : teachers) {
                teacherCombo.addItem(teacher.get("displayName") + " - ID: " + teacher.get("teacherId"));
            }
            
            if (teachers.isEmpty()) {
                teacherCombo.addItem("No teachers available");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading teachers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void assignTeacherToSubject(int programId, JComboBox<String> subjectCombo, JComboBox<String> teacherCombo) {
        String subjectSelection = (String) subjectCombo.getSelectedItem();
        String teacherSelection = (String) teacherCombo.getSelectedItem();
        
        if (subjectSelection == null || teacherSelection == null || 
            subjectSelection.contains("No unassigned") || teacherSelection.contains("No teachers")) {
            JOptionPane.showMessageDialog(this, "Please select both subject and teacher", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int subjectId = extractIdFromSelection(subjectSelection);
            int teacherId = extractIdFromSelection(teacherSelection);
            
            boolean success = adminController.assignTeacherToSubject(teacherId, subjectId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Teacher assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Refresh the combo boxes
                loadUnassignedSubjects(programId, subjectCombo);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to assign teacher", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error assigning teacher: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int extractIdFromSelection(String selection) {
        if (selection != null && selection.contains("ID: ")) {
            String idPart = selection.substring(selection.lastIndexOf("ID: ") + 4);
            return Integer.parseInt(idPart.trim());
        }
        return -1;
    }
    
    // Button renderer for tables
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (table.getColumnName(column).equals("Actions")) {
                setText((value == null) ? "" : value.toString());
                setBackground(LIGHT_BLUE);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            } else {
                return new DefaultTableCellRenderer().getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            }
            return this;
        }
    }
    
    // Button editor for program table
    class ProgramButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String currentProgramId;
        private String currentProgramName;
        private boolean isPushed;
        
        public ProgramButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = createStyledButton("Manage", Color.WHITE, LIGHT_BLUE);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentProgramId = table.getValueAt(row, 0).toString();
            currentProgramName = (String) table.getValueAt(row, 1);
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                showProgramManagementDialog(currentProgramId, currentProgramName);
            }
            isPushed = false;
            return "Manage";
        }
        
        private void showProgramManagementDialog(String programId, String programName) {
            JDialog dialog = new JDialog(AdminView.this, "Manage Program: " + programName, true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(AdminView.this);
            
            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JButton manageTopicsButton = createStyledButton("📚 Manage Topics & Teachers", Color.WHITE, LIGHT_BLUE);
            JButton viewTeachersButton = createStyledButton("👨‍🏫 View Assigned Teachers", Color.WHITE, LIGHT_BLUE);
            JButton editProgramButton = createStyledButton("✏️ Edit Program Information", Color.WHITE, LIGHT_BLUE);
            JButton viewStudentsButton = createStyledButton("👥 View Program Students", Color.WHITE, LIGHT_BLUE);
            JButton programStatsButton = createStyledButton("📊 Program Statistics", Color.WHITE, LIGHT_BLUE);
            JButton deleteProgramButton = createStyledButton("🗑️ Delete Program", Color.WHITE, LIGHT_BLUE);
            
            // Add action listeners
            manageTopicsButton.addActionListener(e -> {
                showProgramTopicsManagement(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            viewTeachersButton.addActionListener(e -> {
                viewProgramTeachers(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            editProgramButton.addActionListener(e -> {
                editProgramInformation(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            viewStudentsButton.addActionListener(e -> {
                viewProgramStudents(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            programStatsButton.addActionListener(e -> {
                showProgramStatistics(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            deleteProgramButton.addActionListener(e -> {
                deleteProgram(Integer.parseInt(currentProgramId), currentProgramName);
                dialog.dispose();
            });
            
            panel.add(manageTopicsButton);
            panel.add(viewTeachersButton);
            panel.add(editProgramButton);
            panel.add(viewStudentsButton);
            panel.add(programStatsButton);
            panel.add(deleteProgramButton);
            
            dialog.add(panel, BorderLayout.CENTER);
            dialog.setVisible(true);
        }
        
    private void viewProgramTeachers(int programId, String programName) {
    System.out.println("🔍 [DEBUG VIEW PROGRAM TEACHERS]");
    System.out.println("   Program ID: " + programId);
    System.out.println("   Program Name: " + programName);
    
    // First, debug the program teachers
    adminController.debugProgramTeachers(programId);
    
    List<Map<String, String>> teachers = adminController.getProgramTeachers(programId);
    
    JDialog teachersDialog = new JDialog(AdminView.this, "Assigned Teachers - " + programName, true);
    teachersDialog.setLayout(new BorderLayout());
    teachersDialog.setSize(600, 400);
    teachersDialog.setLocationRelativeTo(AdminView.this);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    if (teachers.isEmpty()) {
        JPanel noTeachersPanel = new JPanel(new BorderLayout());
        
        JLabel noTeachersLabel = new JLabel(
            "<html><center><b>No teachers assigned to this program yet.</b><br><br>" +
            "To assign teachers:<br>" +
            "1. Go to 'Manage Topics & Teachers'<br>" +
            "2. Click 'Teacher Assignments' tab<br>" +
            "3. Select a subject and assign a teacher</center></html>", 
            JLabel.CENTER
        );
        noTeachersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noTeachersLabel.setForeground(Color.RED);
        
        JButton assignTeachersButton = createStyledButton("👨‍🏫 Go Assign Teachers", Color.WHITE, LIGHT_BLUE);
        assignTeachersButton.addActionListener(e -> {
            teachersDialog.dispose();
            showProgramTopicsManagement(programId, programName);
        });
        
        noTeachersPanel.add(noTeachersLabel, BorderLayout.CENTER);
        noTeachersPanel.add(assignTeachersButton, BorderLayout.SOUTH);
        mainPanel.add(noTeachersPanel, BorderLayout.CENTER);
    } else {
        String[] columns = {"Teacher", "Subject", "Semester", "Specialty"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        for (Map<String, String> assignment : teachers) {
            model.addRow(new Object[]{
                assignment.get("teacherName"),
                assignment.get("subjectName"),
                "S" + assignment.get("semester"),
                assignment.get("specialty")
            });
        }
        
        JTable teachersTable = new JTable(model);
        teachersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        teachersTable.setRowHeight(25);
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < teachersTable.getColumnCount(); i++) {
            teachersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(teachersTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = createStyledButton("🔄 Refresh", Color.WHITE, LIGHT_BLUE);
        refreshButton.addActionListener(e -> {
            teachersDialog.dispose();
            viewProgramTeachers(programId, programName); // Refresh
        });
        toolbarPanel.add(refreshButton);
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
    }
    
    teachersDialog.add(mainPanel);
    teachersDialog.setVisible(true);
}
        
        private void editProgramInformation(int programId, String programName) {
            // Create edit dialog
            JDialog editDialog = new JDialog(AdminView.this, "Edit Program: " + programName, true);
            editDialog.setLayout(new BorderLayout());
            editDialog.setSize(500, 400);
            editDialog.setLocationRelativeTo(AdminView.this);
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 8, 8, 8);
            
            // Get current program data
            Map<String, String> programData = adminController.getProgramData(programId);
            
            // Program Name
            gbc.gridx = 0; gbc.gridy = 0;
            mainPanel.add(new JLabel("Program Name:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 0;
            JTextField nameField = new JTextField(20);
            nameField.setText(programData.get("programName"));
            mainPanel.add(nameField, gbc);
            
            // Program Year
            gbc.gridx = 0; gbc.gridy = 1;
            mainPanel.add(new JLabel("Program Year:*"), gbc);
            gbc.gridx = 1; gbc.gridy = 1;
            JTextField yearField = new JTextField(20);
            yearField.setText(programData.get("programYear"));
            mainPanel.add(yearField, gbc);
            
            // Description
            gbc.gridx = 0; gbc.gridy = 2;
            mainPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; gbc.gridy = 2;
            JTextArea descArea = new JTextArea(4, 20);
            descArea.setText(programData.get("description"));
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            JScrollPane descScroll = new JScrollPane(descArea);
            mainPanel.add(descScroll, gbc);
            
            // Buttons - ALL BLUE
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout());
            
            JButton saveButton = createStyledButton("💾 Save Changes", Color.WHITE, LIGHT_BLUE);
            saveButton.addActionListener(e -> {
                saveProgramChanges(programId, nameField.getText().trim(), yearField.getText().trim(), 
                                 descArea.getText().trim(), editDialog);
            });
            
            JButton cancelButton = createStyledButton("❌ Cancel", Color.WHITE, LIGHT_BLUE);
            cancelButton.addActionListener(e -> editDialog.dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            mainPanel.add(buttonPanel, gbc);
            
            editDialog.add(mainPanel);
            editDialog.setVisible(true);
        }
        
        private void saveProgramChanges(int programId, String name, String year, String description, JDialog dialog) {
            if (name.isEmpty() || year.isEmpty()) {
                JOptionPane.showMessageDialog(AdminView.this, 
                    "Please fill all required fields (Program Name and Program Year)", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int programYear = Integer.parseInt(year);
                boolean success = adminController.updateProgram(programId, name, programYear, description);
                
                if (success) {
                    JOptionPane.showMessageDialog(AdminView.this, 
                        "✅ Program updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPrograms(); // Refresh the programs table
                } else {
                    JOptionPane.showMessageDialog(AdminView.this, 
                        "❌ Failed to update program.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(AdminView.this, 
                    "Please enter a valid number for Program Year", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void viewProgramStudents(int programId, String programName) {
            try {
                List<Map<String, String>> students = adminController.getProgramStudents(programId);
                
                JDialog studentsDialog = new JDialog(AdminView.this, "Students in " + programName, true);
                studentsDialog.setLayout(new BorderLayout());
                studentsDialog.setSize(600, 400);
                studentsDialog.setLocationRelativeTo(AdminView.this);
                
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                if (students.isEmpty()) {
                    JLabel noStudentsLabel = new JLabel("No students enrolled in this program yet.", JLabel.CENTER);
                    noStudentsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    noStudentsLabel.setForeground(Color.RED);
                    mainPanel.add(noStudentsLabel, BorderLayout.CENTER);
                } else {
                    String[] columns = {"Student ID", "First Name", "Last Name", "Email", "Academic Year"};
                    DefaultTableModel model = new DefaultTableModel(columns, 0);
                    
                    for (Map<String, String> student : students) {
                        model.addRow(new Object[]{
                            student.get("studentId"),
                            student.get("firstName"),
                            student.get("lastName"),
                            student.get("email"),
                            student.get("academicYear")
                        });
                    }
                    
                    JTable studentsTable = new JTable(model);
                    studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    studentsTable.setRowHeight(25);
                    
                    JScrollPane scrollPane = new JScrollPane(studentsTable);
                    mainPanel.add(scrollPane, BorderLayout.CENTER);
                }
                
                studentsDialog.add(mainPanel);
                studentsDialog.setVisible(true);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(AdminView.this,
                    "Error loading students: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showProgramStatistics(int programId, String programName) {
            try {
                Map<String, Object> stats = adminController.getProgramStatistics(programId);
                
                JDialog statsDialog = new JDialog(AdminView.this, "Statistics - " + programName, true);
                statsDialog.setLayout(new BorderLayout());
                statsDialog.setSize(400, 300);
                statsDialog.setLocationRelativeTo(AdminView.this);
                
                JPanel mainPanel = new JPanel(new GridLayout(0, 1, 10, 10));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                mainPanel.add(new JLabel("📊 Program Statistics: " + programName, JLabel.CENTER));
                mainPanel.add(new JLabel("Total Students: " + stats.get("totalStudents")));
                mainPanel.add(new JLabel("Success Rate: " + stats.get("successRate") + "%"));
                mainPanel.add(new JLabel("Average Grade: " + stats.get("averageGrade") + "/20"));
                mainPanel.add(new JLabel("Total Subjects: " + stats.get("totalSubjects")));
                
                statsDialog.add(mainPanel);
                statsDialog.setVisible(true);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(AdminView.this,
                    "Error loading statistics: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void deleteProgram(int programId, String programName) {
            int confirm = JOptionPane.showConfirmDialog(AdminView.this,
                "Are you sure you want to delete program:\n" +
                programName + " (ID: " + programId + ")\n\n" +
                "⚠️  This action cannot be undone!",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = adminController.deleteProgram(programId);
                if (success) {
                    JOptionPane.showMessageDialog(AdminView.this,
                        "✅ Program deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPrograms();
                } else {
                    JOptionPane.showMessageDialog(AdminView.this,
                        "❌ Failed to delete program. It may have associated students or subjects.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // Button editor for user table - COMPLETE FIXED VERSION
class UserButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String currentUserId;
    private boolean isPushed;
    
    public UserButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = createStyledButton("Manage", Color.WHITE, LIGHT_BLUE);
        button.addActionListener(e -> fireEditingStopped());
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentUserId = table.getValueAt(row, 0).toString();
        isPushed = true;
        return button;
    }
   
    public Object getCellEditorValue() {
        if (isPushed) {
            showUserManagementDialog(currentUserId);
        }
        isPushed = false;
        return "Manage";
    }
    
    private void showUserManagementDialog(String userId) {
        // Get user data
        Map<String, String> userData = adminController.getUserData(Integer.parseInt(userId));
        
        JDialog userDialog = new JDialog(AdminView.this, "Manage User: " + userData.get("username"), true);
        userDialog.setLayout(new BorderLayout());
        userDialog.setSize(500, 500);
        userDialog.setLocationRelativeTo(AdminView.this);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // User Information Display
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("👤 User Information", JLabel.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainPanel.add(infoLabel, gbc);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField usernameField = new JTextField(20);
        usernameField.setText(userData.get("username"));
        usernameField.setEditable(false);
        mainPanel.add(usernameField, gbc);
        
        // User Type
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField userTypeField = new JTextField(20);
        userTypeField.setText(userData.get("userType"));
        userTypeField.setEditable(false);
        mainPanel.add(userTypeField, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JTextField nameField = new JTextField(20);
        nameField.setText(userData.get("firstName") + " " + userData.get("lastName"));
        nameField.setEditable(false);
        mainPanel.add(nameField, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(userData.get("status"));
        mainPanel.add(statusCombo, gbc);
        
        // Password Reset Section
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel passwordLabel = new JLabel("🔒 Password Management", JLabel.CENTER);
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(passwordLabel, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        JPasswordField newPasswordField = new JPasswordField(20);
        mainPanel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 7;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        mainPanel.add(confirmPasswordField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton saveButton = createStyledButton("💾 Save Changes", Color.WHITE, LIGHT_BLUE);
        saveButton.addActionListener(e -> {
            saveUserChanges(Integer.parseInt(userId), statusCombo, newPasswordField, confirmPasswordField, userDialog);
        });
        
        JButton cancelButton = createStyledButton("❌ Cancel", Color.WHITE, LIGHT_BLUE);
        cancelButton.addActionListener(e -> userDialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, gbc);
        
        userDialog.add(mainPanel);
        userDialog.setVisible(true);
    }
    
    private void saveUserChanges(int userId, JComboBox<String> statusCombo, 
                               JPasswordField newPasswordField, JPasswordField confirmPasswordField, JDialog dialog) {
        try {
            boolean changesMade = false;
            
            // Update user status
            String newStatus = (String) statusCombo.getSelectedItem();
            boolean newActiveStatus = "Active".equals(newStatus);
            boolean statusSuccess = adminController.updateUserStatus(userId, newActiveStatus);
            
            if (statusSuccess) {
                changesMade = true;
                System.out.println("✅ User status updated to: " + newStatus);
            }
            
            // Update password if provided
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (!newPassword.isEmpty()) {
                if (newPassword.equals(confirmPassword)) {
                    if (newPassword.length() >= 6) {
                        boolean passwordSuccess = adminController.resetUserPassword(userId, newPassword);
                        if (passwordSuccess) {
                            changesMade = true;
                            JOptionPane.showMessageDialog(AdminView.this, 
                                "✅ Password updated successfully!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(AdminView.this, 
                                "❌ Failed to update password.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(AdminView.this, 
                            "❌ Password must be at least 6 characters long.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(AdminView.this, 
                        "❌ Passwords do not match.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            if (changesMade) {
                JOptionPane.showMessageDialog(AdminView.this, 
                    "✅ User updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadUsers(); // Refresh users table
            } else {
                JOptionPane.showMessageDialog(AdminView.this, 
                    "ℹ️ No changes were made.", 
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(AdminView.this, 
                "❌ Error updating user: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
}