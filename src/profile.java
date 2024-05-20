import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class profile extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int musicianId;
    private int bandId;

    public void setMusicianId(int musicianId) {
        this.musicianId = musicianId;
    }

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("PROFILE");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Musician Information
        Label nameLabel = new Label("Name:");
        Label surnameLabel = new Label("Surname:");
        Label experienceLabel = new Label("Experience:");
        Label cityLabel = new Label("City:");

        Label nameValue = new Label();
        Label surnameValue = new Label();
        Label experienceValue = new Label();
        Label cityValue = new Label();

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> showAlert(Alert.AlertType.INFORMATION, "Edit", "Edit functionality is not implemented yet."));

        // Layout for musician information
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(10);
        infoGrid.setHgap(10);
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(nameValue, 1, 0);
        infoGrid.add(surnameLabel, 0, 1);
        infoGrid.add(surnameValue, 1, 1);
        infoGrid.add(experienceLabel, 0, 2);
        infoGrid.add(experienceValue, 1, 2);
        infoGrid.add(cityLabel, 0, 3);
        infoGrid.add(cityValue, 1, 3);
        infoGrid.add(editButton, 1, 4);

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            search searchPage = new search();
            searchPage.setBandId(bandId);
            try {
                searchPage.start(new Stage());
                primaryStage.close(); // Close the current profile stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load data
        loadMusicianInfo(nameValue, surnameValue, experienceValue, cityValue);

        // Layout
        VBox infoBox = new VBox(10, new Label("Information:"), infoGrid);
        VBox root = new VBox(20, backButton, titleLabel, infoBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Profile");
        primaryStage.show();
    }

    private void loadMusicianInfo(Label nameValue, Label surnameValue, Label experienceValue, Label cityValue) {
        String musicianQuery = "SELECT musicians.name, musicians.surname, musicians.experience, cities.name AS city_name " +
                "FROM musicians " +
                "JOIN cities ON musicians.city_id = cities.id " +
                "WHERE musicians.id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement musicianStatement = connection.prepareStatement(musicianQuery)) {

            musicianStatement.setInt(1, musicianId);
            ResultSet musicianResultSet = musicianStatement.executeQuery();
            if (musicianResultSet.next()) {
                nameValue.setText(musicianResultSet.getString("name"));
                surnameValue.setText(musicianResultSet.getString("surname"));
                experienceValue.setText(musicianResultSet.getString("experience"));
                cityValue.setText(musicianResultSet.getString("city_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
