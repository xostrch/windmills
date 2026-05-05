package models;

import java.time.LocalDateTime;

public class MaintenanceEntry extends LogEntry{
    private final String type;
    private final double duration;

    public MaintenanceEntry(LocalDateTime timestamp, String turbineId, String eventType, String operatorName, SensorReading[] readings, String type, double duration) {
        super(timestamp, turbineId, eventType, operatorName, readings);
            if(duration < 0){
                throw new IllegalArgumentException("Planowany czas trwania nie moze byc mniejszy niz 0");
            }

            String[] allTypes = {"PLANNED", "EMERGENCY", "INSPECTION"};
            boolean isType = false;
            for(String s : allTypes){
                if(s.equals(type)){
                    isType=true;
                    break;
                }
            }
            if(!isType){
                throw new IllegalArgumentException("Nieprawidlowy typ");
            }

            this.type = type;
            this.duration = duration;

    }

    @Override
    public String describe(){
        return String.format("KONSERWACJA: %s (%.1f h)", type, duration);
    }

    @Override
    public String toCsv(){
        return String.format("%s|%s|%f", super.toCsv(), type, duration);
    }

    @Override
    public String toJson(){
        String baseJson = super.toJson();
        baseJson = baseJson.substring(0, baseJson.length() - 1);
        return String.format(
                "%s," +
                        "\"type\":\"%s\"," +
                        "\"duration\":%.1f" +
                        "}", baseJson, type, duration);
    }




}
