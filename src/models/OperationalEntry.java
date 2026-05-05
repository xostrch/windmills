package models;

import java.time.LocalDateTime;

public class OperationalEntry extends LogEntry{
    public OperationalEntry(LocalDateTime timestamp, String turbineId, String eventType, String operatorName, SensorReading[] readings) {
        super(timestamp, turbineId, eventType, operatorName, readings);
        String[] allowedTypes = {"OPERATIONAL", "STARTUP", "SHUTDOWN"};
        boolean isAllowed = false;
        for(String type : allowedTypes){
            if(type.equals(eventType)){
                isAllowed = true;
                break;
            }
        }
        if(!isAllowed){
            throw new IllegalArgumentException("Nieprawidlowy typ zdarzenia");
        }
    }

    @Override
    public String describe() {
        double power = computePowerOutput();
        if (power >= 0) {
            return String.format("Praca standardowa - Moc: %.2f kW", power);
        }
        return "Praca standardowa (brak odczytów mocy)";
    }


}
