package app;

import analysis.FarmAnalytics;
import data.DaneStudenta;
import models.LogEntry;
import models.SensorReading;
import models.WindFarm;
import models.WindTurbine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class TurbineApp {
    private WindFarm farm;
    private FarmAnalytics analytics;
    private Scanner scanner;

    public TurbineApp(){
        this.farm = DaneStudenta.utworzFarme();
        this.analytics = new FarmAnalytics();
        this.scanner = new Scanner(System.in);
    }

    public void run(){
        boolean running = true;
        while(running){
            System.out.println("========================");
            System.out.println("--- MENU GŁÓWNE ---");
            System.out.println("========================");
            System.out.println("| 1. Statystyki farmy |");
            System.out.println("| 2. Analiza turbiny |");
            System.out.println("| 3. Przeglądaj zdarzenie |");
            System.out.println("| 4. Dodaj wpis |");
            System.out.println("| 5. Informacje o farmie |");
            System.out.println("| 0. Wyjście |");

            String choice = scanner.nextLine();
            switch (choice){
                case "1": showFarmStatistics(); break;
                case "2": analyzeTurbine(); break;
                case "3": browseEvents(); break;
                case "4": addLogEntry(); break;
                case "5": showFarmInfo(); break;
                case "0": running = false; break;
                default:
                    System.out.println("Niepoprawny wybór. Wpisz cyfre od 0 do 5");
            }
        }
    }

    public void showFarmStatistics(){
        System.out.println("=======STATYSTYKI FARMY=======");
        if (farm == null || farm.logCount() == 0) {
            System.out.println("Błąd: Brak danych na farmie do wygenerowania statystyk.");
            return;
        }
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

        if (topOperator != null) {
            System.out.println("Operator z największą liczbą wpisów: " + topOperator);
        } else {
            System.out.println("Operator z największą liczbą wpisów: [Brak danych]");
        }
    }

    public void printTurbinesRanking(){
        String[] turbineIds = farm.getUniqueTurbineIds();
        System.out.println("===RANKING TURBIN: ===");
        double farmPowerTotal = analytics.getTotalPower(farm);
        if (farmPowerTotal == 0) return;
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

        if (maxAlarmsInHour == 0) {
            System.out.println("Brak alarmów w historii farmy.");
            return;
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

        String yes = "TAK";
        String no = "NO";

        System.out.println("Wyposażenie dodatkowe:");
        if(turbine.hasSensor("POWER")){
            System.out.println("- Miernik mocy (POWER): " + yes);
        }else{
            System.out.println("- Miernik mocy (POWER): " + no);
        }
        if(turbine.hasSensor("WIND_SPEED")){
            System.out.println("- Czujnik wiatru (WIND_SPEED): " + yes);
        }else{
            System.out.println("- Czujnik wiatru (WIND_SPEED): " + no);
        }
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
        WindFarm filtered = null;
        boolean selectionValid = false;

        while (!selectionValid) {
            System.out.println("1. Filtruj według turbiny");
            System.out.println("2. Filtruj według typu zdarzenia");
            System.out.println("3. Filtruj według operatora");
            System.out.println("4. Filtruj według zakresu dat");
            System.out.print("Wybór: ");

            String choice = scanner.nextLine().trim();

            switch (choice){
                case "1":
                    System.out.print("Podaj ID turbiny: ");
                    filtered = farm.filterByTurbine(scanner.nextLine().trim());
                    selectionValid = true;
                    break;
                case "2":
                    System.out.print("Podaj typ zdarzenia: ");
                    filtered = farm.filterByEventType(scanner.nextLine().trim().toUpperCase());
                    selectionValid = true;
                    break;
                case "3":
                    System.out.print("Podaj imię i nazwisko operatora: ");
                    filtered = farm.filterByOperator(scanner.nextLine());
                    selectionValid = true;
                    break;
                case "4":
                    filtered = askForDate();
                    selectionValid = true;
                    break;
                default:
                    System.out.println("Błąd: Nieprawidłowy wybór ('" + choice + "'). Wybierz cyfrę od 1 do 4.");
            }
        }

            if(filtered != null){
                displayFilteredResults(filtered);
            }
    }

    public WindFarm askForDate(){
        DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate start = null;
        LocalDate end = null;

        while(start == null){
            System.out.println("Podaj date początkową: ");
            String input = scanner.nextLine().trim();
            try{
                start = LocalDate.parse(input, dtf);
            }catch(java.time.format.DateTimeParseException e) {
                System.out.println("Błąd: '" + input + "' to nie jest poprawna data. Spróbuj ponownie.");
            }
        }

        while(end == null){
            System.out.println("Podaj date końcową: ");
            String input = scanner.nextLine().trim();
            try{
                java.time.LocalDate tempEnd = LocalDate.parse(input, dtf);
                if (!tempEnd.isBefore(start)) {
                    end = tempEnd;
                } else {
                    System.out.println("Błąd: Data końcowa nie może być wcześniejsza niż " + start.format((dtf)) + "!");
                }
            }catch(java.time.format.DateTimeParseException e) {
                System.out.println("Błąd: '" + input + "' to nie jest poprawna data. Spróbuj ponownie.");
            }
        }
        return farm.filterByDateRange(start, end);
    }

    public void displayFilteredResults(WindFarm filtered){
        ArrayList<LogEntry> logs = filtered.getLogs();
        if (logs.isEmpty()) {
            System.out.println("Brak wpisów.");
            return;
        }
        System.out.println("=== PODSUMOWANIE ===");
        double totalPower = analytics.getTotalPower(filtered);
        System.out.printf("Łączna moc (POWER): %.2f kW", totalPower);
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

    public void addLogEntry(){
        System.out.println("======= DODAWANIE NOWEGO WPISU=======");
        WindTurbine selectedTurbine = null;
        while (selectedTurbine == null) {
            System.out.print("Podaj ID turbiny: ");
            String id = scanner.nextLine().trim();

            selectedTurbine = farm.getTurbineById(id);

            if (selectedTurbine == null) {
                System.out.println("Błąd: Turbina o ID '" + id + "' nie istnieje w systemie!");
            }
        }
        System.out.println("Wybrane turbiny o ID: " + selectedTurbine.getTurbineId());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = null;
        while (date == null) {
            System.out.print("Podaj datę zdarzenia (DD.MM.RRRR): ");
            String input = scanner.nextLine().trim();
            try {
                date = LocalDate.parse(input, dtf);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Błąd: '" + input + "' to nieprawidłowy format lub data. Użyj DD.MM.RRRR (np. 12.03.2024).");
            }
        }

        LocalTime time = null;
        while (time == null) {
            System.out.print("Podaj czas zdarzenia (GG:MM, np. 14:30): ");
            String input = scanner.nextLine().trim();
            try {
                time = LocalTime.parse(input);
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Błąd: '" + input + "' to nieprawidłowy format czasu. Wpisz np. 08:15.");
            }
        }
        LocalDateTime timestamp = LocalDateTime.of(date, time);
        System.out.println("Ustawiono pełną datę: " + timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        String eventType = "";
        String[] allowedEventTypes = {"OPERATIONAL", "ALARM", "MAINTENANCE", "SHUTDOWN", "STARTUP"};

        while(eventType.isEmpty()){
            System.out.print("Podaj typ zdarzenia: ");
            String input = scanner.nextLine().trim().toUpperCase();
            boolean found = false;
            for(int i = 0; i < allowedEventTypes.length; i++){
                if(allowedEventTypes[i].equals(input)){
                    found = true;
                    break;
                }
            }
            if(found){
                eventType = input;
            }else{
                System.out.println("Błąd: Nieprawidłowy typ. Spróbuj ponownie.");
            }
        }

        String operatorName = "";
        while(operatorName.isEmpty()){
            System.out.print("Podaj imię i nazwisko operatora: ");
            operatorName = scanner.nextLine().trim();
            if(operatorName.isEmpty()){
                System.out.println("Błąd: Imię i nazwisko operatora nie może być puste");
            }
        }

        SensorReading[] turbineSensors = selectedTurbine.getSensors();
        SensorReading[] newReadings = new SensorReading[turbineSensors.length];
        System.out.println("--- Podaj wartości dla sensorów turbiny " + selectedTurbine.getTurbineId() + " ---");

        for (int i = 0; i < turbineSensors.length; i++) {
            String sName = turbineSensors[i].getSensorName();
            double value = Double.NaN;
            while(Double.isNaN(value)){
                System.out.print("Sensor [" + sName + "]: ");
                try {
                    value = Double.parseDouble(scanner.nextLine().trim());
                    newReadings[i] = new SensorReading(sName, value);
                } catch (Exception e) {
                    System.out.println("Błąd: Wpisz liczbę.");
                }
            }
        }
        LogEntry newEntry = new LogEntry(timestamp, selectedTurbine.getTurbineId(), eventType, operatorName, newReadings);
        farm.addLog(newEntry);
        System.out.println(">>> SUKCES: Wpis został dodany pomyślnie!");
    }

    public void showFarmInfo(){
        System.out.println("=======INFORMACJE O FARMIE=======");
        System.out.println("Nazwa farmy:       " + farm.getName());
        System.out.println("Nazwa operatora:       " + farm.getOperator());
        System.out.println("Lokalizacja:       " + farm.getLocation());
        System.out.println("Liczba turbin:     " + farm.turbineCount());
        System.out.println("Całkowita liczba logów: " + farm.logCount());

        ArrayList<LogEntry> allLogs = farm.getLogs();
        if (allLogs.isEmpty()) {
            System.out.println("Brak wpisów w historii, nie można wyznaczyć zakresu dat ani operatorów.");
            return;
        }

        LocalDateTime minDate = allLogs.get(0).getTimestamp();
        LocalDateTime maxDate = allLogs.get(0).getTimestamp();
        for (LogEntry log : allLogs) {
            if (log.getTimestamp().isBefore(minDate)) minDate = log.getTimestamp();
            if (log.getTimestamp().isAfter(maxDate)) maxDate = log.getTimestamp();
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        System.out.println("Zakres czasowy:    od " + minDate.format(dtf) + " do " + maxDate.format(dtf));

        System.out.println("--- Lista unikalnych operatorów ---");
        String[] operators = farm.getUniqueOperators();
        if (operators.length > 0) {
            for (int i = 0; i < operators.length; i++) {
                System.out.println((i + 1) + ". " + operators[i]);
            }
        } else {
            System.out.println("Brak zarejestrowanych operatorów w systemie.");
        }

        System.out.println("--- Statystyki typów zdarzeń");
        String[] eventReport = analytics.getEventTypesReport(farm);

        if (eventReport.length > 0) {
            for (String line : eventReport) {
                String[] parts = line.split(":");
                String type = parts[0];
                String count = parts[1];

                System.out.printf("Typ: %s | Liczba wpisów: %s\n", type, count);
            }
        } else {
            System.out.println("Brak danych o zdarzeniach.");
        }
    }

}
