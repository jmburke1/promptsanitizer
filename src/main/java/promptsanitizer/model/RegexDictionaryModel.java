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
    private final List<RegexReplaceRecord> regexReplacements = new ArrayList<>();
    private static final String[] COLUMN_NAMES = {"Regex", "Replacement", "Direction"};

    @Override public int getRowCount()              { return regexReplacements.size(); }
    @Override public int getColumnCount()           { return 3; }
    @Override public String getColumnName(int c)    { return COLUMN_NAMES[c]; }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override public Object getValueAt(int r, int c) {
        if (c == 0) return regexReplacements.get(r).regex();
        if (c == 1) return regexReplacements.get(r).replacement();
        if (c == 2) return regexReplacements.get(r).direction();
        return null;
    }

    @Override public void setValueAt(Object v, int r, int c) {
        String s = (v == null) ? "" : v.toString();
        RegexReplaceRecord regexReplRec;
        if (c == 0) {
            regexReplRec = new RegexReplaceRecord(s, regexReplacements.get(r).replacement(), regexReplacements.get(r).direction());
        } else if (c == 1) {
            regexReplRec = new RegexReplaceRecord(regexReplacements.get(r).regex(), s, regexReplacements.get(r).direction());
        } else {
            if(!"<".equals(s) && !">".equals(s)) {
                return;
            }
            regexReplRec = new RegexReplaceRecord(regexReplacements.get(r).regex(), regexReplacements.get(r).replacement(), s);
        }
        regexReplacements.set(r, regexReplRec);
        fireTableCellUpdated(r, c);
    }

    @Override public boolean isCellEditable(int r, int c) { return true; }

    /** Add a blank row and return its row index. */
    public int addRow() {
        regexReplacements.add(new RegexReplaceRecord("", "", ">"));
        fireTableRowsInserted(regexReplacements.size() - 1, regexReplacements.size() - 1);
        return regexReplacements.size() - 1;
    }

    /** Remove the given row index (shifts subsequent entries). */
    public void removeRow(int r) {
        fireTableRowsDeleted(r, r);
        regexReplacements.remove(r);
    }

    /** Sort the JTable by sensitive values. */
    public void sortByRegexes() {
        Comparator<RegexReplaceRecord> comp = (ss1, ss2) -> ss1.direction().compareTo(ss2.direction());
        comp = comp.thenComparing((ss1, ss2) -> ss1.regex().compareTo(ss2.regex()));
        regexReplacements.sort(comp);
        fireTableDataChanged();
    }

    /** Sort the JTable by safe values. */
    public void sortByReplacements() {
        Comparator<RegexReplaceRecord> comp = (ss1, ss2) -> ss1.direction().compareTo(ss2.direction());
        comp = comp.thenComparing((ss1, ss2) -> ss1.replacement().compareTo(ss2.replacement()));
        regexReplacements.sort(comp);
        fireTableDataChanged();
    }

    /** Load all entries from the JSON file into this model. */
    public void load(JSONObject json) {
        regexReplacements.clear();
        for (String k : json.keySet()) {
            JSONObject jo = json.getJSONObject(k);
            String dir = jo.getString("dir");
            if(!"<".equals(dir) && !">".equals(dir)) continue;
            regexReplacements.add(new RegexReplaceRecord(k, jo.getString("repl"), dir));
        }
        fireTableDataChanged();
    }

    /** Serialize this model back into a JSONObject. */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        for (int i = 0; i < regexReplacements.size() ; i++) {
            String k = regexReplacements.get(i).regex();
            String r1 = regexReplacements.get(i).replacement();
            String r2 = regexReplacements.get(i).direction();
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

