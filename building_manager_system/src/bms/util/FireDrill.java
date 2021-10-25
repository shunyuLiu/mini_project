package bms.util;

import bms.exceptions.FireDrillException;
import bms.room.RoomType;

/**
 * Denotes a class containing a routine to carry out fire drills on rooms
 * of a given type.
 * @ass1
 */
public interface FireDrill {
    void fireDrill(RoomType roomType) throws FireDrillException;
}
