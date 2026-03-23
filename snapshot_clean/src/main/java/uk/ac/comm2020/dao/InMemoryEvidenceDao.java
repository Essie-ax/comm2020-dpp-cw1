package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.Evidence;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEvidenceDao implements EvidenceRepository {

    private final List<Evidence> store = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public InMemoryEvidenceDao() {
        seedDemoData();
    }

    private void seedDemoData() {
        // Passport 1 has enough evidence to produce a higher confidence score.
        store.add(new Evidence(1, 1, "chemistry", "CERTIFICATE", "TUV Rheinland", "Li-Ion safety certification"));
        store.add(new Evidence(2, 1, "recyclable", "AUDIT", "EU Recycling Board", "Meets EU battery recycling directive"));
        store.add(new Evidence(3, 1, "origin", "SUPPLIER_STATEMENT", "GreenCell GmbH", "Manufactured in Berlin facility"));

        store.add(new Evidence(4, 2, "origin", "SUPPLIER_STATEMENT", "Dhaka Textiles Ltd", "Factory audit passed"));

        // Passport 3 keeps zero evidence so confidence can drop to 0 in tests.
        store.add(new Evidence(5, 4, "recyclable", "TEST_REPORT", "SGS Labs", "Material recovery rate tested at 85%"));

        // Passport 5 has evidence but no CERTIFICATE, used for organic-rule negative test.
        store.add(new Evidence(6, 5, "origin", "AUDIT", "Fair Trade Org", "Fair trade certified supplier"));
        store.add(new Evidence(7, 5, "weight", "TEST_REPORT", "TextileLab", "Weight per unit verified"));

        store.add(new Evidence(8, 6, "recyclable", "CERTIFICATE", "WEEE Compliance", "Meets WEEE directive requirements"));
        store.add(new Evidence(9, 6, "origin", "SUPPLIER_STATEMENT", "TechNova Seoul", "Assembled in Suwon plant"));

        idGen.set(10);
    }

    @Override
    public void save(Evidence evidence) {
        long id = idGen.getAndIncrement();
        store.add(new Evidence(id, evidence.getPassportId(), evidence.getFieldKey(),
                evidence.getType(), evidence.getIssuer(), evidence.getSummary()));
    }

    @Override
    public List<Evidence> findByPassportId(long passportId) {
        List<Evidence> result = new ArrayList<>();
        for (Evidence e : store) {
            if (e.getPassportId() == passportId) {
                result.add(e);
            }
        }
        return result;
    }
}
