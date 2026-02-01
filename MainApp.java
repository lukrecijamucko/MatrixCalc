package hr.unizg.pmf.matrixcalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var root = FXMLLoader.load(getClass().getResource(
                "/hr/unizg/pmf/matrixcalc/ui/MainView.fxml"
        ));
        stage.setTitle("MatrixCalc");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
