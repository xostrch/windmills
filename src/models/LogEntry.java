package models;

import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String turbineId;
    private final String eventType;
    private final String operatorName;
    private final SensorReading[] readings;


}
