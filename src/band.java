import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

public class band extends Application {

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
        Label titleLabel = new Label("BAND");
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Band Information
        Label nameLabel = new Label("Name:");
        Label genreLabel = new Label("Genre:");
        Label cityLabel = new Label("City:");
        Label nameValue = new Label();
        Label genreValue = new Label();
        Label cityValue = new Label();

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> showAlert(Alert.AlertType.INFORMATION, "Edit", "Edit functionality is not implemented yet."));

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
        infoGrid.add(editButton, 1, 3);

        // Members
        TableView<Member> membersTable = new TableView<>();
        TableColumn<Member, String> memberNameColumn = new TableColumn<>("Name");
        memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Member, String> memberSurnameColumn = new TableColumn<>("surname");
        memberSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        TableColumn<Member, Void> viewProfileColumn = new TableColumn<>("View");
        viewProfileColumn.setCellFactory(col -> new TableCell<Member, Void>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    openProfile(member.getId(), primaryStage);
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

        TableColumn<Member, Void> removeMemberColumn = new TableColumn<>("Remove");
        removeMemberColumn.setCellFactory(col -> new TableCell<Member, Void>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    Member member = getTableView().getItems().get(getIndex());
                    handleRemove(member, membersTable);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });

        membersTable.getColumns().addAll(memberNameColumn, memberSurnameColumn, viewProfileColumn, removeMemberColumn);

        // Pending Invites
        TableView<Member> invitesTable = new TableView<>();
        TableColumn<Member, String> inviteNameColumn = new TableColumn<>("Name");
        inviteNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Member, String> inviteSurnameColumn = new TableColumn<>("surname");
        inviteSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        invitesTable.getColumns().addAll(inviteNameColumn, inviteSurnameColumn);

        // Load data
        loadBandInfo(nameValue, genreValue, cityValue);
        loadMembers(membersTable);
        loadPendingInvites(invitesTable);

        // Search for Musicians Button
        Button searchButton = new Button("Search for Musicians");
        searchButton.setOnAction(event -> {
            search searchPage = new search();
            searchPage.setBandId(bandId);
            try {
                searchPage.start(new Stage());
                primaryStage.close(); // Close the current band stage
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Layout
        VBox infoBox = new VBox(10, new Label("Information:"), infoGrid);
        VBox membersBox = new VBox(10, new Label("Members:"), membersTable);
        VBox invitesBox = new VBox(10, new Label("Pending invites:"), invitesTable);

        HBox mainLayout = new HBox(20, infoBox, membersBox, invitesBox);
        VBox root = new VBox(20, titleLabel, mainLayout, searchButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-border-color: black; -fx-border-width: 2px;");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Band");
        primaryStage.show();
    }

    private void loadBandInfo(Label nameValue, Label genreValue, Label cityValue) {
        String query = "{ call load_band_info(?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, bandId);
            ResultSet resultSet = callableStatement.executeQuery();
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
        String query = "{ call load_band_members(?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, bandId);
            ResultSet resultSet = callableStatement.executeQuery();
            while (resultSet.next()) {
                members.add(new Member(resultSet.getInt("musician_id"), resultSet.getString("musician_name"), resultSet.getString("musician_surname")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }

        membersTable.getItems().setAll(members);
    }

    private void loadPendingInvites(TableView<Member> invitesTable) {
        List<Member> invites = new ArrayList<>();
        String query = "{ call load_pending_invites(?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, bandId);
            ResultSet resultSet = callableStatement.executeQuery();
            while (resultSet.next()) {
                invites.add(new Member(resultSet.getInt("musician_id"), resultSet.getString("musician_name"), resultSet.getString("musician_surname")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }

        invitesTable.getItems().setAll(invites);
    }

    private void handleRemove(Member member, TableView<Member> membersTable) {
        String query = "{ call remove_band_member(?, ?) }";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement callableStatement = connection.prepareCall(query)) {

            callableStatement.setInt(1, bandId);
            callableStatement.setInt(2, member.getId());
            int rowsAffected = callableStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Member Removed", "The member has been removed from the band.");
                loadMembers(membersTable); // Refresh the members table
            } else {
                showAlert(Alert.AlertType.ERROR, "Remove Failed", "Failed to remove the member.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        }
    }

    private void openProfile(int musicianId, Stage bandStage) {
        profile profile = new profile();
        profile.setMusicianId(musicianId);
        profile.setBandId(bandId);
        try {
            profile.start(new Stage());
            bandStage.close(); // Close the current band stage
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
