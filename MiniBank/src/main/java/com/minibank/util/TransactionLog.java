package com.minibank.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;

public final class TransactionLog {
    private TransactionLog() {}

    public static void append(Path file, String line) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
        String entry = LocalDateTime.now() + " " + line + System.lineSeparator();
        Files.writeString(file, entry, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
