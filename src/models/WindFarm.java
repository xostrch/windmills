package models;

import java.time.LocalDate;
import java.util.ArrayList;

public class WindFarm {
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

    public WindTurbine getTurbineById(String id){
        for(WindTurbine t : turbines){
            if(t.getTurbineId().equals(id)){
                return t;
            }
        }
        return null;
    }

    public String[] getUniqueTurbineIds(){
        ArrayList<String> tempUnique = new ArrayList<>();
        for(LogEntry log : logs){
            String id = log.getTurbineId();
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
            String operator = log.getOperatorName();
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
            if(log.getTurbineId().equalsIgnoreCase(id)){
                filteredFarm.logs.add(log);
            }
        }
        return filteredFarm;
    }

    public WindFarm filterByEventType(String eventType){
        WindFarm filteredFarm = new WindFarm(this.name, this.operator, this.location);
        filteredFarm.turbines.addAll(this.turbines);
        for(LogEntry log : this.logs){
            if(log.getEventType().equalsIgnoreCase(eventType)){
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
        return logs;
    }
}
