
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class BuyController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, String> nameCol;

    @FXML
    private TableColumn<Product, String> descCol;

    @FXML
    private TableColumn<Product, Double> priceCol;

    @FXML
    private TableColumn<Product, String> sellerCol;

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> searchChoiceBox;

    @FXML
    private Button searchButton;

    @FXML
    private Button buyButton;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    private Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "123password456");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void initialize() {
        // Setup table columns
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        sellerCol.setCellValueFactory(cellData -> cellData.getValue().sellerProperty());

        // Populate choice box
        searchChoiceBox.getItems().addAll("Name", "Description", "Seller");
        searchChoiceBox.setValue("Name"); // default

        // Load all products initially
        loadProducts("");

        // Event handlers
        searchButton.setOnAction(e -> performSearch());
        buyButton.setOnAction(e -> performBuy());
    }

    private void loadProducts(String condition) {
        productList.clear();
        String query = "SELECT * FROM products " + condition;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("seller")
                );
                productList.add(p);
            }

            productTable.setItems(productList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        String field = switch (searchChoiceBox.getValue()) {
            case "Description" -> "description";
            case "Seller" -> "seller";
            default -> "name";
        };

        if (!keyword.isEmpty()) {
            loadProducts("WHERE " + field + " LIKE '%" + keyword + "%'");
        } else {
            loadProducts(""); // load all
        }
    }

    private void performBuy() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a product to buy.");
            return;
        }

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            // Insert into sold_products
            String insertSQL = "INSERT INTO sold_products (name, description, price, seller,buyer) VALUES (?, ?, ?, ?,?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                insertStmt.setString(1, selected.getName());
                insertStmt.setString(2, selected.getDescription());
                insertStmt.setDouble(3, selected.getPrice());
                insertStmt.setString(4, selected.getSeller());
                insertStmt.setString(5,MainController.currentUser);
                insertStmt.executeUpdate();
            }

            // Delete from products
            String deleteSQL = "DELETE FROM products WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                deleteStmt.setInt(1, selected.getId());
                deleteStmt.executeUpdate();
            }

            conn.commit();
            showAlert("Success", "Product purchased successfully!");
            loadProducts(""); // Refresh table

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to complete purchase.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
