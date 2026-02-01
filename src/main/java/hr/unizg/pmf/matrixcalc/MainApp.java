package hr.unizg.pmf.matrixcalc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/hr/unizg/pmf/matrixcalc/ui/MainView.fxml")
        );


        Scene scene = new Scene(root);
        stage.setTitle("MatrixCalc");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
