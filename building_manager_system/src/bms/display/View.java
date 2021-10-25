package bms.display;

import bms.building.Building;
import bms.floor.Floor;
import bms.room.Room;
import bms.room.RoomType;
import bms.sensors.OccupancySensor;
import bms.sensors.Sensor;
import bms.util.StudyRoomRecommender;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ass2_given
 */
public class View {
    /**
     * @ass2_given
     */
    public final static double WINDOW_WIDTH = 900;
    /**
     * @ass2_given
     */
    public final static double WINDOW_HEIGHT = 600;

    private static final double INFO_BOX_WIDTH = 200;
    private static final double BUTTON_PANE_WIDTH = 150;

    private Stage stage;
    private VBox mainWindow;
    private Pane structurePane;
    private TabPane tabPane;
    private ViewModel viewModel;

    // Queue of key presses
    private LinkedList<KeyCode> input;

    private long lastNanoTime = 0;

    /**
     * @ass2_given
     */
    public View(Stage stage, ViewModel viewModel) {
        this.stage = stage;
        this.viewModel = viewModel;
        this.input = new LinkedList<>();

        stage.setTitle("Building Management System");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setMinWidth(WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);

        Scene rootScene = new Scene(createWindow());
        stage.setScene(rootScene);

        viewModel.updateInfoText();

        // Send key presses to the input queue
        rootScene.setOnKeyPressed(keyEvent -> {
            KeyCode code = keyEvent.getCode();
            input.push(code);
        });
    }

    private Pane createWindow() {
        this.mainWindow = new VBox();

        var topBar = createTopBar();
        this.structurePane = createStructurePane();

        mainWindow.getChildren().addAll(topBar, structurePane);

        return mainWindow;
    }

    private Pane createStructurePane() {
        var structurePane = new HBox();

        var infoPane = createInfoBox();
        createTabPane();
        var buttonPane = createButtonPane();

        structurePane.getChildren().addAll(infoPane, this.tabPane, buttonPane);

        return structurePane;
    }

    private TextArea createInfoBox() {
        var infoBox = new TextArea();
        infoBox.textProperty().bind(viewModel.getInfoTextProperty());
        infoBox.setMaxWidth(INFO_BOX_WIDTH);
        infoBox.setEditable(false);
        infoBox.setFocusTraversable(false);
        infoBox.setWrapText(false);
        return infoBox;
    }

