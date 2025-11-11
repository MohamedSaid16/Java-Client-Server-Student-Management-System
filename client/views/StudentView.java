import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.MessageFormat;
public class StudentView extends JFrame {
    private StudentController studentController;
    private Map<String, String> userInfo;
    
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel;
    private JLabel averageLabel;
    private JLabel statusLabel;
    private JTable gradesTable;
    private JTextArea infoTextArea;
    private JTable transcriptTable;
private JLabel transcriptAverageLabel;
private JLabel transcriptStatusLabel;
private JLabel transcriptRankLabel;
private JLabel transcriptCreditsLabel;
    public StudentView(StudentController studentController, Map<String, String> userInfo) {
        this.studentController = studentController;
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
        tabbedPane.addTab("📚 My Subjects", createSubjectsPanel()); 
        tabbedPane.addTab("📄 Relevé de Notes", createTranscriptTab());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
private JPanel createTranscriptTab() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    panel.setBackground(Color.WHITE);

    // Header with school information
    JPanel headerPanel = createTranscriptHeader();
    panel.add(headerPanel, BorderLayout.NORTH);

    // Toolbar
    JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    toolbarPanel.setBackground(Color.WHITE);
    toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

    JButton generateTranscriptButton = new JButton("📄 Générer Relevé de Notes");
    styleButton(generateTranscriptButton, new Color(33, 150, 243));
    generateTranscriptButton.addActionListener(e -> generateTranscript());

    JButton printButton = new JButton("🖨️ Imprimer");
    styleButton(printButton, new Color(76, 175, 80));
    printButton.addActionListener(e -> printTranscript());

    toolbarPanel.add(generateTranscriptButton);
    toolbarPanel.add(printButton);

    // Transcript Table - Simplified columns
    String[] columns = {"Matière", "Coefficient", "Moyenne", "Crédits", "Statut"};
    DefaultTableModel transcriptModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    };

    // Initialize the instance variable
    transcriptTable = new JTable(transcriptModel);
    transcriptTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    transcriptTable.setRowHeight(30);
    transcriptTable.setIntercellSpacing(new Dimension(10, 5));
    transcriptTable.setShowGrid(true);
    transcriptTable.setGridColor(new Color(240, 240, 240));

    // Center align all columns
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    for (int i = 0; i < transcriptTable.getColumnCount(); i++) {
        transcriptTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    // Set column widths
    transcriptTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Matière
    transcriptTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Coefficient
    transcriptTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Moyenne
    transcriptTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Crédits
    transcriptTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Statut

    JScrollPane tableScrollPane = new JScrollPane(transcriptTable);
    tableScrollPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
        "Relevé de Notes Détaillé",
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        new Font("Segoe UI", Font.BOLD, 14),
        new Color(41, 128, 185)
    ));
    tableScrollPane.setBackground(Color.WHITE);

    // Summary Panel - Improved design
    JPanel summaryPanel = createSummaryPanel();

    // Initialize the summary label instance variables
    Component[] summaryComponents = summaryPanel.getComponents();
    if (summaryComponents.length >= 4) {
        transcriptAverageLabel = (JLabel) summaryComponents[0];
        transcriptStatusLabel = (JLabel) summaryComponents[1];
        transcriptRankLabel = (JLabel) summaryComponents[2];
        transcriptCreditsLabel = (JLabel) summaryComponents[3];
    }

    // Main content panel
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.setBackground(Color.WHITE);
    contentPanel.add(toolbarPanel, BorderLayout.NORTH);
    contentPanel.add(tableScrollPane, BorderLayout.CENTER);
    contentPanel.add(summaryPanel, BorderLayout.SOUTH);

    panel.add(contentPanel, BorderLayout.CENTER);

    System.out.println("✅ Transcript tab initialized with table: " + transcriptTable);
    
    return panel;
}

