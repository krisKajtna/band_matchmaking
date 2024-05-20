import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class search extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int bandId;

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("SEARCH");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Filter
        Label filterLabel = new Label("Filter:");
        Label instrumentLabel = new Label("Instrument:");
        ComboBox<String> instrumentComboBox = new ComboBox<>();

        // Available Musicians
        TableView<Musician> musiciansTable = new TableView<>();
        TableColumn<Musician, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Musician, String> surnameColumn = new TableColumn<>("Surname");
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        TableColumn<Musician, String> experienceColumn = new TableColumn<>("Experience");
        experienceColumn.setCellValueFactory(new PropertyValueFactory<>("experience"));
        TableColumn<Musician, Void> viewProfileColumn = new TableColumn<>("View");
        viewProfileColumn.setCellFactory(col -> new TableCell<Musician, Void>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    Musician musician = getTableView().getItems().get(getIndex());
                    openProfile(musician.getId(), primaryStage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        TableColumn<Musician, Void> inviteColumn = new TableColumn<>("Invite");
        inviteColumn.setCellFactory(col -> new TableCell<Musician, Void>() {
            private final Button inviteButton = new Button("Invite");

            {
                inviteButton.setOnAction(event -> {
                    Musician musician = getTableView().getItems().get(getIndex());
                    handleInvite(musician);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(inviteButton);
                }
            }
        });
        musiciansTable.getColumns().addAll(nameColumn, surnameColumn, experienceColumn, viewProfileColumn, inviteColumn);

        instrumentComboBox.setOnAction(event -> {
            String selectedInstrument = instrumentComboBox.getValue();
            loadMusicians(musiciansTable, selectedInstrument);
        });

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            band bandPage = new band();
            bandPage.setBandId(bandId);
            try {
                bandPage.start(new Stage());
                primaryStage.close(); // Close the current search stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Load data
        loadInstruments(instrumentComboBox);

        // Layout
        HBox filterBox = new HBox(10, instrumentLabel, instrumentComboBox);
        VBox musiciansBox = new VBox(10, new Label("Available musicians:"), musiciansTable);

        VBox root = new VBox(20, backButton, titleLabel, filterLabel, filterBox, musiciansBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Search");
        primaryStage.show();
    }

    private void loadInstruments(ComboBox<String> instrumentComboBox) {
        List<String> instruments = new ArrayList<>();
        String query = "SELECT name FROM instruments";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                instruments.add(resultSet.getString("name"));
            }

            instrumentComboBox.getItems().setAll(instruments);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void loadMusicians(TableView<Musician> musiciansTable, String instrument) {
        List<Musician> musicians = new ArrayList<>();
        String query = "SELECT musicians.id, musicians.name, musicians.surname, musicians.experience " +
                "FROM musicians " +
                "JOIN instruments_musicians ON musicians.id = instruments_musicians.musician_id " +
                "JOIN instruments ON instruments_musicians.instrument_id = instruments.id " +
                "LEFT JOIN bands_musicians ON musicians.id = bands_musicians.musician_id " +
                "WHERE instruments.name = ? AND (bands_musicians.status IS NULL OR bands_musicians.status NOT IN ('pending', 'accepted'))";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, instrument);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                musicians.add(new Musician(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("surname"), resultSet.getString("experience")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }

        musiciansTable.getItems().setAll(musicians);
    }

    private void handleInvite(Musician musician) {
        String query = "INSERT INTO bands_musicians (bands_id, musician_id, status) VALUES (?, ?, 'pending')";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, bandId);
            preparedStatement.setInt(2, musician.getId());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Invite Sent", "The invite has been sent.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Invite Failed", "Failed to send the invite.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void openProfile(int musicianId, Stage searchStage) {
        profile profilePage = new profile();
        profilePage.setMusicianId(musicianId);
        profilePage.setBandId(bandId);
        try {
            profilePage.start(new Stage());
            searchStage.close(); // Close the current search stage
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

    // Musician class to represent available musicians
    public static class Musician {
        private final int id;
        private final String name;
        private final String surname;
        private final String experience;

        public Musician(int id, String name, String surname, String experience) {
            this.id = id;
            this.name = name;
            this.surname = surname;
            this.experience = experience;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSurname() {
            return surname;
        }

        public String getExperience() {
            return experience;
        }
    }
}
