/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import promptsanitizer.view.TableModelWrapper;

import static org.junit.jupiter.api.Assertions.*;

class WrappedRegexDictionaryModelTest {

    @Test
    void numerousThingsAreTrueAfterUserAddsFiveRows() {
        RegexDictionaryModel regexDictMdl = new RegexDictionaryModel();
        TableModelWrapper model = new TableModelWrapper(regexDictMdl);
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        //(a|b)
        //$1
        model.setValueAt("(a|b)2", 0, 0);
        model.setValueAt("(a|b)3", 1, 0);
        model.setValueAt("(a|b)1", 2, 0);
        model.setValueAt(null, 3, 0);
        model.setValueAt("(a|b)4", 4, 0);
        model.setValueAt("value_$1_2", 0, 1);
        model.setValueAt("value_$1_5", 1, 1);
        model.setValueAt("value_$1_4", 2, 1);
        model.setValueAt("value_$1_1", 3, 1);
        model.setValueAt("value_$1_3", 4, 1);
        model.setValueAt("<", 0, 2);
        model.setValueAt("<", 1, 2);
        model.setValueAt("<", 2, 2);
        model.setValueAt("<", 3, 2);
        model.setValueAt("invalid", 4, 2);
        assertEquals("", model.getValueAt(3, 0));
        model.setValueAt("(a|b)5", 3, 0);
        assertEquals("(a|b)2", model.getValueAt(0, 0));
        assertEquals("(a|b)3", model.getValueAt(1, 0));
        assertEquals("(a|b)1", model.getValueAt(2, 0));
        assertEquals("(a|b)5", model.getValueAt(3, 0));
        assertEquals("(a|b)4", model.getValueAt(4, 0));
        assertEquals("value_$1_2", model.getValueAt(0, 1));
        assertEquals("value_$1_5", model.getValueAt(1, 1));
        assertEquals("value_$1_4", model.getValueAt(2, 1));
        assertEquals("value_$1_1", model.getValueAt(3, 1));
        assertEquals("value_$1_3", model.getValueAt(4, 1));
        assertEquals("<", model.getValueAt(0, 2));
        assertEquals("<", model.getValueAt(1, 2));
        assertEquals("<", model.getValueAt(2, 2));
        assertEquals("<", model.getValueAt(3, 2));
        assertEquals(">", model.getValueAt(4, 2));
        assertNull(model.getValueAt(1, 3));
        assertEquals(5, model.getRowCount());
        assertEquals(3, model.getColumnCount());
        assertSame(String.class, model.getColumnClass(0));
        assertEquals("Regex", model.getColumnName(0));
        assertEquals("Replacement", model.getColumnName(1));
        assertEquals("Direction", model.getColumnName(2));
        assertTrue(model.isCellEditable(100, 100));
    }

    @Test
    void userRemovesARow() {
        RegexDictionaryModel regexDictMdl = new RegexDictionaryModel();
        TableModelWrapper model = new TableModelWrapper(regexDictMdl);
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        model.setValueAt("(a|b)2", 0, 0);
        model.setValueAt("(a|b)3", 1, 0);
        model.setValueAt("(a|b)1", 2, 0);
        model.setValueAt("(a|b)5", 3, 0);
        model.setValueAt("(a|b)4", 4, 0);
        model.setValueAt("value_$1_2", 0, 1);
        model.setValueAt("value_$1_5", 1, 1);
        model.setValueAt("value_$1_4", 2, 1);
        model.setValueAt("value_$1_1", 3, 1);
        model.setValueAt("value_$1_3", 4, 1);
        model.setValueAt(">", 0, 2);
        model.setValueAt(">", 1, 2);
        model.setValueAt(">", 2, 2);
        model.setValueAt(">", 3, 2);
        model.setValueAt(">", 4, 2);
        regexDictMdl.removeRow(3);
        assertEquals("(a|b)2", model.getValueAt(0, 0));
        assertEquals("(a|b)3", model.getValueAt(1, 0));
        assertEquals("(a|b)1", model.getValueAt(2, 0));
        assertEquals("(a|b)4", model.getValueAt(3, 0));
        assertEquals("value_$1_2", model.getValueAt(0, 1));
        assertEquals("value_$1_5", model.getValueAt(1, 1));
        assertEquals("value_$1_4", model.getValueAt(2, 1));
        assertEquals("value_$1_3", model.getValueAt(3, 1));
        assertEquals(">", model.getValueAt(0, 2));
        assertEquals(">", model.getValueAt(1, 2));
        assertEquals(">", model.getValueAt(2, 2));
        assertEquals(">", model.getValueAt(3, 2));
    }

