/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DictionaryModelTest {

    private StringBuilder initModelBehaviors(DictionaryModel model) {
        StringBuilder sb = new StringBuilder();
        model.initBehaviors(
                i -> sb.append("inserted: " + i + "\n"),
                d -> sb.append("deleted: " + d + "\n"),
                () -> sb.append("chgd\n"),
                (r, c) -> sb.append("cellEdited: " + r + " " + c +"\n")
        );
        return sb;
    }

    @Test
    void numerousThingsAreTrueAfterUserAddsFiveRows() {
        DictionaryModel model = new DictionaryModel();
        StringBuilder assertLater = initModelBehaviors(model);
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.setValueAt("key2", 0, 0);
        model.setValueAt("key3", 1, 0);
        model.setValueAt("key1", 2, 0);
        model.setValueAt(null, 3, 0);
        model.setValueAt("key4", 4, 0);
        model.setValueAt("value2", 0, 1);
        model.setValueAt("value5", 1, 1);
        model.setValueAt("value4", 2, 1);
        model.setValueAt("value1", 3, 1);
        model.setValueAt("value3", 4, 1);
        assertEquals("", model.getValueAt(3, 0));
        model.setValueAt("key5", 3, 0);
        assertEquals("key2", model.getValueAt(0, 0));
        assertEquals("key3", model.getValueAt(1, 0));
        assertEquals("key1", model.getValueAt(2, 0));
        assertEquals("key5", model.getValueAt(3, 0));
        assertEquals("key4", model.getValueAt(4, 0));
        assertEquals("value2", model.getValueAt(0, 1));
        assertEquals("value5", model.getValueAt(1, 1));
        assertEquals("value4", model.getValueAt(2, 1));
        assertEquals("value1", model.getValueAt(3, 1));
        assertEquals("value3", model.getValueAt(4, 1));
        assertNull(model.getValueAt(1, 2));
        assertEquals(5, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("Sensitive", model.getColumnName(0));
        assertEquals("Safe", model.getColumnName(1));
        assertEquals("inserted: 0\n" +
                "inserted: 1\n" +
                "inserted: 2\n" +
                "inserted: 3\n" +
                "inserted: 4\n" +
                "cellEdited: 0 0\n" +
                "cellEdited: 1 0\n" +
                "cellEdited: 2 0\n" +
                "cellEdited: 3 0\n" +
                "cellEdited: 4 0\n" +
                "cellEdited: 0 1\n" +
                "cellEdited: 1 1\n" +
                "cellEdited: 2 1\n" +
                "cellEdited: 3 1\n" +
                "cellEdited: 4 1\n" +
                "cellEdited: 3 0\n", assertLater.toString());
    }

    @Test
    void userRemovesARow() {
        DictionaryModel model = new DictionaryModel();
        StringBuilder assertLater = initModelBehaviors(model);
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.setValueAt("key2", 0, 0);
        model.setValueAt("key3", 1, 0);
        model.setValueAt("key1", 2, 0);
        model.setValueAt("key5", 3, 0);
        model.setValueAt("key4", 4, 0);
        model.setValueAt("value2", 0, 1);
        model.setValueAt("value5", 1, 1);
        model.setValueAt("value4", 2, 1);
        model.setValueAt("value1", 3, 1);
        model.setValueAt("value3", 4, 1);
        model.removeRow(3);
        assertEquals("key2", model.getValueAt(0, 0));
        assertEquals("key3", model.getValueAt(1, 0));
        assertEquals("key1", model.getValueAt(2, 0));
        assertEquals("key4", model.getValueAt(3, 0));
        assertEquals("value2", model.getValueAt(0, 1));
        assertEquals("value5", model.getValueAt(1, 1));
        assertEquals("value4", model.getValueAt(2, 1));
        assertEquals("value3", model.getValueAt(3, 1));
        assertEquals("inserted: 0\n" +
                "inserted: 1\n" +
                "inserted: 2\n" +
                "inserted: 3\n" +
                "inserted: 4\n" +
                "cellEdited: 0 0\n" +
                "cellEdited: 1 0\n" +
                "cellEdited: 2 0\n" +
                "cellEdited: 3 0\n" +
                "cellEdited: 4 0\n" +
                "cellEdited: 0 1\n" +
                "cellEdited: 1 1\n" +
                "cellEdited: 2 1\n" +
                "cellEdited: 3 1\n" +
                "cellEdited: 4 1\n" +
                "deleted: 3\n", assertLater.toString());
    }

    @Test
    void userSortsBySensitive() {
        DictionaryModel model = new DictionaryModel();
        StringBuilder assertLater = initModelBehaviors(model);
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.setValueAt("key2", 0, 0);
        model.setValueAt("key3", 1, 0);
        model.setValueAt("key1", 2, 0);
        model.setValueAt("key5", 3, 0);
        model.setValueAt("key4", 4, 0);
        model.setValueAt("value2", 0, 1);
        model.setValueAt("value5", 1, 1);
        model.setValueAt("value4", 2, 1);
        model.setValueAt("value1", 3, 1);
        model.setValueAt("value3", 4, 1);
        model.sortByFirstColumn();
        assertEquals("key1", model.getValueAt(0, 0));
        assertEquals("key2", model.getValueAt(1, 0));
        assertEquals("key3", model.getValueAt(2, 0));
        assertEquals("key4", model.getValueAt(3, 0));
        assertEquals("key5", model.getValueAt(4, 0));
        assertEquals("value4", model.getValueAt(0, 1));
        assertEquals("value2", model.getValueAt(1, 1));
        assertEquals("value5", model.getValueAt(2, 1));
        assertEquals("value3", model.getValueAt(3, 1));
        assertEquals("value1", model.getValueAt(4, 1));
        assertEquals("inserted: 0\n" +
                "inserted: 1\n" +
                "inserted: 2\n" +
                "inserted: 3\n" +
                "inserted: 4\n" +
                "cellEdited: 0 0\n" +
                "cellEdited: 1 0\n" +
                "cellEdited: 2 0\n" +
                "cellEdited: 3 0\n" +
                "cellEdited: 4 0\n" +
                "cellEdited: 0 1\n" +
                "cellEdited: 1 1\n" +
                "cellEdited: 2 1\n" +
                "cellEdited: 3 1\n" +
                "cellEdited: 4 1\n" +
                "chgd\n", assertLater.toString());
    }

    @Test
    void userSortsBySafe() {
        DictionaryModel model = new DictionaryModel();
        StringBuilder assertLater = initModelBehaviors(model);
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.setValueAt("key2", 0, 0);
        model.setValueAt("key3", 1, 0);
        model.setValueAt("key1", 2, 0);
        model.setValueAt("key5", 3, 0);
        model.setValueAt("key4", 4, 0);
        model.setValueAt("value2", 0, 1);
        model.setValueAt("value5", 1, 1);
        model.setValueAt("value4", 2, 1);
        model.setValueAt("value1", 3, 1);
        model.setValueAt("value3", 4, 1);
        model.sortBySecondColumn();
        assertEquals("key5", model.getValueAt(0, 0));
        assertEquals("key2", model.getValueAt(1, 0));
        assertEquals("key4", model.getValueAt(2, 0));
        assertEquals("key1", model.getValueAt(3, 0));
        assertEquals("key3", model.getValueAt(4, 0));
        assertEquals("value1", model.getValueAt(0, 1));
        assertEquals("value2", model.getValueAt(1, 1));
        assertEquals("value3", model.getValueAt(2, 1));
        assertEquals("value4", model.getValueAt(3, 1));
        assertEquals("value5", model.getValueAt(4, 1));
        assertEquals("inserted: 0\n" +
                "inserted: 1\n" +
                "inserted: 2\n" +
                "inserted: 3\n" +
                "inserted: 4\n" +
                "cellEdited: 0 0\n" +
                "cellEdited: 1 0\n" +
                "cellEdited: 2 0\n" +
                "cellEdited: 3 0\n" +
                "cellEdited: 4 0\n" +
                "cellEdited: 0 1\n" +
                "cellEdited: 1 1\n" +
                "cellEdited: 2 1\n" +
                "cellEdited: 3 1\n" +
                "cellEdited: 4 1\n" +
                "chgd\n", assertLater.toString());
    }

    @Test
    void canSaveAndLoad() {
        DictionaryModel model = new DictionaryModel();
        StringBuilder assertLaterSrc = initModelBehaviors(model);
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.addRow();
        model.setValueAt("key2", 0, 0);
        model.setValueAt("key3", 1, 0);
        model.setValueAt("key1", 2, 0);
        model.setValueAt("key5", 3, 0);
        model.setValueAt("key4", 4, 0);
        model.setValueAt("key88", 5, 0);
        model.setValueAt("", 6, 0);
        model.setValueAt("value2", 0, 1);
        model.setValueAt("value5", 1, 1);
        model.setValueAt("value4", 2, 1);
        model.setValueAt("value1", 3, 1);
        model.setValueAt("value3", 4, 1);
        model.setValueAt("", 5, 1);
        model.setValueAt("value99", 6, 1);
        DictionaryModel model2 = new DictionaryModel();
        StringBuilder assertLaterSnk = initModelBehaviors(model2);
        model2.load(model.toJSON());
        model2.sortByFirstColumn();
        assertEquals("", model2.getValueAt(0, 0));
        assertEquals("key1", model2.getValueAt(1, 0));
        assertEquals("key2", model2.getValueAt(2, 0));
        assertEquals("key3", model2.getValueAt(3, 0));
        assertEquals("key4", model2.getValueAt(4, 0));
        assertEquals("key5", model2.getValueAt(5, 0));
        assertEquals("key88", model2.getValueAt(6, 0));
        assertEquals("value99", model2.getValueAt(0, 1));
        assertEquals("value4", model2.getValueAt(1, 1));
        assertEquals("value2", model2.getValueAt(2, 1));
        assertEquals("value5", model2.getValueAt(3, 1));
        assertEquals("value3", model2.getValueAt(4, 1));
        assertEquals("value1", model2.getValueAt(5, 1));
        assertEquals("", model2.getValueAt(6, 1));
        assertEquals("inserted: 0\n" +
                "inserted: 1\n" +
                "inserted: 2\n" +
                "inserted: 3\n" +
                "inserted: 4\n" +
                "inserted: 5\n" +
                "inserted: 6\n" +
                "inserted: 7\n" +
                "cellEdited: 0 0\n" +
                "cellEdited: 1 0\n" +
                "cellEdited: 2 0\n" +
                "cellEdited: 3 0\n" +
                "cellEdited: 4 0\n" +
                "cellEdited: 5 0\n" +
                "cellEdited: 6 0\n" +
                "cellEdited: 0 1\n" +
                "cellEdited: 1 1\n" +
                "cellEdited: 2 1\n" +
                "cellEdited: 3 1\n" +
                "cellEdited: 4 1\n" +
                "cellEdited: 5 1\n" +
                "cellEdited: 6 1\n",assertLaterSrc.toString());
        assertEquals("chgd\n" +
                "chgd\n",assertLaterSnk.toString());
    }

    @Test
    void rangeCheck() {
        DictionaryModel model = new DictionaryModel();
        model.initBehaviors(i -> {}, d -> {}, () -> {}, (r, c) -> {});
        model.addRow();
        model.addRow();
        model.addRow();
        assertTrue(model.isOutOfRange(-1, 0));
        assertFalse(model.isOutOfRange(0, 0));
        assertFalse(model.isOutOfRange(1, 0));
        assertFalse(model.isOutOfRange(2, 0));
        assertTrue(model.isOutOfRange(3, 0));
        assertTrue(model.isOutOfRange(1, -1));
        assertFalse(model.isOutOfRange(1, 0));
        assertFalse(model.isOutOfRange(1, 1));
        assertTrue(model.isOutOfRange(1, 2));
    }
}
