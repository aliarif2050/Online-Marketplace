// MainController.java
import io.github.cdimascio.dotenv.Dotenv;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class MainController {
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton regis_radio;
    @FXML private RadioButton login_radio;
    @FXML private RadioButton buyer_radio;
    @FXML private RadioButton seller_radio;
    @FXML public static String currentUser;
    @FXML
    private void confirmRes() {
        String username = nameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and Password cannot be empty.");
            return;
        }

        if (regis_radio.isSelected()) {
            registerUser(username, password);
        } else if (login_radio.isSelected()) {
            loginUser(username, password);
        } else {
            showAlert("Error", "Please select Register or Login.");
        }
    }
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("src") // Make sure .env is in 'src' folder
            .ignoreIfMissing()
            .load();

    private void registerUser(String username, String password) {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String pass = dotenv.get("DB_PASS");
        try (Connection conn = DriverManager.getConnection(url,user,pass)) {
            String role = buyer_radio.isSelected() ? "buyer" : seller_radio.isSelected() ? "seller" : "";
            if (role.isEmpty()) {
                showAlert("Error", "Please select Buyer or Seller.");
                return;
            }
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            showAlert("Success", "Registration successful.");
        } catch (SQLException e) {
            showAlert("Error", "Registration failed: " + e.getMessage());
        }
    }

    private void loginUser(String username, String password) {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String pass = dotenv.get("DB_PASS");
        try (Connection conn = DriverManager.getConnection(url,user , pass)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equals("seller")) {
                    ProductController.currentSeller = username;
                }
                if (role.equals("buyer")) {
                    currentUser = username;
                }

                showAlert("Success", "Login successful as " + role);
            } else {
                showAlert("Error", "Invalid username or password.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Login failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
