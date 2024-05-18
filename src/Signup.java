import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Signup extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";

    @Override
    public void start(Stage primaryStage) {
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField surnameField = new TextField();
        surnameField.setPromptText("Surname");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        TextField experienceField = new TextField();
        experienceField.setPromptText("Experience (years)");

        TextField videoField = new TextField();
        videoField.setPromptText("Video URL");

        ComboBox<String> cityComboBox = new ComboBox<>();
        populateCityComboBox(cityComboBox);

        Button signupButton = new Button("Signup");
        signupButton.setOnAction(event -> handleSignup(
                nameField.getText(), surnameField.getText(), emailField.getText(),
                passwordField.getText(), experienceField.getText(), videoField.getText(),
                cityComboBox.getValue()));

        VBox root = new VBox(10, nameField, surnameField, emailField, passwordField, experienceField, videoField, cityComboBox, signupButton);
        Scene scene = new Scene(root, 400, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Musician Signup");
        primaryStage.show();
    }

    private void populateCityComboBox(ComboBox<String> cityComboBox) {
        List<String> cities = new ArrayList<>();
        String query = "SELECT id, name FROM cities";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                cities.add(id + " - " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        cityComboBox.getItems().addAll(cities);
    }

    private void handleSignup(String name, String surname, String email, String password, String experience, String video, String city) {
        if (city == null || city.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Failed", "Please select a city.");
            return;
        }

        int cityId = Integer.parseInt(city.split(" - ")[0]);
        String query = "INSERT INTO musicians (name, surname, mail, password, experience, video, city_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, password);
            preparedStatement.setInt(5, Integer.parseInt(experience));
            preparedStatement.setString(6, video);
            preparedStatement.setInt(7, cityId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Your account has been created.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Unable to create your account.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Signup Error", "An error occurred while trying to create your account.");
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
