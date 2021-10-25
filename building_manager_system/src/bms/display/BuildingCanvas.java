package bms.display;

import bms.building.Building;
import bms.floor.Floor;
import bms.room.Room;
import bms.sensors.OccupancySensor;
import bms.sensors.Sensor;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @ass2_given
 */
public class BuildingCanvas extends Canvas {

    private static class ClickableRegion {

        private final double x;
        private final double y;
        private final double width;
        private final double height;
        public ClickableRegion(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean wasClicked(double clickX, double clickY) {
            return clickX > this.x && clickX < this.x + this.width &&
                    clickY > this.y && clickY < this.y + this.height;
        }

    }

    // Height of a floor in the building
    private static final int FLOOR_HEIGHT = 110;

    // Building drawn on this canvas
    private Building building;

    // View model
    private ViewModel viewModel;

    // Mapping of clickable regions to floors
    private Map<ClickableRegion, Floor> drawnFloors;
    // Mapping of clickable regions to rooms
    private Map<ClickableRegion, Room> drawnRooms;

    // Last pressed X and Y coordinates
    private double pressedX;
    private double pressedY;

    // Offsets used to pan the canvas
    private double xOffset;
    private double yOffset;

    /**
     * @ass2_given
     */
    public BuildingCanvas(ViewModel viewModel, Building building) {
        this.drawnFloors = new HashMap<>();
        this.drawnRooms = new HashMap<>();
        this.viewModel = viewModel;
        this.building = building;

        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());

        setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) return;
            this.pressedX = mouseEvent.getX();
            this.pressedY = mouseEvent.getY();
        });

        setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) return;
            pan(mouseEvent.getX() - this.pressedX,
                    mouseEvent.getY() - this.pressedY);
            draw();
            mouseEvent.consume();
            this.pressedX = mouseEvent.getX();
            this.pressedY = mouseEvent.getY();
        });

        setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            double x = event.getX();
            double y = event.getY();
            Floor clickedFloor = null;
            Room clickedRoom = null;
            for (Map.Entry<ClickableRegion, Room> entry : drawnRooms.entrySet()) {
                if (entry.getKey().wasClicked(x, y)) {
                    clickedRoom = entry.getValue();
                }
            }
            for (Map.Entry<ClickableRegion, Floor> entry : drawnFloors.entrySet()) {
                if (entry.getKey().wasClicked(x, y)) {
                    clickedFloor = entry.getValue();
                }
            }
            viewModel.setSelected(clickedFloor, clickedRoom);
        });
    }

    /**
     * @ass2_given
     */
    public void draw() {
        // Clear the mapping of clickable regions to floors and rooms
        this.drawnFloors.clear();
        this.drawnRooms.clear();

        GraphicsContext gc = getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());

        // Draw width of the smallest room, must be large enough to fit text
        // inside room
        final double minRoomDrawWidth = 110;
        final double padding = 20;
        double startX = padding;
        double startY = this.getHeight() - padding;
        final double roomPadding = 5;
        double x, y;

        int numFloors = building.getFloors().size();
        if (numFloors == 0) {
            return;
        }

        // Area of the smallest room in the building
        double minRoomArea = Double.MAX_VALUE;

        // Area of the floor containing the smallest room
        // If there are no rooms in the building at all, this value will remain
        // at 0 but is unused
        double minFloorArea = 0;
        boolean buildingHasRooms = false;
        // TODO extract calculation?
        for (Floor floor : building.getFloors()) {
            for (Room room : floor.getRooms()) {
                buildingHasRooms = true;
                if (room.getArea() < minRoomArea) {
                    minRoomArea = room.getArea();
                    minFloorArea = floor.calculateArea();
                }
            }
        }
        final double minFloorDrawWidth = minRoomDrawWidth * minFloorArea / minRoomArea;

        double firstFloorArea = building.getFloorByNumber(1).calculateArea();
        // Draw each floor
        for (int i = 0; i < numFloors; ++i) {
            Floor floor = building.getFloors().get(i);
            double floorArea = floor.calculateArea();
            double relativeFloorArea = floorArea / firstFloorArea;
            double floorOriginX = startX;
            double floorOriginY = startY - (i + 1) * FLOOR_HEIGHT;
            double floorDrawWidth;
            if (buildingHasRooms) {
                floorDrawWidth = Math.max((floorArea / minFloorArea) * minFloorDrawWidth,
                        (this.getWidth() - 2 * padding) * relativeFloorArea);
            } else {
                floorDrawWidth = (this.getWidth() - 2 * padding) * relativeFloorArea;
            }

            gc.setStroke(Color.BLACK);
            x = this.xOffset + floorOriginX;
            y = this.yOffset + floorOriginY;
            gc.strokeRect(x, y, floorDrawWidth, FLOOR_HEIGHT);
            if (floor.equals(viewModel.getSelectedFloor())) {
                gc.setFill(Color.LIGHTPINK);
            } else {
                gc.setFill(Color.LIGHTGRAY);
            }
            gc.fillRect(x, y, floorDrawWidth, FLOOR_HEIGHT);

            this.drawnFloors.put(new ClickableRegion(
                    x, y, floorDrawWidth, FLOOR_HEIGHT), floor);

            // Draw each room on the current floor
            int numRooms = floor.getRooms().size();
            double roomOriginX = floorOriginX;
            for (int j = 0; j < numRooms; ++j) {
                Room room = floor.getRooms().get(j);
                double roomArea = room.getArea();
                double roomDrawWidth = roomArea / floor.calculateArea() * floorDrawWidth;

                gc.setStroke(Color.BLUE);
                x = this.xOffset + roomOriginX + roomPadding;
                y = this.yOffset + floorOriginY + roomPadding;
                gc.strokeRect(x, y,
                        roomDrawWidth - 2 * roomPadding,
                        FLOOR_HEIGHT - 2 * roomPadding);
                if (room.equals(viewModel.getSelectedRoom())) {
                    gc.setFill(Color.LIGHTYELLOW);
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(x, y,
                        roomDrawWidth - 2 * roomPadding,
                        FLOOR_HEIGHT - 2 * roomPadding);

                this.drawnRooms.put(new ClickableRegion(x, y,
                        roomDrawWidth - 2 * roomPadding,
                        FLOOR_HEIGHT - 2 * roomPadding), room);

                // Draw a coloured rectangle to represent the hazard level
                if (room.getHazardEvaluator() != null) {
                    double hazardPct = room.getHazardEvaluator()
                            .evaluateHazardLevel() / 100.0;
                    double fullHeight = FLOOR_HEIGHT - 2 * roomPadding;
                    double height = hazardPct * fullHeight;
                    gc.setFill(Color.ORANGE);
                    gc.fillRect(x, y + fullHeight - height, 5, height);
                    gc.setStroke(Color.BLACK);

                    gc.strokeLine(x + 5.5, y, x + 5.5, y + FLOOR_HEIGHT - 2 * roomPadding);
                }

                StringJoiner joiner = new StringJoiner(System.lineSeparator());
                joiner.add("Room #" + room.getRoomNumber());
                joiner.add(room.getType().toString());
                joiner.add(room.evaluateRoomState().toString());
                Sensor tempSensor = room.getSensor("TemperatureSensor");
                if (tempSensor != null) {
                    joiner.add(String.format("%d°C",
                            tempSensor.getCurrentReading()));
                }
                Sensor noiseSensor = room.getSensor("NoiseSensor");
                if (noiseSensor != null) {
                    joiner.add(String.format("%ddB",
                            noiseSensor.getCurrentReading()));
                }
                Sensor co2Sensor = room.getSensor("CarbonDioxideSensor");
                if (co2Sensor != null) {
                    joiner.add(String.format("%dppm",
                            co2Sensor.getCurrentReading()));
                }
                OccupancySensor occupancySensor = (OccupancySensor) room.getSensor("OccupancySensor");
                if (occupancySensor != null) {
                    joiner.add(String.format("%d/%d",
                            occupancySensor.getCurrentReading(),
                            occupancySensor.getCapacity()));
                }
                String roomDescription = joiner.toString();

                gc.setFill(Color.BLACK);
                gc.setTextBaseline(VPos.TOP);
                x = this.xOffset + roomOriginX + roomPadding + 2
                        + (room.getHazardEvaluator() == null ? 0 : 6);
                y = this.yOffset + floorOriginY + roomPadding + 2;
                gc.setFont(Font.font("monospace"));
                gc.fillText(roomDescription, x, y);

                roomOriginX += roomDrawWidth;
            }
        }
    }

    private void pan(double deltaX, double deltaY) {
        this.xOffset += deltaX;
        this.yOffset += deltaY;
        this.draw();
    }

    /**
     * @ass2_given
     */
    public void resetView() {
        this.xOffset = 0;
        this.yOffset = 0;
        this.draw();
    }

    /**
     * @ass2_given
     */
    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * @ass2_given
     */
    @Override
    public double prefWidth(double v) {
        return getWidth();
    }

    /**
     * @ass2_given
     */
    @Override
    public double prefHeight(double v) {
        return getHeight();
    }
}