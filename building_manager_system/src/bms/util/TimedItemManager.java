package bms.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class which manages all the timed items.
 * <p>
 * All classes that implement TimedItem must be registered with this manager,
 * which will allow their {@link TimedItemManager#elapseOneMinute()} method to
 * be called at regular time intervals.
 * <p>
 * Once a class is registered with the timed item manager by calling
 * {@link TimedItemManager#registerTimedItem(TimedItem)} ()} and passing itself,
 * the manager will ensure that its {@code elapseOneMinute()} method is called
 * at regular intervals.
 * @ass1
 */
public class TimedItemManager implements TimedItem {
    /**
     * Singleton instance.
     */
    private static TimedItemManager instance = new TimedItemManager();

    /**
     * List of timed items currently registered with the manager.
     */
    private List<TimedItem> timedItems;

    /**
     * Creates a new timed item manager with an empty list of registered items.
     * @ass1
     */
    private TimedItemManager() {
        this.timedItems = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of the timed item manager.
     *
     * @return singleton instance
     * @ass1
     */
    public static TimedItemManager getInstance() {
        return instance;
    }

    /**
     * Registers a timed item with the manager.
     * <p>
     * After calling this method, the manager will call the given timed item's
     * {@code elapseOneMinute()} method at regular intervals.
     *
     * @param timedItem a timed item to register with the manager
     * @ass1
     */
    public void registerTimedItem(TimedItem timedItem) {
        this.timedItems.add(timedItem);
    }

    /**
     * Calls {@code elapseOneMinute()} on each registered timed item.
     * @ass1
     */
    @Override
    public void elapseOneMinute() {
        for (TimedItem timedItem : this.timedItems) {
            timedItem.elapseOneMinute();
        }
    }
}
