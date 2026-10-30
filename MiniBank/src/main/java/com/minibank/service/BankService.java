package com.minibank.service;

import com.minibank.exception.*;
import com.minibank.model.*;
import com.minibank.repository.Repository;
import com.minibank.util.TransactionLog;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import static com.minibank.util.Validator.validate;

public class BankService {
    private final Repository<Account> repository;
    private final ConcurrentHashMap<String, Account> concurrentAccounts = new ConcurrentHashMap<>();
    private final AccountStore accountStore = new AccountStore();
    private final Path logFile;

    public BankService(Repository<Account> repository, Path logFile) {
        this.repository = repository;
        this.logFile = logFile;
        for (Account account : repository.findAll()) index(account);
    }

    private void index(Account account) {
        concurrentAccounts.put(account.getAccountNumber(), account);
        accountStore.add(account);
    }

    public Account openAccount(String owner, String type, long openingBalance) throws BankException {
        if (owner == null || owner.isBlank()) throw new InvalidAmountException("Owner name cannot be blank.");
        if (openingBalance <= 0) throw new InvalidAmountException("Opening balance must be greater than zero.");

        Account account = switch (type.toLowerCase()) {
            case "savings" -> new SavingsAccount(owner, openingBalance);
            case "current" -> new CurrentAccount(owner, openingBalance, 5000);
            case "fixed", "fixed deposit", "fd" -> new FixedDepositAccount(owner, openingBalance);
            default -> throw new InvalidAmountException("Unknown account type.");
        };

        String[] errors = validate(account);
        if (errors.length > 0) throw new InvalidAmountException(String.join(", ", errors));
        repository.save(account);
        index(account);
        log("OPEN " + account.getAccountNumber() + " " + openingBalance);
        return account;
    }

    public Account findAccount(String accountNumber) throws AccountNotFoundException {
        Account account = concurrentAccounts.get(accountNumber);
        if (account == null) throw new AccountNotFoundException(accountNumber);
        return account;
    }

    public void deposit(String accountNumber, long amount) throws BankException {
        Account account = findAccount(accountNumber);
        account.deposit(amount);
        log("DEPOSIT " + accountNumber + " " + amount);
        accountStore.addHistory("DEPOSIT " + accountNumber + " " + amount);
    }

    public void withdraw(String accountNumber, long amount) throws BankException {
        Account account = findAccount(accountNumber);
        account.withdraw(amount);
        log("WITHDRAW " + accountNumber + " " + amount);
        accountStore.addHistory("WITHDRAW " + accountNumber + " " + amount);
    }

    public void transfer(String fromNumber, String toNumber, long amount) throws BankException {
        if (amount <= 0) throw new InvalidAmountException("Transfer amount must be greater than zero.");
        Account from = findAccount(fromNumber);
        Account to = findAccount(toNumber);
        if (from == to) throw new InvalidAmountException("Source and destination must be different.");

        Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account second = first == from ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                try {
                    to.deposit(amount);
                } catch (BankException | RuntimeException ex) {
                    try {
                        from.deposit(amount);
                    } catch (BankException rollbackError) {
                        ex.addSuppressed(rollbackError);
                    }
                    if (ex instanceof BankException bankException) throw bankException;
                    throw ex;
                }
            }
        }
        log("TRANSFER " + fromNumber + " " + toNumber + " " + amount);
    }

    public String statement(String accountNumber) throws AccountNotFoundException {
        return findAccount(accountNumber).statementLine();
    }

    public ArrayList<Account> listAccounts() {
        ArrayList<Account> result = new ArrayList<>(concurrentAccounts.values());
        result.sort(Comparator.comparing(Account::getAccountNumber));
        return result;
    }

    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> result = new ArrayList<>(concurrentAccounts.values());
        result.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return result;
    }

    public void runBatchDeposits(String accountNumber, int count, long amount) throws BankException {
        Account account = findAccount(accountNumber);
        try (TransactionProcessor processor = new TransactionProcessor()) {
            TransactionBuffer buffer = new TransactionBuffer();
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < count; i++) {
                        buffer.put(() -> {
                            try {
                                account.deposit(amount);
                            } catch (BankException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    buffer.finish();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    buffer.finish();
                }
            }, "Transaction-Producer");

            Thread consumer = new Thread(() -> {
                try {
                    Runnable task;
                    while ((task = buffer.take()) != null) processor.submit(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Transaction-Consumer");

            producer.start();
            consumer.start();
            try {
                producer.join();
                consumer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BankException("Batch processing was interrupted.");
            }
        }
    }

    public int accountCount() { return concurrentAccounts.size(); }

    private void log(String line) throws BankException {
        try {
            TransactionLog.append(logFile, line);
        } catch (IOException e) {
            throw new BankException("Transaction completed but log could not be written: " + e.getMessage());
        }
    }
}
