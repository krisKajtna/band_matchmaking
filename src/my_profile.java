import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class my_profile extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int musicianId;

    public void setMusicianId(int musicianId) {
        this.musicianId = musicianId;
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
        Label bandLabel = new Label("Band:");

        Label nameValue = new Label();
        Label surnameValue = new Label();
        Label experienceValue = new Label();
        Label cityValue = new Label();
        Label bandValue = new Label();

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> {
            edit_profile editProfilePage = new edit_profile();
            editProfilePage.setMusicianId(musicianId);
            try {
                editProfilePage.start(new Stage());
                primaryStage.close(); // Close the current my_profile stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            instruments instrumentsPage = new instruments();
            instrumentsPage.setMusicianId(musicianId); // Pass the logged-in musician ID
            try {
                instrumentsPage.start(new Stage());
                primaryStage.close(); // Close the current my_profile stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
        infoGrid.add(bandLabel, 0, 4);
        infoGrid.add(bandValue, 1, 4);
        infoGrid.add(editButton, 1, 5);

        // Band Requests
        TableView<BandRequest> requestsTable = new TableView<>();
        TableColumn<BandRequest, String> bandNameColumn = new TableColumn<>("Band name");
        bandNameColumn.setCellValueFactory(new PropertyValueFactory<>("bandName"));
        TableColumn<BandRequest, Void> viewProfileColumn = new TableColumn<>("View");
        viewProfileColumn.setCellFactory(col -> new TableCell<BandRequest, Void>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    BandRequest request = getTableView().getItems().get(getIndex());
                    showAlert(Alert.AlertType.INFORMATION, "Band Profile", "Band profile view is not implemented yet.");
                    openBandView(primaryStage, request.getBandId());
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
        TableColumn<BandRequest, Void> acceptColumn = new TableColumn<>("Accept");
        acceptColumn.setCellFactory(col -> new TableCell<BandRequest, Void>() {
            private final Button acceptButton = new Button("Accept");

            {
                acceptButton.setOnAction(event -> {
                    BandRequest request = getTableView().getItems().get(getIndex());
                    handleRequest(request, "accepted", requestsTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(acceptButton);
                }
            }
        });
        TableColumn<BandRequest, Void> declineColumn = new TableColumn<>("Decline");
        declineColumn.setCellFactory(col -> new TableCell<BandRequest, Void>() {
            private final Button declineButton = new Button("Decline");

            {
                declineButton.setOnAction(event -> {
                    BandRequest request = getTableView().getItems().get(getIndex());
                    handleRequest(request, "declined", requestsTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(declineButton);
                }
            }
        });
        requestsTable.getColumns().addAll(bandNameColumn, viewProfileColumn, acceptColumn, declineColumn);

        // Load data
        loadMusicianInfo(nameValue, surnameValue, experienceValue, cityValue, bandValue);
        loadBandRequests(requestsTable);

        // Layout
        VBox infoBox = new VBox(10, new Label("Information:"), infoGrid);
        VBox requestsBox = new VBox(10, new Label("Requests:"), requestsTable);

        HBox mainLayout = new HBox(20, infoBox, requestsBox);
        VBox root = new VBox(20, titleLabel, mainLayout, backButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Profile");
        primaryStage.show();
    }

    private void loadMusicianInfo(Label nameValue, Label surnameValue, Label experienceValue, Label cityValue, Label bandValue) {
        String query = "{ call load_musician_info(?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, musicianId);
            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                nameValue.setText(resultSet.getString("musician_name"));
                surnameValue.setText(resultSet.getString("musician_surname"));
                experienceValue.setText(String.valueOf(resultSet.getInt("musician_experience")));
                cityValue.setText(resultSet.getString("city_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void loadBandRequests(TableView<BandRequest> requestsTable) {
        List<BandRequest> requests = new ArrayList<>();
        String query = "{ call load_band_requests(?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, musicianId);
            ResultSet resultSet = callableStatement.executeQuery();
            while (resultSet.next()) {
                requests.add(new BandRequest(resultSet.getInt("band_id"), resultSet.getString("band_name")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }

        requestsTable.getItems().setAll(requests);
    }

    private void handleRequest(BandRequest request, String status, TableView<BandRequest> requestsTable) {
        String query = "{ call handle_request(?, ?, ?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, request.getBandId());
            callableStatement.setInt(2, musicianId);
            callableStatement.setString(3, status);

            int rowsAffected = callableStatement.executeUpdate();
            if (rowsAffected > 0) {
                String message = status.equals("declined") ? "The request has been declined and removed." : "The request has been " + status + ".";
                showAlert(Alert.AlertType.INFORMATION, "Request " + status, message);
                loadBandRequests(requestsTable);
            } else {
                showAlert(Alert.AlertType.ERROR, "Request Failed", "Failed to update the request.");
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

    private void openBandView(Stage currentStage, int bandId) {
        band_view bandViewPage = new band_view();
        bandViewPage.setBandId(bandId);
        bandViewPage.setLoggedInMusicianId(musicianId); // Pass the logged-in musician ID
        try {
            bandViewPage.start(new Stage());
            currentStage.close(); // Close the current stage
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // BandRequest class to represent band requests
    public static class BandRequest {
        private final int bandId;
        private final String bandName;

        public BandRequest(int bandId, String bandName) {
            this.bandId = bandId;
            this.bandName = bandName;
        }

        public int getBandId() {
            return bandId;
        }

        public String getBandName() {
            return bandName;
        }
    }
}
