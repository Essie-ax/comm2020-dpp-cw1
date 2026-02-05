package uk.ac.comm2020.dao;

import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DAO for product table.
// Do DB query here, keep it simple.
public class ProductDao {
    private final Database database;

    // keep db helper here
    public ProductDao(Database database) {
        this.database = database;
    }

    // Find all products in one category.
    public List<Product> findByCategory(String category) throws SQLException {
        String sql = "SELECT product_id, name, category, brand, description, passport_version " +
                "FROM product WHERE category = ?";
        List<Product> products = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category);

            // read list from DB
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        }

        return products;
    }

    // Check product id exist or not.
    public boolean exists(long productId) throws SQLException {
        String sql = "SELECT 1 FROM product WHERE product_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            // if any row, then it exists
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Map one DB row -> Product object.
    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("brand"),
                rs.getString("description"),
                rs.getInt("passport_version")
        );
    }
}
