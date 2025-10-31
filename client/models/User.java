

public class User {
    private int userId;
    private String username;
    private String userType;
    private String firstName;
    private String lastName;
    private boolean active;

    public User(int userId, String username, String userType, String firstName, String lastName, boolean active) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getFullName() { 
        return firstName + " " + lastName; 
    }
}