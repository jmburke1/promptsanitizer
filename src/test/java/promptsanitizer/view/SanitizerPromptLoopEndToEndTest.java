/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanitizerPromptLoopEndToEndTest {

    private ByteArrayOutputStream capturedOutput;
    private ByteArrayOutputStream capturedError;
    private PrintStream mockOut;
    private PrintStream mockErr;
    private Path tmpPersonalDict;
    private Path tmpRegexPersonalDict;

    @BeforeEach
    void setUp() throws IOException {
        capturedOutput = new ByteArrayOutputStream();
        capturedError = new ByteArrayOutputStream();
        mockOut = new PrintStream(capturedOutput);
        mockErr = new PrintStream(capturedError);
        tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Files.writeString(tmpPersonalDict, "{\n" +
                "  \"abcde\": \"fghij\",\n" +
                "  \"vuwxy\": \"0z123\",\n" +
                "  \"uvwxy\": \"z0123\"\n" +
                "}");
        tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
        Files.writeString(tmpRegexPersonalDict, "{\n" +
                "  \"ace([0-9]*)\": {\n" +
                "    \"repl\": \"$1bdf\",\n" +
                "    \"dir\": \">\"\n" +
                "  },\n" +
                "  \"welp([a-z]*)\": {\n" +
                "    \"repl\": \"$1zepp\",\n" +
                "    \"dir\": \">\"\n" +
                "  },\n" +
                "  \"([0-9]*)bdf\": {\n" +
                "    \"repl\": \"ace$1\",\n" +
                "    \"dir\": \"<\"\n" +
                "  },\n" +
                "  \"([a-z]*)zepp\": {\n" +
                "    \"repl\": \"welp$1\",\n" +
                "    \"dir\": \"<\"\n" +
                "  }\n" +
                "}");
    }
    @AfterEach
    void tearDown() throws IOException {
        Files.delete(tmpPersonalDict);
        Files.delete(tmpRegexPersonalDict);
    }

    @Test
    void shouldUnknownCommandFollowedByHelp() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickAsteriskTildeButton\nhungry\nhelp\nclickCancel\nclickTildeButton\nhungry\nhelp\nclickCancel\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: **********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: Unknown command.  Type 'help' for a list of commands.\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: You are in the regex dictionary editor prompt loop.  Choices are:\n" +
                "  clickCancel               - Close the regex dictionary editor and return to main loop\n" +
                "  clickAdd                    - Add a new empty row to the regex dictionary\n" +
                "  clickRemove                 - Remove the selected row (prompts for row number)\n" +
                "  clickSortByRegex            - Sort rows by the regex column\n" +
                "  clickSortByReplacement      - Sort rows by the replacement column\n" +
                "  printTable                  - Print the current regex dictionary table\n" +
                "  editCellContents            - Edit a cell (prompts for row, column, and new value)\n" +
                "  clickSaveToFile             - Save the regex dictionary to file\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: **********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: Unknown command.  Type 'help' for a list of commands.\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: You are in the dictionary editor prompt loop.  Choices are:\n" +
                "  clickCancel               - Close the dictionary editor and return to main loop\n" +
                "  clickAdd                    - Add a new empty row to the dictionary\n" +
                "  clickRemove                 - Remove the selected row (prompts for row number)\n" +
                "  clickSortBySensitive        - Sort rows by the sensitive (left) column\n" +
                "  clickSortBySafe             - Sort rows by the safe (right) column\n" +
                "  printTable                  - Print the current dictionary table\n" +
                "  editCellContents            - Edit a cell (prompts for row, column, and new value)\n" +
                "  clickSaveToFile             - Save the dictionary to file\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void implicitExit_shouldTerminateLoop() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickAsteriskTildeButton\n".getBytes())
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: **********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldBeAbleToSanitizePrompt() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterLeft: Lorem ipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nclickMoveRight\nprintRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: Lorem ipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nSanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldBeAbleToPersonalizeResponse() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight: Lorem ipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nclickMoveLeft\nprintLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: Lorem ipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nSanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldBeAbleToEditDictionary() throws IOException {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickTildeButton\neditCellContents\n-1\n40\nqqqqq\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: TTVVV\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "TTVVV\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
        String err = capturedError.toString();
        assertEquals("invalid, either row or column are out of range\n", err);
        JSONObject jo = new JSONObject(Files.readString(tmpPersonalDict));
        assertEquals("0z123", jo.getString("TTVVV"));
        assertEquals("fghij", jo.getString("abcde"));
        assertEquals("z0123", jo.getString("uvwxy"));
    }

    @Test
    void shouldCatchRowNotParseAsIntegerAndColumnNotParseAsIntegerEditDictionary() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickTildeButton\neditCellContents\nq\neditCellContents\n1\nv\nprintTable\nclickCancel\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
        String err = capturedError.toString();
        assertEquals("invalid, row doesn't parse as integer\n" +
                "invalid, column doesn't parse as integer\n", err);
    }


    /*@Test
    void enterRightFollowedByPrintRight_shouldPrintRightAreaText() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight: sanitized text\nprintRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: sanitized text\nSanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
    void enterLeft_withEscapedNewlines_shouldReplaceBackslashN() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterLeft: line1\\nline2\nprintLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertTrue(output.contains("line1\nline2"));
    }

    @Test
    void enterRight_withEscapedNewlines_shouldReplaceBackslashN() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight: line1\\nline2\nprintRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertTrue(output.contains("line1\nline2"));
    }

    @Test
    void enterLeft_withoutColon_shouldPrintErrorToStderr() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.endsWith("invalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>\n"));
    }

    @Test
    void enterRight_withoutColon_shouldPrintErrorToStderr() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String errorOutput = capturedError.toString();
        assertTrue(errorOutput.contains("invalid"));
    }

    @Test
    void clickMoveRight_shouldInvokeDoClickOnMoveRightButton() {
        SanitizerController controller = Mockito.mock(SanitizerController.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                controller,
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickMoveRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(controller).moveText(Mockito.any(Supplier.class), Mockito.any(Consumer.class), Mockito.any(Consumer.class), Mockito.eq(false));
    }

    @Test
    void clickMoveLeft_shouldInvokeDoClickOnMoveLeftButton() {
        SanitizerController controller = Mockito.mock(SanitizerController.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                controller,
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickMoveLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(controller).moveText(Mockito.any(Supplier.class), Mockito.any(Consumer.class), Mockito.any(Consumer.class), Mockito.eq(true));
    }

    @Test
    void clickTildeButton_shouldInvokeDoClickOnTildeButton() {
        SanitizerController controller = Mockito.mock(SanitizerController.class);
        ByteArrayInputStream mockIn = new ByteArrayInputStream("clickTildeButton\nexit\n".getBytes());
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                controller,
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                mockIn
        );

        loop.promptForWhatToDo();

        Mockito.verify(controller).handleTilde(mockOut, mockErr, mockIn);
    }

    @Test
    void clickAsteriskTildeButton_shouldInvokeDoClickOnAsteriskTildeButton() {
        ByteArrayInputStream mockIn = new ByteArrayInputStream("clickAsteriskTildeButton\nexit\n".getBytes());
        SanitizerController controller = Mockito.mock(SanitizerController.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                controller,
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                mockIn
        );

        loop.promptForWhatToDo();

        Mockito.verify(controller).handleAsteriskTilde(mockOut, mockErr, mockIn);
    }

    @Test
    void invalidCommand_shouldPrintHelpMessage() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("bogusCommand\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: Unknown command.  Type 'help' for a list of commands.\nSanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
    void help_shouldPrintHelp() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                "dictionary.json",
                "regex_dictionary.json",
                Mockito.mock(SanitizerController.class),
                Mockito.mock(SanitizerModel.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("help\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertTrue(output.contains("clickMoveRight              - Sanitize left panel and write to right panel"));
    }*/

    /*@Test
    void multiCommandSequence_shouldProcessAllCommands() {
        JTextArea leftArea = Mockito.mock(JTextArea.class);
        JButton moveRightButton = Mockito.mock(JButton.class);
        String input = "enterLeft: test data\n" +
                "printLeft\n" +
                "clickMoveRight\n" +
                "exit\n";
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                leftArea,
                Mockito.mock(JTextArea.class),
                moveRightButton,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream(input.getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(leftArea).setText("test data");
        Mockito.verify(leftArea).getText();
        Mockito.verify(moveRightButton).doClick();
    }*/
}

