package io;

import models.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Scanner;

public class FarmDataReader implements java.lang.AutoCloseable {
    private final Scanner scanner;
    private final String path;

    public FarmDataReader(String path) throws IOException {
        this.path = path;
        File file = new File(path);

        if(!file.exists()){
            throw new IOException("Plik nie istnieje pod ścieżką: " + path);
        }
        this.scanner = new Scanner(file);
    }

    public void close(){
        if(scanner != null){
            scanner.close();
        }
    }

    public WindFarm readFarm(){
        String name = null;
        String operator = null;
        String location = null;
        WindFarm farm = null;

        while(scanner.hasNextLine()){
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("---")) continue;

            if(line.startsWith("name: ")){
                name = line.split(":")[1].trim();
            }else if(line.startsWith("operator: ")){
                operator = line.split(":")[1].trim();
            }else if(line.startsWith("location: ")){
                location = line.split(":")[1].trim();
            }

            if(farm == null && name != null && operator != null && location != null){
                farm = new WindFarm(name, operator, location);
            }

            if(farm != null && line.contains("|")){
                String[] p = line.split("\\|");
                if (p[0].contains("-")) {
                    try {
                        LogEntry entry = parseLogLine(p);
                        farm.addLog(entry);
                    } catch (Exception e) {
                        throw new WdfParseError("Błąd krytyczny w linii: " + line, e);
                    }
                }
            }
        }
        if (farm == null) {
            throw new WdfParseError("Nie znaleziono nagłówka #WINDFARM w pliku!");
        }
        return farm;
    }

    private LogEntry parseLogLine(String[] p){
        LocalDateTime ts = LocalDateTime.parse(p[0] + "T" + p[1]);
        String turbineId = p[2];
        String eventType = p[3];
        String operatorName = p[4];

        switch (eventType.toUpperCase()){
            case "ALARM":
                String severity = p[5];
                String alarmCode = p[6];
                SensorReading[] alarmSensors = parseSensor(p[7]);
                return new AlarmEntry(ts, turbineId, eventType, operatorName, alarmSensors, alarmCode, severity);
            case "MAINTENANCE":
                String type = p[5];
                double duration= Double.parseDouble(p[6]);
                SensorReading[] maintenanceSensors = parseSensor(p[7]);
                return new MaintenanceEntry(ts, turbineId, eventType, operatorName, maintenanceSensors, type, duration);
            default:
                SensorReading[] opSensors = parseSensor(p[5]);
                return new OperationalEntry(ts, turbineId, eventType, operatorName, opSensors);
        }

    }

    private SensorReading[] parseSensor(String data){
        if(data == null || data.isEmpty() || data.equals("-")){
            return new SensorReading[0];
        }
        String[] pairs = data.split(",");
        SensorReading[] readings = new SensorReading[pairs.length];

        for(int i = 0; i < pairs.length; i++){
            String[] p = pairs[i].split(":");
            readings[i] = new SensorReading(p[0], Double.parseDouble(p[1]));
        }
        return readings;
    }
}
