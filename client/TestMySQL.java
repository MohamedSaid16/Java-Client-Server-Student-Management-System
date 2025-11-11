import java.sql.*;

public class TestMySQL {
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...");
        
        try {
            // 1. Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(" Driver loaded");
            
            // 2. Try connection
            String url = "jdbc:mysql://localhost:3306/gestion_scolarite";
            String user = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println(" Connected to MySQL!");
            
            // 3. Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 as test");
            if (rs.next()) {
                System.out.println(" Query executed successfully");
            }
            
            conn.close();
            System.out.println("🎉 All tests passed! MySQL is working correctly.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver error: " + e.getMessage());
            System.err.println("💡 Check mysql-connector-java-8.0.33.jar in lib folder");
        } catch (SQLException e) {
            System.err.println("❌ MySQL connection error: " + e.getMessage());
            System.err.println("💡 Check:");
            System.err.println("   - Is XAMPP MySQL running?");
            System.err.println("   - Is database 'gestion_scolarite' created?");
            System.err.println("   - Username/password correct?");
        }
    }
}