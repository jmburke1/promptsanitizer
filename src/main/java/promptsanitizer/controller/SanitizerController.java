package promptsanitizer.controller;

import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.DictionaryEditorView;

import javax.swing.*;

public class SanitizerController {
    private SanitizerModel model;
    private String fileName;

    public void init(SanitizerModel model, String fileName) {
        this.model = model;
        this.fileName = fileName;
    }
    /** Move text from one area to another, applying the dictionary replacements in the appropriate direction. */
    public void moveText(JTextArea fromArea, JTextArea toArea, boolean isReverseDirection) {
        if (!model.isValidDictionary()) {
            model.loadDictionary();
            if (!model.isStronglyValidDictionary()) {
                JOptionPane.showMessageDialog(null,
                        "You either haven't configured a personal dictionary yet or it has no data in it.\nClick the ~ button to set one up.",
                        "No Dictionary Configured",
                        JOptionPane.INFORMATION_MESSAGE);
                model.invalidateDictionary();
                return;
            }
        }
        String text = fromArea.getText();
        if(!text.isEmpty()) {
            toArea.setText(model.applyDictionary(text, isReverseDirection));
            fromArea.setText("");
        }
    }
    public void handleTilde() {
        model.invalidateDictionary();
        new DictionaryEditorView(fileName, new DictionaryEditorController(), new DictionaryModel()).createUI();
    }
}
