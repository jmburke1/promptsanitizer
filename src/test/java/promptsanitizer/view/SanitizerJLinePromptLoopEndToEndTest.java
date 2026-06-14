/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.DictionaryModel;
import promptsanitizer.model.SanitizerModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SanitizerJLinePromptLoopEndToEndTest {

    private ByteArrayOutputStream capturedOutput;
    private PrintStream mockOut;
    private PrintStream mockErr;
    private Path tmpPersonalDict;
    private Path tmpRegexPersonalDict;

    @BeforeEach
    void setUp() throws IOException {
        capturedOutput = new ByteArrayOutputStream();
        mockOut = new PrintStream(capturedOutput);
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
    void shouldUnknownCommandFollowedByHelp() throws IOException {
        String feedInAsInput = "stareOutAWindow\nhelp\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: stareOutAWindow\n" +
                "Unknown command.  Type 'help' for a list of commands.\n" +
                "SanitizerPromptLoop ... What do you want to do: help\n" +
                "You are in the sanitizer prompt loop.  Choices are:\n" +
                "  exit                        - Exit the application\n" +
                "  help                        - Show this menu of available commands\n" +
                "  printLeft                   - Print the unsanitized left panel text\n" +
                "  enterLeft: <text>           - Set the unsanitized left panel text (use \\n for newlines)\n" +
                "  printRight                  - Print the right panel sanitized text\n" +
                "  enterRight: <text>          - Set the right panel sanitized text which is the answer you received (use \\n for newlines)\n" +
                "  clickMoveRight              - Sanitize left panel and write to right panel\n" +
                "  clickMoveLeft               - Personalize right panel and write to left panel\n" +
                "  clickTildeButton            - Open the dictionary editor (~)\n" +
                "  clickAsteriskTildeButton    - Open the regex dictionary editor (*~)\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(capturedOutput.toString(), feedInAsInput));
    }

    @Test
    void shouldUnknownCommandFollowedByHelp2() throws IOException {
        String feedInAsInput = "clickAsteriskTildeButton\nhungry\nhelp\nclickCancel\nclickTildeButton\nhungry\nhelp\nclickCancel\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickAsteriskTildeButton\n**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: hungry\nUnknown command.  Type 'help' for a list of commands.\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: help\nYou are in the regex dictionary editor prompt loop.  Choices are:\n" +
                "  clickCancel               - Close the regex dictionary editor and return to main loop\n" +
                "  clickAdd                    - Add a new empty row to the regex dictionary\n" +
                "  clickRemove                 - Remove the selected row (prompts for row number)\n" +
                "  clickSortByRegex            - Sort rows by the regex column\n" +
                "  clickSortByReplacement      - Sort rows by the replacement column\n" +
                "  printTable                  - Print the current regex dictionary table\n" +
                "  editCellContents            - Edit a cell (prompts for row, column, and new value)\n" +
                "  clickSaveToFile             - Save the regex dictionary to file and closes the regex dictionary editor to return to main loop\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: clickCancel\nSanitizerPromptLoop ... What do you want to do: clickTildeButton\n**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: hungry\nUnknown command.  Type 'help' for a list of commands.\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: help\nYou are in the dictionary editor prompt loop.  Choices are:\n" +
                "  clickCancel               - Close the dictionary editor and return to main loop\n" +
                "  clickAdd                    - Add a new empty row to the dictionary\n" +
                "  clickRemove                 - Remove the selected row (prompts for row number)\n" +
                "  clickSortBySensitive        - Sort rows by the sensitive (left) column\n" +
                "  clickSortBySafe             - Sort rows by the safe (right) column\n" +
                "  printTable                  - Print the current dictionary table\n" +
                "  editCellContents            - Edit a cell (prompts for row, column, and new value)\n" +
                "  clickSaveToFile             - Save the dictionary to file and closes the dictionary editor to return to main loop\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickCancel\nSanitizerPromptLoop ... What do you want to do: exit", cleanup(capturedOutput.toString(), feedInAsInput));
    }

    @Test
    void implicitExit_shouldTerminateLoop() throws IOException {
        String feedInAsInput = "clickAsteriskTildeButton\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickAsteriskTildeButton\n**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: \nSanitizerPromptLoop ... What do you want to do: ", cleanup(capturedOutput.toString(), feedInAsInput));
    }

    @Test
    void shouldBeAbleToSanitizePrompt() throws IOException {
        String feedInAsInput = "enterLeft\nenterLeft: Lorem \\nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nclickMoveRight\nprintRight\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: enterLeft\ninvalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>\nSanitizerPromptLoop ... What do you want to do: enterLeft: Lorem \\nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nSanitizerPromptLoop ... What do you want to do: clickMoveRight\nSanitizerPromptLoop ... What do you want to do: printRight\nLorem \nipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nSanitizerPromptLoop ... What do you want to do: exit", cleanup(capturedOutput.toString(), feedInAsInput));
    }

    @Test
    void uninitializedDictionaries() throws IOException {
        String feedInAsInput = "enterLeft\nenterLeft: Lorem \\nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nclickMoveRight\nprintRight\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        Path tmpPersonalDictNotExist = Path.of(tmpPersonalDict.toString().replace("personalDict", "personalDictNotExist"));
        Path tmpRegexPersonalDictNotExist = Path.of(tmpRegexPersonalDict.toString().replace("regexPersonalDict", "regexPersonalDictNotExist"));
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDictNotExist.toString(),
                tmpRegexPersonalDictNotExist.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        String firstPart = "[No Dictionary Configured] You either haven't configured a personal dictionary yet or it has no data in it.";
        String capturedOutputString = capturedOutput.toString();
        assertTrue(capturedOutputString.contains(firstPart));
        assertTrue(capturedOutputString.substring(capturedOutputString.indexOf(firstPart)+firstPart.length()).contains("Click the ~ button to set one up."));
    }

    @Test
    void correctErrorMessageWhenSaveError() throws IOException {
        String feedInAsInput = "clickTildeButton\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        Path tmpPersonalDictNotExist = Path.of(tmpPersonalDict.toString().replace("personalDict", "directoryNotExist" + System.getProperty("file.separator") + "personalDict"));
        SanitizerModel model = new SanitizerModel();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController() {
                    @Override
                    public void handleTilde(
                            Terminal t
                    ) {
                        model.invalidateDictionary();
                        assertNotNull(t);
                        new DictionaryEditorJLinePromptLoop(tmpPersonalDictNotExist.toString(), new DictionaryEditorController(), new DictionaryModel(), t).promptForWhatToDo();
                    }

                },
                model,
                terminal
        );

        loop.promptForWhatToDo();

        String err = capturedOutput.toString();
        assertTrue(err.contains("[Save Error] Could not save to "));
    }


    @Test
    void correctErrorMessageWhenLoadError() throws IOException {
        String feedInAsInput = "clickTildeButton\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerModel model = new SanitizerModel();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController() {
                    @Override
                    public void handleTilde(
                            Terminal t
                    ) {
                        boolean caught = false;
                        try {
                            Files.writeString(tmpPersonalDict, "{{ a bunch of random junk not really JSON format");
                        } catch(IOException ioex) {
                            caught = true;
                        }
                        assertFalse(caught);
                        super.handleTilde(t);
                    }

                },
                model,
                terminal
        );

        loop.promptForWhatToDo();

        String err = capturedOutput.toString();
        assertTrue(err.contains("[Load Error] Could not read "));
    }


    @Test
    void shouldBeAbleToPersonalizeResponse() throws IOException {
        String feedInAsInput = "enterRight\nenterRight: Lorem \\nipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nclickMoveLeft\nprintLeft\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: enterRight\ninvalid.  Expected format is \"enterRight: <text you want to enter with just type \\n for newlines>\nSanitizerPromptLoop ... What do you want to do: enterRight: Lorem \\nipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nSanitizerPromptLoop ... What do you want to do: clickMoveLeft\nSanitizerPromptLoop ... What do you want to do: printLeft\nLorem \nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nSanitizerPromptLoop ... What do you want to do: exit", cleanup(capturedOutput.toString(), feedInAsInput));
    }

    @Test
    void shouldBeAbleToEditDictionary() throws IOException {
        String feedInAsInput = "clickTildeButton\neditCellContents\n-1\n40\nqqqqq\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickTildeButton\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n-1\n" +
                "Enter column number (counting from zero'th column):\n40\n" +
                "Enter new value:\nqqqqq\n" +
                "invalid, either row or column are out of range\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n1\n" +
                "Enter column number (counting from zero'th column):\n0\n" +
                "Enter new value:\nTTVVV\n" +
                "Cell contents changed to: TTVVV\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: printTable\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "TTVVV\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickSaveToFile\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(output, feedInAsInput));
        JSONObject jo = new JSONObject(Files.readString(tmpPersonalDict));
        assertEquals("0z123", jo.getString("TTVVV"));
        assertEquals("fghij", jo.getString("abcde"));
        assertEquals("z0123", jo.getString("uvwxy"));
    }

    @Test
    void shouldCatchRowNotParseAsIntegerAndColumnNotParseAsIntegerEditDictionary() throws IOException {
        String feedInAsInput = "clickTildeButton\neditCellContents\nq\neditCellContents\n1\nv\nprintTable\nclickCancel\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickTildeButton\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\nq\n" +
                "invalid, row doesn't parse as integer\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n1\n" +
                "Enter column number (counting from zero'th column):\nv\n" +
                "invalid, column doesn't parse as integer\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: printTable\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickCancel\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(output, feedInAsInput));
    }

    @Test
    void shouldBeAbleToEditThirdColumnWithLessThanOrGreaterThanOnly() throws IOException {
        String feedInAsInput = "clickAsteriskTildeButton\neditCellContents\n2\n2\n%\neditCellContents\n2\n2\n>\nprintTable\neditCellContents\n2\n2\n<\nprintTable\neditCellContents\n2\n1\nabe$1\nprintTable\nclickCancel\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickAsteriskTildeButton\n" +
                "**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n2\n" +
                "Enter column number (counting from zero'th column):\n2\n" +
                "Enter new value:\n%\n" +
                "invalid, direction column must be either < or >\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n2\n" +
                "Enter column number (counting from zero'th column):\n2\n" +
                "Enter new value:\n>\n" +
                "Cell contents changed to: >\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: printTable\n" +
                "**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t>\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: editCellContents\nEnter row number (counting from zero'th row):\n2\n" +
                "Enter column number (counting from zero'th column):\n2\n" +
                "Enter new value:\n<\n" +
                "Cell contents changed to: <\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: printTable\n**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: editCellContents\nEnter row number (counting from zero'th row):\n2\n" +
                "Enter column number (counting from zero'th column):\n1\n" +
                "Enter new value:\nabe$1\n" +
                "Cell contents changed to: abe$1\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: printTable\n**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tabe$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: clickCancel\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(output, feedInAsInput));
    }


    @Test
    void shouldBeAbleToAddAndRemoveRows() throws IOException {
        String feedInAsInput = "clickTildeButton\nclickAdd\neditCellContents\n3\n0\nbbbbb\neditCellContents\n3\n1\nqqqqq\nprintTable\nclickRemove\n1\nclickRemove\n-1\nclickRemove\n99\nclickRemove\nx\nprintTable\nclickCancel\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickTildeButton\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickAdd\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "\t\t\t<<<<<\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n3\n" +
                "Enter column number (counting from zero'th column):\n0\n" +
                "Enter new value:\nbbbbb\n" +
                "Cell contents changed to: bbbbb\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: editCellContents\n" +
                "Enter row number (counting from zero'th row):\n3\n" +
                "Enter column number (counting from zero'th column):\n1\n" +
                "Enter new value:\nqqqqq\n" +
                "Cell contents changed to: qqqqq\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: printTable\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickRemove\n" +
                "Enter row number (counting from zero'th row):\n1\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123<<<<<\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickRemove\n" +
                "Enter row number (counting from zero'th row):\n-1\n" +
                "invalid, row is out of range\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickRemove\n" +
                "Enter row number (counting from zero'th row):\n99\n" +
                "invalid, row is out of range\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickRemove\n" +
                "Enter row number (counting from zero'th row):\nx\n" +
                "invalid, row doesn't parse as integer\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: printTable\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickCancel\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(output, feedInAsInput));
    }

    @Test
    void shouldBeAbleToSortRows() throws IOException {
        String feedInAsInput = "clickTildeButton\nclickSortBySensitive\nclickSortBySafe\nclickCancel\nexit\n";
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream(feedInAsInput.getBytes()), mockOut)
                .build();        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: clickTildeButton\n" +
                "**********************\n" +
                "abcde			fghij\n" +
                "vuwxy			0z123\n" +
                "uvwxy			z0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickSortBySensitive\n" +
                "**********************\n" +
                "abcde			fghij\n" +
                "uvwxy			z0123\n" +
                "vuwxy			0z123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickSortBySafe\n" +
                "**********************\n" +
                "vuwxy			0z123\n" +
                "abcde			fghij\n" +
                "uvwxy			z0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: clickCancel\n" +
                "SanitizerPromptLoop ... What do you want to do: exit", cleanup(output, feedInAsInput));
    }
    private String cleanup(String t, String feedInAsInput) {
        t = t
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("\\u001B\\[[;?0-9]*[ -/]*[@-~]", "")
                .replaceAll("\\u001B[=>]", "");
        assertTrue(t.startsWith(feedInAsInput));
        t = t.substring(feedInAsInput.length());
        while(t.contains("\n\n")) {
            t = t.replace("\n\n", "\n");
        }
        if(t.endsWith("\n")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }
}

