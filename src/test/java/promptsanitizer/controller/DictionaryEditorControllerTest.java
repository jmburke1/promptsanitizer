/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import promptsanitizer.model.DictionaryModel;

import javax.swing.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private void injectFields(DictionaryEditorController controller, String fileName,
                              DictionaryModel model, JTable table, JFrame frame) throws Exception {
        setPrivate(controller, "fileName", fileName);
        setPrivate(controller, "model", model);
        setPrivate(controller, "table", table);
        setPrivate(controller, "frame", frame);
    }

    private void setPrivate(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // --- init / loadFromFile ---

    @Test
    void init_shouldSetFieldsAndCallLoadWhenFileExists() throws Exception {
        Path tmp = Files.createTempFile("dict", ".json");
        Files.writeString(tmp, "{\"key1\":\"value1\"}");
        String fileName = tmp.toString();
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        controller.init(fileName, model, table, frame);

        Mockito.verify(model).load(Mockito.argThat(jsonObject -> jsonObject.getString("key1").equals("value1")));
        Files.delete(tmp);
    }

    @Test
    void init_shouldDoNothingWhenFileDoesNotExist() {
        String fileName = "/tmp/nonexistent/file.json";
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        controller.init(fileName, model, table, frame);

        Mockito.verify(model, Mockito.never()).load(Mockito.any());
    }

    @Test
    void init_shouldShowErrorDialogWhenJsonIsInvalid() throws Exception {
        Path tmp = Files.createTempFile("bad", ".json");
        Files.writeString(tmp, "not valid json {{{");
        String fileName = tmp.toString();
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryEditorController controller = new DictionaryEditorController();

        try(MockedStatic<JOptionPane> jOptionPaneMockedStatic = Mockito.mockStatic(JOptionPane.class)) {
            controller.init(fileName, model, table, frame);
            jOptionPaneMockedStatic.verify(() -> JOptionPane.showMessageDialog(Mockito.isNull(),
                    Mockito.matches("Could not read.*"),
                    Mockito.eq("Load Error"), Mockito.eq(JOptionPane.ERROR_MESSAGE)));
        }

        // loadFromFile caught the exception; model.load() was never called.
        Mockito.verify(model, Mockito.never()).load(Mockito.any());
        Files.delete(tmp);
    }

    // --- addRow ---

    @Test
    void addRow_shouldAddRowAndSelectIt() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        Mockito.when(model.addRow()).thenReturn(2);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, table, frame);

        controller.addRow();

        Mockito.verify(model).addRow();
        Mockito.verify(table).setRowSelectionInterval(2, 2);
        Mockito.verify(table).editCellAt(2, 0);
    }

    // --- removeRow ---

    @Test
    void removeRow_shouldRemoveWhenRowSelected() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        Mockito.when(table.getSelectedRow()).thenReturn(1);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, table, frame);

        controller.removeRow();

        Mockito.verify(model).removeRow(1);
    }

    @Test
    void removeRow_shouldDoNothingWhenNoSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        Mockito.when(table.getSelectedRow()).thenReturn(-1);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, table, frame);

        controller.removeRow();

        Mockito.verify(model, Mockito.never()).removeRow(Mockito.anyInt());
    }

    // --- sortBySensitive / sortBySafe ---

    @Test
    void sortBySensitive_shouldDelegateToModelAndClearSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, table, frame);

        controller.sortBySensitive();

        Mockito.verify(model).sortBySensitive();
        Mockito.verify(table).clearSelection();
    }

    @Test
    void sortBySafe_shouldDelegateToModelAndClearSelection() {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        controller.init("/tmp/nonexistent/file.json", model, table, frame);

        controller.sortBySafe();

        Mockito.verify(model).sortBySafe();
        Mockito.verify(table).clearSelection();
    }

    // --- saveToFile ---

    @Test
    void saveToFile_shouldSerializeAndWriteToJsonFile() throws Exception {
        Path tmp = Files.createTempFile("save", ".json");
        String fileName = tmp.toString();
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        JSONObject expectedJson = new JSONObject().put("hello", "greetings").put("bye", "farewell");
        Mockito.when(model.toJSON()).thenReturn(expectedJson);
        DictionaryEditorController controller = new DictionaryEditorController();
        // Inject fields directly to avoid loadFromFile reading a real file.
        injectFields(controller, fileName, model, table, frame);

        controller.saveToFile();

        String written = Files.readString(tmp);
        JSONObject parsed = new JSONObject(written);
        assertEquals("greetings", parsed.getString("hello"));
        assertEquals("farewell", parsed.getString("bye"));
        Mockito.verify(frame).dispose();
        Files.delete(tmp);
    }

    @Test
    void saveToFile_shouldShowErrorDialogWhenWriteFails() throws Exception {
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        JFrame frame = Mockito.mock(JFrame.class);
        Mockito.when(model.toJSON()).thenReturn(new JSONObject());
        DictionaryEditorController controller = new DictionaryEditorController();
        // Inject fields with an invalid path to force an IOException on write.
        String badPath = "/tmp/nonexistent/directory/file.json";
        injectFields(controller, badPath, model, table, frame);

        try(MockedStatic<JOptionPane> jOptionPaneMockedStatic = Mockito.mockStatic(JOptionPane.class)) {
            controller.saveToFile();
            jOptionPaneMockedStatic.verify(() -> JOptionPane.showMessageDialog(Mockito.isNull(),
                    Mockito.matches("Could not save to .*"),
                    Mockito.eq("Save Error"), Mockito.eq(JOptionPane.ERROR_MESSAGE)));
        }

        Mockito.verify(frame, Mockito.never()).dispose();
    }

    // --- cancel ---

    @Test
    void cancel_shouldDisposeFrame() throws Exception {
        JFrame frame = Mockito.mock(JFrame.class);
        DictionaryModel model = Mockito.mock(DictionaryModel.class);
        JTable table = Mockito.mock(JTable.class);
        DictionaryEditorController controller = new DictionaryEditorController();
        injectFields(controller, "/tmp/nonexistent/file.json", model, table, frame);

        controller.cancel();

        Mockito.verify(frame).dispose();
    }
}
