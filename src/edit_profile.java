import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class edit_profile extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int musicianId;

    public void setMusicianId(int musicianId) {
        this.musicianId = musicianId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("EDIT PROFILE");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Musician Information
        Label nameLabel = new Label("Name:");
        Label surnameLabel = new Label("Surname:");
        Label experienceLabel = new Label("Experience:");
        Label cityLabel = new Label("City:");

        TextField nameField = new TextField();
        TextField surnameField = new TextField();
        TextField experienceField = new TextField();
        ComboBox<String> cityComboBox = new ComboBox<>();

        // Load current information and cities
        loadMusicianInfoAndCities(nameField, surnameField, experienceField, cityComboBox);

        // Buttons
        Button updateButton = new Button("Update");
        updateButton.setOnAction(event -> {
            try {
                int experience = Integer.parseInt(experienceField.getText());
                updateMusicianInfo(nameField.getText(), surnameField.getText(), experience, cityComboBox.getValue());
                openMyProfile(primaryStage);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Experience must be an integer.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> openMyProfile(primaryStage));

        HBox buttonBox = new HBox(10, updateButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Layout for musician information
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(10);
        infoGrid.setHgap(10);
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(nameField, 1, 0);
        infoGrid.add(surnameLabel, 0, 1);
        infoGrid.add(surnameField, 1, 1);
        infoGrid.add(experienceLabel, 0, 2);
        infoGrid.add(experienceField, 1, 2);
        infoGrid.add(cityLabel, 0, 3);
        infoGrid.add(cityComboBox, 1, 3);

        VBox root = new VBox(20, titleLabel, infoGrid, buttonBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Profile");
        primaryStage.show();
    }

    private void loadMusicianInfoAndCities(TextField nameField, TextField surnameField, TextField experienceField, ComboBox<String> cityComboBox) {
        String query = "{ call load_musician_info_and_cities(?) }";
        String citiesQuery = "{ call load_cities() }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement musicianCallableStatement = connection.prepareCall(query);
             CallableStatement citiesCallableStatement = connection.prepareCall(citiesQuery)) {

            // Load musician info
            musicianCallableStatement.setInt(1, musicianId);
            ResultSet musicianResultSet = musicianCallableStatement.executeQuery();
            if (musicianResultSet.next()) {
                nameField.setText(musicianResultSet.getString("musician_name"));
                surnameField.setText(musicianResultSet.getString("musician_surname"));
                experienceField.setText(String.valueOf(musicianResultSet.getInt("musician_experience")));
                String currentCity = musicianResultSet.getString("city_name");

                // Load cities
                ResultSet citiesResultSet = citiesCallableStatement.executeQuery();
                while (citiesResultSet.next()) {
                    cityComboBox.getItems().add(citiesResultSet.getString("city_name"));
                }

                cityComboBox.setValue(currentCity); // Set current city as selected
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void updateMusicianInfo(String name, String surname, int experience, String city) {
        String query = "{ call update_musician_info(?, ?, ?, ?, ?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, musicianId);
            callableStatement.setString(2, name);
            callableStatement.setString(3, surname);
            callableStatement.setInt(4, experience);
            callableStatement.setString(5, city);

            callableStatement.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Musician information updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void openMyProfile(Stage currentStage) {
        my_profile profilePage = new my_profile();
        profilePage.setMusicianId(musicianId);
        try {
            profilePage.start(new Stage());
            currentStage.close(); // Close the current edit_profile stage
        } catch (Exception e) {
            e.printStackTrace();
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
