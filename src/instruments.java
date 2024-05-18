import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class instruments extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int musicianId;

    public void setMusicianId(int musicianId) {
        this.musicianId = musicianId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("INŠTRUMENTI");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("WHAT INSTRUMENTS DO YOU PLAY:");
        instructionLabel.setStyle("-fx-font-size: 16px;");

        ComboBox<String> instrumentComboBox = new ComboBox<>();
        populateInstrumentComboBox(instrumentComboBox);

        Button addInstrumentButton = new Button("ADD INSTRUMENT");
        addInstrumentButton.setStyle("-fx-font-size: 14px;");
        addInstrumentButton.setOnAction(event -> {
            String selectedInstrument = instrumentComboBox.getValue();
            if (selectedInstrument != null) {
                handleAddInstrument(selectedInstrument);
            } else {
                showAlert(Alert.AlertType.ERROR, "No Instrument Selected", "Please select an instrument.");
            }
        });

        Button bandsButton = new Button("BANDS");
        bandsButton.setStyle("-fx-font-size: 14px;");
        bandsButton.setOnAction(event -> {
            create_band createBand = new create_band();
            try {
                createBand.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
        });

        HBox buttonsBox = new HBox(20, addInstrumentButton, bandsButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, titleLabel, instructionLabel, instrumentComboBox, buttonsBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 600, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Instruments");
        primaryStage.show();
    }

    private void populateInstrumentComboBox(ComboBox<String> instrumentComboBox) {
        List<String> instruments = new ArrayList<>();
        String query = "SELECT name FROM instruments";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                instruments.add(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        instrumentComboBox.getItems().addAll(instruments);
    }

    private void handleAddInstrument(String instrumentName) {
        String selectQuery = "SELECT id FROM instruments WHERE name = ?";
        String checkQuery = "SELECT * FROM instruments_musicians WHERE musician_id = ? AND instrument_id = ?";
        String insertQuery = "INSERT INTO instruments_musicians (musician_id, instrument_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
             PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

            // Get the instrument ID from the instrument name
            selectStatement.setString(1, instrumentName);
            ResultSet selectResultSet = selectStatement.executeQuery();
            if (selectResultSet.next()) {
                int instrumentId = selectResultSet.getInt("id");

                // Check if the instrument is already added for the musician
                checkStatement.setInt(1, musicianId);
                checkStatement.setInt(2, instrumentId);
                ResultSet checkResultSet = checkStatement.executeQuery();
                if (checkResultSet.next()) {
                    showAlert(Alert.AlertType.ERROR, "Already Selected", "You have already selected this instrument.");
                } else {
                    // Insert the new instrument for the musician
                    insertStatement.setInt(1, musicianId);
                    insertStatement.setInt(2, instrumentId);
                    int rowsAffected = insertStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Successfully Added", "The instrument has been successfully added.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Insert Failed", "Failed to add the instrument.");
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Instrument Not Found", "The selected instrument was not found in the database.");
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
