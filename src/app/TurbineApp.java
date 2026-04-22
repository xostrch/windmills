package app;

import analysis.FarmAnalytics;
import models.LogEntry;
import models.WindFarm;
import models.WindTurbine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class TurbineApp {
    private WindFarm farm;
    private FarmAnalytics analytics;
    private Scanner scanner;

    public TurbineApp(){
        //this.farm = DaneStudenta.utworzFarme();
        this.analytics = new FarmAnalytics();
        this.scanner = new Scanner(System.in);
    }

    public void run(){
        boolean running = true;
        while(running){
            String choice = scanner.nextLine();
            switch (choice){
                case "1": showFarmStatistics(); break;
                case "2": analyzeTurbine(); break;
                case "3": browseEvents(); break;
                case "4": ;
                case "5": ;
            }
        }
    }

    public void showFarmStatistics(){
        System.out.println("=======STATYSTYKI FARMY=======");
        printGeneralPowerStats();
        printTurbinesRanking();
        printEventTypesReport();
        printMonthlyReport();
        drawAlarmChart();
    }

    public void printGeneralPowerStats(){
        System.out.println("===GENERALNE STATYSTYKI===");
        double farmPowerTotal = analytics.getTotalPower(farm);
        String topOperator = analytics.getOperatorWithMaxLogs(farm);
        System.out.println("Łączna moc: " + farmPowerTotal);
        System.out.println("Operator z największą liczbą wpisów: " + topOperator);
    }

    public void printTurbinesRanking(){
        String[] turbineIds = farm.getUniqueTurbineIds();
        System.out.println("===RANKING TURBIN: ===");
        double farmPowerTotal = analytics.getTotalPower(farm);
        for(String id : turbineIds){
            WindTurbine turbine = farm.getTurbineById(id);
            double turbinePower = analytics.getTotalPower(farm.filterByTurbine(id));
            double percentageShare = (turbinePower / farmPowerTotal) * 100;
            String turbineDesc = turbine.toString();
            System.out.printf("\n%s | Moc: %f kW | Udział: %.2f procent", turbineDesc, turbinePower, percentageShare);
        }
    }

    public void printEventTypesReport(){
        System.out.println("===LICZBA WPISÓW PER ZDARZENIE===");
        String[] report = analytics.getEventTypesReport(farm);

        if (report.length == 0) {
            System.out.println("Brak danych o zdarzeniach.");
            return;
        }

        for(String line : report){
            String[] parts = line.split(":");
            String type = parts[0];
            String count = parts[1];
            System.out.println(String.format("Typ: %s | Liczba: %s", type, count));
        }
    }

    public void printMonthlyReport(){
        System.out.println("===ZESTAWIENIE MIESIĘCZNE===");
        String[] report = analytics.getMonthlyPowerReport(farm);

        if(report.length == 0){
            System.out.println("Brak danych miesięcznych.");
            return;
        }

        for(String line : report){
            String[] parts = line.split(":");
            String month = parts[0];
            String power = parts[1];
            System.out.println(String.format("Miesiąc: %s | Moc: %s", month, power));
        }
    }

    public void drawAlarmChart(){
        System.out.println("===ROZKŁAD ALARMÓW W CIĄGU DOBY===");
        int[] hourlyStats = analytics.getHourlyAlarmStats(farm);
        int maxAlarmsInHour = 0;
        for(int count : hourlyStats){
            if(count > maxAlarmsInHour){
                maxAlarmsInHour = count;
            }
        }

        for(int hour = 0; hour < 24; hour++){
            int currentCount = hourlyStats[hour];
            String bar = "";

            if(currentCount > 0 && maxAlarmsInHour > 0){
                int starsAmount = (currentCount * 20) / maxAlarmsInHour;

                for (int i = 0; i < starsAmount; i++) {
                    bar += "*";
                }

                System.out.println(String.format("%02d:00 | %-20s | %d", hour, bar, currentCount));
            }else{
                System.out.println(String.format("%02d:00 | %-20s | 0", hour, ""));
            }
        }
    }

    public void analyzeTurbine(){
        System.out.println("=======ANALIZA TURBINY=======");
        printAvailableTurbines();

        boolean idValid = false;
        String selectedId = "";
        while(!idValid){
            System.out.print("Podaj ID turbiny(czyli [id]): ");
            selectedId = scanner.nextLine().trim();
            if (farm.getTurbineById(selectedId) != null) {
                idValid = true;
            }else{
                System.out.println("Błąd: Nie znaleziono turbiny o ID: " + selectedId);
            }
        }
        printTurbineData(selectedId);
        printTurbineLogStats(selectedId);
        printMonthlyPowerPerTurbine(selectedId);
        printPaginatedLogs(selectedId);
    }

    public void printAvailableTurbines(){
        System.out.println("===WSZYSTKIE TURBINY===");
        String[] availableIds = farm.getUniqueTurbineIds();
        for(String id : availableIds){
            WindTurbine turbine = farm.getTurbineById(id);
            if (turbine != null) {
                System.out.println(String.format("[%s] Model: %s", id, turbine.getModel()));
            }else{
                System.out.println(String.format("[%s]", id));
            }
        }

    }

    public void printTurbineData(String selectedId){
        System.out.println("===DANE TECHNICZNE TURBINY===");
        WindTurbine turbine = farm.getTurbineById(selectedId);
        System.out.println("[ID] Model | Moc | Wysokość | Liczba sensorów");
        System.out.println(turbine.toString());
    }

    public void printTurbineLogStats(String selectedId){
        WindFarm filtered = farm.filterByTurbine(selectedId);
        ArrayList<LogEntry> logs = filtered.getLogs();
        int alarmCount = 0;
        int maintenanceCount = 0;

        for (LogEntry log : logs) {
            if (log.isAlarm()) {
                alarmCount++;
            }
            else if (log.getEventType().equalsIgnoreCase("MAINTENANCE")) {
                maintenanceCount++;
            }
        }
        System.out.println("=== STATYSTYKI WPISÓW ===");
        System.out.println("Łączna liczba wpisów: " + logs.size());
        System.out.println("Liczba ALARM:         " + alarmCount);
        System.out.println("Liczba MAINTENANCE:   " + maintenanceCount);
    }

    public void printMonthlyPowerPerTurbine(String selectedId){
        System.out.println("=== ZESTAWIENIE MIESIĘCZNE MOCY ===");
        WindFarm turbineData = farm.filterByTurbine(selectedId);
        String[] report = analytics.getMonthlyPowerReport(turbineData);
        if (report.length > 0) {
            for (String line : report) {
                System.out.println(line);
            }
        } else {
            System.out.println("Brak danych MOCY dla tej turbiny.");
        }
    }

    public void printPaginatedLogs(String selectedId){
        ArrayList<LogEntry> logs = farm.filterByTurbine(selectedId).getLogs();
        if (logs.isEmpty()) {
            System.out.println("Brak wpisów.");
            return;
        }
        System.out.println("=== LISTA WPISÓW (po 20) ===");
        for(int i = 0; i < logs.size(); i++){
            System.out.println((i + 1) + ". " + logs.get(i).toString());
            if ((i + 1) % 20 == 0 && i != logs.size() - 1) {
                System.out.println("--- Wyświetlono 20 wpisów. Czy pokazać kolejne 20? (t/n): ");
                String decision = scanner.nextLine().toLowerCase();
                if (!decision.equals("t")) {
                    break;
                }
            }
        }
        System.out.println("--- Koniec listy ---");
    }

    public void browseEvents(){
        System.out.println("======= PRZEGLĄDAJ ZDARZENIA =======");
        System.out.println("1. Filtruj według turbiny");
        System.out.println("2. Filtruj według typu zdarzenia");
        System.out.println("3. Filtruj według operatora");
        System.out.println("4. Filtruj według zakresu dat");
        System.out.print("Wybór: ");
        String choice = scanner.nextLine();
        WindFarm filtered = null;

        switch (choice){
            case "1":
                System.out.print("Podaj ID turbiny: ");
                filtered = farm.filterByTurbine(scanner.nextLine().trim());
                break;
            case "2":
                System.out.print("Podaj typ zdarzenia: ");
                filtered = farm.filterByEventType(scanner.nextLine().trim().toUpperCase());
                break;
            case "3":
                System.out.print("Podaj imię i nazwisko operatora: ");
                filtered = farm.filterByOperator(scanner.nextLine());
                break;
            case "4":
                filtered = askForDate();
                break;
            default:
                System.out.println("Nieprawidłowy wybór.");
                return;
        }

    }

    //DO ZROBIENIA
    /*public WindFarm askForDate(){
        LocalDate start = null;
        LocalDate end = null;

        boolean validStart = false;
        start = LocalDate.parse(scanner.nextLine().trim());

    }*/

}
