/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.RegexDictionaryModel;

import javax.swing.*;

public class RegexDictionaryEditorViewTest {
    @Test
    void createUIMakesTheFrameVisible() {
        try(MockedConstruction<JFrame> jfMC = Mockito.mockConstruction(JFrame.class)) {
            RegexDictionaryEditorView regexDictionaryEditorView = new RegexDictionaryEditorView(
                    "/path/to/file.json",
                    Mockito.mock(DictionaryEditorController.class),
                    Mockito.mock(RegexDictionaryModel.class)
            );
            regexDictionaryEditorView.createUI();
            Mockito.verify(jfMC.constructed().getFirst()).setVisible(true);
        }
    }
}
