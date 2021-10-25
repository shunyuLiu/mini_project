package bms;

import bms.building.BuildingInitialiser;
import bms.display.View;
import bms.display.ViewModel;
import bms.exceptions.FileFormatException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the CSSE2002/7023 Building Management System.
 * @ass2_given
 */
public class Launcher extends Application {

    /**
     * Entry point to the GUI.
     * <p>
     * Command line arguments: [filename]
     * <p>
     * where <code>filename</code> is the path of the file containing the
     * buildings to be displayed by the GUI.
     *
     * @param args command line arguments
     * @ass2_given
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: [filename]");
            System.out.println("You need to add a command line argument to your"
                    + " program in IntelliJ. Go to \"Run > Edit Configurations "
                    + "> Launcher > Program Arguments\" and add your file name "
                    + "to the text box.");
            System.exit(1);
        }
        Application.launch(Launcher.class, args);
    }

    /**
     * Runs the GUI.
     *
     * @param stage stage to render to
     * @ass2_given
     */
    @Override
    public void start(Stage stage) {
        List<String> params = getParameters().getRaw();

        View view;
        try {
            view = new View(stage, new ViewModel(
                    BuildingInitialiser.loadBuildings(params.get(0))));
        } catch (IOException | FileFormatException e) {
            System.err.println("Error loading from file \"" + params.get(0)
                    + "\". Stack trace below:");
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }

        view.run();
    }
}
