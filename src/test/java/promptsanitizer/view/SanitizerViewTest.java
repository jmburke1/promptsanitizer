/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;

import javax.swing.*;

public class SanitizerViewTest {
    @Test
    void createUIMakesTheFrameVisible() {
        try(MockedConstruction<JFrame> jfMC = Mockito.mockConstruction(JFrame.class)) {
            SanitizerView sanitizerView = new SanitizerView(
                    "/path/to/file.json",
                    Mockito.mock(SanitizerController.class),
                    Mockito.mock(SanitizerModel.class)
            );
            sanitizerView.createUI();
            Mockito.verify(jfMC.constructed().get(0)).setVisible(true);
        }
    }
}
