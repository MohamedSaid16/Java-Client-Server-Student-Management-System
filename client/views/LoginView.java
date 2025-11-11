import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LoginView extends JFrame {
    private ClientService clientService;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public LoginView(ClientService clientService) {
        this.clientService = clientService;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Student Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(74, 144, 226);
                Color color2 = new Color(41, 98, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(400, 450));
        
        // Header
        JLabel headerLabel = new JLabel("Student Management System", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 30, 20));
        
        // Login form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(userLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(passLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addActionListener(e -> handleLogin());
        formPanel.add(loginButton, gbc);
        
        // Test credentials info
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel testLabel = new JLabel("<html><center><font color='white'>Test: admin/password, prof1/password<br>ahmed/password, resp/password</font></center></html>", JLabel.CENTER);
        testLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        formPanel.add(testLabel, gbc);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    public void handleLogin() {
        String username = getUsername();
        String password = getPassword();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        // Show loading
        loginButton.setText("Logging in...");
        loginButton.setEnabled(false);
        
        // Perform login in background thread
        new Thread(() -> {
            try {
                // Use database authentication
                Map<String, String> userInfo = clientService.authenticateWithDatabase(username, password);
                
                SwingUtilities.invokeLater(() -> {
                    if (userInfo != null) {
                        navigateToDashboard(userInfo);
                    } else {
                        showError("Invalid username or password. Try:\n" +
                                 "• admin / password\n" +
                                 "• prof1 / password\n" + 
                                 "• ahmed / password\n" +
                                 "• resp / password");
                        loginButton.setText("Login");
                        loginButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Login error: " + e.getMessage());
                    loginButton.setText("Login");
                    loginButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    public String getUsername() {
        return usernameField.getText();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void navigateToDashboard(Map<String, String> userInfo) {
        String userType = userInfo.get("userType");
        
        System.out.println("Navigating to dashboard for: " + userType);
        System.out.println("User info: " + userInfo);
        
        switch (userType) {
            case "STUDENT":
                if (userInfo.get("studentId") == null) {
                    userInfo.put("studentId", "1001");
                }
                StudentController studentController = new StudentController();
                new StudentView(studentController, userInfo).setVisible(true);
                break;
                
            case "TEACHER":
                if (userInfo.get("teacherId") == null) {
                    userInfo.put("teacherId", "2001");
                }
                // Use ClientService directly since we don't have TeacherController
                new TeacherView(clientService, userInfo).setVisible(true);
                break;
                
            case "RESPONSABLE":
                if (userInfo.get("responsableId") == null) {
                    userInfo.put("responsableId", "3001");
                }
                ResponsableController responsableController = new ResponsableController();
                new ResponsableView(responsableController, userInfo).setVisible(true);
                break;
                
            case "ADMIN":
                AdminController adminController = new AdminController();
                new AdminView(adminController, userInfo).setVisible(true);
                break;
                
            default:
                showError("Unknown user type: " + userType);
                loginButton.setText("Login");
                loginButton.setEnabled(true);
                return;
        }
        this.dispose();
    }
}