package models;

import functional.EntryComparators;
import functional.EntryPredicates;
import interfaces.Reportable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class WindFarm implements Reportable {
    private final String name;
    private final String operator;
    private final String location;
    private final ArrayList<WindTurbine> turbines;
    private final ArrayList<LogEntry> logs;

    public WindFarm(String name, String operator, String location) {
        this.name = name;
        this.operator = operator;
        this.location = location;
        this.turbines = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getOperator() {
        return operator;
    }

    public String getLocation() {
        return location;
    }

    public WindTurbine getTurbineById(String id){
        return turbines.stream()
                .filter(t -> t.getTurbineId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public String[] getUniqueTurbineIds(){
        return turbines.stream()
                .map(WindTurbine::getTurbineId)
                .distinct()
                .toArray(String[]::new);
    }

    public String[] getUniqueOperators(){
        return logs.stream()
                .map(log -> log.getOperatorName().trim().toUpperCase())
                .distinct()
                .toArray(String[]::new);
    }

    public String[] getUniqueEventTypes(){
        return logs.stream()
                .map(LogEntry::getEventType)
                .distinct()
                .toArray(String[]::new);
    }

    public int logCount(){
        return logs.size();
    }

    public int turbineCount(){
        return turbines.size();
    }

    public WindFarm filterByTurbine(String id){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        filteredFarm.logs.addAll(this.logs.stream()
                .filter(EntryPredicates.byTurbine(id))
                .collect(toList()));

        return filteredFarm;
    }

    public WindFarm filterByEventType(String eventType){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        filteredFarm.logs.addAll(this.logs.stream()
                .filter(EntryPredicates.byType(eventType))
                .collect(toList()));
        return filteredFarm;
    }

    public WindFarm filterByOperator(String operator){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        filteredFarm.logs.addAll(this.logs.stream()
                .filter(EntryPredicates.byOperator(operator))
                .collect(toList()));
        return filteredFarm;
    }

    public WindFarm filterByDateRange(LocalDate start, LocalDate end){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        filteredFarm.logs.addAll(this.logs.stream()
                .filter(EntryPredicates.byDateRange(start, end))
                .collect(toList()));
        return filteredFarm;
    }

    public ArrayList<LogEntry> getLogs() {
        return new ArrayList<>(logs);
    }

    public void addLog(LogEntry entry) {
        if (entry != null) {
            this.logs.add(entry);
        }
    }

    public void addTurbine(WindTurbine turbine){
        if (turbine != null) {
            turbines.add(turbine);
        }
    }

    public Optional<WindTurbine> findTurbine(String id) {
        return turbines.stream()
                .filter(t -> t.getTurbineId().equalsIgnoreCase(id))
                .findFirst();
    }

    public Optional<LogEntry> findLatestEntry(String turbineId){
        return logs.stream()
                .filter(l -> l.getTurbineId().equalsIgnoreCase(turbineId))
                .max(EntryComparators.byDateTime());
    }

    public Optional<AlarmEntry> findFirstCriticalAlarm() {
        return logs.stream()
                .filter(e -> e instanceof AlarmEntry)
                .map(e -> (AlarmEntry) e)
                .filter(a -> a.getSeverity().equalsIgnoreCase("CRITICAL"))
                .findFirst();
    }

    @Override
    public String generateReport(){
        if(logs.isEmpty()){
            return String.format("Raport dla farmy: %s - (brak wpisów w systemie)", name);
        }

        LocalDate minDate = logs.stream()
                .map(log -> log.getTimestamp().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate maxDate = logs.stream()
                .map(log -> log.getTimestamp().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        String eventDistribution = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getEventType, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> String.format("   - %s: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));

        String operatorsAll = String.join(", ", getUniqueOperators());

        StringBuilder report = new StringBuilder();
        report.append("==============\n");
        report.append(String.format("RAPORT FARMY: %s\n", name.toUpperCase()));
        report.append(String.format("LOKALIZACJA: %s\n", location));
        report.append(String.format("OPERATOR: %s\n", operator));
        report.append("--------------\n");
        report.append(String.format("Liczba turbin: %d\n", turbines.size()));
        report.append(String.format("Liczba wpisów: %d\n", logs.size()));
        report.append(String.format("Zakres danych: %s - %s\n", minDate, maxDate));
        report.append("--------------\n");
        report.append("ROZKŁAD ZDARZEŃ:\n");
        report.append(eventDistribution);
        report.append("--------------\n");
        report.append("LISTA OPERATORÓW:\n");
        report.append(operatorsAll).append("\n");
        report.append("==============");
        return report.toString();

    }
}
