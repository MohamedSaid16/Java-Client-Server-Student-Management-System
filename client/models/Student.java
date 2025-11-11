public class Student {
    private int studentId;
    private String firstName;
    private String lastName;
    private String schoolOrigin;
    private String email;
    private String phone;

    public Student(int studentId, String firstName, String lastName, String schoolOrigin, String email, String phone) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolOrigin = schoolOrigin;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSchoolOrigin() { return schoolOrigin; }
    public void setSchoolOrigin(String schoolOrigin) { this.schoolOrigin = schoolOrigin; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}