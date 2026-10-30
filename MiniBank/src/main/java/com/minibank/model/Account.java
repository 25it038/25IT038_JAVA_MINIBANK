package com.minibank.model;

import com.minibank.exception.InsufficientFundsException;
import com.minibank.exception.InvalidAmountException;
import com.minibank.model.annotation.Id;
import com.minibank.model.annotation.Positive;
import java.io.Serializable;
import java.util.Objects;

public abstract class Account implements Transactable, InterestBearing,
        Comparable<Account>, Serializable {
    private static final long serialVersionUID = 1L;
    private static long accountCounter = 0;

    @Id
    private final String accountNumber;
    private String ownerName;
    @Positive
    private long balance;
    private boolean active;

    protected Account(String ownerName, long openingBalance) {
        if (openingBalance < 0) throw new IllegalArgumentException("Opening balance cannot be negative.");
        this.accountNumber = String.format("AC%04d", ++accountCounter);
        this.ownerName = Objects.requireNonNull(ownerName);
        this.balance = openingBalance;
        this.active = true;
    }

    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be greater than zero.");
        if (!active) throw new InvalidAmountException("Account is inactive.");
        balance = Math.addExact(balance, amount);
    }

    public synchronized boolean withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        if (!active) throw new InvalidAmountException("Account is inactive.");
        if (!canWithdraw(amount)) {
            long shortfall = Math.max(0, amount - balance);
            throw new InsufficientFundsException("Withdrawal is not allowed for this account.", shortfall);
        }
        balance -= amount;
        return true;
    }

    public abstract boolean canWithdraw(long amount);
    public abstract double interestRate();

    public synchronized long getBalance() { return balance; }
    protected synchronized void setBalanceForTransfer(long newBalance) { balance = newBalance; }
    public String getAccountNumber() { return accountNumber; }

    public static synchronized void synchronizeCounter(String existingAccountNumber) {
        if (existingAccountNumber != null && existingAccountNumber.startsWith("AC")) {
            try {
                long existing = Long.parseLong(existingAccountNumber.substring(2));
                accountCounter = Math.max(accountCounter, existing);
            } catch (NumberFormatException ignored) {
            }
        }
    }
    public String getOwnerName() { return ownerName; }
    public boolean isActive() { return active; }
    public void closeAccount() { active = false; }

    @Override
    public int compareTo(Account other) {
        return accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public String toString() {
        return accountNumber + " | " + ownerName + " | " + getClass().getSimpleName()
                + " | Balance: " + getBalance() + " | Active: " + active;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Account other)) return false;
        return accountNumber.equals(other.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }

    public String statementLine() {
        return "Account: " + accountNumber + System.lineSeparator()
                + "Owner: " + ownerName + System.lineSeparator()
                + "Type: " + getClass().getSimpleName() + System.lineSeparator()
                + "Balance: " + getBalance() + System.lineSeparator()
                + "Interest rate: " + interestRate() + "%";
    }
}
