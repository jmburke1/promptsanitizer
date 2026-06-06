/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JButton;
import javax.swing.JTextArea;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanitizerPromptLoopTest {

    private final ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
    private final ByteArrayOutputStream capturedError = new ByteArrayOutputStream();
    private PrintStream mockOut;
    private PrintStream mockErr;

    @BeforeEach
    void setUp() {
        mockOut = new PrintStream(capturedOutput);
        mockErr = new PrintStream(capturedError);
    }

    @Test
    void exit_shouldTerminateLoop() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("exit\n".getBytes())
        );

        loop.promptForWhatToDo();

        // The prompt should have been printed once
        assertEquals("SanitizerPromptLoop ... What do you want to do: ", capturedOutput.toString());
    }

    @Test
    void printLeft_shouldPrintLeftAreaText() {
        JTextArea leftArea = Mockito.mock(JTextArea.class);
        Mockito.when(leftArea.getText()).thenReturn("sensitive data here");
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                leftArea,
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("printLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: sensitive data here\nSanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
    void printRight_shouldPrintRightAreaText() {
        JTextArea rightArea = Mockito.mock(JTextArea.class);
        Mockito.when(rightArea.getText()).thenReturn("sanitized text");
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                rightArea,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("printRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: sanitized text\nSanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
    void enterLeft_shouldSetLeftAreaText() {
        JTextArea leftArea = Mockito.mock(JTextArea.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                leftArea,
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterLeft: hello world\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(leftArea).setText("hello world");
    }

    @Test
    void enterLeft_withEscapedNewlines_shouldReplaceBackslashN() {
        JTextArea leftArea = Mockito.mock(JTextArea.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                leftArea,
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterLeft: line1\\nline2\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(leftArea).setText("line1\nline2");
    }

    @Test
    void enterRight_shouldSetRightAreaText() {
        JTextArea rightArea = Mockito.mock(JTextArea.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                rightArea,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight: some text\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(rightArea).setText("some text");
    }

    @Test
    void enterRight_withEscapedNewlines_shouldReplaceBackslashN() {
        JTextArea rightArea = Mockito.mock(JTextArea.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                rightArea,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("enterRight: line1\\nline2\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(rightArea).setText("line1\nline2");
    }

    @Test
    void enterLeft_withoutColon_shouldPrintErrorToStderr() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
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
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
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
        JButton moveRightButton = Mockito.mock(JButton.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                moveRightButton,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickMoveRight\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(moveRightButton).doClick();
    }

    @Test
    void clickMoveLeft_shouldInvokeDoClickOnMoveLeftButton() {
        JButton moveLeftButton = Mockito.mock(JButton.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                moveLeftButton,
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickMoveLeft\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(moveLeftButton).doClick();
    }

    @Test
    void clickTildeButton_shouldInvokeDoClickOnTildeButton() {
        JButton tildeButton = Mockito.mock(JButton.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                tildeButton,
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickTildeButton\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(tildeButton).doClick();
    }

    @Test
    void clickAsteriskTildeButton_shouldInvokeDoClickOnAsteriskTildeButton() {
        JButton asteriskTildeButton = Mockito.mock(JButton.class);
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                asteriskTildeButton,
                mockOut,
                mockErr,
                new ByteArrayInputStream("clickAsteriskTildeButton\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        Mockito.verify(asteriskTildeButton).doClick();
    }

    @Test
    void invalidCommand_shouldPrintHelpMessage() {
        SanitizerPromptLoop loop = new SanitizerPromptLoop(
                Mockito.mock(JTextArea.class),
                Mockito.mock(JTextArea.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                Mockito.mock(JButton.class),
                mockOut,
                mockErr,
                new ByteArrayInputStream("bogusCommand\nexit\n".getBytes())
        );

        loop.promptForWhatToDo();

        String output = capturedOutput.toString();
        assertEquals("SanitizerPromptLoop ... What do you want to do: Unknown command.  Type 'help' for a list of commands.\nSanitizerPromptLoop ... What do you want to do: ", output);
    }

    @Test
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
    }
}

