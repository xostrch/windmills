package io;

import interfaces.Exportable;
import models.LogEntry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FarmExporter {
    private void ensureDirectoryExists() {
        File dir = new File("data/exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    public void exportToCsv(List<LogEntry> logs, String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        ensureDirectoryExists();

        String csvContent = logs.stream()
                .map(Exportable::toCsv)
                .collect(Collectors.joining(
                        "\n",
                        Exportable.csvHeader() + "\n",
                        ""
                ));

        Files.writeString(path, csvContent, StandardCharsets.UTF_8);
    }

    public void exportToJson(List<LogEntry> logs, String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        ensureDirectoryExists();

        String jsonContent = logs.stream()
                .map(Exportable::toJson)
                .collect(Collectors.joining(
                        ",\n  ",
                        "[\n  ",
                        "\n]"
                ));

        Files.writeString(path, jsonContent, StandardCharsets.UTF_8);
    }
}