private JPanel createTranscriptHeader() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(Color.WHITE);
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(41, 128, 185)),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    
    // School info
    JPanel schoolPanel = new JPanel();
    schoolPanel.setBackground(Color.WHITE);
    schoolPanel.setLayout(new BoxLayout(schoolPanel, BoxLayout.Y_AXIS));
    
    JLabel schoolLabel = new JLabel("UNIVERSITÉ IBN KHALDOUN TIARET");
    schoolLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    schoolLabel.setForeground(new Color(41, 128, 185));
    
    JLabel facultyLabel = new JLabel("Faculté des Mathématiques et Informatique");
    facultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    facultyLabel.setForeground(Color.DARK_GRAY);
    
    JLabel departmentLabel = new JLabel("Département d'Informatique");
    departmentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    departmentLabel.setForeground(Color.GRAY);
    
    schoolPanel.add(schoolLabel);
    schoolPanel.add(facultyLabel);
    schoolPanel.add(departmentLabel);
    
    // Document title
    JLabel titleLabel = new JLabel("RELEVÉ DE NOTES", JLabel.CENTER);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    titleLabel.setForeground(new Color(192, 57, 43));
    
    // Student info
    JPanel studentPanel = new JPanel();
    studentPanel.setBackground(Color.WHITE);
    studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
    studentPanel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
    
    JLabel studentNameLabel = new JLabel("Étudiant: " + userInfo.get("firstName") + " " + userInfo.get("lastName"));
    studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
    
    JLabel studentIdLabel = new JLabel("ID: " + userInfo.get("studentId"));
    studentIdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    
    JLabel yearLabel = new JLabel("Année Académique: 2024-2025");
    yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    
    studentPanel.add(studentNameLabel);
    studentPanel.add(studentIdLabel);
    studentPanel.add(yearLabel);
    
    headerPanel.add(schoolPanel, BorderLayout.WEST);
    headerPanel.add(titleLabel, BorderLayout.CENTER);
    headerPanel.add(studentPanel, BorderLayout.EAST);
    
    return headerPanel;
}

private JPanel createSummaryPanel() {
    JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
    summaryPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Résumé Académique"),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    summaryPanel.setBackground(Color.WHITE);
    
    JLabel averageLabel = createSummaryLabel("Moyenne Générale: --", new Color(41, 128, 185));
    JLabel statusLabel = createSummaryLabel("Statut: --", new Color(39, 174, 96));
    JLabel rankLabel = createSummaryLabel("Classement: --", new Color(155, 89, 182));
    JLabel creditsLabel = createSummaryLabel("Crédits Obtenus: --", new Color(230, 126, 34));
    
    summaryPanel.add(averageLabel);
    summaryPanel.add(statusLabel);
    summaryPanel.add(rankLabel);
    summaryPanel.add(creditsLabel);
    
    // Store references
    summaryPanel.putClientProperty("averageLabel", averageLabel);
    summaryPanel.putClientProperty("statusLabel", statusLabel);
    summaryPanel.putClientProperty("rankLabel", rankLabel);
    summaryPanel.putClientProperty("creditsLabel", creditsLabel);
    
    return summaryPanel;
}

private JLabel createSummaryLabel(String text, Color color) {
    JLabel label = new JLabel(text, JLabel.CENTER);
    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
    label.setForeground(Color.WHITE);
    label.setBackground(color);
    label.setOpaque(true);
    label.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(color.darker(), 1),
        BorderFactory.createEmptyBorder(8, 5, 8, 5)
    ));
    label.setPreferredSize(new Dimension(0, 50));
    return label;
}

private void styleButton(JButton button, Color color) {
    button.setBackground(color);
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Segoe UI", Font.BOLD, 12));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(color.darker()),
        BorderFactory.createEmptyBorder(8, 15, 8, 15)
    ));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Hover effects
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(color.brighter());
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(color);
        }
    });
}

