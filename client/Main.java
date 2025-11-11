import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ClientService clientService = new ClientService();
        
        // Try to connect (will work even without server)
        if (clientService.connect("localhost", 8080)) {
            System.out.println("Connected to server");
        } else {
            System.out.println("Server not available - running in local mode");
        }
        
        // Create and show login view
        LoginView loginView = new LoginView(clientService);
        loginView.setVisible(true);
    }
}