/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.RegexDictionaryModel;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.DictionaryEditorView;
import promptsanitizer.view.ViewSetupUtil;

import java.util.List;
import javax.swing.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SanitizerControllerSetupUsingViewSetupUtilTest {

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = Mockito.mockitoSession()
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    // --- init ---

    @Test
    void init_shouldSetModelAndFileName() throws Exception {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        String fileName = "/tmp/dict.json";
        String regexFileName = "/tmp/regex_dict.json";
        SanitizerController controller = new SanitizerController();

        ViewSetupUtil.initSanitizerController(controller, model, fileName, regexFileName);

        // Verify fields are set via reflection (no getter on the class)
        var f1 = SanitizerController.class.getDeclaredField("model");
        f1.setAccessible(true);
        assertEquals(model, f1.get(controller));
        var f2 = SanitizerController.class.getDeclaredField("fileName");
        f2.setAccessible(true);
        assertEquals(fileName, f2.get(controller));
    }

    // --- moveText ---

    @Test
    void moveText_shouldApplyDictionaryAndClearSource() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(true);
        Mockito.when(model.applyDictionary("hello", false)).thenReturn("world");
        JTextArea fromArea = new JTextArea("hello");
        JTextArea toArea = new JTextArea();
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, "/tmp/dict.json", "/tmp/regex_dict.json");

        controller.moveText(fromArea::getText, toArea::setText, fromArea::setText, false);

        assertEquals("world", toArea.getText());
        assertEquals("", fromArea.getText());
        Mockito.verify(model).applyDictionary("hello", false);
    }

    @Test
    void moveText_shouldNotMoveWhenSourceIsEmpty() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(true);
        JTextArea fromArea = new JTextArea("");
        JTextArea toArea = new JTextArea();
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, "/tmp/dict.json", "/tmp/regex_dict.json");

        controller.moveText(fromArea::getText, toArea::setText, fromArea::setText, false);

        assertEquals("", toArea.getText());
        Mockito.verify(model, Mockito.never()).applyDictionary(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    void moveText_shouldLoadDictionaryWhenNotValid() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(false);
        Mockito.when(model.isStronglyValidDictionary()).thenReturn(true);
        Mockito.when(model.applyDictionary("test", true)).thenReturn("replaced");
        JTextArea fromArea = new JTextArea("test");
        JTextArea toArea = new JTextArea();
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, "/tmp/dict.json", "/tmp/regex_dict.json");

        controller.moveText(fromArea::getText, toArea::setText, fromArea::setText, true);

        Mockito.verify(model).loadDictionary();
        Mockito.verify(model).applyDictionary("test", true);
        assertEquals("replaced", toArea.getText());
    }

    @Test
    void moveText_shouldShowDialogAndReturnEarlyWhenDictionaryNotStronglyValid() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(false);
        Mockito.when(model.isStronglyValidDictionary()).thenReturn(false);

        JTextArea fromArea = new JTextArea("test");
        JTextArea toArea = new JTextArea();
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, "/tmp/dict.json", "/tmp/regex_dict.json");

        try (MockedStatic<JOptionPane> jOptionPaneMockedStatic = Mockito.mockStatic(JOptionPane.class)) {
            controller.moveText(fromArea::getText, toArea::setText, fromArea::setText, false);

            jOptionPaneMockedStatic.verify(() -> JOptionPane.showMessageDialog(
                    Mockito.isNull(),
                    Mockito.matches("You either haven't configured.*"),
                    Mockito.eq("No Dictionary Configured"),
                    Mockito.eq(JOptionPane.INFORMATION_MESSAGE)
            ));
        }

        Mockito.verify(model).invalidateDictionary();
        assertEquals("", toArea.getText());
    }

    @Test
    void moveText_reverseDirection_shouldPassTrueToModel() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(true);
        Mockito.when(model.applyDictionary("reversed", true)).thenReturn("forward");
        JTextArea fromArea = new JTextArea("reversed");
        JTextArea toArea = new JTextArea();
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, "/tmp/dict.json", "/tmp/regex_dict.json");

        controller.moveText(fromArea::getText, toArea::setText, fromArea::setText, true);

        Mockito.verify(model).applyDictionary("reversed", true);
        assertEquals("forward", toArea.getText());
    }

    // --- handleTilde ---

    @Test
    void handleTilde_shouldInvalidateDictionaryAndOpenEditorView() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        String fileName = "/tmp/dict.json";
        String regexFileName = "/tmp/regex_dict.json";
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, fileName, regexFileName);

        try (MockedConstruction<DictionaryEditorView> viewMockedConstruction = Mockito.mockConstruction(
                DictionaryEditorView.class, (mock, context) -> {
                    assertInstanceOf(DictionaryModel.class, context.arguments().get(2));
                })) {
            controller.handleTilde(null, null, null);

            Mockito.verify(model).invalidateDictionary();
            List<DictionaryEditorView> editorViews = viewMockedConstruction.constructed();
            assertEquals(1, editorViews.size());
            Mockito.verify(editorViews.getFirst()).createUI();
        }
    }

    // --- handleAsteriskTilde ---

    @Test
    void handleAsteriskTilde_shouldInvalidateDictionaryAndOpenEditorView() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        String fileName = "/tmp/dict.json";
        String regexFileName = "/tmp/regex_dict.json";
        SanitizerController controller = new SanitizerController();
        ViewSetupUtil.initSanitizerController(controller, model, fileName, regexFileName);

        try (MockedConstruction<DictionaryEditorView> viewMockedConstruction = Mockito.mockConstruction(
                DictionaryEditorView.class, (mock, context) -> {
                    assertInstanceOf(RegexDictionaryModel.class, context.arguments().get(2));
                })) {
            controller.handleAsteriskTilde(null, null, null);

            Mockito.verify(model).invalidateDictionary();
            List<DictionaryEditorView> editorViews = viewMockedConstruction.constructed();
            assertEquals(1, editorViews.size());
            Mockito.verify(editorViews.getFirst()).createUI();
        }
    }
}
