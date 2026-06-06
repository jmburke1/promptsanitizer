/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;
import promptsanitizer.model.AbstractDictionaryModel;

import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public class DictionaryEditorController {
    public void init(
            String fileName,
            AbstractDictionaryModel model,
            Supplier<Integer> tableRowSource,
            Consumer<Integer> tableIndexSink,
            Runnable tableClrSel,
            Runnable frameDisposed,
            BiConsumer<String, String> handleErrorMessage
    ) {
        this.model = model;
        this.tableRowSource = tableRowSource;
        this.tableIndexSink = tableIndexSink;
        this.tableClrSel = tableClrSel;
        this.fileName = fileName;
        this.frameDisposed = frameDisposed;
        this.handleErrorMessage = handleErrorMessage;
        loadFromFile();
    }

    private String fileName;

    private AbstractDictionaryModel model;
    private Supplier<Integer> tableRowSource;
    private Consumer<Integer> tableIndexSink;
    private Runnable tableClrSel;
    private Runnable frameDisposed;
    private BiConsumer<String, String> handleErrorMessage;

    /** Read the JSON file and populate the table. */
    private void loadFromFile() {
        File f = new File(fileName);
        if (!f.exists()) return;
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(fileName)));
            model.load(json);
        } catch (Exception ex) {
            handleErrorMessage.accept("Load Error", "Could not read " + fileName + ":\n" + ex.getMessage());
        }
    }
    public void addRow() {
        int idx = model.addRow();
        tableIndexSink.accept(idx);
    }

    public void removeRow() {
        int r = tableRowSource.get();
        if (r < 0) return;
        model.removeRow(r);
    }

    public void sortByFirstColumn() {
        model.sortByFirstColumn();
        tableClrSel.run();
    }

    public void sortBySecondColumn() {
        model.sortBySecondColumn();
        tableClrSel.run();
    }

    /** Serialize the table back to JSON and write it to disk. */
    public void saveToFile() {
        Path p = Path.of(fileName);
        try {
            JSONObject json = model.toJSON();
            Files.writeString(p, json.toString(2));   // pretty-print with 2-space indent
            frameDisposed.run();
        } catch (IOException ex) {
            handleErrorMessage.accept("Save Error", "Could not save to " + fileName + ":\n" + ex.getMessage());
        }
    }
    /** Cancel the operation. */
    public void cancel() {
        frameDisposed.run();
    }
}
