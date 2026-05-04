package interfaces;

public interface Exportable {
    String toCsv();
    String toJson();

    static String csvHeader(){
        return "Data|Czas|TurbinaID|Typ|Operator|Szczegoly|Odczyty";
    }
}
