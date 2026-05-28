/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

/** Lightweight model backed by a Map<Integer, String>. */
public class DictionaryModel extends javax.swing.table.AbstractTableModel {
    private final List<SensitiveSafeRecord> sensitiveSafes   = new ArrayList<>();
    private static final String[] COLUMN_NAMES = {"Sensitive", "Safe"};

    @Override public int getRowCount()              { return sensitiveSafes.size(); }
    @Override public int getColumnCount()           { return 2; }
    @Override public String getColumnName(int c)    { return COLUMN_NAMES[c]; }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override public Object getValueAt(int r, int c) {
        if (c == 0) return sensitiveSafes.get(r).sensitive();
        if (c == 1) return sensitiveSafes.get(r).safe();
        return null;
    }

    @Override public void setValueAt(Object v, int r, int c) {
        String s = (v == null) ? "" : v.toString();
        SensitiveSafeRecord senSafRec;
        if (c == 0) {
            senSafRec = new SensitiveSafeRecord(s, sensitiveSafes.get(r).safe());
        } else {
            senSafRec = new SensitiveSafeRecord(sensitiveSafes.get(r).sensitive(), s);
        }
        sensitiveSafes.set(r, senSafRec);
        fireTableCellUpdated(r, c);
    }

    @Override public boolean isCellEditable(int r, int c) { return true; }

    /** Add a blank row and return its row index. */
    public int addRow() {
        sensitiveSafes.add(new SensitiveSafeRecord("", ""));
        fireTableRowsInserted(sensitiveSafes.size() - 1, sensitiveSafes.size() - 1);
        return sensitiveSafes.size() - 1;
    }

    /** Remove the given row index (shifts subsequent entries). */
    public void removeRow(int r) {
        fireTableRowsDeleted(r, r);
        sensitiveSafes.remove(r);
    }

    /** Sort the JTable by sensitive values. */
    public void sortBySensitive() {
        sensitiveSafes.sort((ss1, ss2) -> ss1.sensitive().compareTo(ss2.sensitive()));
        fireTableDataChanged();
    }

    /** Sort the JTable by safe values. */
    public void sortBySafe() {
        sensitiveSafes.sort((ss1, ss2) -> ss1.safe().compareTo(ss2.safe()));
        fireTableDataChanged();
    }

    /** Load all entries from the JSON file into this model. */
    public void load(JSONObject json) {
        sensitiveSafes.clear();
        for (String k : json.keySet()) {
            sensitiveSafes.add(new SensitiveSafeRecord(k, json.getString(k)));
        }
        fireTableDataChanged();
    }

    /** Serialize this model back into a JSONObject. */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        for (int i = 0; i < sensitiveSafes.size() ; i++) {
            String k = sensitiveSafes.get(i).sensitive();
            String v = sensitiveSafes.get(i).safe();
            if (k.isEmpty() && v.isEmpty()) continue; // skip blank rows
            result.put(k, v);
        }
        return result;
    }
}

