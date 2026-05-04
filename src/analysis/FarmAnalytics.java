package analysis;

import models.LogEntry;
import models.WindFarm;

import java.time.LocalDate;
import java.util.*;

public class FarmAnalytics {

    public double getTotalPower(WindFarm farm) {
        double total = 0;
        for (LogEntry log : farm.getLogs()) {
            double power = log.computePowerOutput();
            if (power != -1.0) {
                total += power;
            }
        }
        return total;
    }

    public double getAveragePowerPerTurbine(WindFarm farm) {
        if (farm.turbineCount() == 0 || farm.logCount() == 0) {
            return -1.0;
        }
        double totalPower = getTotalPower(farm);

        return totalPower / farm.turbineCount();
    }


    public String[] getEventTypesReport(WindFarm farm) {
        Map<String, Integer> counts = new HashMap<>();
        for (String type : farm.getUniqueEventTypes()) {
            counts.put(type, farm.filterByEventType(type).logCount());
        }
        return sortMapToReport(counts);
    }

    public String[] getAlarmsPerTurbineReport(WindFarm farm) {
        Map<String, Integer> counts = new HashMap<>();
        for (String id : farm.getUniqueTurbineIds()) {
            WindFarm turbineLogs = farm.filterByTurbine(id);
            int alarmCount = 0;
            for (LogEntry log : turbineLogs.getLogs()) {
                if (log.isAlarm()) alarmCount++;
            }
            counts.put(id, alarmCount);
        }
        return sortMapToReport(counts);
    }

    private String[] sortMapToReport(Map<String, Integer> dataMap) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(dataMap.entrySet());

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        String[] report = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            report[i] = entry.getKey() + ":" + entry.getValue();
        }
        return report;
    }

    public String[] getMonthlyPowerReport(WindFarm farm){
        ArrayList<String> uniqueMonths = new ArrayList<>();
        for(LogEntry log : farm.getLogs()){
            String key = String.format("%04d-%02d",
                            log.getTimestamp().getYear(),
                            log.getTimestamp().getMonthValue());
            if(!uniqueMonths.contains(key)){
                uniqueMonths.add(key);
            }
        }
        Collections.sort(uniqueMonths);

        String[] raport = new String[uniqueMonths.size()];
        for(int i = 0; i < uniqueMonths.size(); i++){
            String monthKey = uniqueMonths.get(i);

            int year = Integer.parseInt(monthKey.substring(0,4));
            int month = Integer.parseInt(monthKey.substring(5,7));

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            WindFarm monthlyFarm = farm.filterByDateRange(start, end);
            double monthlyPower = getTotalPower(monthlyFarm);
            raport[i] = monthKey + ":" + String.format("%.2f kW", monthlyPower);
        }
        return raport;
    }

    public int[] getHourlyAlarmStats(WindFarm farm){
        int[] hourlyAlarms = new int[24];
        WindFarm alarmsOnly = farm.filterByEventType("ALARM");
        for(LogEntry log : alarmsOnly.getLogs()){
            String timeStr = String.format("%02d:%02d",
                            log.getTimestamp().getHour(),
                            log.getTimestamp().getMinute());
            String hourPart = timeStr.substring(0,2);
            int hour = Integer.parseInt(hourPart);

            hourlyAlarms[hour]++;
        }
        return hourlyAlarms;
    }

    public String getOperatorWithMaxLogs(WindFarm farm){
        String[] operators = farm.getUniqueOperators();
        if (operators.length == 0) return null;

        int maxLogs = -1;
        String maxLogsOperator = null;

        for(String op : operators){
            int currentCount = farm.filterByOperator(op).logCount();
            if(currentCount > maxLogs){
                maxLogs = currentCount;
                maxLogsOperator = op;
            }
        }
        return maxLogsOperator;
    }

    public String getTurbineWithMaxPower(WindFarm farm){
        String[] ids = farm.getUniqueTurbineIds();
        if(ids.length == 0) return null;

        double maxPower = -1.0;
        String maxPowerTurbine = null;

        for(String id : ids){
            WindFarm turbineFarm = farm.filterByTurbine(id);
            double currentPower = getTotalPower(turbineFarm);
            if(currentPower > maxPower){
                maxPower = currentPower;
                maxPowerTurbine = id;
            }
        }
        return maxPowerTurbine;
    }

    public String getMonthWithMaxPower(WindFarm farm){
        String[] monthlyReport = getMonthlyPowerReport(farm);
        if (monthlyReport.length == 0) return null;

        double maxPower = -1.0;
        String maxPowerMonth = null;

        for(String line : monthlyReport){
            String[] parts = line.split(":");
            String monthKey = parts[0];
            String monthValue = parts[1].replace(" kW", "").trim().replace(",", ".");
            double currentPower = Double.parseDouble(monthValue);

            if(currentPower > maxPower){
                maxPower = currentPower;
                maxPowerMonth = monthKey;
            }
        }
        return maxPowerMonth;
    }


}
