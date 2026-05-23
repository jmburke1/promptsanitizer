/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.SanitizerModel;

import javax.swing.*;

public class DictionaryEditorViewTest {
    @Test
    void createUIMakesTheFrameVisible() {
        try(MockedConstruction<JFrame> jfMC = Mockito.mockConstruction(JFrame.class)) {
            DictionaryEditorView dictionaryEditorView = new DictionaryEditorView(
                    "/path/to/file.json",
                    Mockito.mock(DictionaryEditorController.class),
                    Mockito.mock(DictionaryModel.class)
            );
            dictionaryEditorView.createUI();
            Mockito.verify(jfMC.constructed().get(0)).setVisible(true);
        }
    }
}
