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
import org.junit.jupiter.api.Disabled;
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

@Disabled
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
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("stareOutAWindow\nhelp\nexit\n".getBytes()), mockOut)
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
        assertEquals("SanitizerPromptLoop ... What do you want to do: " +
                "Unknown command.  Type 'help' for a list of commands.\n" +
                "SanitizerPromptLoop ... What do you want to do: " +
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
                "SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldUnknownCommandFollowedByHelp2() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickAsteriskTildeButton\nhungry\nhelp\nclickCancel\nclickTildeButton\nhungry\nhelp\nclickCancel\nexit\n".getBytes()), mockOut)
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
        assertEquals("SanitizerPromptLoop ... What do you want to do: **********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: Unknown command.  Type 'help' for a list of commands.\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: You are in the regex dictionary editor prompt loop.  Choices are:\n" +
                "  clickCancel               - Close the regex dictionary editor and return to main loop\n" +
                "  clickAdd                    - Add a new empty row to the regex dictionary\n" +
                "  clickRemove                 - Remove the selected row (prompts for row number)\n" +
                "  clickSortByRegex            - Sort rows by the regex column\n" +
                "  clickSortByReplacement      - Sort rows by the replacement column\n" +
                "  printTable                  - Print the current regex dictionary table\n" +
                "  editCellContents            - Edit a cell (prompts for row, column, and new value)\n" +
                "  clickSaveToFile             - Save the regex dictionary to file and closes the regex dictionary editor to return to main loop\n" +
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
                "  clickSaveToFile             - Save the dictionary to file and closes the dictionary editor to return to main loop\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void implicitExit_shouldTerminateLoop() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickAsteriskTildeButton\n".getBytes()), mockOut)
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
        assertEquals("SanitizerPromptLoop ... What do you want to do: **********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldBeAbleToSanitizePrompt() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("enterLeft\nenterLeft: Lorem \\nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nclickMoveRight\nprintRight\nexit\n".getBytes()), mockOut)
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
        assertEquals("SanitizerPromptLoop ... What do you want to do: invalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>\nSanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: Lorem \nipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nSanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void uninitializedDictionaries() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("enterLeft\nenterLeft: Lorem \\nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nclickMoveRight\nprintRight\nexit\n".getBytes()), mockOut)
                .build();
        Path tmpPersonalDictNotExist = Path.of(tmpPersonalDict.toString().replace("personalDict", "personalDictNotExist"));
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
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n".getBytes()), mockOut)
                .build();
        Path tmpPersonalDictNotExist = Path.of(tmpPersonalDict.toString().replace("personalDict", "directoryNotExist" + System.getProperty("file.separator") + "personalDict"));
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
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerModel model = new SanitizerModel();
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
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("enterRight\nenterRight: Lorem \\nipsum fghij ficum 47bdf acaazepp 0z123 landum z0123\nclickMoveLeft\nprintLeft\nexit\n".getBytes()), mockOut)
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
        assertEquals("SanitizerPromptLoop ... What do you want to do: invalid.  Expected format is \"enterRight: <text you want to enter with just type \\n for newlines>\nSanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: SanitizerPromptLoop ... What do you want to do: Lorem \nipsum abcde ficum ace47 welpacaa vuwxy landum uvwxy\nSanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void shouldBeAbleToEditDictionary() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\neditCellContents\n-1\n40\nqqqqq\neditCellContents\n1\n0\nTTVVV\nprintTable\nclickSaveToFile\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
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
                "invalid, either row or column are out of range\n" +
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
        JSONObject jo = new JSONObject(Files.readString(tmpPersonalDict));
        assertEquals("0z123", jo.getString("TTVVV"));
        assertEquals("fghij", jo.getString("abcde"));
        assertEquals("z0123", jo.getString("uvwxy"));
    }

    @Test
    void shouldCatchRowNotParseAsIntegerAndColumnNotParseAsIntegerEditDictionary() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\neditCellContents\nq\neditCellContents\n1\nv\nprintTable\nclickCancel\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
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
                "invalid, row doesn't parse as integer\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "invalid, column doesn't parse as integer\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
    void shouldBeAbleToEditThirdColumnWithLessThanOrGreaterThanOnly() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickAsteriskTildeButton\neditCellContents\n2\n2\n%\neditCellContents\n2\n2\n>\nprintTable\neditCellContents\n2\n2\n<\nprintTable\neditCellContents\n2\n1\nabe$1\nprintTable\nclickCancel\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "invalid, direction column must be either < or >\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: >\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t>\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: <\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: **********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tace$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: abe$1\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: **********************\n" +
                "ace([0-9]*)\t\t\t$1bdf\t>\n" +
                "welp([a-z]*)\t\t\t$1zepp\t>\n" +
                "([0-9]*)bdf\t\t\tabe$1\t<\n" +
                "([a-z]*)zepp\t\t\twelp$1\t<\n" +
                "**********************\n" +
                "RegexDictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
    }


    @Test
    void shouldBeAbleToAddAndRemoveRows() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\nclickAdd\neditCellContents\n3\n0\nbbbbb\neditCellContents\n3\n1\nqqqqq\nprintTable\nclickRemove\n1\nclickRemove\n-1\nclickRemove\n99\nclickRemove\nx\nprintTable\nclickCancel\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
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
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "\t\t\t<<<<<\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: bbbbb\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "Enter column number (counting from zero'th column):\n" +
                "Enter new value:\n" +
                "Cell contents changed to: qqqqq\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "vuwxy\t\t\t0z123<<<<<\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "Enter row number (counting from zero'th row):\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde\t\t\tfghij\n" +
                "uvwxy\t\t\tz0123\n" +
                "bbbbb\t\t\tqqqqq\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
        /*String err = capturedError.toString();
        assertEquals("invalid, row is out of range\n" +
                "invalid, row is out of range\n" +
                "invalid, row doesn't parse as integer\n", err);*/
    }

    @Test
    void shouldBeAbleToSortRows() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(new ByteArrayInputStream("clickTildeButton\nclickSortBySensitive\nclickSortBySafe\nclickCancel\nexit\n".getBytes()), mockOut)
                .build();
        SanitizerJLinePromptLoop loop = new SanitizerJLinePromptLoop(
                tmpPersonalDict.toString(),
                tmpRegexPersonalDict.toString(),
                new SanitizerController(),
                new SanitizerModel(),
                terminal
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde			fghij\n" +
                "vuwxy			0z123\n" +
                "uvwxy			z0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "abcde			fghij\n" +
                "uvwxy			z0123\n" +
                "vuwxy			0z123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "**********************\n" +
                "vuwxy			0z123\n" +
                "abcde			fghij\n" +
                "uvwxy			z0123\n" +
                "**********************\n" +
                "DictionaryEditorPromptLoop ... What do you want to do: " +
                "SanitizerPromptLoop ... What do you want to do: ", output);
    }
}

