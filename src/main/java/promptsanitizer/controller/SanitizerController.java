/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.RegexDictionaryModel;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.DictionaryEditorPromptLoop;
import promptsanitizer.view.DictionaryEditorView;
import promptsanitizer.view.RegexDictionaryEditorPromptLoop;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public class SanitizerController {
    private SanitizerModel model;
    private String fileName;
    private String regexFileName;
    private BiConsumer<String, String> infoMessageHandler;

    public void init(SanitizerModel model, String fileName, String regexFileName, BiConsumer<String, String> infoMessageHandler) {
        this.model = model;
        this.fileName = fileName;
        this.regexFileName = regexFileName;
        this.infoMessageHandler = infoMessageHandler;
    }
    /** Move text from one area to another, applying the dictionary replacements in the appropriate direction. */
    public void moveText(Supplier<String> fromArea, Consumer<String> toArea, Consumer<String> fromAreaConsumer, boolean isReverseDirection) {
        if (!model.isValidDictionary()) {
            model.loadDictionary();
            if (!model.isStronglyValidDictionary()) {
                infoMessageHandler.accept("No Dictionary Configured", "You either haven't configured a personal dictionary yet or it has no data in it.\nClick the ~ button to set one up.");
                model.invalidateDictionary();
                return;
            }
        }
        String text = fromArea.get();
        if(!text.isEmpty()) {
            toArea.accept(model.applyDictionary(text, isReverseDirection));
            fromAreaConsumer.accept("");
        }
    }
    public void handleTilde(
            PrintStream shouldBeSystemOut,
            PrintStream shouldBeSystemErr,
            InputStream shouldBeSystemIn
    ) {
        model.invalidateDictionary();
        if(shouldBeSystemOut != null) {
            new DictionaryEditorPromptLoop(fileName, new DictionaryEditorController(), new DictionaryModel(), shouldBeSystemOut, shouldBeSystemErr, shouldBeSystemIn).promptForWhatToDo();
        } else {
            new DictionaryEditorView(fileName, new DictionaryEditorController(), new DictionaryModel()).createUI();
        }
    }
    public void handleAsteriskTilde(
            PrintStream shouldBeSystemOut,
            PrintStream shouldBeSystemErr,
            InputStream shouldBeSystemIn
    ) {
        model.invalidateDictionary();
        if(shouldBeSystemOut != null) {
            new RegexDictionaryEditorPromptLoop(regexFileName, new DictionaryEditorController(), new RegexDictionaryModel(), shouldBeSystemOut, shouldBeSystemErr, shouldBeSystemIn).promptForWhatToDo();
        } else {
            new DictionaryEditorView(regexFileName, new DictionaryEditorController(), new RegexDictionaryModel()).createUI();
        }
    }
}
