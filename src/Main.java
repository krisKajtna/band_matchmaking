import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";

    @Override
    public void start(Stage primaryStage) {
        ListView<String> listView = new ListView<>();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name FROM cities")) {

            while (resultSet.next()) {
                String city = resultSet.getString("name");
                listView.getItems().add(city);
            }

        } catch (Exception e) {
            e.printStackTrace();
            listView.getItems().add("Error loading cities");
        }

        VBox root = new VBox(listView);
        Scene scene = new Scene(root, 300, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Cities List");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
