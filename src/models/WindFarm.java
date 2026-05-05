package models;

import interfaces.Reportable;

import java.time.LocalDate;
import java.util.ArrayList;

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
        for(WindTurbine t : turbines){
            if(t.getTurbineId().equalsIgnoreCase(id)){
                return t;
            }
        }
        return null;
    }

    public String[] getUniqueTurbineIds(){
        ArrayList<String> tempUnique = new ArrayList<>();
        for(WindTurbine t : turbines){
            String id = t.getTurbineId();
            if(!tempUnique.contains(id)){
                tempUnique.add(id);
            }
        }
        String[] uniqueTurbineIds = new String[tempUnique.size()];
        return tempUnique.toArray(uniqueTurbineIds);
    }

    public String[] getUniqueOperators(){
        ArrayList<String> tempUnique = new ArrayList<>();
        for(LogEntry log : logs){
            String operator = log.getOperatorName().trim().toUpperCase();
            if(!tempUnique.contains(operator)){
                tempUnique.add(operator);
            }
        }
        String[] uniqueOperators = new String[tempUnique.size()];
        return tempUnique.toArray(uniqueOperators);
    }

    public String[] getUniqueEventTypes(){
        ArrayList<String> tempUnique = new ArrayList<>();
        for(LogEntry log : logs){
            String eventType = log.getEventType();
            if(!tempUnique.contains(eventType)){
                tempUnique.add(eventType);
            }
        }
        String[] uniqueEventTypes = new String[tempUnique.size()];
        return tempUnique.toArray(uniqueEventTypes);
    }

    public int logCount(){
        return logs.size();
    }

    public int turbineCount(){
        return turbines.size();
    }

    public WindFarm filterByTurbine(String id) {
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        for(LogEntry log : this.logs) {
            if(log.getTurbineId().trim().equalsIgnoreCase(id)){
                filteredFarm.logs.add(log);
            }
        }
        return filteredFarm;
    }

    public WindFarm filterByEventType(String eventType){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        for(LogEntry log : this.logs){
            if(log.getEventType().trim().equalsIgnoreCase(eventType)){
                filteredFarm.logs.add(log);
            }
        }
        return filteredFarm;
    }

    public WindFarm filterByOperator(String operator){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        for(LogEntry log : this.logs){
            if(log.getOperatorName().equalsIgnoreCase(operator)){
                filteredFarm.logs.add(log);
            }
        }
        return filteredFarm;
    }

    public WindFarm filterByDateRange(LocalDate start, LocalDate end){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        for(LogEntry log : this.logs){
            LocalDate logDate = log.getTimestamp().toLocalDate();
            if(!logDate.isBefore(start) && !logDate.isAfter(end)){
                filteredFarm.logs.add(log);
            }
        }
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

    @Override
    public String generateReport(){
        if(logs.isEmpty()){
            return String.format("Raport dla farmy: %s - (brak wpisów w systemie)", name);
        }

        java.time.LocalDateTime minDate = logs.get(0).getTimestamp();
        java.time.LocalDateTime maxDate = logs.get(0).getTimestamp();

        for (LogEntry log : logs) {
            if (log.getTimestamp().isBefore(minDate)) minDate = log.getTimestamp();
            if (log.getTimestamp().isAfter(maxDate)) maxDate = log.getTimestamp();
        }

        String[] eventTypes = getUniqueEventTypes();
        StringBuilder eventDistribution = new StringBuilder();
        for (String type : eventTypes) {
            int count = 0;
            for (LogEntry log : logs) {
                if (log.getEventType().equalsIgnoreCase(type)) {
                    count++;
                }
            }
            eventDistribution.append(String.format("   - %s: %d\n", type, count));
        }

        String[] operatorsList = getUniqueOperators();
        String operatorsAll = String.join(", ", operatorsList);

        StringBuilder report = new StringBuilder();
        report.append("==============\n");
        report.append(String.format("RAPORT FARMY: %s\n", name.toUpperCase()));
        report.append(String.format("LOKALIZACJA: %s\n", location));
        report.append(String.format("OPERATOR: %s\n", operator));
        report.append("--------------\n");
        report.append(String.format("Liczba turbin: %d\n", turbines.size()));
        report.append(String.format("Liczba wpisów: %d\n", logs.size()));
        report.append(String.format("Zakres danych: %s - %s\n", minDate.toLocalDate(), maxDate.toLocalDate()));
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
