package models;

import interfaces.Inspectable;

import java.time.LocalDateTime;

public class AlarmEntry extends LogEntry implements Inspectable {
    private final int alarmCode;
    private final String severity;

    public AlarmEntry(LocalDateTime timestamp, String turbineId, String eventType, String operatorName, SensorReading[] readings, int alarmCode, String severity) {
        super(timestamp, turbineId, eventType, operatorName, readings);

        String[] validSeverities = {"LOW","MEDIUM","HIGH","CRITICAL"};
        boolean isValid = false;
        for(String s : validSeverities){
            if(s.equals(severity)){
                isValid = true;
            }
        }

        if(!isValid){
            throw new IllegalArgumentException("Nieprawidlowy poziom waznosci");
        }

        if(alarmCode < 0){
            throw new IllegalArgumentException("Kod alarmu nie moze byc ujemny");
        }

        this.alarmCode = alarmCode;
        this.severity = severity;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String describe(){
        return String.format("ALARM %d (POZIOM: %s)", alarmCode, severity);
    }

    @Override
    public String inspect(){
        return String.format("%s | Odczyty: %s", describe(), getReadingsAsString());
    }

    @Override
    public boolean isHealthy(){
        return !(severity.equals("HIGH")) && !(severity.equals("CRITICAL"));
    }

    @Override
    public String toCsv(){
        return String.format("%s|%d|%s", super.toCsv(), alarmCode, severity);
    }

    @Override
    public String toJson(){
        String baseJson = super.toJson();
        baseJson = baseJson.substring(0, baseJson.length() - 1);
        return String.format(
                "%s," +
                        "\"alarmCode\":%d," +
                        "\"severity\":\"%s\"" +
                        "}",baseJson, alarmCode, severity);
    }
}
