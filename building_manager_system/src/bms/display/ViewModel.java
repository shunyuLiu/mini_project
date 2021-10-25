package bms.display;

import bms.building.Building;
import bms.exceptions.*;
import bms.floor.Floor;
import bms.hazardevaluation.RuleBasedHazardEvaluator;
import bms.hazardevaluation.WeightingBasedHazardEvaluator;
import bms.room.Room;
import bms.room.RoomType;
import bms.sensors.*;
import bms.util.TimedItemManager;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * View model for the GUI - you will need to implement a few methods here.
 * @ass2_part_given
 */
public class ViewModel {
    private List<Building> buildings;

    private BooleanProperty paused = new SimpleBooleanProperty(true);
    private IntegerProperty ticks = new SimpleIntegerProperty(0);
    private StringProperty timeElapsed = new SimpleStringProperty(
            this.ticks.getValue() + " minutes elapsed");
    private StringProperty pauseButtonText = new SimpleStringProperty(
            "Unpause");

    private StringProperty infoText = new SimpleStringProperty(
            "Building/floor/room info");

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    private ReadOnlyIntegerProperty currentBuildingIndex;
    private Floor selectedFloor;
    private Room selectedRoom;

    private BooleanProperty floorSelected = new SimpleBooleanProperty(false);
    private BooleanProperty roomSelected = new SimpleBooleanProperty(false);
    private BooleanProperty noSelected = new SimpleBooleanProperty(true);

    /**
     * @ass2_given
     */
    public ViewModel(List<Building> buildings) {
        this.buildings = buildings;
    }

    /**
     * Accepts keyboard input and performs an action based on the key pressed.
     *
     * <table border="1">
     * <caption>Keyboard input table</caption>
     * <tr><th>Key</th><th>Action</th></tr>
     * <tr><td>P, p</td><td>Toggles whether the simulation is paused</td></tr>
     * <tr><td>Q, q</td><td>Quits the application entirely</td></tr>
     * <tr><td>S, s</td><td>Saves the simulation data to "saves/quicksave.txt",
     * see {@link ViewModel#save(String)}
     * </td></tr>
     * </table>
     *
     * @param keyCode key that was pressed
     * @ass2
     */
    public void accept(KeyCode keyCode) {
        // TODO implement for assignment 2
    }

    /**
     * Toggles whether or not the simulation is paused.
     *
     * The value of the internal <code>paused</code> BooleanProperty should be
     * inverted.
     * <p>
     * The value of the internal <code>pauseButtonText</code> StringProperty
     * should be set to "Unpause" if the simulation is now paused, and to
     * "Pause" if the simulation is now unpaused.
     *
     * @ass2
     */
    public void togglePause() {
        // TODO implement for assignment 2
    }

    /**
     * Saves the data used by the building management system to the given file
     * location.
     *
     * The file should contain the encoded representation of all the buildings
     * stored by the ViewModel, joined by a line separator (see
     * {@link System#lineSeparator()}).
     * <p>
     * If an IOException occurs when writing to file, it should be propagated
     * out of this method.
     *
     * @param filename path of file to save to
     * @throws IOException if any IOExceptions are encountered while writing to
     * the file
     * @ass2
     */
    public void save(String filename) throws IOException {
        // TODO implement for assignment 2
    }

    /**
     * Moves the simulation forward by one "virtual" minute if not paused.
     * <p>
     * If the simulation is paused, no action should be taken.
     * Otherwise, the {@link TimedItemManager#elapseOneMinute()} method should
     * be called to  elapse one minute on all registered timed items.
     * <p>
     * The value of the internal <code>ticks</code> IntegerProperty should be
     * incremented by one, and the value of the <code>timeElapsed</code>
     * StringProperty should be updated to contain "X minutes elapsed" where
     * X is the number of ticks elapsed.
     * <p>
     * Finally, the <code>registerChange()</code> method should be called.
     *
     * @ass2
     */
    public void tick() {
        // TODO implement for assignment 2
    }

    /**
     * @ass2_given
     */
    public List<Building> getBuildings() {
        return buildings;
    }

    /**
     * @ass2_given
     */
    public void setCurrentBuildingIndexProperty(ReadOnlyIntegerProperty prop) {
        this.currentBuildingIndex = prop;
        this.currentBuildingIndex.addListener((observableValue, number, t1) -> {
            updateInfoText();
            // Clear the currently selected floor/room when changing building
            setSelected(null, null);
        });
    }

    /**
     * @ass2_given
     */
    public ReadOnlyIntegerProperty getCurrentBuildingIndexProperty() {
        return this.currentBuildingIndex;
    }

