/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lightweight model backed by a Map<Integer, String>. */
public class RegexDictionaryModel extends javax.swing.table.AbstractTableModel {
    private final List<ReplacementRecord> replacementValues = new ArrayList<>();
    private static final String[] COLUMN_NAMES = {"Regex", "Replacement", "Direction"};

    @Override public int getRowCount()              { return replacementValues.size(); }
    @Override public int getColumnCount()           { return 3; }
    @Override public String getColumnName(int c)    { return COLUMN_NAMES[c]; }
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
        replacementValues.add(new RegexReplaceRecord("", "", ">"));
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
        Comparator<ReplacementRecord> comp = (ss1, ss2) -> ((RegexReplaceRecord)ss1).direction().compareTo(((RegexReplaceRecord)ss2).direction());
        comp = comp.thenComparing((ss1, ss2) -> ((RegexReplaceRecord)ss1).regex().compareTo(((RegexReplaceRecord)ss2).regex()));
        replacementValues.sort(comp);
        fireTableDataChanged();
    }

    /** Sort the JTable by safe values. */
    public void sortBySecondColumn() {
        Comparator<ReplacementRecord> comp = (ss1, ss2) -> ((RegexReplaceRecord)ss1).direction().compareTo(((RegexReplaceRecord)ss2).direction());
        comp = comp.thenComparing((ss1, ss2) -> ((RegexReplaceRecord)ss1).replacement().compareTo(((RegexReplaceRecord)ss2).replacement()));
        replacementValues.sort(comp);
        fireTableDataChanged();
    }

    /** Load all entries from the JSON file into this model. */
    public void load(JSONObject json) {
        replacementValues.clear();
        for (String k : json.keySet()) {
            JSONObject jo = json.getJSONObject(k);
            String dir = jo.getString("dir");
            if(!"<".equals(dir) && !">".equals(dir)) continue;
            replacementValues.add(new RegexReplaceRecord(k, jo.getString("repl"), dir));
        }
        fireTableDataChanged();
    }

    /** Serialize this model back into a JSONObject. */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        for (int i = 0; i < replacementValues.size() ; i++) {
            String k = ((RegexReplaceRecord)replacementValues.get(i)).regex();
            String r1 = ((RegexReplaceRecord)replacementValues.get(i)).replacement();
            String r2 = ((RegexReplaceRecord)replacementValues.get(i)).direction();
            if (!"<".equals(r2) && !">".equals(r2)) continue; // skip the ones where the direction is invalid
            if (k.isEmpty()) continue; // skip the ones where there isn't a regular expression
            JSONObject jo = new JSONObject();
            jo.put("repl", r1);
            jo.put("dir", r2);
            result.put(k, jo);
        }
        return result;
    }
}

