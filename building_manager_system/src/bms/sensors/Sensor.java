package bms.sensors;

/**
 * Represents a device that is used to monitor and report readings of some
 * observed variable at a location.
 * <p>
 * All sensor readings are provided as integers but may have different meanings
 * depending on the concrete sensor type.
 * @ass1
 */
public interface Sensor {
    /**
     * Returns the current sensor reading observed by the sensor.
     *
     * @return the current sensor reading
     * @ass1
     */
    int getCurrentReading();
}
