package promptsanitizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import javax.swing.table.TableModel;
import static org.junit.jupiter.api.Assertions.*;

class DictionaryModelTest {

    private DictionaryModel model;

    @BeforeEach void setUp() {
        model = new DictionaryModel();
    }

    // --- row / column basics ---

    @Test void empty_model_has_zero_rows_and_two_columns() {
        assertEquals(2, model.getColumnCount());
        assertEquals("Sensitive", model.getColumnName(0));
        assertEquals("Safe", model.getColumnName(1));
    }

    // --- setValueAt / getValueAt ---

    @Test void set_value_updates_cell() {
        int row = model.addRow();
        model.setValueAt("secret", row, 0);
        model.setValueAt("placeholder", row, 1);
        assertEquals("secret", model.getValueAt(row, 0));
        assertEquals("placeholder", model.getValueAt(row, 1));
    }

    @Test void null_value_becomes_empty_string() {
        int row = model.addRow();
        model.setValueAt(null, row, 0);
        assertEquals("", model.getValueAt(row, 0));
    }

    // --- addRow / removeRow ---

    @Test void add_row_increases_count() {
        assertEquals(0, model.getRowCount());
        int idx = model.addRow();
        assertEquals(1, model.getRowCount());
        assertEquals(idx, 0);
    }

    @Test void remove_row_decreases_count() {
        int r0 = model.addRow();
        int r1 = model.addRow();
        assertEquals(2, model.getRowCount());
        model.removeRow(r0);
        assertEquals(1, model.getRowCount());
    }

    // --- sorting ---

    @Test void sortBySensitive_orders_lexicographically() {
        model.setValueAt("zebra", 0, 0);
        model.setValueAt("alpha", 1, 0);
        model.addRow(); // blank row at index 2
        model.sortBySensitive();
        assertEquals("alpha", model.getValueAt(0, 0));
        assertEquals("zebra", model.getValueAt(1, 0));
    }

    @Test void sortBySafe_orders_lexicographically() {
        model.setValueAt("a", "zzz", 0);
        model.setValueAt("b", "aaa", 1);
        model.sortBySafe();
        assertEquals("b", model.getValueAt(0, 0));
        assertEquals("a", model.getValueAt(1, 0));
    }

    // --- toJSON / load round-trip ---

    @Test void to_json_skips_blank_rows() {
        int filled = model.addRow();
        model.setValueAt("key", "value", filled, 0);
        model.addRow(); // blank row after
        JSONObject json = model.toJSON();
        assertEquals(1, json.length());
        assertTrue(json.has("key"));
    }

    @Test void load_populates_model_from_json() {
        JSONObject json = new JSONObject();
        json.put("alpha", "beta");
        json.put("gamma", "delta");
        model.load(json);
        assertEquals(2, model.getRowCount());
        // Order is not guaranteed by load(), but values must be present
        assertTrue(model.toJSON().has("alpha"));
        assertTrue(model.toJSON().has("gamma"));
    }

    @Test void round_trip_to_json_load_preserves_data() {
        int r0 = model.addRow();
        model.setValueAt("k1", "v1", r0, 0);
        int r1 = model.addRow();
        model.setValueAt("k2", "v2", r1, 0);

        JSONObject json = model.toJSON();

        DictionaryModel fresh = new DictionaryModel();
        fresh.load(json);

        assertEquals(model.getRowCount(), fresh.getRowCount());
        assertTrue(fresh.toJSON().has("k1"));
        assertTrue(fresh.toJSON().has("k2"));
    }

    // --- column class ---

    @Test void column_class_is_string() {
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
    }
}
