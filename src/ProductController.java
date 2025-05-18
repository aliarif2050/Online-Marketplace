import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;

import java.sql.*;

public class ProductController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, String> colSeller;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    public static String currentSeller;

//    public void setCurrentSeller(String seller) {
//        this.currentSeller = seller;
//        loadProducts();
//    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));

        productTable.setItems(productList);
        loadProducts();
    }
    @FXML
    public void refresh(){
        loadProducts();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "123password456");
    }

    private void loadProducts() {
        productList.clear();
        String sql = "SELECT * FROM products";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("seller")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error loading products: " + e.getMessage());
        }
    }

    @FXML
    private void addProduct() {
        String name = nameField.getText();
        String desc = descField.getText();
        String priceText = priceField.getText();

        if (name.isEmpty() || priceText.isEmpty() || currentSeller.isEmpty()) {
            showAlert("Please fill in all fields and make sure you're logged in as a seller.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert("Invalid price format.");
            return;
        }

        String sql = "INSERT INTO products (name, description, price, seller) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, desc);
            stmt.setDouble(3, price);
            stmt.setString(4, currentSeller);
            stmt.executeUpdate();
            showAlert("Product added successfully!");
            clearFields();
            loadProducts();
        } catch (SQLException e) {
            showAlert("Error adding product: " + e.getMessage());
        }
    }

    @FXML
    private void removeProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a product to remove.");
            return;
        }
        if (!selected.getSeller().equals(currentSeller)) {
            showAlert("You can only remove your own products.");
            return;
        }

        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            showAlert("Product removed successfully!");
            loadProducts();
        } catch (SQLException e) {
            showAlert("Error removing product: " + e.getMessage());
        }
    }

    private void clearFields() {
        nameField.clear();
        descField.clear();
        priceField.clear();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }
}