    @Test
    void userSortsByRegexes() {
        RegexDictionaryModel regexDictMdl = new RegexDictionaryModel();
        TableModelWrapper model = new TableModelWrapper(regexDictMdl);
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        model.setValueAt("(a|b)2", 0, 0);
        model.setValueAt("(a|b)3", 1, 0);
        model.setValueAt("(a|b)1", 2, 0);
        model.setValueAt("(a|b)5", 3, 0);
        model.setValueAt("(a|b)4", 4, 0);
        model.setValueAt("value_$1_2", 0, 1);
        model.setValueAt("value_$1_5", 1, 1);
        model.setValueAt("value_$1_4", 2, 1);
        model.setValueAt("value_$1_1", 3, 1);
        model.setValueAt("value_$1_3", 4, 1);
        model.setValueAt("<", 0, 2);
        model.setValueAt(">", 1, 2);
        model.setValueAt("<", 2, 2);
        model.setValueAt("<", 3, 2);
        model.setValueAt("<", 4, 2);
        regexDictMdl.sortByFirstColumn();
        assertEquals("(a|b)1", model.getValueAt(0, 0));
        assertEquals("(a|b)2", model.getValueAt(1, 0));
        assertEquals("(a|b)4", model.getValueAt(2, 0));
        assertEquals("(a|b)5", model.getValueAt(3, 0));
        assertEquals("(a|b)3", model.getValueAt(4, 0));
        assertEquals("value_$1_4", model.getValueAt(0, 1));
        assertEquals("value_$1_2", model.getValueAt(1, 1));
        assertEquals("value_$1_3", model.getValueAt(2, 1));
        assertEquals("value_$1_1", model.getValueAt(3, 1));
        assertEquals("value_$1_5", model.getValueAt(4, 1));
        assertEquals("<", model.getValueAt(0, 2));
        assertEquals("<", model.getValueAt(1, 2));
        assertEquals("<", model.getValueAt(2, 2));
        assertEquals("<", model.getValueAt(3, 2));
        assertEquals(">", model.getValueAt(4, 2));
    }

    @Test
    void userSortsByReplacements() {
        RegexDictionaryModel regexDictMdl = new RegexDictionaryModel();
        TableModelWrapper model = new TableModelWrapper(regexDictMdl);
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        model.setValueAt("(a|b)2", 0, 0);
        model.setValueAt("(a|b)3", 1, 0);
        model.setValueAt("(a|b)1", 2, 0);
        model.setValueAt("(a|b)5", 3, 0);
        model.setValueAt("(a|b)4", 4, 0);
        model.setValueAt("value_$1_2", 0, 1);
        model.setValueAt("value_$1_5", 1, 1);
        model.setValueAt("value_$1_4", 2, 1);
        model.setValueAt("value_$1_1", 3, 1);
        model.setValueAt("value_$1_3", 4, 1);
        model.setValueAt("<", 0, 2);
        model.setValueAt("<", 1, 2);
        model.setValueAt("<", 2, 2);
        model.setValueAt("<", 3, 2);
        model.setValueAt(">", 4, 2);
        regexDictMdl.sortBySecondColumn();
        assertEquals("(a|b)5", model.getValueAt(0, 0));
        assertEquals("(a|b)2", model.getValueAt(1, 0));
        assertEquals("(a|b)1", model.getValueAt(2, 0));
        assertEquals("(a|b)3", model.getValueAt(3, 0));
        assertEquals("(a|b)4", model.getValueAt(4, 0));
        assertEquals("value_$1_1", model.getValueAt(0, 1));
        assertEquals("value_$1_2", model.getValueAt(1, 1));
        assertEquals("value_$1_4", model.getValueAt(2, 1));
        assertEquals("value_$1_5", model.getValueAt(3, 1));
        assertEquals("value_$1_3", model.getValueAt(4, 1));
        assertEquals("<", model.getValueAt(0, 2));
        assertEquals("<", model.getValueAt(1, 2));
        assertEquals("<", model.getValueAt(2, 2));
        assertEquals("<", model.getValueAt(3, 2));
        assertEquals(">", model.getValueAt(4, 2));
    }

