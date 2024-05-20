import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class band_view extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int bandId; // Assume this is set when the band is created
    private int loggedInMusicianId; // Store the logged-in musician's ID

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

    public void setLoggedInMusicianId(int loggedInMusicianId) {
        this.loggedInMusicianId = loggedInMusicianId;
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("BAND");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            my_profile profilePage = new my_profile();
            profilePage.setMusicianId(loggedInMusicianId); // Use the logged-in musician's ID
            try {
                profilePage.start(new Stage());
                primaryStage.close(); // Close the current band_view stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Band Information
        Label nameLabel = new Label("Name:");
        Label genreLabel = new Label("Genre:");
        Label cityLabel = new Label("City:");
        Label nameValue = new Label();
        Label genreValue = new Label();
        Label cityValue = new Label();

        // Layout for band information
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(10);
        infoGrid.setHgap(10);
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(nameValue, 1, 0);
        infoGrid.add(genreLabel, 0, 1);
        infoGrid.add(genreValue, 1, 1);
        infoGrid.add(cityLabel, 0, 2);
        infoGrid.add(cityValue, 1, 2);

        // Members
        TableView<Member> membersTable = new TableView<>();
        membersTable.setId("membersTable");
        TableColumn<Member, String> memberNameColumn = new TableColumn<>("Name");
        memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Member, String> memberSurnameColumn = new TableColumn<>("Surname");
        memberSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        TableColumn<Member, Void> viewProfileColumn = new TableColumn<>("View");
        viewProfileColumn.setCellFactory(col -> new TableCell<Member, Void>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    // Add functionality to view member profile if needed
                    showAlert(Alert.AlertType.INFORMATION, "Member Profile", "This feature has not been implemented yet.");
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

        membersTable.getColumns().addAll(memberNameColumn, memberSurnameColumn, viewProfileColumn);

        // Load data
        loadBandInfo(nameValue, genreValue, cityValue);
        loadMembers(membersTable);

        // Layout
        VBox infoBox = new VBox(10, new Label("Information:"), infoGrid);
        VBox membersBox = new VBox(10, new Label("Members:"), membersTable);

        HBox mainLayout = new HBox(20, infoBox, membersBox);
        VBox root = new VBox(20, backButton, titleLabel, mainLayout);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Band View");
        primaryStage.show();
    }

    private void loadBandInfo(Label nameValue, Label genreValue, Label cityValue) {
        String query = "SELECT bands.name AS band_name, genres.name AS genre_name, cities.name AS city_name " +
                "FROM bands " +
                "JOIN genres ON bands.genre_id = genres.id " +
                "JOIN cities ON bands.city_id = cities.id " +
                "WHERE bands.id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, bandId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                nameValue.setText(resultSet.getString("band_name"));
                genreValue.setText(resultSet.getString("genre_name"));
                cityValue.setText(resultSet.getString("city_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void loadMembers(TableView<Member> membersTable) {
        List<Member> members = new ArrayList<>();
        String query = "SELECT musicians.id, musicians.name, musicians.surname " +
                "FROM musicians " +
                "JOIN bands_musicians ON musicians.id = bands_musicians.musician_id " +
                "WHERE bands_musicians.bands_id = ? AND bands_musicians.status = 'accepted'";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, bandId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                members.add(new Member(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("surname")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }

        membersTable.getItems().setAll(members);
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

    // Member class to represent musicians
    public static class Member {
        private final int id;
        private final String name;
        private final String surname;

        public Member(int id, String name, String surname) {
            this.id = id;
            this.name = name;
            this.surname = surname;
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
    }
}
