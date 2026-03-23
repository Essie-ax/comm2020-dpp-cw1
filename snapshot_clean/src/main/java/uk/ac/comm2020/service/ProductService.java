package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.ProductDao;
import uk.ac.comm2020.model.Product;

import java.sql.SQLException;
import java.util.List;

// Service for product.
// Mostly just call DAO, and wrap DB error.
public class ProductService {
    private final ProductDao productDao;

    // keep dao here
    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    // Get products list by category.
    public List<Product> getProductsByCategory(String category) {
        try {
            return productDao.findByCategory(category);
        } catch (SQLException e) {
            // DB fail, return 500
            throw new ServiceException("DATABASE_ERROR", "Failed to load products", 500, null, e);
        }
    }
}
