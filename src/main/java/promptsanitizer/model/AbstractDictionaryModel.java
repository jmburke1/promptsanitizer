/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

/** Lightweight model backed by a Map<Integer, String>. */
public abstract class AbstractDictionaryModel {
    private Consumer<Integer> insertedBehavior;
    private Consumer<Integer> deletedBehavior;
    private Runnable changedBehavior;
    private BiConsumer<Integer, Integer> cellChangeBehavior;
    private final List<ReplacementRecord> replacementValues = new ArrayList<>();

    public int getRowCount()              {  return replacementValues.size(); }

    public String getValueAt(int r, int c) {
        return replacementValues.get(r).getColumnValue(c);
    }

    public void setValueAt(String v, int r, int c) {
        String s = (v == null) ? "" : v.toString();
        replacementValues.set(r, replacementValues.get(r).createOther(s, c));
        cellChangeBehavior.accept(r, c);
    }

    /** Add a blank row and return its row index. */
    public int addRow() {
        replacementValues.add(createReplacementRecord());
        insertedBehavior.accept(replacementValues.size() - 1);
        return replacementValues.size() - 1;
    }

    /** Remove the given row index (shifts subsequent entries). */
    public void removeRow(int r) {
        deletedBehavior.accept(r);
        replacementValues.remove(r);
    }

    /** Sort the JTable by sensitive values. */
    public void sortByFirstColumn() {
        replacementValues.sort((ss1, ss2) -> ss1.contextCompareToOther("FIRST_COLUMN", ss2));
        changedBehavior.run();
    }

    /** Sort the JTable by safe values. */
    public void sortBySecondColumn() {
        replacementValues.sort((ss1, ss2) -> ss1.contextCompareToOther("SECOND_COLUMN", ss2));
        changedBehavior.run();
    }

    /** Load all entries from the JSON file into this model. */
    public void load(JSONObject json) {
        replacementValues.clear();
        for (String k : json.keySet()) {
            (createReplacementRecord()).pushIntoArrayList(k, json, replacementValues);
        }
        changedBehavior.run();
    }

    /** Serialize this model back into a JSONObject. */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        for (int i = 0; i < replacementValues.size() ; i++) {
            replacementValues.get(i).pushIntoJSONObject(result);
        }
        return result;
    }

    abstract protected ReplacementRecord createReplacementRecord();
    abstract public int getColumnCount();
    abstract public String getColumnName(int c);

    public void initBehaviors(
            Consumer<Integer> insBeh,
            Consumer<Integer> delBeh,
            Runnable chgBeh,
            BiConsumer<Integer, Integer> cellChgBeh) {
        insertedBehavior = insBeh;
        deletedBehavior = delBeh;
        changedBehavior = chgBeh;
        cellChangeBehavior = cellChgBeh;
    }

    public boolean isOutOfRange(int row, int column) {
        if((row < 0) || (getRowCount() <= row)) {
            return true;
        }
        if((column < 0) || (getColumnCount() <= column)) {
            return true;
        }
        return false;
    }
}

