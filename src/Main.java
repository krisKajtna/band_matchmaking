import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main extends Application {

    private static final String URL = "jdbc:postgresql://ep-rapid-wind-a2vh6va0.eu-central-1.aws.neon.tech:5432/band_match";
    private static final String USER = "band_match_owner";
    private static final String PASSWORD = "U5FJBfvqLa4D";
    private int loggedInMusicianId;

    @Override
    public void start(Stage primaryStage) {
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> handleLogin(emailField.getText(), passwordField.getText(), primaryStage));

        Button signupButton = new Button("Signup");
        signupButton.setOnAction(event -> {
            Signup signup = new Signup();
            try {
                signup.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
        });

        VBox root = new VBox(10, emailField, passwordField, loginButton, signupButton);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Musician Login");
        primaryStage.show();
    }

    private void handleLogin(String email, String password, Stage loginStage) {
        String query = "SELECT * FROM musicians WHERE mail = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                loggedInMusicianId = resultSet.getInt("id");
                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + resultSet.getString("name") + "!");
                instruments instruments = new instruments();
                instruments.setMusicianId(loggedInMusicianId);
                instruments.start(new Stage());
                loginStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login Error", "An error occurred while trying to log in.");
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