    /**
     * @ass2_given
     */
    public StringProperty getTimeElapsedProperty() {
        return timeElapsed;
    }

    /**
     * @ass2_given
     */
    public StringProperty getInfoTextProperty() {
        return infoText;
    }

    /**
     * @ass2_given
     */
    public StringProperty getPauseButtonTextProperty() {
        return pauseButtonText;
    }

    /**
     * @ass2_given
     */
    public BooleanProperty isFloorSelected() {
        return floorSelected;
    }

    /**
     * @ass2_given
     */
    public BooleanProperty isRoomSelected() {
        return roomSelected;
    }

    /**
     * @ass2_given
     */
    public Floor getSelectedFloor() {
        return selectedFloor;
    }

    /**
     * @ass2_given
     */
    public Room getSelectedRoom() {
        return selectedRoom;
    }

    /**
     * @ass2_given
     */
    public Building getSelectedBuilding() {
        int buildingIndex = currentBuildingIndex.get();
        return this.buildings.get(buildingIndex);
    }

    /**
     * @ass2_given
     */
    public void setSelected(Floor floor, Room room) {
        floorSelected.setValue(floor != null);
        roomSelected.setValue(room != null);
        noSelected.setValue(floor == null && room == null);
        selectedFloor = floor;
        selectedRoom = room;
        updateInfoText();
        registerChange();
    }

    /**
     * @ass2_given
     */
    public void updateInfoText() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        Building building = this.getSelectedBuilding();
        joiner.add("Building");
        joiner.add("Name: " + building.getName());
        joiner.add("Floors: " + building.getFloors().size());

        if (noSelected.get()) {
            this.getInfoTextProperty().setValue(joiner.toString());
            return;
        }

        joiner.add("\nFloor");
        joiner.add("Number: " + this.selectedFloor.getFloorNumber());
        joiner.add("Width: " + this.selectedFloor.getWidth());
        joiner.add("Length: " + this.selectedFloor.getLength());
        joiner.add("Area: " + this.selectedFloor.calculateArea());
        joiner.add("Occupied Area: " + this.selectedFloor.occupiedArea());
        joiner.add("Free Area: " + (this.selectedFloor.calculateArea()
                - this.selectedFloor.occupiedArea()));
        joiner.add("Rooms: " + this.selectedFloor.getRooms().size());
        if (this.selectedFloor.getMaintenanceSchedule() != null) {
            joiner.add("Maintenance Sched.: " +
                    this.selectedFloor.getMaintenanceSchedule().encode());
        }

