package bms.building;

import bms.exceptions.DuplicateFloorException;
import bms.exceptions.FireDrillException;
import bms.exceptions.FloorTooSmallException;
import bms.exceptions.NoFloorBelowException;
import bms.floor.Floor;
import bms.room.RoomType;
import bms.util.FireDrill;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a building of floors, which in turn, contain rooms.
 * <p>
 * A building needs to manage and keep track of the floors that make up the
 * building.
 * <p>
 * A building can be evacuated, which causes all rooms on all floors within
 * the building to be evacuated.
 * @ass1
 */
public class Building implements FireDrill {

    /**
     * The name of the building.
     */
    private String name;

    /**
     * List of floors tracked by the building.
     */
    private List<Floor> floors;

    /**
     * Creates a new empty building with no rooms.
     *
     * @param name name of this building, eg. "General Purpose South"
     * @ass1
     */
    public Building(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
    }

    /**
     * Returns the name of the building.
     *
     * @return name of this building
     * @ass1
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a new list containing all the floors in this building.
     * <p>
     * Adding or removing floors from this list should not affect the
     * building's internal list of floors.
     *
     * @return new list containing all floors in the building
     * @ass1
     */
    public List<Floor> getFloors() {
        return new ArrayList<>(this.floors);
    }

    /**
     * Searches for the floor with the specified floor number.
     * <p>
     * Returns the corresponding Floor object, or null if the floor was not
     * found.
     *
     * @param floorNumber floor number of floor to search for
     * @return floor with the given number if found; null if not found
     * @ass1
     */
    public Floor getFloorByNumber(int floorNumber) {
        for (Floor floor : this.floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }

    /**
     * Adds a floor to the building.
     * <p>
     * If the given arguments are invalid, the floor already exists,
     * there is no floor below, or the floor below does not have enough area
     * to support this floor, an exception should be thrown and no action
     * should be taken.
     *
     * @param newFloor object representing the new floor
     * @throws IllegalArgumentException if floor number is &lt;= 0,
     * width &lt; Floor.getMinWidth(), or length &lt; Floor.getMinLength()
     * @throws DuplicateFloorException if a floor at this level already exists
     * in the building
     * @throws NoFloorBelowException if this is at level 2 or above and there
     * is no floor below to support this new floor
     * @throws FloorTooSmallException if this is at level 2 or above and
     * the floor below is not big enough to support this new floor
     *
     * @ass1
     */
    public void addFloor(Floor newFloor) throws
            IllegalArgumentException, DuplicateFloorException,
            NoFloorBelowException, FloorTooSmallException {

        int newFloorNumber = newFloor.getFloorNumber();
        if (newFloorNumber < 1) {
            throw new IllegalArgumentException(
                    "Floor number must be 1 or higher.");
        } else if (newFloor.getWidth() < Floor.getMinWidth())  {
            throw new IllegalArgumentException(
                    "Width cannot be less than " + Floor.getMinWidth());
        } else if (newFloor.getLength() < Floor.getMinLength()) {
            throw new IllegalArgumentException(
                    "Length cannot be less than " + Floor.getMinLength());
        }
        if (this.getFloorByNumber(newFloorNumber) != null) {
            throw new DuplicateFloorException(
                    "This floor level already exists in the building.");
        }

        Floor floorBelow = this.getFloorByNumber(newFloorNumber - 1);
        if (newFloorNumber >= 2 && floorBelow == null) {
            throw new NoFloorBelowException("There is no floor below to "
                    + "support this new floor.");
        }
        if (newFloorNumber >= 2 && (newFloor.getWidth() > floorBelow.getWidth()
                || newFloor.getLength() > floorBelow.getLength())) {
            throw new FloorTooSmallException("The floor below does not "
                    + "have enough area to support this floor. ");
        }

        // No problems, so add floor to the list of floors
        floors.add(newFloor);
    }

    /**
     * Start a fire drill in all rooms of the given type in the building.
     * Only rooms of the given type must start a fire alarm.
     * Rooms other than the given type must not start a fire alarm.
     * * <p>
     * If the room type given is null, then <b>all</b> rooms in the building
     * must start a fire drill.
     * <p>
     * If there are no rooms (of any type) in the building, a
     * FireDrillException must be thrown. Note that floors may be in the
     * building, but the floors may not contain rooms yet.
     *
     * @param roomType the type of room to carry out fire drills on; null if
     *                 fire drills are to be carried out in all rooms
     * @throws FireDrillException if there are no floors in the building, or
     * there are floors but no rooms in the building
     * @ass1
     */
    public void fireDrill(RoomType roomType) throws FireDrillException {
        if (this.floors.size() < 1) {
            throw new FireDrillException("Cannot conduct fire drill because "
                    + "there are no floors in the building yet!");
        }
        boolean hasRooms = false;
        for (Floor floor : this.floors) {
            if (!floor.getRooms().isEmpty()) {
                hasRooms = true;
            }
        }
        if (!hasRooms) {
            throw new FireDrillException("Cannot conduct fire drill because "
                    + "there are no rooms in the building yet!");
        } else {
            for (Floor floor : this.floors) {
                floor.fireDrill(roomType);
            }
        }
    }

    /**
     * Cancels any ongoing fire drill in the building.
     * <p>
     * All rooms must have their fire alarm cancelled regardless of room type.
     *
     * @ass1
     */
    public void cancelFireDrill() {
        for (Floor floor : this.floors) {
            floor.cancelFireDrill();
        }
    }

    /**
     * Returns the human-readable string representation of this building.
     * <p>
     * The format of the string to return is
     * "Building: name="'buildingName'", floors='numFloors'"
     * without the single quotes, where 'buildingName' is the building's name,
     * and 'numFloors' is the number of floors in the building.
     * <p>
     * For example:
     * "Building: name="GP South", floors=7"
     *
     * @return string representation of this building
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("Building: name=\"%s\", floors=%d",
                this.name, this.floors.size());
    }
}
