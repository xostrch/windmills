package functional;

import models.AlarmEntry;
import models.LogEntry;

import java.util.Comparator;
import java.util.List;

public class EntryComparators {
    private static final List<String> SEVERITIES = List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    public static Comparator<LogEntry> byDateTime(){
        return Comparator.comparing((LogEntry entry) -> entry.getTimestamp().toLocalDate())
                .thenComparing(entry -> entry.getTimestamp().toLocalDate());
    }

    public static Comparator<LogEntry> byTurbine(){
        return Comparator.comparing(LogEntry::getTurbineId, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LogEntry> byOperator(){
        return Comparator.comparing(LogEntry::getOperatorName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<LogEntry> bySeverity() {
        return Comparator.comparingInt((LogEntry entry) -> {
            if (entry instanceof AlarmEntry) {
                AlarmEntry alarm = (AlarmEntry) entry;
                return SEVERITIES.indexOf(alarm.getSeverity().toUpperCase());
            }
            return -1;
        });
    }

}
