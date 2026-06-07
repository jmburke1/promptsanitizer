/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.AbstractDictionaryModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictionaryEditorPromptLoopTest {

    private final ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
    private final ByteArrayOutputStream capturedError = new ByteArrayOutputStream();
    private PrintStream mockSystemOutOut;
    private PrintStream mockSystemErr;

    @BeforeEach
    void setUp() {
        mockSystemOutOut = new PrintStream(capturedOutput);
        mockSystemErr = new PrintStream(capturedError);
    }

    @Test
    void implicitClickCancel_shouldTerminateLoop() {
        /*DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                "dictionary.json",
                Mockito.mock(DictionaryEditorController.class),
                Mockito.mock(AbstractDictionaryModel.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("".getBytes())
        );

        loop.promptForWhatToDo();

        assertEquals("DictionaryEditorPromptLoop ... What do you want to do: ", capturedOutput.toString());*/
    }

    /*@Test
    void clickCancel_shouldTerminateLoop() {
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        assertEquals("DictionaryEditorPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void clickHelp_shouldShowHelp() {
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("help\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String capturedOutString = capturedOutput.toString();
        assertTrue(capturedOutString.contains("clickSortBySensitive        - Sort rows by the sensitive (left) column"));
        assertTrue(capturedOutString.contains("clickSortBySafe             - Sort rows by the safe (right) column"));
    }

    @Test
    void clickAdd_shouldInvokeDoClickOnAddButton() {
        JButton addBtn = Mockito.mock(JButton.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                addBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickAdd\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(addBtn).doClick();
    }

    @Test
    void clickRemove_shouldInvokeDoClickOnRemoveButton() {
        JButton rmBtn = Mockito.mock(JButton.class);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(8);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                rmBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickRemove\n3\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table).setRowSelectionInterval(3, 3);
        Mockito.verify(rmBtn).doClick();
    }

    @Test
    void clickRemove_outOfRangeTooBigShouldNotInvokeDoClickOnRemoveButton() {
        JButton rmBtn = Mockito.mock(JButton.class);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(8);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                rmBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickRemove\n13\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table, Mockito.never()).setRowSelectionInterval(Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(rmBtn, Mockito.never()).doClick();
        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, row is out of range"));
    }

    @Test
    void clickRemove_outOfRangeNegativeShouldNotInvokeDoClickOnRemoveButton() {
        JButton rmBtn = Mockito.mock(JButton.class);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(8);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                rmBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickRemove\n-1\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table, Mockito.never()).setRowSelectionInterval(Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(rmBtn, Mockito.never()).doClick();
        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, row is out of range"));
    }

    @Test
    void clickRemove_notANumberShouldNotInvokeDoClickOnRemoveButton() {
        JButton rmBtn = Mockito.mock(JButton.class);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(8);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                rmBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickRemove\nqrstu\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table, Mockito.never()).setRowSelectionInterval(Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(rmBtn, Mockito.never()).doClick();
        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, row doesn't parse as integer"));
    }

    @Test
    void clickSortBySensitive_shouldInvokeDoClickOnSortBySensitiveButton() {
        JButton sortBySensitiveBtn = Mockito.mock(JButton.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                sortBySensitiveBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickSortBySensitive\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(sortBySensitiveBtn).doClick();
    }

    @Test
    void clickSortBySafe_shouldInvokeDoClickOnSortBySafeButton() {
        JButton sortBySafeBtn = Mockito.mock(JButton.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                sortBySafeBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickSortBySafe\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(sortBySafeBtn).doClick();
    }

    @Test
    void clickSaveToFile_shouldInvokeDoClickOnSaveButton() {
        JButton saveBtn = Mockito.mock(JButton.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                saveBtn,
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickSaveToFile\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(saveBtn).doClick();
    }

    @Test
    void printTable_shouldPrintTableRowContents() {
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(2);
        Mockito.when(table.getValueAt(0, 0)).thenReturn("sensitive1");
        Mockito.when(table.getValueAt(0, 1)).thenReturn("safe1");
        Mockito.when(table.getValueAt(1, 0)).thenReturn("sensitive2");
        Mockito.when(table.getValueAt(1, 1)).thenReturn("safe2");
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("printTable\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertTrue(output.contains("**********************"));
        assertTrue(output.contains("sensitive1"));
        assertTrue(output.contains("safe1"));
        assertTrue(output.contains("sensitive2"));
        assertTrue(output.contains("safe2"));
    }

    @Test
    void invalidCommand_shouldPrintHelpMessage() {
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("bogusCommand\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertTrue(output.contains("Unknown command.  Type 'help' for a list of commands"));
    }

    @Test
    void multiCommandSequence_shouldProcessAllCommands() {
        JButton addBtn = Mockito.mock(JButton.class);
        JButton saveBtn = Mockito.mock(JButton.class);
        String input = "clickAdd\n" +
                "clickSortBySensitive\n" +
                "clickSaveToFile\n" +
                "clickCancel\n";
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                addBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                saveBtn,
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream(input.getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(addBtn).doClick();
        Mockito.verify(saveBtn).doClick();
    }

    @Test
    void editCellContents_shouldEditCellAndStopEditing() {
        JTextField textField = Mockito.mock(JTextField.class);
        DefaultCellEditor defaultEditor = Mockito.mock(DefaultCellEditor.class);
        Mockito.when(defaultEditor.getComponent()).thenReturn(textField);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(0, 1)).thenReturn(true);
        Mockito.when(table.getCellEditor()).thenReturn(defaultEditor);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\n1\nnewValue\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table).editCellAt(0, 1);
        Mockito.verify(textField).setText("newValue");
    }

    @Test
    void editCellContents_withOutOfRange_shouldPrintErrorToStderr() {
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(99, 99)).thenReturn(false);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n99\n99\nval\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, either row or column are out of range"));
    }

    @Test
    void editCellContents_withUnparsableColumn_shouldPrintErrorToStderr() {
        JTable table = Mockito.mock(JTable.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\nv\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, column doesn't parse as integer"));
    }

    @Test
    void editCellContents_withUnparsableRow_shouldPrintErrorToStderr() {
        JTable table = Mockito.mock(JTable.class);
        DictionaryEditorPromptLoop loop = new DictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\nv\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, row doesn't parse as integer"));
    }*/
}

