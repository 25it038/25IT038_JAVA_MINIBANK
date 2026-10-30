# MiniBank – Java OOP Semester Project

**Course:** Object Oriented Programming (CEUE203)  
**Technology:** Java 17+, Maven, JUnit 5  
**Application type:** Console-based educational banking simulation

## Features

- Open Savings, Current and Fixed Deposit accounts
- Deposit, withdraw and transfer money
- View account details, statements and account listings
- Save account data between runs using Java serialization
- Append-only transaction logs and an end-of-day report
- Generic `Repository<T>` using reflection and the `@Id` annotation
- Collections (`HashMap`, `ArrayList`, `ConcurrentHashMap`), sorting and comparators
- Interfaces, abstract classes, inheritance, polymorphism, lambdas and marker interfaces
- Custom annotations and reflection-based validation
- Checked exception hierarchy
- Thread-safe account operations and a four-thread transaction processor
- JUnit 5 tests

> This is a classroom simulation. It does not connect to real banks, payment networks or real customer accounts. Do not enter real financial or personal information.

## Requirements

- JDK 17 or later
- Maven 3.8+ (or use an IDE with Maven support)

Check installation:

```bash
java -version
mvn -version
```

## Run in IntelliJ IDEA / Eclipse / VS Code

1. Extract the ZIP file.
2. Open the `MiniBank_Project` folder as a Maven project.
3. Wait for Maven dependencies to download.
4. Run `src/main/java/com/minibank/MiniBank.java`.

## Build and run from terminal

Open a terminal in the folder containing `pom.xml`:

```bash
mvn clean test package
java -jar target/minibank-1.0.0.jar
```

The runnable JAR is created at `target/minibank-1.0.0.jar`.

## Data files

The application creates these automatically:

- `data/accounts.dat` – saved account objects
- `data/logs/transactions.log` – transaction history
- `data/reports/daily-report.txt` – generated report

The `data/` folder is runtime data and should not be committed to Git.

## Suggested demo

1. Open a Savings account with an opening balance.
2. Deposit money.
3. Withdraw a valid amount.
4. Transfer money to another account.
5. View the statement and list of accounts.
6. Generate the report.
7. Exit and run the program again to confirm that saved accounts load.

## GitHub submission checklist

- Create a private repository named `oop-minibank-<your-roll-number>`.
- Add your faculty member as a collaborator, as instructed.
- Commit the source code, `pom.xml`, tests and README.
- Do not commit generated `data/` files or `target/`.
- Build and test before submission.

## Important

Replace the sample bank name/branch and add your own name, roll number and college details if your faculty requires them. Read and understand the code, run the test cases, and be ready to explain each class in your viva.
