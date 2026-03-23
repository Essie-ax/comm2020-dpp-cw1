package uk.ac.comm2020.dao;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import uk.ac.comm2020.model.Passport;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryPassportDao implements PassportRepository {

    private final Map<Long, Passport> store = new LinkedHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public InMemoryPassportDao() {
        seedDemoData();
    }

    private void seedDemoData() {
        // Keep one clean sample so positive-path validation is easy to test.
        JsonObject f1 = new JsonObject();
        f1.addProperty("name", "EcoPower Li-Ion 3000mAh");
        f1.addProperty("brand", "GreenCell");
        f1.addProperty("category", "Battery");
        f1.addProperty("origin", "Germany");
        f1.addProperty("weight", 0.35);
        f1.addProperty("recyclable", true);
        f1.addProperty("recyclable_percentage", 78);
        f1.addProperty("end_of_life", "Return to certified e-waste facility");
        f1.addProperty("manufacture_date", "2025-06-15");
        f1.addProperty("expiry_date", "2030-06-15");
        f1.addProperty("chemistry", "Lithium-Ion");
        store.put(1L, new Passport(1, 1, 1, "PUBLISHED", f1, 92.0, 80.0));

        JsonObject f2 = new JsonObject();
        f2.addProperty("category", "Textiles");
        f2.addProperty("origin", "Bangladesh");
        f2.addProperty("weight", 0.2);
        f2.addProperty("recyclable", false);
        f2.addProperty("manufacture_date", "2025-03-01");
        store.put(2L, new Passport(2, 2, 1, "DRAFT", f2, 35.0, 20.0));

        // Intentionally broken values to exercise date/number validation.
        JsonObject f3 = new JsonObject();
        f3.addProperty("name", "Glow Serum X");
        f3.addProperty("brand", "PureSkin");
        f3.addProperty("category", "Cosmetics");
        f3.addProperty("weight", -5.0);
        f3.addProperty("manufacture_date", "2028-01-01");
        f3.addProperty("expiry_date", "2027-06-01");
        store.put(3L, new Passport(3, 3, 2, "DRAFT", f3, 40.0, 0.0));

        JsonObject f4 = new JsonObject();
        f4.addProperty("name", "QuickCharge 500");
        f4.addProperty("brand", "VoltMax");
        f4.addProperty("category", "Battery");
        f4.addProperty("origin", "China");
        f4.addProperty("weight", 0.15);
        f4.addProperty("recyclable", true);
        f4.addProperty("recyclable_percentage", 120);
        f4.addProperty("end_of_life", "Recycle at local centre");
        f4.addProperty("manufacture_date", "2025-09-01");
        store.put(4L, new Passport(4, 4, 1, "DRAFT", f4, 60.0, 40.0));

        // Organic=true without a certificate should trigger evidence-dependent rule.
        JsonObject f5 = new JsonObject();
        f5.addProperty("name", "Organic Cotton Tee");
        f5.addProperty("brand", "EcoWear");
        f5.addProperty("category", "Textiles");
        f5.addProperty("origin", "India");
        f5.addProperty("weight", 0.3);
        f5.addProperty("organic", true);
        f5.addProperty("recyclable", false);
        f5.addProperty("manufacture_date", "2025-08-10");
        store.put(5L, new Passport(5, 5, 1, "PUBLISHED", f5, 75.0, 60.0));

        JsonObject f6 = new JsonObject();
        f6.addProperty("name", "SmartTracker Pro");
        f6.addProperty("brand", "TechNova");
        f6.addProperty("category", "Electronics");
        f6.addProperty("origin", "South Korea");
        f6.addProperty("weight", 0.08);
        f6.addProperty("recyclable", true);
        f6.addProperty("recyclable_percentage", 65);
        f6.addProperty("end_of_life", "Return to manufacturer");
        f6.addProperty("manufacture_date", "2025-11-20");
        store.put(6L, new Passport(6, 6, 2, "PUBLISHED", f6, 70.0, 40.0));

        idGen.set(7);
    }

    @Override
    public Optional<Passport> findById(long passportId) {
        return Optional.ofNullable(store.get(passportId));
    }

    @Override
    public Passport createDraft(long productId, long templateId, long createdBy) {
        long id = idGen.getAndIncrement();
        Passport p = new Passport(id, productId, templateId, "DRAFT", new JsonObject(), 0.0, 0.0);
        store.put(id, p);
        return p;
    }

    @Override
    public void updateFields(long passportId, String fieldsJson, double completenessScore, double confidenceScore) {
        Passport old = store.get(passportId);
        if (old == null) return;
        JsonObject newFields = JsonParser.parseString(fieldsJson).getAsJsonObject();
        store.put(passportId, new Passport(
                passportId, old.getProductId(), old.getTemplateId(),
                old.getStatus(), newFields, completenessScore, confidenceScore));
    }

    public List<Passport> findAll() {
        return new ArrayList<>(store.values());
    }
}
