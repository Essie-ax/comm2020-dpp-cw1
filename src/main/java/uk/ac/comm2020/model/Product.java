package uk.ac.comm2020.model;

// Product model.
public class Product {
    private final long productId;
    private final String name;
    private final String category;
    private final String brand;
    private final String description;
    private final int passportVersion;

    // Build one product object.
    public Product(long productId, String name, String category, String brand, String description, int passportVersion) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.description = description;
        this.passportVersion = passportVersion;
    }

    // product id in DB
    public long getProductId() {
        return productId;
    }

    // product name
    public String getName() {
        return name;
    }

    // product category
    public String getCategory() {
        return category;
    }

    // product brand
    public String getBrand() {
        return brand;
    }

    // simple text about product
    public String getDescription() {
        return description;
    }

    // version for passport data (for upgrade maybe)
    public int getPassportVersion() {
        return passportVersion;
    }
}
