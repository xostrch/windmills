package models;

public class SensorReading {
    private final String sensorName;
    private final double value;

    public SensorReading(String sensorName, double value) {
        if(sensorName == null || sensorName.trim().isEmpty()){
            throw new IllegalArgumentException("Nazwa sensora nie może być pusta");
        }
        this.sensorName = sensorName;
        this.value = value;
    }

    public String getSensorName() {
        return sensorName;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString(){
        return String.format("%s: %.3f",sensorName ,value);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        SensorReading other = (SensorReading) obj;
        boolean namesEqual = this.sensorName.equals(other.sensorName);
        boolean valuesEqual = Math.abs(this.value - other.value) < 1e-9;
        return namesEqual && valuesEqual;
    }
}
