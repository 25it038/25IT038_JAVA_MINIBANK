package com.minibank;

import com.minibank.exception.BankException;
import com.minibank.exception.InsufficientFundsException;
import com.minibank.model.Account;
import com.minibank.model.SavingsAccount;
import com.minibank.repository.Repository;
import com.minibank.service.BankService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class BankServiceTest {
    @Test
    void depositIncreasesBalance() throws Exception {
        Repository<Account> repository = new Repository<>();
        BankService service = new BankService(repository, Path.of("target/test-logs/deposit.log"));
        Account account = service.openAccount("Riya", "current", 1000);
        service.deposit(account.getAccountNumber(), 500);
        assertEquals(1500, account.getBalance());
    }

    @Test
    void withdrawalAboveSavingsLimitThrowsException() throws Exception {
        Repository<Account> repository = new Repository<>();
        BankService service = new BankService(repository, Path.of("target/test-logs/withdraw.log"));
        Account account = service.openAccount("Riya", "savings", 1000);
        assertThrows(InsufficientFundsException.class,
                () -> service.withdraw(account.getAccountNumber(), 900));
        assertEquals(1000, account.getBalance());
    }

    @Test
    void concurrentDepositsKeepCorrectBalance() throws Exception {
        Account account = new SavingsAccount("Test User", 1000, 0);
        int threads = 10;
        int depositsPerThread = 1000;
        CountDownLatch start = new CountDownLatch(1);
        Thread[] workers = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < depositsPerThread; j++) account.deposit(1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[i].start();
        }

        start.countDown();
        for (Thread worker : workers) worker.join();
        assertEquals(1000L + (long) threads * depositsPerThread, account.getBalance());
    }
}
