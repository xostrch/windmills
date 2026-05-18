package analysis;

import models.AlarmEntry;
import models.LogEntry;
import models.MaintenanceEntry;
import models.WindFarm;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FarmAnalytics {

    public double getTotalPower(WindFarm farm){
        return farm.getLogs().stream()
                .mapToDouble(LogEntry::computePowerOutput)
                .filter(power -> power >= 0)
                .sum();
    }

    public double getAveragePowerPerTurbine(WindFarm farm){
        return java.util.stream.Stream.of(farm)
                .mapToDouble(f -> f.turbineCount() == 0 ? -1.0 : getTotalPower(f))
                .findFirst()
                .orElse(-1.0);
    }

    public String[] getEventTypesReport(WindFarm farm) {
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getEventType, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .toArray(String[]::new);
    }

    public String[] getAlarmsPerTurbineReport(WindFarm farm){
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getTurbineId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .toArray(String[]::new);
    }

    public String[] getMonthlyPowerReport(WindFarm farm){
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(
                        log -> String.format("%04d-%02d", log.getTimestamp().getYear(), log.getTimestamp().getMonthValue()),
                        Collectors.summingDouble(log -> Math.max(0.0, log.computePowerOutput()))

                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s:%.2f kW", entry.getKey(), entry.getValue()))
                .toArray(String[]::new);
    }

    public int[] getHourlyAlarmStats(WindFarm farm) {
        return farm.getLogs().stream()
                .filter(LogEntry::isAlarm)
                .collect(
                        () -> new int[24],
                        (array, log) -> array[log.getTimestamp().getHour()]++,
                        (array1, array2) -> java.util.stream.IntStream.range(0, 24)
                                .forEach(i -> array1[i] += array2[i])
                );
    }

    public String getOperatorWithMaxLogs(WindFarm farm) {
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getOperatorName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getTurbineWithMaxPower(WindFarm farm) {
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(
                        LogEntry::getTurbineId,
                        Collectors.summingDouble(log -> Math.max(0.0, log.computePowerOutput()))
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getMonthWithMaxPower(WindFarm farm) {
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(
                        log -> String.format("%04d-%02d", log.getTimestamp().getYear(), log.getTimestamp().getMonthValue()),
                        Collectors.summingDouble(log -> Math.max(0.0, log.computePowerOutput()))
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public List<AlarmEntry> getAllAlarms(WindFarm farm){
        return farm.getLogs().stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .collect(Collectors.toList());
    }


    public List<MaintenanceEntry> getAllMaintenances(WindFarm farm) {
        return farm.getLogs().stream()
                .filter(e -> e instanceof MaintenanceEntry)
                .map(e -> (MaintenanceEntry) e)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getAlarmSeverityDistribution(WindFarm farm) {
        return countAlarmsBySeverity(farm);
    }

    public List<AlarmEntry> getUnhealthyAlarms(WindFarm farm) {
        return farm.getLogs().stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .filter(alarm -> !alarm.isHealthy())
                .collect(Collectors.toList());
    }

    public String getTechnicalSummary(WindFarm farm) {
        return Stream.of(farm)
                .map(f -> "=====PODSUMOWANIE STANU TECHNICZNEGO=====\n" +
                        String.format("Ogólna liczba wpisów: %d\n", f.logCount()) +
                        String.format("Liczba wszystkich alarmów: %d\n", f.getLogs().stream().filter(LogEntry::isAlarm).count()) +
                        String.format("Liczba alarmów o wysokim priorytecie(HIGH/CRITICAL): %d\n",
                                f.getLogs().stream()
                                        .filter(e -> e instanceof AlarmEntry)
                                        .map(e -> (AlarmEntry) e)
                                        .filter(a -> a.getSeverity().equalsIgnoreCase("HIGH") || a.getSeverity().equalsIgnoreCase("CRITICAL"))
                                        .count()) +
                        "---------------\n" +
                        "ROZKŁAD ALARMÓW PER TURBINA\n" +
                        (f.getLogs().stream().filter(LogEntry::isAlarm).count() == 0 ? "Brak zarejestrowanych alarmów dla turbin.\n" :
                                f.getLogs().stream()
                                        .filter(LogEntry::isAlarm)
                                        .collect(Collectors.groupingBy(LogEntry::getTurbineId, Collectors.counting()))
                                        .entrySet().stream()
                                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                        .map(entry -> String.format("Turbina: %s: %d alarmów", entry.getKey(), entry.getValue()))
                                        .collect(Collectors.joining("\n")) + "\n") +
                        "====================")
                .findFirst()
                .orElse("");
    }

    public Map<String, Long> countByType(WindFarm farm) {
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getEventType, Collectors.counting()));
    }

    public Map<String, Long> countAlarmsBySeverity(WindFarm farm) {
        return farm.getLogs().stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .collect(Collectors.groupingBy(a -> a.getSeverity().toUpperCase(), Collectors.counting()));
    }

    public Map<String, List<LogEntry>> entriesByTurbine(WindFarm farm){
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getTurbineId));
    }

    public Map<Boolean, List<AlarmEntry>> partitionByHealth(WindFarm farm){
        return farm.getLogs().stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .collect(Collectors.partitioningBy(AlarmEntry::isHealthy));
    }

    public List<Map.Entry<String, Long>> topTurbinesByAlarmCount(WindFarm farm, int n){
        return farm.getLogs().stream()
                .filter(LogEntry::isAlarm)
                .collect(Collectors.groupingBy(LogEntry::getTurbineId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public OptionalDouble averagePower(WindFarm farm){
        return farm.getLogs().stream()
                .mapToDouble(LogEntry::computePowerOutput)
                .filter(power -> power >= 0)
                .average();
    }

    public double totalPower(WindFarm farm){
        return getTotalPower(farm);
    }

    public Set<String> uniqueOperators(WindFarm farm){
        return farm.getLogs().stream()
                .map(log -> log.getOperatorName().trim())
                .collect(Collectors.toSet());
    }

    public Map<String, Long> operatorsByActivity(WindFarm farm){
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(LogEntry::getOperatorName, Collectors.counting()));
    }

    public Map<String, Long> entriesPerMonth(WindFarm farm){
        return farm.getLogs().stream()
                .collect(Collectors.groupingBy(
                        log -> String.format("%04d-%02d", log.getTimestamp().getYear(), log.getTimestamp().getMonthValue()),
                        Collectors.counting()
                ));
    }

    public String alarmCodesSummary(WindFarm farm){
        return farm.getLogs().stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .map(AlarmEntry::getAlarmCode)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public long filterAndCount(WindFarm farm, Predicate<LogEntry> p){
        return farm.getLogs().stream()
                .filter(p)
                .count();
    }

    public List<LogEntry> findEntries(WindFarm farm, Predicate<LogEntry> p){
        return farm.getLogs().stream()
                .filter(p)
                .collect(Collectors.toList());
    }
}