private void generateTranscript() {
    try {
        int studentId = Integer.parseInt(userInfo.get("studentId"));
        
        // Show loading
        JOptionPane.showMessageDialog(this, "Génération du relevé de notes en cours...", 
                                    "Information", JOptionPane.INFORMATION_MESSAGE);
        
        // Get transcript data
        List<Map<String, String>> transcript = studentController.getStudentTranscript(studentId);
        
        if (transcript == null || transcript.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Aucune note disponible pour générer le relevé.", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        System.out.println("🔍 [DEBUG] Found " + transcript.size() + " grade records");
        
        // Check if transcript table is initialized
        if (transcriptTable == null) {
            System.err.println("❌ [DEBUG] transcriptTable instance variable is null");
            JOptionPane.showMessageDialog(this, 
                "Erreur: Tableau des notes non initialisé. Veuillez redémarrer l'application.", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("✅ [DEBUG] Labels found - Average: " + (transcriptAverageLabel != null) + 
                         ", Status: " + (transcriptStatusLabel != null) + 
                         ", Rank: " + (transcriptRankLabel != null) + 
                         ", Credits: " + (transcriptCreditsLabel != null));
        
        // Update table with only subject averages
        DefaultTableModel model = (DefaultTableModel) transcriptTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        double overallAverage = 0;
        int subjectCount = 0;
        int totalCredits = 0;
        
        // Filter only subject averages and count them
        for (Map<String, String> grade : transcript) {
            if ("MOYENNE MATIÈRE".equals(grade.get("exam"))) {
                subjectCount++;
            }
        }
        
        System.out.println("🔍 [DEBUG] Found " + subjectCount + " subject averages in transcript");
        
        // Add subject averages to table
        for (Map<String, String> grade : transcript) {
            if ("MOYENNE MATIÈRE".equals(grade.get("exam"))) {
                String subject = grade.get("subject");
                String coefficient = grade.get("coefficient");
                String score = grade.get("score");
                
                // Calculate credits (5 credits if passed, 0 if failed)
                double subjectAverage = Double.parseDouble(score);
                int credits = subjectAverage >= 10 ? 5 : 0;
                totalCredits += credits;
                
                // Determine status with emoji
                String status = subjectAverage >= 10 ? "✅ Validé" : "❌ Non Validé";
                
                model.addRow(new Object[]{
                    subject, 
                    coefficient, 
                    String.format("%.2f/20", subjectAverage),
                    String.valueOf(credits),
                    status
                });
                
                overallAverage += subjectAverage;
            }
        }
        
        // Update summary
        if (subjectCount > 0) {
            overallAverage = overallAverage / subjectCount;
            
            if (transcriptAverageLabel != null) {
                transcriptAverageLabel.setText(String.format("Moyenne: %.2f/20", overallAverage));
                
                // Color code the average label based on performance
                if (overallAverage >= 16) {
                    transcriptAverageLabel.setBackground(new Color(39, 174, 96)); // Green for excellent
                } else if (overallAverage >= 12) {
                    transcriptAverageLabel.setBackground(new Color(41, 128, 185)); // Blue for good
                } else if (overallAverage >= 10) {
                    transcriptAverageLabel.setBackground(new Color(230, 126, 34)); // Orange for passing
                } else {
                    transcriptAverageLabel.setBackground(new Color(231, 76, 60)); // Red for failing
                }
            }
            
            if (transcriptStatusLabel != null) {
                String overallStatus = overallAverage >= 10 ? "✅ Admis" : "❌ Redoublant";
                transcriptStatusLabel.setText("Statut: " + overallStatus);
            }
            
            if (transcriptRankLabel != null) {
                transcriptRankLabel.setText("Classement: En cours");
            }
            
            if (transcriptCreditsLabel != null) {
                transcriptCreditsLabel.setText("Crédits: " + totalCredits);
            }
            
            System.out.println("✅ [DEBUG] Transcript generated successfully");
            System.out.println("✅ [DEBUG] Overall average: " + overallAverage);
            System.out.println("✅ [DEBUG] Total credits: " + totalCredits);
        } else {
            System.out.println("❌ [DEBUG] No subject averages found in transcript data");
            if (transcriptAverageLabel != null) transcriptAverageLabel.setText("Moyenne: --");
            if (transcriptStatusLabel != null) transcriptStatusLabel.setText("Statut: --");
            if (transcriptRankLabel != null) transcriptRankLabel.setText("Classement: --");
            if (transcriptCreditsLabel != null) transcriptCreditsLabel.setText("Crédits: --");
        }
        
        JOptionPane.showMessageDialog(this, 
            "Relevé de notes généré avec succès!\n\n" +
            "Le relevé contient le détail de vos moyennes par matière.", 
            "Succès", JOptionPane.INFORMATION_MESSAGE);
            
    } catch (Exception e) {
        System.err.println("❌ [DEBUG] Error generating transcript: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Erreur lors de la génération du relevé: " + e.getMessage(), 
            "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}


// Helper method to find transcript table by searching the component tree
private JTable findTranscriptTable(Container container) {
    for (Component comp : container.getComponents()) {
        if (comp instanceof JTable) {
            return (JTable) comp;
        } else if (comp instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) comp;
            if (scrollPane.getViewport().getView() instanceof JTable) {
                return (JTable) scrollPane.getViewport().getView();
            }
        } else if (comp instanceof Container) {
            JTable found = findTranscriptTable((Container) comp);
            if (found != null) {
                return found;
            }
        }
    }
    return null;
}

private void printTranscript() {
    if (transcriptTable == null) {
        JOptionPane.showMessageDialog(this, 
            "Erreur: Tableau des notes non initialisé. Veuillez d'abord générer le relevé.", 
            "Erreur", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        // Create a professional header
        MessageFormat header = new MessageFormat("Relevé de Notes - " + 
            userInfo.get("firstName") + " " + userInfo.get("lastName") + 
            " (ID: " + userInfo.get("studentId") + ") - Page {0}");
        
        MessageFormat footer = new MessageFormat("Université XYZ - Faculté des Sciences - " + 
            new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        
        boolean complete = transcriptTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        
        if (complete) {
            JOptionPane.showMessageDialog(this, 
                "Impression du relevé de notes lancée avec succès.", 
                "Impression", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Impression annulée par l'utilisateur.", 
                "Impression", JOptionPane.WARNING_MESSAGE);
        }
    } catch (java.awt.print.PrinterException e) {
        JOptionPane.showMessageDialog(this, 
            "Erreur d'impression: " + e.getMessage(), 
            "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}



private String calculateCredits(List<Map<String, String>> transcript) {
    // Simple credit calculation based on passed subjects
    int credits = 0;
    for (Map<String, String> grade : transcript) {
        if ("MOYENNE MATIÈRE".equals(grade.get("exam"))) {
            double score = Double.parseDouble(grade.get("score"));
            if (score >= 10) {
                credits += 5; // 5 credits per passed subject
            }
        }
    }
    return String.valueOf(credits);
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
        
        // Load personal info from controller
        Map<String, String> studentInfo = studentController.getStudentInfo(studentId);
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
        
        // Load grades from controller
        List<Map<String, String>> grades = studentController.getStudentGrades(studentId);
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
        
        // Subjects are automatically loaded in createSubjectsPanel()
        // So no need to load them here separately
        
        // Load overall average from controller
        double average = studentController.getOverallAverage(studentId);
        averageLabel.setText(String.format("Overall Average: %.2f", average));
        
        // Load final status from controller - FIXED NULL CHECK
        String status = studentController.getFinalStatus(studentId);
        String statusText = "Status: " + 
            (status != null ? 
                (status.equals("ADMIS") ? "Admitted" : 
                 status.equals("REDOUBLANT") ? "Repeating" : 
                 status.equals("EXCLU") ? "Excluded" : "Unknown") 
                : "Not Set");
        statusLabel.setText(statusText);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void loadStudentStatistics(JTextArea statsTextArea) {
        try {
            int studentId = Integer.parseInt(userInfo.get("studentId"));
            
            // Get statistics from controller
            Map<String, Object> progress = studentController.getAcademicProgress(studentId);
            Map<String, Double> subjectAverages = studentController.getSubjectAverages(studentId);
            
            StringBuilder statsText = new StringBuilder();
            statsText.append("Academic Statistics\n\n");
            statsText.append("Overall Performance:\n");
            statsText.append("• Overall Average: ").append(progress.get("overallAverage")).append("/20\n");
            statsText.append("• Total Exams: ").append(progress.get("totalExams")).append("\n");
            statsText.append("• Exams Passed: ").append(progress.get("passedExams")).append("\n");
            statsText.append("• Success Rate: ").append(progress.get("progressPercentage")).append("%\n");
            statsText.append("• Academic Level: ").append(progress.get("academicLevel")).append("\n\n");
            
            statsText.append("Subject-wise Performance:\n");
            if (subjectAverages != null && !subjectAverages.isEmpty()) {
                for (Map.Entry<String, Double> entry : subjectAverages.entrySet()) {
                    statsText.append("• ").append(entry.getKey())
                            .append(": ").append(String.format("%.2f", entry.getValue()))
                            .append("/20\n");
                }
            } else {
                statsText.append("• No subject averages available\n");
            }
            
            statsTextArea.setText(statsText.toString());
        } catch (Exception e) {
            statsTextArea.setText("Error loading statistics: " + e.getMessage());
        }
    }
 private JPanel createSubjectsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    String[] columns = {"Subject", "Semester", "Coefficient", "Credits", "Volume Horaire"};
    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    JTable subjectsTable = new JTable(model);
    subjectsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    subjectsTable.setRowHeight(25);
    
    // Center align numeric columns
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    subjectsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Semester
    subjectsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Coefficient
    subjectsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Credits
    subjectsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Volume Horaire
    
    // Load subjects with debug information
    try {
        int studentId = Integer.parseInt(userInfo.get("studentId"));
        System.out.println("🔍 [DEBUG] Loading subjects for student ID: " + studentId);
        
        // First, let's check if the student has a program
        Map<String, String> studentInfo = studentController.getStudentInfo(studentId);
        if (studentInfo != null) {
            String programName = studentInfo.get("program");
            System.out.println("🔍 [DEBUG] Student program: " + programName);
            
            if (programName == null || programName.equals("Not assigned") || programName.equals("Unknown Program")) {
                System.out.println("❌ [DEBUG] Student is not assigned to any program!");
                model.addRow(new Object[]{"NOT ASSIGNED TO PROGRAM", "Contact administrator", "", "", ""});
            } else {
                System.out.println("✅ [DEBUG] Student is in program: " + programName);
                
                // Now load subjects
                List<Map<String, String>> subjects = studentController.getStudentSubjects(studentId);
                
                if (subjects != null && !subjects.isEmpty()) {
                    System.out.println("✅ [DEBUG] Found " + subjects.size() + " subjects");
                    for (Map<String, String> subject : subjects) {
                        System.out.println("   - " + subject.get("subjectName") + " (S" + subject.get("semester") + ")");
                        model.addRow(new Object[]{
                            subject.get("subjectName"),
                            subject.get("semester"),
                            subject.get("coefficient"),
                            subject.get("credits"),
                            subject.get("volumeHoraire") + "h"
                        });
                    }
                } else {
                    System.out.println("❌ [DEBUG] No subjects returned from controller");
                    model.addRow(new Object[]{"NO SUBJECTS FOUND", "Check program configuration", "", "", ""});
                }
            }
        } else {
            System.out.println("❌ [DEBUG] Could not load student info");
            model.addRow(new Object[]{"ERROR LOADING STUDENT INFO", "", "", "", ""});
        }
        
    } catch (Exception e) {
        System.err.println("❌ [DEBUG] Error loading subjects: " + e.getMessage());
        e.printStackTrace();
        model.addRow(new Object[]{"ERROR: " + e.getMessage(), "", "", "", ""});
    }
    
    JScrollPane scrollPane = new JScrollPane(subjectsTable);
    scrollPane.setBorder(BorderFactory.createTitledBorder("My Subjects"));
    
    // Add debug button
    JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton refreshButton = new JButton("Refresh Subjects");
    JButton debugButton = new JButton("Debug Info");
    
    refreshButton.addActionListener(e -> refreshSubjectsTable(model));
    debugButton.addActionListener(e -> showDebugInfo());
    
    toolbarPanel.add(refreshButton);
    toolbarPanel.add(debugButton);
    
    panel.add(toolbarPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    return panel;
}

private void showDebugInfo() {
    try {
        int studentId = Integer.parseInt(userInfo.get("studentId"));
        StringBuilder debugInfo = new StringBuilder();
        
        debugInfo.append("=== DEBUG INFORMATION ===\n\n");
        debugInfo.append("Student ID: ").append(studentId).append("\n");
        
        // Get student info
        Map<String, String> studentInfo = studentController.getStudentInfo(studentId);
        if (studentInfo != null) {
            debugInfo.append("First Name: ").append(studentInfo.get("firstName")).append("\n");
            debugInfo.append("Last Name: ").append(studentInfo.get("lastName")).append("\n");
            debugInfo.append("Program: ").append(studentInfo.get("program")).append("\n");
            debugInfo.append("Academic Year: ").append(studentInfo.get("academicYear")).append("\n");
        } else {
            debugInfo.append("❌ Could not load student info\n");
        }
        
        debugInfo.append("\n=== SUBJECTS ===\n");
        List<Map<String, String>> subjects = studentController.getStudentSubjects(studentId);
        if (subjects != null) {
            debugInfo.append("Number of subjects: ").append(subjects.size()).append("\n");
            for (Map<String, String> subject : subjects) {
                debugInfo.append(" - ").append(subject.get("subjectName"))
                         .append(" (S").append(subject.get("semester")).append(")\n");
            }
        } else {
            debugInfo.append("❌ Subjects list is null\n");
        }
        
        JTextArea textArea = new JTextArea(debugInfo.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Debug Information", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Debug error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void refreshSubjectsTable(DefaultTableModel model) {
    try {
        model.setRowCount(0); // Clear existing data
        
        int studentId = Integer.parseInt(userInfo.get("studentId"));
        List<Map<String, String>> subjects = studentController.getStudentSubjects(studentId);
        
        if (subjects != null && !subjects.isEmpty()) {
            for (Map<String, String> subject : subjects) {
                model.addRow(new Object[]{
                    subject.get("subjectName"),
                    subject.get("semester"),
                    subject.get("coefficient"),
                    subject.get("credits"),
                    subject.get("volumeHoraire") + "h"
                });
            }
            JOptionPane.showMessageDialog(this, "Subjects refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            model.addRow(new Object[]{"No subjects found", "", "", "", ""});
        }
    } catch (Exception e) {
        System.err.println("Error refreshing subjects: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Error refreshing subjects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}