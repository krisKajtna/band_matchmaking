import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class create_band extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int musicianId;

    public void setMusicianId(int musicianId) {
        this.musicianId = musicianId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("CREATE BAND");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        TextField bandNameField = new TextField();
        bandNameField.setPromptText("Band name");

        ComboBox<String> genreComboBox = new ComboBox<>();
        populateGenreComboBox(genreComboBox);

        ComboBox<String> cityComboBox = new ComboBox<>();
        populateCityComboBox(cityComboBox);

        Button createButton = new Button("CREATE");
        createButton.setStyle("-fx-font-size: 14px;");
        createButton.setOnAction(event -> {
            String bandName = bandNameField.getText();
            String selectedGenre = genreComboBox.getValue();
            String selectedCity = cityComboBox.getValue();
            if (bandName.isEmpty() || selectedGenre == null || selectedCity == null) {
                showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill out all fields.");
            } else {
                handleCreateBand(bandName, selectedGenre, selectedCity, primaryStage);
            }
        });

        VBox root = new VBox(20, titleLabel, new Label("Band name:"), bandNameField, new Label("Genre:"), genreComboBox, new Label("City:"), cityComboBox, createButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 600, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Create Band");
        primaryStage.show();
    }

    private void populateGenreComboBox(ComboBox<String> genreComboBox) {
        List<String> genres = new ArrayList<>();
        String query = "SELECT name FROM genres";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                genres.add(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        genreComboBox.getItems().addAll(genres);
    }

    private void populateCityComboBox(ComboBox<String> cityComboBox) {
        List<String> cities = new ArrayList<>();
        String query = "SELECT name FROM cities";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                cities.add(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cityComboBox.getItems().addAll(cities);
    }

    private void handleCreateBand(String bandName, String genreName, String cityName, Stage primaryStage) {
        String genreQuery = "SELECT id FROM genres WHERE name = ?";
        String cityQuery = "SELECT id FROM cities WHERE name = ?";
        String insertBandQuery = "INSERT INTO bands (name, genre_id, city_id, no_musicians) VALUES (?, ?, ?, 0)";
        String insertBandMusicianQuery = "INSERT INTO bands_musicians (bands_id, musician_id, status) VALUES (?, ?, 'accepted')";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement genreStatement = connection.prepareStatement(genreQuery);
             PreparedStatement cityStatement = connection.prepareStatement(cityQuery);
             PreparedStatement insertBandStatement = connection.prepareStatement(insertBandQuery, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement insertBandMusicianStatement = connection.prepareStatement(insertBandMusicianQuery)) {

            // Get the genre ID from the genre name
            genreStatement.setString(1, genreName);
            ResultSet genreResultSet = genreStatement.executeQuery();
            if (!genreResultSet.next()) {
                showAlert(Alert.AlertType.ERROR, "Genre Not Found", "The selected genre was not found in the database.");
                return;
            }
            int genreId = genreResultSet.getInt("id");

            // Get the city ID from the city name
            cityStatement.setString(1, cityName);
            ResultSet cityResultSet = cityStatement.executeQuery();
            if (!cityResultSet.next()) {
                showAlert(Alert.AlertType.ERROR, "City Not Found", "The selected city was not found in the database.");
                return;
            }
            int cityId = cityResultSet.getInt("id");

            // Insert the new band into the bands table
            insertBandStatement.setString(1, bandName);
            insertBandStatement.setInt(2, genreId);
            insertBandStatement.setInt(3, cityId);
            int rowsAffected = insertBandStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = insertBandStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int bandId = generatedKeys.getInt(1);

                    // Insert the musician into the bands_musicians table
                    insertBandMusicianStatement.setInt(1, bandId);
                    insertBandMusicianStatement.setInt(2, musicianId);
                    insertBandMusicianStatement.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Band Created", "The band has been successfully created.");
                    band band = new band();
                    band.setMusicianId(musicianId);
                    band.setBandId(bandId);
                    band.start(new Stage());
                    primaryStage.close();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Insert Failed", "Failed to create the band.");
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
