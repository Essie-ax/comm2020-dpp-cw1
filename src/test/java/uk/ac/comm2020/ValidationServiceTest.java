package uk.ac.comm2020;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.comm2020.model.Evidence;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.service.ValidationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationServiceTest {

    private ValidationService service;

    @BeforeEach
    void setUp() {
        service = new ValidationService();
    }

    // Build a minimal valid passport so individual rules can be tested in isolation.
    private Passport makePassport(JsonObject fields) {
        return new Passport(1, 1, 1, "DRAFT", fields, 0, 0);
    }

    // --- Test 1: Valid passport with all required fields passes ---
    @Test
    void validPassportProducesNoErrors() {
        JsonObject f = new JsonObject();
        f.addProperty("name", "EcoBattery");
        f.addProperty("brand", "GreenCell");
        f.addProperty("category", "Battery");
        f.addProperty("origin", "Germany");
        f.addProperty("chemistry", "Lithium-Ion");

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.isEmpty(), "Expected no errors but got: " + errors);
    }

    // --- Test 2: Missing brand triggers error ---
    @Test
    void missingBrandReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("name", "Test");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "UK");

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("brand")));
    }

    // --- Test 3: Negative weight fails ---
    @Test
    void negativeWeightReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "Acme");
        f.addProperty("name", "Widget");
        f.addProperty("category", "Electronics");
        f.addProperty("origin", "US");
        f.addProperty("weight", -1.5);

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("weight")));
    }

    // --- Test 4: recyclable_percentage > 100 fails ---
    @Test
    void recyclablePercentageOver100ReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "Eco");
        f.addProperty("name", "Tray");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "France");
        f.addProperty("recyclable_percentage", 150);

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("recyclable_percentage")));
    }

    // --- Test 5: Future manufacture_date fails ---
    @Test
    void futureManufactureDateReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "Acme");
        f.addProperty("name", "Widget");
        f.addProperty("category", "Electronics");
        f.addProperty("origin", "US");
        f.addProperty("manufacture_date", "2099-01-01");

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("manufacture_date")));
    }

    // --- Test 6: expiry_date before manufacture_date fails ---
    @Test
    void expiryBeforeManufactureDateReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "Acme");
        f.addProperty("name", "Widget");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "US");
        f.addProperty("manufacture_date", "2024-01-01");
        f.addProperty("expiry_date", "2023-01-01");

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("expiry_date")));
    }

    // --- Test 7: Organic claim without CERTIFICATE evidence fails ---
    @Test
    void organicClaimWithoutCertificateReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "PureLeaf");
        f.addProperty("name", "Tea");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "India");
        f.addProperty("organic", true);

        // No evidence passed - should fail
        List<String> errors = service.validate(makePassport(f), List.of());
        assertTrue(errors.stream().anyMatch(e -> e.contains("organic")));
    }

    // --- Test 8: Battery without chemistry fails ---
    @Test
    void batteryWithoutChemistryReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "VoltMax");
        f.addProperty("name", "AA Cell");
        f.addProperty("category", "Battery");
        f.addProperty("origin", "China");

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("chemistry")));
    }

    // --- Test 9: Recyclable true but missing end_of_life fails ---
    @Test
    void recyclableWithoutEndOfLifeReturnsError() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "GreenPack");
        f.addProperty("name", "Box");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "Sweden");
        f.addProperty("recyclable", true);

        List<String> errors = service.validate(makePassport(f));
        assertTrue(errors.stream().anyMatch(e -> e.contains("end_of_life")));
    }

    // --- Test 10: Organic with CERTIFICATE evidence passes that rule ---
    @Test
    void organicClaimWithCertificatePassesOrganicRule() {
        JsonObject f = new JsonObject();
        f.addProperty("brand", "PureLeaf");
        f.addProperty("name", "Tea");
        f.addProperty("category", "Textiles");
        f.addProperty("origin", "India");
        f.addProperty("organic", true);

        // Provide a CERTIFICATE evidence - the organic rule should not fire
        Evidence cert = new Evidence(1, 1, "organic", "CERTIFICATE", "BioOrg", "Certified organic");
        List<String> errors = service.validate(makePassport(f), List.of(cert));
        assertTrue(errors.stream().noneMatch(e -> e.contains("organic")));
    }
}
