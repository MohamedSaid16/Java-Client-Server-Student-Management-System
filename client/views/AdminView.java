import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.List;

public class AdminView extends JFrame {
    private ClientService clientService;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JTable usersTable;
    private JTable programsTable;
    private JTextArea statsTextArea;
    
    // Form fields
    private JTextField programNameField, programYearField, programDescField;
    private JButton addProgramButton, refreshUsersButton, generateStatsButton;
    
    public AdminView(ClientService clientService, Map<String, String> userInfo) {
        this.clientService = clientService;
        this.userInfo = userInfo;
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setTitle("Admin Dashboard - " + userInfo.get("firstName"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Program Management", createProgramsTab());
        tabbedPane.addTab("User Management", createUsersTab());
        tabbedPane.addTab("Statistics & Reports", createStatsTab());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(244, 67, 54));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, Admin " + userInfo.get("firstName"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(244, 67, 54));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> System.exit(0));
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProgramsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Program"));
        formPanel.setBackground(Color.WHITE);
        
        formPanel.add(new JLabel("Program Name:"));
        programNameField = new JTextField();
        formPanel.add(programNameField);
        
        formPanel.add(new JLabel("Program Year:"));
        programYearField = new JTextField();
        formPanel.add(programYearField);
        
        formPanel.add(new JLabel("Description:"));
        programDescField = new JTextField();
        formPanel.add(programDescField);
        
        addProgramButton = new JButton("Add Program");
        addProgramButton.setBackground(new Color(76, 175, 80));
        addProgramButton.setForeground(Color.WHITE);
        addProgramButton.addActionListener(this::addProgram);
        formPanel.add(new JLabel()); // empty cell
        formPanel.add(addProgramButton);
        
        // Programs Table
        String[] programColumns = {"Program ID", "Program Name", "Year", "Description"};
        DefaultTableModel programModel = new DefaultTableModel(programColumns, 0);
        programsTable = new JTable(programModel);
        programsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        programsTable.setRowHeight(30);
        
        JScrollPane tableScrollPane = new JScrollPane(programsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Existing Programs"));
        
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshUsersButton = new JButton("Refresh Users");
        refreshUsersButton.setBackground(new Color(33, 150, 243));
        refreshUsersButton.setForeground(Color.WHITE);
        refreshUsersButton.addActionListener(e -> loadUsers());
        toolbarPanel.add(refreshUsersButton);
        
        // Users Table
        String[] userColumns = {"User ID", "Username", "User Type", "First Name", "Last Name", "Status"};
        DefaultTableModel userModel = new DefaultTableModel(userColumns, 0);
        usersTable = new JTable(userModel);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.setRowHeight(30);
        
        JScrollPane tableScrollPane = new JScrollPane(usersTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("User Management"));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateStatsButton = new JButton("Generate Statistics Report");
        generateStatsButton.setBackground(new Color(156, 39, 176));
        generateStatsButton.setForeground(Color.WHITE);
        generateStatsButton.addActionListener(e -> generateStatistics());
        toolbarPanel.add(generateStatsButton);
        
        // Statistics Text Area
        statsTextArea = new JTextArea();
        statsTextArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        statsTextArea.setEditable(false);
        statsTextArea.setText("System Statistics Report\n\n" +
                "• Total Students: --\n" +
                "• Total Teachers: --\n" +
                "• Total Programs: --\n" +
                "• Active Users: --\n" +
                "• Success Rate: --\n" +
                "• Failure Rate: --\n" +
                "• System Uptime: --\n\n" +
                "Click 'Generate Statistics Report' to load current data.");
        
        JScrollPane scrollPane = new JScrollPane(statsTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Statistics"));
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addProgram(ActionEvent e) {
        String name = programNameField.getText();
        String year = programYearField.getText();
        String desc = programDescField.getText();
        
        if (name.isEmpty() || year.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Call clientService to add program to database
            boolean success = clientService.addProgram(name, Integer.parseInt(year), desc);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Program added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                programNameField.setText("");
                programYearField.setText("");
                programDescField.setText("");
                
                // Refresh table
                loadPrograms();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add program", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid year", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding program: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadData() {
        loadPrograms();
        loadUsers();
    }
    
    private void loadPrograms() {
        try {
            // Get programs from database
            List<Map<String, String>> programs = clientService.getAllPrograms();
            DefaultTableModel model = (DefaultTableModel) programsTable.getModel();
            model.setRowCount(0);
            
            // Add data from database
            for (Map<String, String> program : programs) {
                model.addRow(new Object[]{
                    program.get("programId"),
                    program.get("programName"),
                    program.get("programYear"),
                    program.get("description")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUsers() {
        try {
            // Get users from database
            List<Map<String, String>> users = clientService.getAllUsers();
            DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
            model.setRowCount(0);
            
            // Add data from database
            for (Map<String, String> user : users) {
                model.addRow(new Object[]{
                    user.get("userId"),
                    user.get("username"),
                    user.get("userType"),
                    user.get("firstName"),
                    user.get("lastName"),
                    user.get("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateStatistics() {
        try {
            // Get statistics from database
            Map<String, Object> stats = clientService.getSystemStatistics();
            
            StringBuilder statsText = new StringBuilder();
            statsText.append("System Statistics Report - Generated on ").append(new java.util.Date()).append("\n\n");
            statsText.append("• Total Students: ").append(stats.get("totalStudents")).append("\n");
            statsText.append("• Total Teachers: ").append(stats.get("totalTeachers")).append("\n");
            statsText.append("• Total Programs: ").append(stats.get("totalPrograms")).append("\n");
            statsText.append("• Active Users: ").append(stats.get("activeUsers")).append("\n");
            statsText.append("• Success Rate: ").append(stats.get("successRate")).append("%\n");
            statsText.append("• Failure Rate: ").append(stats.get("failureRate")).append("%\n");
            statsText.append("• Average Grade: ").append(stats.get("averageGrade")).append("/20\n");
            statsText.append("• System Uptime: ").append(stats.get("systemUptime")).append("%\n\n");
            
            // Add detailed analysis if available
            if (stats.containsKey("detailedAnalysis")) {
                statsText.append("Detailed Analysis:\n").append(stats.get("detailedAnalysis"));
            }
            
            statsTextArea.setText(statsText.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating statistics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}