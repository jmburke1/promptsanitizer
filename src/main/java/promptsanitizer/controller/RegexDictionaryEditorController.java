/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.json.JSONObject;
import promptsanitizer.model.DictionaryModel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RegexDictionaryEditorController {
    public void init(
            /*String fileName,
            DictionaryModel model,
            JTable table,*/
            JFrame frame
    ) {
        /*this.model = model;
        this.table = table;
        this.fileName = fileName;*/
        this.frame = frame;
        //loadFromFile();
    }

    /*private String fileName;

    private DictionaryModel model;
    private JTable table;*/
    private JFrame frame;

    /** Read the JSON file and populate the table. */
    /*private void loadFromFile() {
        File f = new File(fileName);
        if (!f.exists()) return;
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(fileName)));
            model.load(json);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Could not read " + fileName + ":\n" + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void addRow() {
        int idx = model.addRow();
        table.setRowSelectionInterval(idx, idx);
        table.editCellAt(idx, 0);
    }

    public void removeRow() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        model.removeRow(r);
    }

    public void sortBySensitive() {
        model.sortBySensitive();
        table.clearSelection();
    }

    public void sortBySafe() {
        model.sortBySafe();
        table.clearSelection();
    }*/

    /** Serialize the table back to JSON and write it to disk. */
    /*public void saveToFile() {
        Path p = Path.of(fileName);
        try {
            JSONObject json = model.toJSON();
            Files.writeString(p, json.toString(2));   // pretty-print with 2-space indent
            frame.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                "Could not save to " + fileName + ":\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }*/
    /** Cancel the operation. */
    public void cancel() {
        frame.dispose();
    }
}
