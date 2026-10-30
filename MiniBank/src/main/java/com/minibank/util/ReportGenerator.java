package com.minibank.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class ReportGenerator {
    private ReportGenerator() {}

    public static String generate(Path logsFolder, Path reportFile) throws IOException {
        long deposits = 0;
        long withdrawals = 0;
        StringBuilder details = new StringBuilder();

        if (Files.exists(logsFolder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder, "*.log")) {
                for (Path file : stream) {
                    if (!Files.isRegularFile(file) || Files.size(file) == 0) continue;
                    try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.trim().split("\\\\s+");
                            if (parts.length < 4) continue;
                            try {
                                long amount = Long.parseLong(parts[parts.length - 1]);
                                if (line.contains(" DEPOSIT ")) deposits += amount;
                                if (line.contains(" WITHDRAW ")) withdrawals += amount;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    details.append(file.getFileName()).append(" | ")
                            .append(attrs.size()).append(" bytes | ")
                            .append(attrs.lastModifiedTime()).append(System.lineSeparator());
                }
            }
        }

        String report = "MINIBANK END-OF-DAY REPORT" + System.lineSeparator()
                + "Total deposits: " + deposits + System.lineSeparator()
                + "Total withdrawals: " + withdrawals + System.lineSeparator()
                + "Net change: " + (deposits - withdrawals) + System.lineSeparator()
                + "Log files:" + System.lineSeparator() + details;
        Path parent = reportFile.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.writeString(reportFile, report, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return report;
    }
}
