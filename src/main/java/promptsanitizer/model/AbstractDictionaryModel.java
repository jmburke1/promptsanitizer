/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Lightweight model backed by a Map<Integer, String>. */
abstract class AbstractDictionaryModel extends javax.swing.table.AbstractTableModel {
    private final List<ReplacementRecord> replacementValues = new ArrayList<>();

    @Override public int getRowCount()              { return replacementValues.size(); }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override public Object getValueAt(int r, int c) {
        return replacementValues.get(r).getColumnValue(c);
    }

    @Override public void setValueAt(Object v, int r, int c) {
        String s = (v == null) ? "" : v.toString();
        replacementValues.set(r, replacementValues.get(r).createOther(s, c));
        fireTableCellUpdated(r, c);
    }

    @Override public boolean isCellEditable(int r, int c) { return true; }

    /** Add a blank row and return its row index. */
    public int addRow() {
        replacementValues.add(createReplacementRecord());
        fireTableRowsInserted(replacementValues.size() - 1, replacementValues.size() - 1);
        return replacementValues.size() - 1;
    }

    /** Remove the given row index (shifts subsequent entries). */
    public void removeRow(int r) {
        fireTableRowsDeleted(r, r);
        replacementValues.remove(r);
    }

    /** Sort the JTable by sensitive values. */
    public void sortByFirstColumn() {
        replacementValues.sort((ss1, ss2) -> ss1.contextCompareToOther("FIRST_COLUMN", ss2));
        fireTableDataChanged();
    }

    /** Sort the JTable by safe values. */
    public void sortBySecondColumn() {
        replacementValues.sort((ss1, ss2) -> ss1.contextCompareToOther("SECOND_COLUMN", ss2));
        fireTableDataChanged();
    }

    /** Load all entries from the JSON file into this model. */
    public void load(JSONObject json) {
        replacementValues.clear();
        for (String k : json.keySet()) {
            (createReplacementRecord()).pushIntoArrayList(k, json, replacementValues);
        }
        fireTableDataChanged();
    }

    /** Serialize this model back into a JSONObject. */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        for (int i = 0; i < replacementValues.size() ; i++) {
            replacementValues.get(i).pushIntoJSONObject(result);
        }
        return result;
    }

    abstract ReplacementRecord createReplacementRecord();
}

