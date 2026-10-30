MiniBank – Java Banking Management System
Student Details
- ID: 25IT038
- Name: Janu Kaila
- College : CHARUSAT University
Project Overview
MiniBank is a console-based banking application developed using Java. It is an educational project that demonstrates object-oriented programming concepts and basic banking operations.

This application is a classroom simulation only. It does not connect to real banks or payment networks.
Technologies Used
- Java 17
- Maven
- JUnit 5
- Java Serialization for saving account data
Main Features
- Create Savings, Current, and Fixed Deposit accounts
- Deposit money into an account
- Withdraw money with validation
- Transfer money between accounts
- View account statements
- Display all available accounts
- Generate transaction reports
- Run batch deposit demonstrations
- Save account information between application runs
- Maintain transaction logs
Java Concepts Demonstrated
- Classes and objects
- Encapsulation, inheritance, and polymorphism
- Abstract classes and interfaces
- Collections and generics
- Exception handling and custom exceptions
- Lambda expressions
- Multithreading and thread-safe operations
- Custom annotations and reflection
- File handling and object serialization
- JUnit testing
Project Requirements
- JDK 17 or later
- Maven 3.8 or later
- Any Java IDE, such as IntelliJ IDEA, Eclipse, or VS Code
How to Run the Project
Using an IDE
1. Extract the project ZIP file.
2. Open the MiniBank folder as a Maven project.
3. Wait for Maven to download the required dependencies.
4. Run src/main/java/com/minibank/MiniBank.java.
Using the Terminal
Open a terminal in the project folder (the folder containing pom.xml) and run:
mvn clean test package
java -jar target/minibank-1.0.0.jar
Application Menu
The application provides these options:
1. Open Account
2. Deposit
3. Withdraw
4. Transfer
5. View Statement
6. List Accounts
7. Generate Report
8. Batch Deposit Demo
9. Exit
Generated Data Files
When the application runs, it creates the following files:
- data/accounts.dat – stores account information
- data/logs/transactions.log – stores transaction history
- data/reports/daily-report.txt – stores the generated report
Conclusion
The MiniBank project provides practical experience in developing a Java application using object-oriented programming, collections, exception handling, file handling, and multithreading. It simulates common banking operations in a simple console-based environment.
