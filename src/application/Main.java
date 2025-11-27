package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML
            Parent root = FXMLLoader.load(getClass().getResource("/application/MainMenu.fxml"));
            
            // Create scene with appropriate size
            Scene scene = new Scene(root, 900, 550); // bigger window for sidebar layout
            
            // Add CSS
            scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
            
            // Set stage
            primaryStage.setTitle("Groco");
            primaryStage.setScene(scene);
            Image appIcon = new Image(getClass().getResourceAsStream("logo1.png"));
            primaryStage.getIcons().add(appIcon);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
       
    }

    public static void main(String[] args) {
        launch(args);
    }
};
