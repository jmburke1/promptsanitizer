/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer;

import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;

public class JLinePromptDemo {

    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        Completer commandCompleter = new StringsCompleter(
                "help",
                "login",
                "search",
                "set-name",
                "multiline",
                "quit"
        );

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(new DefaultParser())
                .completer(commandCompleter)
                .variable(LineReader.HISTORY_FILE, ".jline-demo-history") //Comment this out if you want session level history only
                .build();

        terminal.writer().println("JLine prompt demo");
        terminal.writer().println("Try arrow keys, backspace, Ctrl+A, Ctrl+E, tab completion, and command history.");
        terminal.writer().println("Commands: help, login, search, set-name, multiline, quit");
        terminal.writer().println();

        String name = "anonymous";

        while (true) {
            try {
                String command = reader.readLine("demo> ").trim();

                switch (command) {
                    case "":
                        break;

                    case "help":
                        printHelp(terminal);
                        break;

                    case "login":
                        String username = reader.readLine("Username: ");
                        String password = reader.readLine("Password: ", '*');

                        terminal.writer().println("Username entered: " + username);
                        terminal.writer().println("Password length: " + password.length());
                        terminal.writer().println("(Not printing the password, obviously.)");
                        break;

                    case "search":
                        String query = reader.readLine("Search query: ");
                        terminal.writer().println("You searched for: " + query);
                        break;

                    case "set-name":
                        String enteredName = reader.readLine("Display name [" + name + "]: ");

                        if (!enteredName.isBlank()) {
                            name = enteredName;
                        }

                        terminal.writer().println("Display name is now: " + name);
                        break;

                    case "multiline":
                        terminal.writer().println("Enter multiple lines. Type a single '.' on its own line to finish.");

                        StringBuilder block = new StringBuilder();

                        while (true) {
                            String line = reader.readLine("... ");

                            if (line.equals(".")) {
                                break;
                            }

                            block.append(line).append(System.lineSeparator());
                        }

                        terminal.writer().println("You entered:");
                        terminal.writer().println(block);
                        break;

                    case "quit":
                    case "exit":
                        terminal.writer().println("Goodbye.");
                        return;

                    default:
                        terminal.writer().println("Unknown command: " + command);
                        terminal.writer().println("Press Tab after typing part of a command, or type help.");
                        break;
                }

                terminal.writer().flush();

            } catch (UserInterruptException e) {
                terminal.writer().println();
                terminal.writer().println("Ctrl+C pressed. Type quit to exit.");
                terminal.writer().flush();

            } catch (EndOfFileException e) {
                terminal.writer().println();
                terminal.writer().println("Ctrl+D pressed. Goodbye.");
                terminal.writer().flush();
                return;
            }
        }
    }

    private static void printHelp(Terminal terminal) {
        List<String> lines = List.of(
                "Available commands:",
                "  help       Show this help",
                "  login      Prompt for username and masked password",
                "  search     Prompt for a search string",
                "  set-name   Prompt with a default value",
                "  multiline  Read multiple lines until '.'",
                "  quit       Exit the program",
                "",
                "JLine features to try:",
                "  Up/Down arrows    command history",
                "  Left/Right arrows  edit the current line",
                "  Tab                command completion",
                "  Ctrl+A             jump to beginning of line",
                "  Ctrl+E             jump to end of line",
                "  Ctrl+C             interrupt current prompt",
                "  Ctrl+D             EOF / exit"
        );

        for (String line : lines) {
            terminal.writer().println(line);
        }
    }
}

/*

for nested readers, just build a second LineReader based on the same terminal.  Then, when that flow of execution is done, be sure to flush before returning.

for junit testing, use

Terminal terminal = TerminalBuilder.builder()
        .system(false)
        .streams(byteArrayInputStream, byteArrayOutputStream)
        .build();

instead of system(true)

*/
