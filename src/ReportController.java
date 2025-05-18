import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import java.sql.*;

public class ReportController {

    @FXML private TextArea reportArea;

    private Connection connect() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "123password456");
    }

    @FXML
    private void generateReport() {
        StringBuilder report = new StringBuilder();
        try (Connection conn = connect()) {
            Statement stmt = conn.createStatement();

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs1.next()) report.append("Total Products Listed for Sale: ").append(rs1.getInt(1)).append("\n");

            ResultSet rs2 = stmt.executeQuery("SELECT name, price FROM products");
            report.append("\nListed Products:\n");
            while (rs2.next()) {
                report.append("- ").append(rs2.getString("name"))
                        .append(" (RS").append(rs2.getDouble("price")).append(")\n");
            }

            // Assuming a 'sold_products' table to track sold items
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*), SUM(price) FROM sold_products");
            if (rs3.next()) {
                report.append("\nProducts Sold: ").append(rs3.getInt(1)).append("\n");
                report.append("Total Revenue: $").append(rs3.getDouble(2)).append("\n");
            }
            ResultSet rs4 = stmt.executeQuery("SELECT name, price ,seller, buyer FROM sold_products");
            report.append("\n SOLD PRODUCTS:\n");
            while (rs4.next()) {
                report.append("- ").append(rs4.getString("name"))
                        .append(" (RS").append(rs4.getDouble("price")).append(")\n");
                        report.append("       Sold by ").append(rs4.getString("seller")).append("\n");
                        report.append("       Bought by ").append(rs4.getString("buyer")).append("\n");
            }

            reportArea.setText(report.toString());
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
