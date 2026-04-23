package models;
import java.util.Arrays;

public class WindTurbine {
    private static final int MIN_RATED_POWER_KW = 500;
    private static final int MAX_RATED_POWER_KW = 10000;
    private static final int MIN_HUB_HEIGHT_M = 50;
    private static final int MAX_HUB_HEIGHT_M = 200;

    private final String turbineId;
    private final String model;
    private final int ratedPowerKw;
    private final int hubHeightM;
    private final SensorReading[] sensors;

    public WindTurbine(String turbineId, String model, int ratedPowerKw, int hubHeightM, SensorReading[] sensors) {
        if(ratedPowerKw > MAX_RATED_POWER_KW || ratedPowerKw < MIN_RATED_POWER_KW){
            throw new IllegalArgumentException("Moc musi być w zakresie " + MIN_RATED_POWER_KW + "-" + MAX_RATED_POWER_KW + " kW");
        }

        if(hubHeightM > MAX_HUB_HEIGHT_M || hubHeightM < MIN_HUB_HEIGHT_M){
            throw new IllegalArgumentException("Wysokość musi być w zakresie " + MIN_HUB_HEIGHT_M + "-" + MAX_HUB_HEIGHT_M + " m");
        }

        this.turbineId = turbineId;
        this.model = model;
        this.ratedPowerKw = ratedPowerKw;
        this.hubHeightM = hubHeightM;

        if(sensors != null){
            this.sensors = Arrays.copyOf(sensors, sensors.length);
        }else{
            this.sensors = new SensorReading[0];
        }
    }

    public String getTurbineId() {
        return turbineId;
    }

    public String getModel() {
        return model;
    }

    public SensorReading[] getSensors() {
        return Arrays.copyOf(sensors, sensors.length);
    }

    public boolean hasSensor(String sensorName){
        if(sensorName == null) return false;
        for(int i = 0; i < sensors.length; i++){
            if(sensors[i].getSensorName().equalsIgnoreCase(sensorName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return String.format("[%s] %s | %d kW | %d m | %d sensorów",
                turbineId, model, ratedPowerKw, hubHeightM, sensors.length);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if (!(obj instanceof WindTurbine)) {
            return false;
        }
        WindTurbine other = (WindTurbine) obj;
        if (this.turbineId == null || other.turbineId == null) {
            return false;
        }
        return this.turbineId.equalsIgnoreCase(other.turbineId);
    }
}
