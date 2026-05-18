package analysis;

import models.WindFarm;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FarmStatistics {
    public static String severityReport(WindFarm farm){
        FarmAnalytics analytics = new FarmAnalytics();
        return analytics.countAlarmsBySeverity(farm).entrySet().stream()
                .map(entry -> String.format("Poziom: %s | Liczba: %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(
                        "\n",
                        "--- ROZKŁAD WAŻNOŚCI ALARMÓW ---\n",
                        ""
                ));
    }

    public static String turbineActivityReport(WindFarm farm, int topN){
        FarmAnalytics analytics = new FarmAnalytics();

        List<Map.Entry<String, Long>> sortedEntries = analytics.entriesByTurbine(farm).entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), (long) entry.getValue().size()))
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .toList();

        return IntStream.range(0, sortedEntries.size())
                .mapToObj(i -> String.format("%d. Turbina %s: %d wpisów", i + 1, sortedEntries.get(i).getKey(), sortedEntries.get(i).getValue()))
                .collect(Collectors.joining("\n", "--- RANKING AKTYWNOŚCI TURBIN ---\n", ""));
    }

    public static String monthlyReport(WindFarm farm){
        FarmAnalytics analytics = new FarmAnalytics();

        return analytics.entriesPerMonth(farm).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("Miesiąc %s: %d wpisów", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(
                        "\n",
                        "--- RAPORT MIESIĘCZNY CHRONOLOGICZNY ---\n",
                        ""
                ));
    }

    public static String operatorRanking(WindFarm farm){
        FarmAnalytics analytics = new FarmAnalytics();

        return analytics.operatorsByActivity(farm).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> String.format("Operator %s: %d aktywnosci", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(
                        "\n",
                        "--- RANKING AKTYWNOŚCI OPERATORÓW ---\n",
                        ""
                ));
    }

    public static String executiveSummary(WindFarm farm){
        analysis.FarmAnalytics analytics = new analysis.FarmAnalytics();

        double avgPower = analytics.averagePower(farm).orElse(0.0);

        long criticalAlarmsCount = analytics.filterAndCount(farm,
                functional.EntryPredicates.bySeverityAtLeast("CRITICAL"));

        return String.format(Locale.US,
                "Farma: %s | Turbin: %d | Wpisów: %d | Średnia moc: %.2f kW | Alarmy CRITICAL: %d",
                farm.getName(), farm.turbineCount(), farm.logCount(), avgPower, criticalAlarmsCount);
    }
}
