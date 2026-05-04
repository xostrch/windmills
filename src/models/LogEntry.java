package models;

import interfaces.Exportable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public abstract class LogEntry implements Exportable {
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

    public abstract String describe();

    public String toJson(){
        return String.format(
                "{" +
                        "\"timestamp\":\"%s\"," +
                        "\"turbineId\":\"%s\"," +
                        "\"eventType\":\"%s\"," +
                        "\"operator\":\"%s\"," +
                        "\"details\":\"%s\"," +
                        "\"readings\":\"%s\"" +
                        "}",
                timestamp.toString(),
                turbineId,
                eventType,
                operatorName,
                describe().replace("\"", "\\\""),
                getReadingsAsString()
        );
    }

    public String toCsv() {
        DateTimeFormatter dF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter tF = DateTimeFormatter.ofPattern("HH:mm");

        return String.format("%s|%s|%s|%s|%s|%s|%s",
                timestamp.format(dF),
                timestamp.format(tF),
                turbineId,
                eventType,
                operatorName,
                describe(),
                getReadingsAsString()
        );
    }

    protected String getReadingsAsString(){
        SensorReading[] currentReadings = getReadings();

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < currentReadings.length; i++){
            sb.append(currentReadings[i].getSensorName())
                    .append(":")
                    .append(String.format(java.util.Locale.US,"%.3f", currentReadings[i].getValue()));
            if(i < currentReadings.length - 1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public final String format() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[%s] %-6s | %-12s | %-15s | %s",
                timestamp.format(formatter),
                turbineId,
                eventType,
                operatorName,
                describe()
        );
    }

    @Override
    public String toString(){
        return format();
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

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if (!(obj instanceof LogEntry)) {
            return false;
        }

        LogEntry other = (LogEntry) obj;
        return java.util.Objects.equals(timestamp, other.timestamp) &&
                java.util.Objects.equals(turbineId, other.turbineId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(timestamp, turbineId);
    }



}
