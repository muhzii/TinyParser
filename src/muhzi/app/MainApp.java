package muhzi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window_layout.fxml"));
        Parent root = fxmlLoader.load();
        ((Controller) fxmlLoader.getController()).setStage(primaryStage);

        primaryStage.setTitle("Tiny language parser");
        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(getClass().getResource("window_styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