    private void canSaveAndLoad(boolean isInvalidCharTest) {
        RegexDictionaryModel regexDictMdl = new RegexDictionaryModel();
        TableModelWrapper model = new TableModelWrapper(regexDictMdl);
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        regexDictMdl.addRow();
        model.setValueAt("(a|b)2", 0, 0);
        model.setValueAt("(a|b)3", 1, 0);
        model.setValueAt("(a|b)1", 2, 0);
        model.setValueAt("(a|b)5", 3, 0);
        model.setValueAt("(a|b)4", 4, 0);
        model.setValueAt("", 5, 0);
        model.setValueAt("value_$1_2", 0, 1);
        model.setValueAt("value_$1_5", 1, 1);
        model.setValueAt("value_$1_4", 2, 1);
        model.setValueAt("value_$1_1", 3, 1);
        model.setValueAt("value_$1_3", 4, 1);
        model.setValueAt("value_$1_99", 5, 1);
        model.setValueAt("<", 1, 2);
        model.setValueAt("<", 2, 2);
        RegexDictionaryModel regexDictMdl2 = new RegexDictionaryModel();
        TableModelWrapper model2 = new TableModelWrapper(regexDictMdl2);
        JSONObject json = regexDictMdl.toJSON();
        if(isInvalidCharTest) {
            json.getJSONObject("(a|b)5").put("dir", "!");
        }
        regexDictMdl2.load(json);
        regexDictMdl2.sortByFirstColumn();
        assertEquals("(a|b)1", model2.getValueAt(0, 0));
        assertEquals("(a|b)3", model2.getValueAt(1, 0));
        assertEquals("(a|b)2", model2.getValueAt(2, 0));
        assertEquals("(a|b)4", model2.getValueAt(3, 0));
        if(isInvalidCharTest) {
            assertEquals(4, model2.getRowCount());
        } else {
            assertEquals(5, model2.getRowCount());
            assertEquals("(a|b)5", model2.getValueAt(4, 0));
        }
        assertEquals("value_$1_4", model2.getValueAt(0, 1));
        assertEquals("value_$1_5", model2.getValueAt(1, 1));
        assertEquals("value_$1_2", model2.getValueAt(2, 1));
        assertEquals("value_$1_3", model2.getValueAt(3, 1));
        if(!isInvalidCharTest) {
            assertEquals("value_$1_1", model2.getValueAt(4, 1));
        }
        assertEquals("<", model2.getValueAt(0, 2));
        assertEquals("<", model2.getValueAt(1, 2));
        assertEquals(">", model2.getValueAt(2, 2));
        assertEquals(">", model2.getValueAt(3, 2));
        if(!isInvalidCharTest) {
            assertEquals(">", model2.getValueAt(4, 2));
        }
    }
    @Test
    void canSaveAndLoadNormalCase() {
        canSaveAndLoad(false);
    }
    @Test
    void canSaveAndLoadInvalidChar() {
        canSaveAndLoad(true);
    }
}
