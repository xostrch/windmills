package interfaces;

public interface Inspectable {
    String inspect();

    default boolean isHealthy(){
        return true;
    }
}
