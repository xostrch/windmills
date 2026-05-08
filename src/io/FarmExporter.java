package io;

import interfaces.Exportable;
import models.LogEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FarmExporter {
    private void ensureDirectoryExists() {
        File dir = new File("data/exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    public void exportToCsv(List<LogEntry> logs, String path) throws IOException {
        ensureDirectoryExists();
        try(FileOutputStream fos = new FileOutputStream(path)){
            String header = Exportable.csvHeader() + "\n";
            fos.write(header.getBytes(StandardCharsets.UTF_8));

            for(LogEntry log : logs){
                String line = log.toCsv() + "\n";
                fos.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public void exportToJson(List<LogEntry> logs, String path) throws IOException{
        ensureDirectoryExists();
        try(FileOutputStream fos = new FileOutputStream(path)){
            fos.write("[\n".getBytes(StandardCharsets.UTF_8));

            for(int i = 0; i < logs.size(); i++){
                String jsonEntry = " " + logs.get(i).toJson();

                if(i < logs.size() - 1){
                    jsonEntry += ",";
                }
                jsonEntry += "\n";
                fos.write(jsonEntry.getBytes(StandardCharsets.UTF_8));
            }

            fos.write("]".getBytes(StandardCharsets.UTF_8));
        }
    }
}
