/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.RegexDictionaryModel;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.DictionaryEditorView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SanitizerControllerTest {

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

        controller.init(model, fileName, regexFileName, null);

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
        String[] fromArea = {"hello"};
        String[] toArea = {""};
        SanitizerController controller = new SanitizerController();
        controller.init(model, "/tmp/dict.json", "/tmp/regex_dict.json", null);

        controller.moveText(() -> fromArea[0], s -> toArea[0] = s, s -> fromArea[0] = s, false);

        assertEquals("world", toArea[0]);
        assertEquals("", fromArea[0]);
        Mockito.verify(model).applyDictionary("hello", false);
    }

    @Test
    void moveText_shouldNotMoveWhenSourceIsEmpty() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(true);
        SanitizerController controller = new SanitizerController();
        controller.init(model, "/tmp/dict.json", "/tmp/regex_dict.json", null);

        controller.moveText(() -> "", null, null, false);

        Mockito.verify(model, Mockito.never()).applyDictionary(Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test
    void moveText_shouldLoadDictionaryWhenNotValid() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(false);
        Mockito.when(model.isStronglyValidDictionary()).thenReturn(true);
        Mockito.when(model.applyDictionary("test", true)).thenReturn("replaced");
        String[] fromArea = {"test"};
        String[] toArea = {""};
        SanitizerController controller = new SanitizerController();
        controller.init(model, "/tmp/dict.json", "/tmp/regex_dict.json", null);

        controller.moveText(() -> fromArea[0], s -> toArea[0] = s, s -> fromArea[0] = s, true);

        Mockito.verify(model).loadDictionary();
        Mockito.verify(model).applyDictionary("test", true);
        assertEquals("replaced", toArea[0]);
    }

    @Test
    void moveText_shouldShowDialogAndReturnEarlyWhenDictionaryNotStronglyValid() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(false);
        Mockito.when(model.isStronglyValidDictionary()).thenReturn(false);

        SanitizerController controller = new SanitizerController();
        String[] titleAndMessage = {"", ""};
        controller.init(model, "/tmp/dict.json", "/tmp/regex_dict.json", (title, message) -> {titleAndMessage[0] = title; titleAndMessage[1] = message;});

        controller.moveText(null, null, null, false);

        assertEquals("No Dictionary Configured", titleAndMessage[0]);
        assertTrue(titleAndMessage[1].startsWith("You either haven't configured"));

    }

    @Test
    void moveText_reverseDirection_shouldPassTrueToModel() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        Mockito.when(model.isValidDictionary()).thenReturn(true);
        Mockito.when(model.applyDictionary("reversed", true)).thenReturn("forward");
        String[] fromArea = {"reversed"};
        String[] toArea = {""};
        SanitizerController controller = new SanitizerController();
        controller.init(model, "/tmp/dict.json", "/tmp/regex_dict.json", null);

        controller.moveText(() -> fromArea[0], s -> toArea[0] = s, s -> fromArea[0] = s, true);

        Mockito.verify(model).applyDictionary("reversed", true);
        assertEquals("forward", toArea[0]);
    }

    // --- handleTilde ---

    @Test
    void handleTilde_shouldInvalidateDictionaryAndOpenEditorView() {
        SanitizerModel model = Mockito.mock(SanitizerModel.class);
        String fileName = "/tmp/dict.json";
        String regexFileName = "/tmp/regex_dict.json";
        SanitizerController controller = new SanitizerController();
        controller.init(model, fileName, regexFileName, null);

        try (MockedConstruction<DictionaryEditorView> viewMockedConstruction = Mockito.mockConstruction(
                DictionaryEditorView.class, (mock, context) -> {
                    assertInstanceOf(DictionaryModel.class, context.arguments().get(2));
                })) {
            controller.handleTilde(null);

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
        controller.init(model, fileName, regexFileName, null);

        try (MockedConstruction<DictionaryEditorView> viewMockedConstruction = Mockito.mockConstruction(
                DictionaryEditorView.class, (mock, context) -> {
                    assertInstanceOf(RegexDictionaryModel.class, context.arguments().get(2));
                })) {
            controller.handleAsteriskTilde(null);

            Mockito.verify(model).invalidateDictionary();
            List<DictionaryEditorView> editorViews = viewMockedConstruction.constructed();
            assertEquals(1, editorViews.size());
            Mockito.verify(editorViews.getFirst()).createUI();
        }
    }
}
