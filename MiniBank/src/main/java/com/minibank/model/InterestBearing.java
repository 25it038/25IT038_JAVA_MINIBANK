package com.minibank.model;

public interface InterestBearing {
    double interestRate();

    default double yearlyInterest() {
        return getBalance() * interestRate() / 100.0;
    }

    default String interestDescription() {
        return String.format("%.2f%% yearly interest", interestRate());
    }

    long getBalance();
}
