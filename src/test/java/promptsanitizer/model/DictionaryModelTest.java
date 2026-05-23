/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DictionaryModelTest {

    @Test
    void numerousThingsAreTrueAfterUserAddsFiveRows() {
        DictionaryModel model = new DictionaryModel();
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
        assertSame(String.class, model.getColumnClass(0));
        assertEquals("Sensitive", model.getColumnName(0));
        assertEquals("Safe", model.getColumnName(1));
        assertTrue(model.isCellEditable(100, 100));
    }

    @Test
    void userRemovesARow() {
        DictionaryModel model = new DictionaryModel();
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
    }

    @Test
    void userSortsBySensitive() {
        DictionaryModel model = new DictionaryModel();
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
        model.sortBySensitive();
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
    }

    @Test
    void userSortsBySafe() {
        DictionaryModel model = new DictionaryModel();
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
        model.sortBySafe();
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
    }

    @Test
    void canSaveAndLoad() {
        DictionaryModel model = new DictionaryModel();
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
        DictionaryModel model2 = new DictionaryModel();
        model2.load(model.toJSON());
        model2.sortBySensitive();
        assertEquals("key1", model2.getValueAt(0, 0));
        assertEquals("key2", model2.getValueAt(1, 0));
        assertEquals("key3", model2.getValueAt(2, 0));
        assertEquals("key4", model2.getValueAt(3, 0));
        assertEquals("key5", model2.getValueAt(4, 0));
        assertEquals("value4", model2.getValueAt(0, 1));
        assertEquals("value2", model2.getValueAt(1, 1));
        assertEquals("value5", model2.getValueAt(2, 1));
        assertEquals("value3", model2.getValueAt(3, 1));
        assertEquals("value1", model2.getValueAt(4, 1));
    }
}
