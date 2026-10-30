package com.minibank;

import com.minibank.exception.BankException;
import com.minibank.model.*;
import com.minibank.repository.Repository;
import com.minibank.service.BankService;
import com.minibank.util.CloseableDemo;
import com.minibank.util.ReportGenerator;
import com.minibank.util.StatePersister;

import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class MiniBank {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path ACCOUNT_FILE = DATA_DIR.resolve("accounts.dat");
    private static final Path LOG_DIR = DATA_DIR.resolve("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("transactions.log");
    private static final Path REPORT_FILE = DATA_DIR.resolve("reports").resolve("daily-report.txt");

    public static void main(String[] args) {
        BankInfo bank = new BankInfo("CHARUSAT MiniBank", "Changa Branch");
        System.out.println("==================================");
        System.out.println(bank.name() + " - " + bank.branch());
        System.out.println("Educational banking simulation");
        System.out.println("==================================");

        Repository<Account> repository = new Repository<>();
        loadAccounts(repository);
        BankService service = new BankService(repository, LOG_FILE);

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt(sc, "Enter choice: ");
                MenuOption option = switch (choice) {
                    case 1 -> MenuOption.OPEN_ACCOUNT;
                    case 2 -> MenuOption.DEPOSIT;
                    case 3 -> MenuOption.WITHDRAW;
                    case 4 -> MenuOption.TRANSFER;
                    case 5 -> MenuOption.STATEMENT;
                    case 6 -> MenuOption.LIST_ACCOUNTS;
                    case 7 -> MenuOption.REPORT;
                    case 8 -> MenuOption.BATCH_DEMO;
                    case 9 -> MenuOption.EXIT;
                    default -> null;
                };

                if (option == null) {
                    System.out.println("Invalid choice. Please select 1-9.");
                    continue;
                }

                try {
                    switch (option) {
                        case OPEN_ACCOUNT -> openAccount(sc, service);
                        case DEPOSIT -> service.deposit(readText(sc, "Account number: "),
                                readLong(sc, "Amount: "));
                        case WITHDRAW -> service.withdraw(readText(sc, "Account number: "),
                                readLong(sc, "Amount: "));
                        case TRANSFER -> service.transfer(readText(sc, "From account: "),
                                readText(sc, "To account: "), readLong(sc, "Amount: "));
                        case STATEMENT -> System.out.println(service.statement(readText(sc, "Account number: ")));
                        case LIST_ACCOUNTS -> {
                            if (service.listAccounts().isEmpty()) System.out.println("No accounts found.");
                            else service.listAccounts().forEach(System.out::println);
                        }
                        case REPORT -> System.out.println(ReportGenerator.generate(LOG_DIR, REPORT_FILE));
                        case BATCH_DEMO -> {
                            String id = readText(sc, "Account number: ");
                            int count = readInt(sc, "Number of deposits (e.g. 100): ");
                            long amount = readLong(sc, "Amount per deposit: ");
                            Account selected = service.findAccount(id);
                            WithdrawRule anonymousRule = new WithdrawRule() {
                                @Override
                                public boolean allow(Account account, long value) {
                                    return value > 0 && account.canWithdraw(value);
                                }
                            };
                            WithdrawRule lambdaRule = (account, value) ->
                                    value > 0 && account.canWithdraw(value);
                            System.out.println("Anonymous WithdrawRule allows 1 rupee: "
                                    + anonymousRule.allow(selected, 1));
                            System.out.println("Lambda WithdrawRule allows 1 rupee: "
                                    + lambdaRule.allow(selected, 1));
                            service.runBatchDeposits(id, count, amount);
                            System.out.println("Batch completed. Current balance: " + selected.getBalance());
                        }
                        case EXIT -> {
                            saveAccounts(repository);
                            running = false;
                            System.out.println("Thank you for using MiniBank. Goodbye!");
                        }
                    }
                    if (running && option != MenuOption.LIST_ACCOUNTS
                            && option != MenuOption.STATEMENT && option != MenuOption.REPORT
                            && option != MenuOption.BATCH_DEMO && option != MenuOption.OPEN_ACCOUNT) {
                        System.out.println("Transaction successful.");
                    }
                } catch (BankException e) {
                    System.out.println("Operation failed: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("File operation failed: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.out.println("Please check the entered values: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Application error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n---------- MAIN MENU ----------");
        System.out.println("1. Open Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. View Statement");
        System.out.println("6. List Accounts");
        System.out.println("7. Generate Report");
        System.out.println("8. Batch Deposit Demo");
        System.out.println("9. Exit");
    }

    private static void openAccount(Scanner sc, BankService service) throws BankException {
        String name = readText(sc, "Customer name: ");
        String type = readText(sc, "Account type (savings/current/fixed): ");
        long balance = readLong(sc, "Opening balance: ");
        Account account = service.openAccount(name, type, balance);
        System.out.println("Account created successfully.");
        System.out.println(account);
    }

    private static void loadAccounts(Repository<Account> repository) {
        try {
            for (Account account : StatePersister.load(ACCOUNT_FILE)) {
                Account.synchronizeCounter(account.getAccountNumber());
                repository.save(account);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Starting with empty account storage: " + e.getMessage());
        }
    }

    private static void saveAccounts(Repository<Account> repository) throws IOException {
        StatePersister.save(repository.findAll().toArray(new Account[0]), ACCOUNT_FILE);
        System.out.println("Account data saved.");
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }

    private static long readLong(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Long.parseLong(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid amount."); }
        }
    }

    private static String readText(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
