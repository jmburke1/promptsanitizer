/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexDictionaryEditorPromptLoopTest {

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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("".getBytes())
        );

        loop.promptForWhatToDo();

        assertEquals("RegexDictionaryEditorPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void clickCancel_shouldTerminateLoop() {
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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

        assertEquals("RegexDictionaryEditorPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void clickHelp_shouldShowHelp() {
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        assertTrue(capturedOutString.contains("clickSortByRegex            - Sort rows by the regex column"));
        assertTrue(capturedOutString.contains("clickSortByReplacement      - Sort rows by the replacement column"));
    }

    @Test
    void clickAdd_shouldInvokeDoClickOnAddButton() {
        JButton addBtn = Mockito.mock(JButton.class);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
    void clickSortByRegex_shouldInvokeDoClickOnSortByRegexButton() {
        JButton sortByRegexBtn = Mockito.mock(JButton.class);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                sortByRegexBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickSortByRegex\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(sortByRegexBtn).doClick();
    }

    @Test
    void clickSortByReplacement_shouldInvokeDoClickOnSortByReplacementButton() {
        JButton sortByReplacementBtn = Mockito.mock(JButton.class);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                sortByReplacementBtn,
                Mockito.mock(JButton.class),
                Mockito.mock(JTable.class),
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("clickSortByReplacement\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(sortByReplacementBtn).doClick();
    }

    @Test
    void clickSaveToFile_shouldInvokeDoClickOnSaveButton() {
        JButton saveBtn = Mockito.mock(JButton.class);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
    void printTable_shouldPrintThreeColumnTableRowContents() {
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.getRowCount()).thenReturn(1);
        Mockito.when(table.getValueAt(0, 0)).thenReturn("graft([0-9]+)");
        Mockito.when(table.getValueAt(0, 1)).thenReturn("$1lark");
        Mockito.when(table.getValueAt(0, 2)).thenReturn(">");
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        assertTrue(output.contains("graft([0-9]+)"));
        assertTrue(output.contains("$1lark"));
        assertTrue(output.contains(">"));
    }

    @Test
    void invalidCommand_shouldPrintHelpMessage() {
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
                "clickSortByRegex\n" +
                "clickSaveToFile\n" +
                "clickCancel\n";
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        Mockito.when(table.editCellAt(0, 0)).thenReturn(true);
        Mockito.when(table.getCellEditor()).thenReturn(defaultEditor);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\n0\nnewRegex\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table).editCellAt(0, 0);
        Mockito.verify(textField).setText("newRegex");
    }

    @Test
    void editCellContents_withOutOfRange_shouldPrintErrorToStderr() {
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(99, 99)).thenReturn(false);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
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
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\nq\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, column doesn't parse as integer"));
    }

    @Test
    void editCellContents_withUnparsableRow_shouldPrintErrorToStderr() {
        JTable table = Mockito.mock(JTable.class);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\nq\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, row doesn't parse as integer"));
    }

    @Test
    void editCellContents_shouldAllowGreaterThanSymbolAtThirdColumn() {
        JTextField textField = Mockito.mock(JTextField.class);
        DefaultCellEditor defaultEditor = Mockito.mock(DefaultCellEditor.class);
        Mockito.when(defaultEditor.getComponent()).thenReturn(textField);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(0, 2)).thenReturn(true);
        Mockito.when(table.getCellEditor()).thenReturn(defaultEditor);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\n2\n>\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table).editCellAt(0, 2);
        Mockito.verify(textField).setText(">");
    }

    @Test
    void editCellContents_shouldAllowLessThanSymbolAtThirdColumn() { //???
        JTextField textField = Mockito.mock(JTextField.class);
        DefaultCellEditor defaultEditor = Mockito.mock(DefaultCellEditor.class);
        Mockito.when(defaultEditor.getComponent()).thenReturn(textField);
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(0, 2)).thenReturn(true);
        Mockito.when(table.getCellEditor()).thenReturn(defaultEditor);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\n2\n<\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(table).editCellAt(0, 2);
        Mockito.verify(textField).setText("<");
    }

    @Test
    void editCellContents_withInvalidDirection_shouldPrintErrorToStderr() { //???
        JTable table = Mockito.mock(JTable.class);
        Mockito.when(table.editCellAt(0, 2)).thenReturn(true);
        RegexDictionaryEditorPromptLoop loop = new RegexDictionaryEditorPromptLoop(
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                table,
                mockSystemOutOut,
                mockSystemErr,
                new ByteArrayInputStream("editCellContents\n0\n2\nbadDir\nclickCancel\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid, direction column must be either < or >"));
    }
}

