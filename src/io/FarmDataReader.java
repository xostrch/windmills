package io;

import models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FarmDataReader implements java.lang.AutoCloseable {
    private final Path filePath;
    private final List<String> skippedLines = new ArrayList<>();

    private int currentSection = 0;
    private String farmName = null;
    private String farmOperator = null;
    private String farmLocation = null;

    public FarmDataReader(String path) throws IOException {
        this.filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("Plik nie istnieje pod ścieżką: " + path);
        }
    }

    public WindFarm readFarm() throws IOException {
        skippedLines.clear();
        currentSection = 0;
        farmName = null;
        farmOperator = null;
        farmLocation = null;

        WindFarm temporaryFarm = new WindFarm("", "", "");

        List<String> allLines = Files.readAllLines(filePath);

        IntStream.range(0, allLines.size()).forEach(i -> {
            int lineIdx = i + 1;
            String line = allLines.get(i).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                if (line.equalsIgnoreCase("#WINDFARM")) {
                    if (currentSection != 0) throw new WdfParseError("Zdublowana lub nieprawidłowa sekcja #WINDFARM w linii " + lineIdx);
                    currentSection = 1;
                } else if (line.equalsIgnoreCase("#TURBINES")) {
                    if (currentSection != 1) throw new WdfParseError("Nieprawidłowa kolejność sekcji. Oczekiwano #TURBINES po #WINDFARM w linii " + lineIdx);
                    if (farmName == null || farmOperator == null || farmLocation == null) {
                        throw new WdfParseError("Brak kompletnych metadanych farmy przed sekcją #TURBINES w linii " + lineIdx);
                    }
                    currentSection = 2;
                } else if (line.equalsIgnoreCase("#LOGS")) {
                    if (currentSection != 2) throw new WdfParseError("Nieprawidłowa kolejność sekcji. Oczekiwano #LOGS po #TURBINES w linii " + lineIdx);
                    currentSection = 3;
                }
                return;
            }

            if (line.startsWith("---")) {
                return;
            }

            if (currentSection == 1) {
                if (line.startsWith("name:")) farmName = line.substring(5).trim();
                else if (line.startsWith("operator:")) farmOperator = line.substring(9).trim();
                else if (line.startsWith("location:")) farmLocation = line.substring(9).trim();
            }

            else if (currentSection == 2) {
                try {
                    if (!line.contains("|")) throw new IllegalArgumentException("Brak separatora '|'");
                    String[] tokens = line.split("\\|", -1);
                    String tId = tokens[0].trim();
                    String tModel = tokens[1].trim();
                    int tPower = Integer.parseInt(tokens[2].trim());
                    int tHeight = Integer.parseInt(tokens[3].trim());

                    SensorReading[] sensors = new SensorReading[0];
                    if (tokens.length > 4 && !tokens[4].trim().isEmpty()) {
                        String[] sNames = tokens[4].split(",");
                        sensors = Stream.of(sNames)
                                .map(name -> new SensorReading(name.trim(), 0.0))
                                .toArray(SensorReading[]::new);
                    }
                    temporaryFarm.addTurbine(new WindTurbine(tId, tModel, tPower, tHeight, sensors));
                } catch (Exception e) {
                    throw new WdfParseError("Błąd krytyczny w sekcji #TURBINES (Linia " + lineIdx + "): " + e.getMessage());
                }
            }

            else if (currentSection == 3) {
                try {
                    if (!line.contains("|")) throw new IllegalArgumentException("Brak separatora '|'");
                    String[] tokens = line.split("\\|", -1);

                    String idTurbiny = tokens[2].trim();
                    if (temporaryFarm.getTurbineById(idTurbiny) == null) {
                        throw new IllegalArgumentException("Turbina o ID " + idTurbiny + " nie istnieje w rejestrze");
                    }

                    LogEntry entry = parseLogLine(tokens);
                    temporaryFarm.addLog(entry);
                } catch (Exception e) {
                    skippedLines.add("Linia " + lineIdx + ": " + e.getMessage());
                }
            }
        });

        if (currentSection < 3) {
            throw new WdfParseError("Plik WDF jest niekompletny. Nie wykryto wszystkich wymaganych sekcji!");
        }

        WindFarm finalFarm = new WindFarm(farmName, farmOperator, farmLocation);

        Stream.of(temporaryFarm.getUniqueTurbineIds())
                .map(temporaryFarm::getTurbineById)
                .forEach(finalFarm::addTurbine);

        temporaryFarm.getLogs().forEach(finalFarm::addLog);

        return finalFarm;
    }

    private LogEntry parseLogLine(String[] p) {
        LocalDateTime ts = LocalDateTime.parse(p[0].trim() + "T" + p[1].trim());
        String turbineId = p[2].trim();
        String eventType = p[3].trim().toUpperCase();
        String operatorName = p[4].trim();

        switch (eventType) {
            case "ALARM":
                if (p.length < 7) throw new IllegalArgumentException("Niewystarczająca liczba pól dla ALARM");
                String severity = p[5].trim().toUpperCase();
                if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(severity)) {
                    throw new IllegalArgumentException("Nieprawidłowy poziom alarmu: " + severity);
                }
                String alarmCode = p[6].trim();
                SensorReading[] alarmSensors = p.length > 7 ? parseSensors(p[7].trim()) : new SensorReading[0];
                return new AlarmEntry(ts, turbineId, eventType, operatorName, alarmSensors, alarmCode, severity);

            case "MAINTENANCE":
                if (p.length < 7) throw new IllegalArgumentException("Niewystarczająca liczba pól dla MAINTENANCE");
                String mType = p[5].trim().toUpperCase();
                if (!List.of("PLANNED", "EMERGENCY", "INSPECTION").contains(mType)) {
                    throw new IllegalArgumentException("Nieprawidłowy typ przeglądu: " + mType);
                }
                double duration = Double.parseDouble(p[6].trim());
                SensorReading[] maintenanceSensors = p.length > 7 ? parseSensors(p[7].trim()) : new SensorReading[0];
                return new MaintenanceEntry(ts, turbineId, eventType, operatorName, maintenanceSensors, mType, duration);

            case "OPERATIONAL":
            case "STARTUP":
            case "SHUTDOWN":
                if (p.length < 5) throw new IllegalArgumentException("Niewystarczająca liczba pól dla " + eventType);
                SensorReading[] opSensors = p.length > 5 ? parseSensors(p[5].trim()) : new SensorReading[0];
                return new OperationalEntry(ts, turbineId, eventType, operatorName, opSensors);

            default:
                throw new IllegalArgumentException("Nieznany typ zdarzenia: " + eventType);
        }
    }

    private SensorReading[] parseSensors(String data) {
        if (data == null || data.isEmpty() || data.equals("-")) {
            return new SensorReading[0];
        }
        String[] pairs = data.split(",");
        return Stream.of(pairs)
                .map(pair -> {
                    String[] splitPair = pair.split(":");
                    if (splitPair.length < 2) throw new IllegalArgumentException("Wadliwy format sensora: " + pair);
                    return new SensorReading(splitPair[0].trim(), Double.parseDouble(splitPair[1].trim()));
                })
                .toArray(SensorReading[]::new);
    }

    public List<String> getSkippedLines() {
        return this.skippedLines;
    }

    @Override
    public void close() {

    }
}