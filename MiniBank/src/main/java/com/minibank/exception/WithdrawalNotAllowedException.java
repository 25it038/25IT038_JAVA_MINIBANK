package com.minibank.exception;

public class WithdrawalNotAllowedException extends BankException {
    public WithdrawalNotAllowedException(String message) { super(message); }
}
