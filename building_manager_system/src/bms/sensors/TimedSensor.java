package bms.sensors;

import bms.util.Encodable;
import bms.util.TimedItem;
import bms.util.TimedItemManager;

import java.util.Arrays;

/**
 * An abstract class to represent a sensor that iterates through observed values
 * on a timer.
 */
public abstract class TimedSensor implements TimedItem, Sensor, Encodable {

    /**
     * Data array representing the readings observed by the sensor.
     * <p>
     * Readings taken one minute apart.
     */
    private int[] sensorReadings;

    /**
     * The current sensor reading observed by the sensor.
     */
    private int currentReading;

    /**
     * The amount of time in minutes that the sensor has been running
     * (according to the system, not real life).
     */
    private int timeElapsed;

    /**
     * The number of minutes that must pass before the current sensor
     * reading is updated.
     */
    private int updateFrequency;

    /**
     * Creates a new timed sensor, using the provided list of sensor readings.
     * These represent "raw" data values, and have different meanings depending
     * on the concrete sensor class used.
     * <p>
     * The provided update frequency must be greater than or equal to one (1),
     * and less than or equal to five (5). The provided sensor readings array
     * must not be null, and must have at least one element. All sensor readings
     * must be non-negative.
     * <p>
     * The new timed sensor should be configured such that the first call
     * to {@link TimedSensor#getCurrentReading()} after calling the
     * constructor must return the first element of the given array.
     * <p>
     * The sensor should be registered as a timed item, see
     * {@link TimedItemManager#registerTimedItem(TimedItem)}.
     *
     * @param sensorReadings a non-empty array of sensor readings
     * @param updateFrequency indicates how often the sensor readings updates,
     *                        in minutes
     * @throws IllegalArgumentException if updateFrequency is &lt; 1 or &gt; 5;
     * or if sensorReadings is null; if sensorReadings is empty; or if any
     * value in sensorReadings is less than zero
     * @ass1
     */
    public TimedSensor(int[] sensorReadings, int updateFrequency) throws
            IllegalArgumentException {
        if ((updateFrequency < 1) || (updateFrequency > 5)) {
            throw new IllegalArgumentException("Update frequency must be "
                    + "between 1 and 5 minutes (inclusive)");
        }
        if (sensorReadings == null || sensorReadings.length == 0) {
            throw new IllegalArgumentException("Sensor readings array must "
                    + "not be null and must have at least one element");
        }
        for (int reading : sensorReadings) {
            if (reading < 0) {
                throw new IllegalArgumentException(
                        "All sensor readings must be non-negative");
            }
        }
        this.sensorReadings = sensorReadings;
        this.currentReading = sensorReadings[0];
        this.updateFrequency = updateFrequency;
        this.timeElapsed = 0;
        TimedItemManager.getInstance().registerTimedItem(this);
    }

    /**
     * Returns the current sensor reading observed by the sensor.
     *
     * @return the current sensor reading
     * @ass1
     */
    public int getCurrentReading() {
        return this.currentReading;
    }

    /**
     * Returns the number of minutes that have elapsed since the sensor was
     * instantiated. Should return 0 immediately after the constructor is
     * called.
     *
     * @return the sensor's time elapsed in minutes
     * @ass1
     */
    public int getTimeElapsed() {
        return timeElapsed;
    }

    /**
     * Returns the number of minutes in between updates to the current sensor
     * reading.
     *
     * @return the sensor's update frequency in minutes
     * @ass1
     */
    public int getUpdateFrequency() {
        return updateFrequency;
    }

    /**
     * Increments the time elapsed (in minutes) by one.
     * <p>
     * If {@link #getTimeElapsed()} divided by {@link #getUpdateFrequency()}
     * leaves zero (0) remainder, the sensor reading needs to be updated.
     * In this case, the current sensor reading is updated to the next value
     * in the array.
     * <p>
     * When the end of the sensor readings array is reached, it must start
     * again at the beginning of the array (in other words it wraps around).
     * @ass1
     * */
    public void elapseOneMinute() {
        this.timeElapsed++;

        // calculate the time taken before wrapping around to the starting value
        // again
        int rotationDuration = this.sensorReadings.length
                * this.updateFrequency;

        // calculate the time remaining in the current rotation
        int timeRemainingInRotation =  this.timeElapsed % rotationDuration;

        // index is time remaining in the current rotation divided by the update
        // frequency
        int index = timeRemainingInRotation / this.updateFrequency;

        this.currentReading = this.sensorReadings[index];
    }

    /**
     * Returns the human-readable string representation of this timed sensor.
     * <p>
     * The format of the string to return is
     * "TimedSensor: freq='updateFrequency', readings='sensorReadings'"
     * without the single quotes, where 'updateFrequency' is this sensor's
     * update frequency (in minutes) and 'sensorReadings' is a comma-separated
     * list of this sensor's readings.
     * <p>
     * For example: "TimedSensor: freq=5, readings=24,25,25,23,26"
     *
     * @return string representation of this sensor
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TimedSensor: freq=%d, readings=%s",
                this.updateFrequency,
                String.join(",", Arrays.stream(this.sensorReadings)
                        .mapToObj(String::valueOf)
                        .toArray(String[]::new)));
    }
}
