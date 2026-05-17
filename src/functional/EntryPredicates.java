package functional;

import models.AlarmEntry;
import models.LogEntry;
import models.MaintenanceEntry;
import models.OperationalEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

public class EntryPredicates {
    private EntryPredicates() {

    }

    public static Predicate<LogEntry> byTurbine(String turbineId){
        return entry -> entry.getTurbineId().equalsIgnoreCase(turbineId);
    }

    public static Predicate<LogEntry> byType(String type){
        return entry -> entry.getEventType().equalsIgnoreCase(type);
    }

    public static Predicate<LogEntry> byOperator(String operator){
        return entry -> entry.getOperatorName().equalsIgnoreCase(operator);
    }

    public static Predicate<LogEntry> byDateRange(LocalDate from, LocalDate to){
        return entry -> {
            LocalDate entryDate = entry.getTimestamp().toLocalDate();
            return (from == null || !entryDate.isBefore(from)) &&
                    (to == null || !entryDate.isAfter(to));
            };
    }

    public static Predicate<LogEntry> bySeverityAtLeast(String minSeverity){
        return entry -> {
            if(!(entry instanceof AlarmEntry)){
                return false;
            }
            AlarmEntry alarm = (AlarmEntry) entry;
            List<String> severityList = List.of("LOW","MEDIUM","HIGH","CRITICAL");
            int minIndex = severityList.indexOf(minSeverity.toUpperCase());
            int currentIndex = severityList.indexOf(alarm.getSeverity().toUpperCase());
            return currentIndex >= minIndex;
        };
    }

    public static Predicate<LogEntry> isAlarm(){
        return entry -> entry instanceof AlarmEntry;
    }

    public static Predicate<LogEntry> isMaintenance(){
        return entry -> entry instanceof MaintenanceEntry;
    }

    public static Predicate<LogEntry> isOperational(){
        return entry -> entry instanceof OperationalEntry;
    }

    public static Predicate<LogEntry> hasSensor(String sensorName){
        return entry -> entry.getReadingByName(sensorName) != null;
    }
}
