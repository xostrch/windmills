package io;

import interfaces.Exportable;
import models.LogEntry;
import models.WindFarm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FarmExporter {
    public void exportToCsv(WindFarm farm, String path) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(path)){
            String header = Exportable.csvHeader() + "\n";
            fos.write(header.getBytes(StandardCharsets.UTF_8));

            for(LogEntry log : farm.getLogs()){
                String line = log.toCsv() + "\n";
                fos.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public void exportToJson(WindFarm farm, String path) throws IOException{
        try(FileOutputStream fos = new FileOutputStream(path)){
            List<LogEntry> logs = farm.getLogs();

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