        if (roomSelected.get()) {
            joiner.add("\nRoom");
            joiner.add("Number: " + this.selectedRoom.getRoomNumber());
            joiner.add("Type: " + this.selectedRoom.getType());
            joiner.add("Area: " + this.selectedRoom.getArea());
            joiner.add("Fire Drill: " + this.selectedRoom.fireDrillOngoing());
            joiner.add("Maintenance: " + this.selectedRoom.maintenanceOngoing());
            joiner.add("State: " + this.selectedRoom.evaluateRoomState());
            joiner.add("Sensors: " + this.selectedRoom.getSensors().size());
            if (this.selectedRoom.getHazardEvaluator() != null) {
                joiner.add("Hazard Evaluator: "
                        + this.selectedRoom.getHazardEvaluator());
            }

            if (!this.selectedRoom.getSensors().isEmpty()) {
                joiner.add("\nSensors");
                for (Sensor s : this.selectedRoom.getSensors()) {
                    joiner.add(s.getClass().getSimpleName());
                }
            }
        }
        this.infoText.setValue(joiner.toString());
    }

    /**
     * @ass2_given
     */
    public boolean isChanged() {
        return changed.get();
    }

    /**
     * @ass2_given
     */
    public void notChanged() {
        changed.setValue(false);
    }

    /**
     * @ass2_given
     */
    public void registerChange() {
        this.changed.setValue(true);
    }

    /**
     * @ass2_given
     */
    public void createErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * @ass2_given
     */
    public void createSuccessDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * @ass2_given
     */
    public void createInfoDialog(String titleText, String headerText,
            String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titleText);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * @ass2_given
     */
    public void takeInstruction(ButtonOptions option,
            List<Optional<String>> args) {
        for (Optional<String> arg : args) {
            if (arg.isEmpty()) {
                return;
            }
        }
        switch (option) {
            case ADD_BUILDING:
                addBuilding(args.get(0).orElse(""));
                break;
            case ADD_FLOOR:
                addFloor(args.get(0).orElse(""), args.get(1).orElse(""));
                break;
            case ADD_ROOM:
                addRoom(args.get(0).orElse(""), args.get(1).orElse(""),
                        args.get(2).orElse(""));
                break;
            case START_FIRE_DRILL:
                startFireDrill(args.get(0).orElse(""));
                break;
            case CANCEL_FIRE_DRILL:
                cancelFireDrill();
                break;
            case RENOVATE_FLOOR:
                renovateFloor(args.get(0).orElse(""), args.get(1).orElse(""));
                break;
            case ADD_MAINTENANCE_SCHEDULE:
                addMaintenanceSchedule(args);
                break;
            case ADD_SENSOR:
                addSensor(args);
                break;
            case ADD_HAZARD_EVALUATOR:
                addHazardEvaluator(args);
                break;
        }
    }

    private void addMaintenanceSchedule(List<Optional<String>> args) {
        if (args.size() == 0) return;

        List<Room> roomOrder = new ArrayList<>();
        for (Optional<String> arg : args) {
            if (arg.isEmpty()) continue;

            int roomNumber;
            try {
                roomNumber = Integer.parseInt(arg.get());
            } catch (NumberFormatException e) {
                createErrorDialog("Could not add maintenance schedule",
                        "Room number must be an integer");
                return;
            }
            roomOrder.add(this.selectedFloor.getRoomByNumber(roomNumber));
        }
        try {
            this.selectedFloor.createMaintenanceSchedule(roomOrder);
        } catch (IllegalArgumentException e) {
            createErrorDialog("Could not add maintenance schedule",
                    e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void addBuilding(String buildingName) {
        if (buildingName.isBlank()) {
            createErrorDialog("Could not create building",
                    "Building name must contain non-whitespace characters");
            return;
        }
        this.buildings.add(new Building(buildingName));
        this.registerChange();
    }

    private void addFloor(String width, String length) {
        // Add the floor to the currently selected building
        Building currentBuilding = getSelectedBuilding();

        // New floor should be on top of building
        int floorNum = 1;
        if (!currentBuilding.getFloors().isEmpty()) {
            floorNum = currentBuilding.getFloors()
                    .get(currentBuilding.getFloors().size() - 1)
                    .getFloorNumber() + 1;
        }

        int floorWidth;
        try {
            floorWidth = Integer.parseInt(width);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add floor",
                    "Floor width must be an integer");
            return;
        }

        int floorLength;
        try {
            floorLength = Integer.parseInt(length);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add floor",
                    "Floor length must be an integer");
            return;
        }

        Floor newFloor = new Floor(floorNum, floorWidth, floorLength);
        try {
            currentBuilding.addFloor(newFloor);
        } catch (DuplicateFloorException | NoFloorBelowException e) {
            // should never happen
            createErrorDialog("Could not add floor", e.getMessage());
            return;
        } catch (FloorTooSmallException | IllegalArgumentException e) {
            createErrorDialog("Could not add floor", e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void addRoom(String number, String type, String area) {
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add room",
                    "Room number must be an integer");
            return;
        }

        RoomType roomType;
        try {
            roomType = RoomType.valueOf(type);
        } catch (IllegalArgumentException e) {
            createErrorDialog("Could not add room",
                    "Room type is invalid");
            return;
        }

        double roomArea;
        try {
            roomArea = Double.parseDouble(area);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add room",
                    "Room area must be a double");
            return;
        }
        Room newRoom = new Room(roomNumber, roomType, roomArea);

        // Add the room to the currently selected floor
        Floor currentFloor = getSelectedBuilding().getFloorByNumber(
                this.selectedFloor.getFloorNumber());

        try {
            currentFloor.addRoom(newRoom);
        } catch (InsufficientSpaceException | DuplicateRoomException
                | IllegalArgumentException e) {
            createErrorDialog("Could not add room", e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void startFireDrill(String type) {
        RoomType roomType;
        try {
            roomType = RoomType.valueOf(type);
        } catch (IllegalArgumentException e) {
            roomType = null;
        }

        try {
            getSelectedBuilding().fireDrill(roomType);
        } catch (FireDrillException e) {
            createErrorDialog("Could not start fire drill", e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void cancelFireDrill() {
        this.getSelectedBuilding().cancelFireDrill();
        this.registerChange();
    }

    private void renovateFloor(String width, String length) {
        double newWidth;
        try {
            newWidth = Double.parseDouble(width);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not renovate floor",
                    "Floor width must be a double");
            return;
        }

        double newLength;
        try {
            newLength = Double.parseDouble(length);
        } catch (NumberFormatException e) {
            createErrorDialog("Could not renovate floor",
                    "Floor length must be a double");
            return;
        }

        try {
            getSelectedBuilding().renovateFloor(
                    getSelectedFloor().getFloorNumber(), newWidth, newLength);
        } catch (FloorTooSmallException | IllegalArgumentException e) {
            createErrorDialog("Could not renovate floor", e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void addSensor(List<Optional<String>> args) {
        if (args.size() < 2) return;
        if (args.get(0).isEmpty() || args.get(1).isEmpty()) return;
        String sensorType = args.get(0).get();

        int[] sensorReadings;
        try {
            sensorReadings = Arrays.stream(args.get(1).get().split(","))
                    .mapToInt(Integer::valueOf).toArray();
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add sensor", "Sensor readings must be "
                    + "a comma-separated list of integers");
            return;
        }

        if (sensorType.equals("TemperatureSensor")) {
            try {
                getSelectedRoom().addSensor(
                        new TemperatureSensor(sensorReadings));
            } catch (DuplicateSensorException e) {
                createErrorDialog("Could not add sensor", e.getMessage());
                return;
            }
            this.registerChange();
            return;
        }

        if (args.size() < 3) return;
        if (args.get(2).isEmpty()) return;

        int updateFrequency;
        try {
            updateFrequency = Integer.parseInt(args.get(2).get());
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add sensor",
                    "Sensor update frequency must be an integer");
            return;
        }

        if (sensorType.equals("NoiseSensor")) {
            try {
                getSelectedRoom().addSensor(new NoiseSensor(sensorReadings,
                        updateFrequency));
            } catch (DuplicateSensorException e) {
                createErrorDialog("Could not add sensor", e.getMessage());
                return;
            }
            this.registerChange();
            return;
        }

        if (args.size() < 4) return;
        if (args.get(3).isEmpty()) return;

        if (sensorType.equals("OccupancySensor")) {
            int capacity;
            try {
                capacity = Integer.parseInt(args.get(3).get());
            } catch (NumberFormatException e) {
                createErrorDialog("Could not add sensor",
                        "Sensor capacity must be an integer");
                return;
            }
            try {
                getSelectedRoom().addSensor(new OccupancySensor(sensorReadings,
                        updateFrequency, capacity));
            } catch (DuplicateSensorException e) {
                createErrorDialog("Could not add sensor", e.getMessage());
                return;
            }
            this.registerChange();
            return;
        }

        if (args.size() < 5) return;
        if (args.get(4).isEmpty()) return;

        int idealValue, varLimit;
        try {
            idealValue = Integer.parseInt(args.get(3).get());
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add sensor",
                    "Sensor ideal value must be an integer");
            return;
        }
        try {
            varLimit = Integer.parseInt(args.get(4).get());
        } catch (NumberFormatException e) {
            createErrorDialog("Could not add sensor",
                    "Sensor variation limit must be an integer");
            return;
        }
        try {
            getSelectedRoom().addSensor(new CarbonDioxideSensor(sensorReadings,
                    updateFrequency, idealValue, varLimit));
        } catch (DuplicateSensorException e) {
            createErrorDialog("Could not add sensor", e.getMessage());
            return;
        }

        this.registerChange();
    }

    private void addHazardEvaluator(List<Optional<String>> args) {
        List<HazardSensor> hazardSensors = new ArrayList<>();
        for (Sensor s : getSelectedRoom().getSensors()) {
            if (s instanceof HazardSensor) {
                hazardSensors.add((HazardSensor) s);
            }
        }
        if (args.isEmpty() || args.get(0).isEmpty()) return;
        String evaluatorType = args.get(0).get();

        if (evaluatorType.equals("Rule Based")) {
            getSelectedRoom().setHazardEvaluator(
                    new RuleBasedHazardEvaluator(hazardSensors));
            this.registerChange();
            return;
        }

        Map<HazardSensor, Integer> weightings = new HashMap<>();
        for (int i = 0; i < getSelectedRoom().getSensors().size(); ++i) {
            Sensor s = getSelectedRoom().getSensors().get(i);
            if (s instanceof HazardSensor) {
                int weighting;
                try {
                    weighting = Integer.parseInt(args.get(i + 1).get());
                } catch (NumberFormatException e) {
                    createErrorDialog("Could not add hazard evaluator",
                            "Weighting must be an integer");
                    return;
                }
                weightings.put((HazardSensor) s, weighting);
            }
        }
        try {
            getSelectedRoom().setHazardEvaluator(
                    new WeightingBasedHazardEvaluator(weightings));
        } catch (IllegalArgumentException e) {
            createErrorDialog("Could not add hazard evaluator",
                    e.getMessage());
            return;
        }

        this.registerChange();
    }
}
