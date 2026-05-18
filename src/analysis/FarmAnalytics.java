package analysis;

import models.AlarmEntry;
import models.LogEntry;
import models.MaintenanceEntry;
import models.WindFarm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FarmAnalytics {

    public double getTotalPower(WindFarm farm){
        return farm.getLogs().stream()
                .mapToDouble(LogEntry::computePowerOutput)
                .filter(power -> power >= 0)
                .sum();
    }

    public double getAveragePowerPerTurbine(WindFarm farm){
        return farm.turbineCount() == 0 ? -1.0 : getTotalPower(farm) / farm.turbineCount();
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
                .collect(Collectors.groupingBy(LogEntry::getEventType, Collectors.counting()))
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
                .max(Map.Entry.comparingByKey())
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
        return "=====PODSUMOWANIE STANU TECHNICZNEGO=====\n" +
                String.format("Ogólna liczba wpisów: %d\n", farm.logCount()) +
                String.format("Liczba wszystkich alarmów: %d\n", getAllAlarms(farm).size()) +
                String.format("Liczba alarmów o wysokim priorytecie(HIGH/CRITICAL): %d\n",
                        countAlarmsBySeverity(farm).getOrDefault("HIGH", 0L) + countAlarmsBySeverity(farm).getOrDefault("CRITICAL", 0L)) +
                "---------------\n" +
                "ROZKŁAD ALARMÓW PER TURBINA\n" +
                (getAlarmsPerTurbineReport(farm).length == 0 ? "Brak zarejestrowanych alarmów dla turbin.\n" :
                        Arrays.stream(getAlarmsPerTurbineReport(farm))
                                .map(line -> String.format("Turbina: %s: %s alarmów", line.split(":")[0], line.split(":")[1]))
                                .collect(Collectors.joining("\n")) + "\n") +
                "====================";
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

}