    private Pane createButtonPane() {
        var buttonPane = new VBox(10);
        buttonPane.setPrefWidth(BUTTON_PANE_WIDTH);
        buttonPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1");
        buttonPane.setPadding(new Insets(10, 10, 0, 10));

        Label globalLabel = new Label("Global Actions");

        var addBuildingButton = new Button("Add Building");
        addBuildingButton.setMaxWidth(Double.MAX_VALUE);
        addBuildingButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            var name = getResponse("Add Building",
                    "Please enter a name for the building", "Name:",
                    "New Building");
            if (name.isEmpty()) return;
            args.add(name);
            viewModel.takeInstruction(ButtonOptions.ADD_BUILDING, args);
        });

        Label buildingActionsLabel = new Label("Building Actions");

        var addFloorButton = new Button("Add Floor");
        addFloorButton.setMaxWidth(Double.MAX_VALUE);
        addFloorButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            var width = getResponse("Add Floor",
                    "Please enter the floor's width", "Width (m):",
                    String.valueOf(Floor.getMinWidth()));
            if (width.isEmpty()) return;
            args.add(width);
            var length = getResponse("Add Floor",
                    "Please enter the floor's length", "Length (m):",
                    String.valueOf(Floor.getMinLength()));
            args.add(length);
            viewModel.takeInstruction(ButtonOptions.ADD_FLOOR, args);
        });

        var startFireDrillButton = new Button("Start Fire Drill");
        startFireDrillButton.setMaxWidth(Double.MAX_VALUE);
        startFireDrillButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            var roomType = getChoice("Start Fire Drill",
                    "Please choose the type of rooms for which to start a " +
                            "fire drill", "Room type:", "All",
                    RoomType.values());
            if (roomType.isEmpty()) return;
            args.add(Optional.ofNullable(roomType.get().toString()));
            viewModel.takeInstruction(ButtonOptions.START_FIRE_DRILL, args);
        });

        var cancelFireDrillButton = new Button("Cancel Fire Drill");
        cancelFireDrillButton.setMaxWidth(Double.MAX_VALUE);
        cancelFireDrillButton.setOnAction(e ->
                viewModel.takeInstruction(ButtonOptions.CANCEL_FIRE_DRILL,
                        new ArrayList<>()));

        Label floorActionsLabel = new Label("Floor Actions");

        var renovateFloorButton = new Button("Renovate Floor");
        renovateFloorButton.setMaxWidth(Double.MAX_VALUE);
        renovateFloorButton.disableProperty().bind(viewModel.isFloorSelected().not());
        renovateFloorButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            var newWidth = getResponse("Renovate Floor",
                    "Please enter the new width of the floor",
                    "Floor width (m):",
                    String.valueOf(viewModel.getSelectedFloor().getWidth()));
            if (newWidth.isEmpty()) return;
            args.add(newWidth);
            var newLength = getResponse("Renovate Floor",
                    "Please enter the new length of the floor",
                    "Floor length (m):",
                    String.valueOf(viewModel.getSelectedFloor().getLength()));
            if (newLength.isEmpty()) return;
            args.add(newLength);
            viewModel.takeInstruction(ButtonOptions.RENOVATE_FLOOR, args);
        });

        var addRoomButton = new Button("Add Room");
        addRoomButton.setMaxWidth(Double.MAX_VALUE);
        addRoomButton.disableProperty().bind(viewModel.isFloorSelected().not());
        addRoomButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            var roomNum = getResponse("Add Room",
                    "Please enter the room's number", "Room number:", "");
            if (roomNum.isEmpty()) return;
            args.add(roomNum);
            var roomType = getChoice("Add Room",
                    "Please choose the room's type", "Room type:",
                    RoomType.values()[0],
                    RoomType.values());
            if (roomType.isEmpty()) return;
            args.add(Optional.ofNullable(roomType.get().toString()));
            var area = getResponse("Add Room",
                    "Please enter the floor's area", "Area (m^2):",
                    String.valueOf(Room.getMinArea()));
            args.add(area);
            viewModel.takeInstruction(ButtonOptions.ADD_ROOM, args);
        });

        var addMaintenanceScheduleButton = new Button(
                "Maintenance");
        addMaintenanceScheduleButton.setMaxWidth(Double.MAX_VALUE);
        addMaintenanceScheduleButton.disableProperty().bind(
                viewModel.isFloorSelected().not());
        addMaintenanceScheduleButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();
            if (viewModel.getSelectedFloor().getRooms().isEmpty()) {
                viewModel.createErrorDialog("Cannot add maintenance schedule",
                        "Floor has no rooms.");
                return;
            }
            while (true) {
                String[] choices = viewModel.getSelectedFloor().getRooms().stream()
                        .map(Room::getRoomNumber)
                        .map(String::valueOf).toArray(String[]::new);
                StringJoiner joiner = new StringJoiner(", ");
                for (Optional<String> roomNum : args) {
                    joiner.add(roomNum.get());
                }
                String roomsSoFar = joiner.toString();
                var roomNum = getChoice(
                        "Create Maintenance Schedule",
                        "Add a room to the maintenance order."
                                + System.lineSeparator()
                                + "Press Cancel after adding the last room."
                                + System.lineSeparator()
                                + "Rooms so far: "
                                + (roomsSoFar.isEmpty() ? "none" : roomsSoFar),
                        "Room",
                        String.valueOf(viewModel.getSelectedFloor().getRooms()
                                .get(0).getRoomNumber()),
                        choices);
                if (roomNum.isEmpty()) break;
                args.add(roomNum);
            }
            viewModel.takeInstruction(ButtonOptions.ADD_MAINTENANCE_SCHEDULE,
                    args);
        });

        Label roomActionsLabel = new Label("Room Actions");

        var addSensorButton = new Button("Add Sensor");
        addSensorButton.setMaxWidth(Double.MAX_VALUE);
        addSensorButton.disableProperty()
                .bind(viewModel.isRoomSelected().not());
        addSensorButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();

            String[] sensorTypeChoices = new String[] {
                    "CarbonDioxideSensor", "NoiseSensor", "OccupancySensor",
                    "TemperatureSensor"
            };
            var sensorType = getChoice("Add Sensor to Room",
                    "Please choose the type of sensor to add", "Sensor type:",
                    sensorTypeChoices[0], sensorTypeChoices);
            if (sensorType.isEmpty()) return;
            args.add(sensorType);

            var readings = getResponse("Add Sensor to Room",
                    "Please enter the sensor readings, comma-separated",
                    "Sensor readings:", "");
            if (readings.isEmpty()) return;
            args.add(readings);

            switch (sensorType.get()) {
                case "TemperatureSensor":
                    viewModel.takeInstruction(ButtonOptions.ADD_SENSOR, args);
                    return;
                case "NoiseSensor":
                case "OccupancySensor":
                case "CarbonDioxideSensor":
                    var updateFrequency = getResponse("Add Sensor to Room",
                            "Please enter the sensor's update frequency, in minutes",
                            "Update frequency:", "1");
                    if (updateFrequency.isEmpty()) return;
                    args.add(updateFrequency);
                    break;
            }

            switch (sensorType.get()) {
                case "OccupancySensor":
                    var capacity = getResponse("Add Sensor to Room",
                            "Please enter the occupancy sensor's maximum capacity",
                            "Capacity:", "");
                    if (capacity.isEmpty()) return;
                    args.add(capacity);
                    break;
                case "CarbonDioxideSensor":
                    var idealValue = getResponse("Add Sensor to Room",
                            "Please enter the CO2 sensor's ideal value, in ppm",
                            "Ideal value:", "700");
                    if (idealValue.isEmpty()) return;
                    args.add(idealValue);

                    var varLimit = getResponse("Add Sensor to Room",
                            "Please enter the CO2 sensor's variation limit, in ppm",
                            "Variation limit:", "");
                    if (varLimit.isEmpty()) return;
                    args.add(varLimit);
                    break;
            }
            viewModel.takeInstruction(ButtonOptions.ADD_SENSOR, args);
        });

        var addHazardEvaluatorButton = new Button("Hazard Evaluator");
        addHazardEvaluatorButton.setMaxWidth(Double.MAX_VALUE);
        addHazardEvaluatorButton.disableProperty()
                .bind(viewModel.isRoomSelected().not());
        addHazardEvaluatorButton.setOnAction(e -> {
            List<Optional<String>> args = new ArrayList<>();

            String[] evaluatorTypes = new String[] {
                    "Rule Based", "Weighting Based"
            };
            var evaluatorType = getChoice("Add Hazard Evaluator",
                    "Please choose the type of hazard evaluator",
                    "Evaluator type:",
                    evaluatorTypes[0], evaluatorTypes);
            if (evaluatorType.isEmpty()) return;
            args.add(evaluatorType);

            if (evaluatorType.get().equals("Rule Based")) {
                viewModel.takeInstruction(ButtonOptions.ADD_HAZARD_EVALUATOR, args);
                return;
            }

            List<Sensor> sensors = viewModel.getSelectedRoom().getSensors();
            for (Sensor s : sensors) {
                var weighting = getResponse(
                        "Add Hazard Evaluator",
                        "Please enter the weighting for the "
                                + s.getClass().getSimpleName(),
                        "Weighting (0-100):",
                        String.valueOf(Math.round(100.0 / sensors.size())));
                if (weighting.isEmpty()) return;
                args.add(weighting);
            }
            viewModel.takeInstruction(ButtonOptions.ADD_HAZARD_EVALUATOR, args);
        });

        Label otherActionsLabel = new Label("Other Actions");

        var findStudyRoomButton = new Button("Find Study Room");
        findStudyRoomButton.setMaxWidth(Double.MAX_VALUE);
        findStudyRoomButton.setOnAction(e -> {
            Room studyRoom = StudyRoomRecommender.recommendStudyRoom(
                    viewModel.getSelectedBuilding());
            if (studyRoom == null) {
                viewModel.createErrorDialog("Could not find study room",
                        "No suitable study rooms found in the building");
                return;
            }
            viewModel.createInfoDialog("Study Room Recommendation", null,
                    studyRoom.toString());
        });

        buttonPane.getChildren().addAll(globalLabel, addBuildingButton,
                buildingActionsLabel, addFloorButton,
                startFireDrillButton, cancelFireDrillButton,
                floorActionsLabel, addRoomButton,
                renovateFloorButton, addMaintenanceScheduleButton,
                roomActionsLabel, addSensorButton, addHazardEvaluatorButton,
                otherActionsLabel, findStudyRoomButton);

        return buttonPane;
    }

    private TabPane createTabPane() {
        this.tabPane = new TabPane();
        this.tabPane.prefWidthProperty().bind(this.mainWindow.widthProperty()
                .subtract(BUTTON_PANE_WIDTH + INFO_BOX_WIDTH));
        this.tabPane.prefHeightProperty().bind(this.mainWindow.heightProperty());

        for (Building building : viewModel.getBuildings()) {
            Tab tab = new Tab(building.getName(), createBuildingCanvas(building));
            tab.setClosable(false);
            tabPane.getTabs().add(tab);
        }
        viewModel.setCurrentBuildingIndexProperty(tabPane.getSelectionModel()
                .selectedIndexProperty());

        // Draw the currently selected building when the selected tab changes
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, tab, t1) -> getCurrentCanvas().draw());

        return tabPane;
    }

    private Canvas createBuildingCanvas(Building building) {
        Canvas canvas = new BuildingCanvas(this.viewModel, building);
        canvas.widthProperty().bind(this.tabPane.widthProperty());
        canvas.heightProperty().bind(this.tabPane.heightProperty()
                .subtract(30)); // account for the height of the actual tab bar
        return canvas;
    }

    private Pane createTopBar() {
        var infoBar = new BorderPane();
        infoBar.setMaxWidth(Double.MAX_VALUE);
        infoBar.setBackground(new Background(new BackgroundFill(
                Color.LIGHTGRAY, null, null)));
        // Margin around info bar (top, right, bottom, left)
        infoBar.setPadding(new Insets(5, 10, 5, 5));

        var pauseButton = new Button("Pause");
        pauseButton.setPrefWidth(70);
        pauseButton.textProperty().bind(viewModel.getPauseButtonTextProperty());
        pauseButton.setOnAction(e -> viewModel.togglePause());

        var saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            var filename = getResponse("Save network to file",
                    "Please enter the file name to save to", "File name:", "");
            if (filename.isEmpty()) return;

            try {
                viewModel.save(filename.get());
            } catch (IOException ioException) {
                viewModel.createErrorDialog("Error saving buildings to file",
                        ioException.getMessage());
                return;
            }
            viewModel.createSuccessDialog("Saved file successfully",
                    "Saved buildings to \"" + filename + "\" successfully.");
        });

        var resetViewButton = new Button("Reset View");
        resetViewButton.setOnAction(e -> getCurrentCanvas().resetView());

        var helpButton = new Button("Help");
        helpButton.setOnAction(e -> viewModel.createInfoDialog("Help", null,
                "- Move around the canvas by clicking and dragging with the "
                        + "right mouse button\n"
                        + "- Select a floor or room by clicking on it with the "
                        + "left mouse button\n"));

        var timeElapsedLabel = new Label();
        timeElapsedLabel.textProperty().bind(viewModel.getTimeElapsedProperty());
        timeElapsedLabel.setFont(new Font(14)); // make it a little larger
        timeElapsedLabel.setMaxHeight(Double.MAX_VALUE); // centered vertically
        timeElapsedLabel.setAlignment(Pos.CENTER);

        var buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(pauseButton, saveButton,
                resetViewButton, helpButton);
        infoBar.setLeft(buttonContainer);
        infoBar.setRight(timeElapsedLabel);
        return infoBar;
    }

    /**
     * @ass2_given
     */
    public void run() {
        new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                while (!input.isEmpty()) {
                    var key = input.pop();
                    viewModel.accept(key);
                }
                if (currentNanoTime - lastNanoTime > 1000000000) {
                    lastNanoTime = currentNanoTime;
                    viewModel.tick();
                }
                if (viewModel.isChanged()) {
                    viewModel.notChanged();
                    update();
                }
            }
        }.start();

        this.stage.show();
        getCurrentCanvas().draw();
    }

    /**
     * @ass2_given
     */
    public Optional<String> getResponse(String title, String header,
            String label, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    /**
     * @ass2_given
     */
    public <T> Optional<T> getChoice(String title, String header, String label,
            T defaultChoice, T... choices) {
        ChoiceDialog dialog = new ChoiceDialog(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(label);
        dialog.setGraphic(null);
        return dialog.showAndWait();
    }

    private BuildingCanvas getCurrentCanvas() {
        int buildingIndex = this.viewModel.getCurrentBuildingIndexProperty().get();
        BuildingCanvas canvas = (BuildingCanvas) this.tabPane.getTabs()
                .get(buildingIndex).getContent();
        return canvas;
    }

    private void update() {
        // Add a new tab for any newly created buildings
        List<String> tabTitles = new ArrayList<>();
        for (Tab tab : tabPane.getTabs()) {
            tabTitles.add(tab.getText());
        }
        for (Building building : viewModel.getBuildings()) {
            String buildingName = building.getName();
            if (!tabTitles.contains(buildingName)) {
                Tab tab = new Tab(buildingName, createBuildingCanvas(building));
                tab.setClosable(false);
                tabPane.getTabs().add(tab);
                // Switch to the new building's tab
                tabPane.getSelectionModel().select(tab);
            }
        }
        getCurrentCanvas().draw();
        viewModel.updateInfoText();
    }
}
