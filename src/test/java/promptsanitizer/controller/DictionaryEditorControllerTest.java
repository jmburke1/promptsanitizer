/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.RegexDictionaryModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class DictionaryEditorControllerTest {

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

    // --- helper: set private fields via reflection to avoid calling init() ---

    // --- init / loadFromFile ---

    @Test
    void init_shouldSetFieldsAndCallLoadWhenFileExists() throws Exception {
        Path tmp = Files.createTempFile("dict", ".json");
        Files.writeString(tmp, "{\"key1\":\"value1\"}");
        String fileName = tmp.toString();
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        controller.init(fileName, model, null, null, null, null, null);

        Mockito.verify(model).load(Mockito.argThat(jsonObject -> jsonObject.getString("key1").equals("value1")));
        Files.delete(tmp);
    }
    private boolean jsonObjectArgThat(JSONObject jo) {
        JSONObject t = jo.getJSONObject("key1");
        return t.getString("repl").equals("value1") && t.getString("dir").equals("<");
    }
    @Test
    void init_shouldSetFieldsAndCallLoadWhenFileExistsRegexModelExample() throws Exception {
        Path tmp = Files.createTempFile("dict", ".json");
        Files.writeString(tmp, "{\"key1\":{\"repl\": \"value1\", \"dir\": \"<\"}}"); //change this?
        String fileName = tmp.toString();
        RegexDictionaryModel model = Mockito.mock(RegexDictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        controller.init(fileName, model, null, null, null, null, null);

        Mockito.verify(model).load(Mockito.argThat(this::jsonObjectArgThat));
        Files.delete(tmp);
    }

    @Test
    void init_shouldDoNothingWhenFileDoesNotExist() {
        String fileName = "/tmp/nonexistent/file.json";
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        controller.init(fileName, model, null, null, null, null, null);

        Mockito.verify(model, Mockito.never()).load(Mockito.any());
    }

    @Test
    void init_shouldShowErrorDialogWhenJsonIsInvalid() throws Exception {
        Path tmp = Files.createTempFile("bad", ".json");
        Files.writeString(tmp, "not valid json {{{");
        String fileName = tmp.toString();
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        String[] titleAndError = {"", ""};

        controller.init(fileName, model, null, null, null, null, (title, message) -> {titleAndError[0] = title; titleAndError[1] = message;});
        assertEquals("Load Error", titleAndError[0]);
        assertTrue(titleAndError[1].startsWith("Could not read"));

        // loadFromFile caught the exception; model.load() was never called.
        Mockito.verify(model, Mockito.never()).load(Mockito.any());
        Files.delete(tmp);
    }

    // --- addRow ---

    @Test
    void addRow_shouldAddRowAndSelectIt() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        Mockito.when(model.addRow()).thenReturn(2);
        DictionaryEditorController controller = new DictionaryEditorController();
        Stack<Integer> accepted = new Stack<>();
        controller.init("/tmp/nonexistent/file.json", model, null, accepted::push, null, null, null);

        controller.addRow();

        Mockito.verify(model).addRow();
        assertEquals(2, accepted.pop());
        assertTrue(accepted.isEmpty());
    }

    // --- removeRow ---

    @Test
    void removeRow_shouldRemoveWhenRowSelected() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, () -> 1, null, null, null, null);

        controller.removeRow();

        Mockito.verify(model).removeRow(1);
    }

    @Test
    void removeRow_shouldDoNothingWhenNoSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, () -> -1, null, null, null, null);

        controller.removeRow();

        Mockito.verify(model, Mockito.never()).removeRow(Mockito.anyInt());
    }

    // --- sortBySensitive / sortBySafe ---

    @Test
    void sortByFirstColumn_shouldDelegateToModelAndClearSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        int[] clrSelCount = {0};
        controller.init("/tmp/nonexistent/file.json", model, null, null, () -> clrSelCount[0]++, null, null);

        controller.sortByFirstColumn();

        Mockito.verify(model).sortByFirstColumn();
        assertEquals(1, clrSelCount[0]);
    }

    @Test
    void sortBySecondColumn_shouldDelegateToModelAndClearSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        int[] clrSelCount = {0};
        controller.init("/tmp/nonexistent/file.json", model, null, null, () -> clrSelCount[0]++, null, null);

        controller.sortBySecondColumn();

        Mockito.verify(model).sortBySecondColumn();
        assertEquals(1, clrSelCount[0]);
    }

    // --- saveToFile ---

    @Test
    void saveToFile_shouldSerializeAndWriteToJsonFile() throws Exception {
        Path tmp = Files.createTempFile("save", ".json");
        String fileName = tmp.toString();
        Files.writeString(tmp, "{}");
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JSONObject expectedJson = new JSONObject().put("hello", "greetings").put("bye", "farewell");
        Mockito.when(model.toJSON()).thenReturn(expectedJson);
        DictionaryEditorController controller = new DictionaryEditorController();
        boolean[] disposedHappened = {false};
        controller.init(fileName, model, null, null, null, () -> disposedHappened[0] = true, null);

        controller.saveToFile();

        String written = Files.readString(tmp);
        JSONObject parsed = new JSONObject(written);
        assertEquals("greetings", parsed.getString("hello"));
        assertEquals("farewell", parsed.getString("bye"));
        assertTrue(disposedHappened[0]);
        Files.delete(tmp);
    }

    @Test
    void saveToFile_shouldShowErrorDialogWhenWriteFails() throws Exception {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        Mockito.when(model.toJSON()).thenReturn(new JSONObject());
        DictionaryEditorController controller = new DictionaryEditorController();
        String badPath = "/tmp/nonexistent/directory/file.json";
        boolean[] disposedHappened = {false};
        String[] titleAndError = {"", ""};
        controller.init(badPath, model, null, null, null, () -> disposedHappened[0] = true, (title, message) -> {titleAndError[0] = title; titleAndError[1] = message;});

        controller.saveToFile();
        assertEquals("Save Error", titleAndError[0]);
        assertTrue(titleAndError[1].startsWith("Could not save to "));

        assertFalse(disposedHappened[0]);
    }

    // --- cancel ---

    @Test
    void cancel_shouldDisposeFrame() throws Exception {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        boolean[] disposedHappened = {false};
        controller.init("/tmp/nonexistent/file.json", model, null, null, null, () -> disposedHappened[0] = true, null);

        controller.cancel();

        assertTrue(disposedHappened[0]);
    }
}
