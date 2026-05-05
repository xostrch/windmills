package models;

import interfaces.Inspectable;

import java.time.LocalDateTime;

public class AlarmEntry extends LogEntry implements Inspectable {
    private final String alarmCode;
    private final String severity;

    public AlarmEntry(LocalDateTime timestamp, String turbineId, String eventType, String operatorName, SensorReading[] readings, String alarmCode, String severity) {
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

        if(alarmCode == null || alarmCode.trim().isEmpty()){
            throw new IllegalArgumentException("Kod alarmu nie moze byc null ani pusty");
        }

        this.alarmCode = alarmCode;
        this.severity = severity;
    }

    public String getAlarmCode() {
        return alarmCode;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String describe(){
        return String.format("ALARM %s (POZIOM: %s)", alarmCode, severity);
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
        return String.format("%s|%s|%s", super.toCsv(), alarmCode, severity);
    }

    @Override
    public String toJson(){
        String baseJson = super.toJson();
        baseJson = baseJson.substring(0, baseJson.length() - 1);
        return String.format(
                "%s," +
                        "\"alarmCode\":\"%s\"," +
                        "\"severity\":\"%s\"" +
                        "}",baseJson, alarmCode, severity);
    }
}
