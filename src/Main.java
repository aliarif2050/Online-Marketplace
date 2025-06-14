import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("The Online MarketPlace");
        try {
            Image icon = new Image(getClass().getResourceAsStream("1.jpg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Error loading window icon: " + e.getMessage());

        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}