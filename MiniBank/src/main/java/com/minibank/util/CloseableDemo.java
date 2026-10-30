package com.minibank.util;

public class CloseableDemo implements AutoCloseable {
    public CloseableDemo() {
        System.out.println("Resource opened.");
    }

    @Override
    public void close() {
        System.out.println("Resource closed.");
    }
}
