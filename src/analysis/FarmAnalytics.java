package analysis;

import models.LogEntry;
import models.WindFarm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

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
        String[] types = farm.getUniqueEventTypes();
        int[] counts = new int[types.length];

        for (int i = 0; i < types.length; i++) {
            counts[i] = farm.filterByEventType(types[i]).logCount();
        }
        for (int i = 0; i < counts.length - 1; i++) {
            for (int j = 0; j < counts.length - i - 1; j++) {
                if (counts[j] < counts[j + 1]) {
                    int tempCount = counts[j];
                    counts[j] = counts[j + 1];
                    counts[j + 1] = tempCount;

                    String tempType = types[j];
                    types[j] = types[j + 1];
                    types[j + 1] = tempType;
                }
            }
        }
        String[] raport = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            raport[i] = types[i] + ":" + counts[i];
        }
        return raport;
    }

    public String[] getAlarmsPerTurbineReport(WindFarm farm){
        String[] turbinesIds = farm.getUniqueTurbineIds();
        int[] counts = new int[turbinesIds.length];

        for(int i = 0; i < turbinesIds.length; i++){
            WindFarm turbineFarm = farm.filterByTurbine(turbinesIds[i]);
            int alarmCount = 0;

            for(LogEntry log : turbineFarm.getLogs()){
                if(log.isAlarm()){
                    alarmCount++;
                }
            }
            counts[i] = alarmCount;
        }

        for (int i = 0; i < counts.length - 1; i++) {
            for (int j = 0; j < counts.length - i - 1; j++) {
                if (counts[j] < counts[j + 1]) {
                    int tempC = counts[j];
                    counts[j] = counts[j + 1];
                    counts[j + 1] = tempC;

                    String tempId = turbinesIds[j];
                    turbinesIds[j] = turbinesIds[j + 1];
                    turbinesIds[j + 1] = tempId;
                }
            }
        }
        String[] raport = new String[turbinesIds.length];
        for(int i = 0; i < raport.length; i++){
            raport[i] = turbinesIds[i] + ":" + counts[i];
        }
        return raport;
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
