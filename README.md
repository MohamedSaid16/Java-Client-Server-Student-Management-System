# Java-Client-Server-Student-Management-System
A comprehensive, multi-role Student Management System built with Java Swing for the client interface and Java Socket programming for client-server communication. This system provides a complete academic management solution with role-based access control.
# 🎓 Student Management System

A Java-based client-server application for managing academic information, built with Socket Programming and MySQL.
It supports multiple user roles with distinct permissions and provides a modular architecture for scalability and maintenance.

# 🚀 Features
## 🧩 Multi-User Role System

Admin — Full system administration
Student — View grades and academic programs
Teacher — Manage student grades and exams
Responsable — Handle administrative functions and reporting

# ⚙️ Core Functionalities

Secure user authentication & authorization
Student grade management
Program & course management
Exam scheduling and tracking
User registration system

# 🛠️ Technologies Used
Backend: Java (Socket Programming)
Database: MySQL
Build Tool: Maven
Architecture: Client–Server Model

# 📦 Installation & Setup
## 🔑 Prerequisites
Make sure you have the following installed:
Java JDK 8 or higher
MySQL Server
Maven

# 🗄️ Database Setup
Create a new database in MySQL.
Run the SQL script located at:
database/creation.sql

# 🧩 Project Structure
StudentManagement/
├── server/
│   ├── Server.java
│   └── pom.xml
├── client/
│   ├── Main.java
│   ├── controllers/
│   │   ├── LoginController.java
│   │   ├── StudentController.java
│   │   ├── TeacherController.java
│   │   ├── AdminController.java
│   │   └── ResponsableController.java
│   ├── views/
│   │   ├── LoginView.java
│   │   ├── StudentView.java
│   │   ├── TeacherView.java
│   │   ├── AdminView.java
│   │   └── ResponsableView.java
│   ├── models/
│   │   ├── User.java
│   │   ├── Student.java
│   │   ├── Grade.java
│   │   ├── Program.java
│   │   ├── Exam.java
│   │   └── Registration.java
│   ├── services/
│   │   └── ClientService.java
│   └── pom.xml
├── lib/
│   └── mysql-connector-java-8.0.33.jar
├── database/
│   └── creation.sql
└── scripts/
    ├── run-server.bat
    ├── run-client.bat
    └── compile-all.bat
# ▶️ How to Run
## server:
1-compile:
javac -cp ".;../lib/mysql-connector-java-8.0.33.jar" *.java
2-run:
java -cp ".;../lib/mysql-connector-java-8.0.33.jar" Server
3-clean calsses:
del *.class /s
## client:
1-compile:
javac -cp ".;../lib/mysql-connector-java-8.0.33.jar" ClientService.java views/*.java controllers/*.java models/*.java Main.java
2-run:
java -cp ".;../lib/mysql-connector-java-8.0.33.jar;services;views;controllers;models" Main
3-clean calsses:
del *.class /s
