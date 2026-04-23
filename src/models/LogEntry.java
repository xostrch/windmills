package models;

import java.time.LocalDateTime;
import java.util.Arrays;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String turbineId;
    private final String eventType;
    private final String operatorName;
    private final SensorReading[] readings;

    public LogEntry(LocalDateTime timestamp, String turbineId, String eventType, String operatorName, SensorReading[] readings) {
        String[] allowedEventTypes = {"OPERATIONAL", "ALARM", "MAINTENANCE", "SHUTDOWN", "STARTUP"};
        boolean isValidEventType = false;
        for(int i = 0; i < allowedEventTypes.length; i++){
            if(eventType.equals(allowedEventTypes[i])){
                isValidEventType = true;
                break;
            }
        }
        if(!isValidEventType){
            throw new IllegalArgumentException("Nieprawidłowe zdarzenie");
        }

        this.timestamp = timestamp;
        this.turbineId = turbineId;
        this.eventType = eventType;
        this.operatorName = operatorName;
        this.readings = Arrays.copyOf(readings, readings.length);
    }

    public String getTurbineId() {
        return turbineId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public SensorReading[] getReadings() {
        return Arrays.copyOf(readings, readings.length);
    }

    public SensorReading getReadingByName(String name){
        for(int i = 0; i < readings.length; i++){
            if(readings[i].getSensorName().equalsIgnoreCase(name)){
                return readings[i];
            }
        }
        return null;
    }

    public double computePowerOutput(){
        SensorReading r = getReadingByName("POWER".toUpperCase());
        if(r != null){
            return r.getValue();
        }else{
            return -1.0;
        }
    }

    public boolean isAlarm(){
        return this.eventType.equals("ALARM");
    }

    public String toString(){
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[%s] %s | %s | %s | %d odczytów",
                timestamp.format(formatter),
                turbineId,
                eventType,
                operatorName,
                readings.length);
    }

}
