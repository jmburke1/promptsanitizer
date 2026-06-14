package promptsanitizer.view;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;

import org.jline.terminal.Terminal;

public class SanitizerJLinePromptLoop {
    private final Terminal terminal;
    private String leftAreaText;
    private String rightAreaText;
    private final SanitizerController controller;
    private final SanitizerModel model;
    private final String fileName;
    private final String regexFileName;
    public SanitizerJLinePromptLoop(String fileName,
                                    String regexFileName,
                                    SanitizerController controller,
                                    SanitizerModel model,
                                    Terminal terminal
    ) {
        this.terminal = terminal;
        this.controller = controller;
        this.model = model;
        this.fileName = fileName;
        this.regexFileName = regexFileName;
    }

    public void promptForWhatToDo() {
        model.init(fileName, regexFileName);
        controller.init(model, fileName, regexFileName, (title, message) -> terminal.writer().println(String.format("[%s] %s", title, message)));
        boolean keepGoing = true;

        Completer commandCompleter = new StringsCompleter(
                "exit",
                "help",
                "printLeft",
                "enterLeft: ",
                "printRight",
                "enterRight: ",
                "clickMoveRight",
                "clickMoveLeft",
                "clickTildeButton",
                "clickAsteriskTildeButton"
        );

        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(new char[]{});
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .completer(commandCompleter)
                .build();

        while(keepGoing) {
            try {
                String command = reader.readLine("SanitizerPromptLoop ... What do you want to do: ").trim();
                if("exit".equals(command)) {
                    keepGoing = false;
                } else if("printLeft".equals(command)) {
                    terminal.writer().println(leftAreaText);
                } else if(command.startsWith("enterLeft")) {
                    if(!command.startsWith("enterLeft: ")) {
                        terminal.writer().println("invalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>");
                        continue;
                    }
                    leftAreaText = command.substring(11).replace("\\n", "\n");
                } else if("printRight".equals(command)) {
                    terminal.writer().println(rightAreaText);
                } else if(command.startsWith("enterRight")) {
                    if(!command.startsWith("enterRight: ")) {
                        terminal.writer().println("invalid.  Expected format is \"enterRight: <text you want to enter with just type \\n for newlines>");
                        continue;
                    }
                    rightAreaText = command.substring(12).replace("\\n", "\n");
                } else if("clickMoveLeft".equals(command)) {
                    controller.moveText(() -> rightAreaText, s -> leftAreaText = s, s -> rightAreaText = s, true);
                } else if("clickMoveRight".equals(command)) {
                    controller.moveText(() -> leftAreaText, s -> rightAreaText = s, s -> leftAreaText = s, false);
                } else if("clickTildeButton".equals(command)) {
                    controller.handleTilde(terminal);
                } else if("clickAsteriskTildeButton".equals(command)) {
                    controller.handleAsteriskTilde(terminal);
                } else if("help".equals(command)) {
                    terminal.writer().println("You are in the sanitizer prompt loop.  Choices are:");
                    terminal.writer().println("  exit                        - Exit the application");
                    terminal.writer().println("  help                        - Show this menu of available commands");
                    terminal.writer().println("  printLeft                   - Print the unsanitized left panel text");
                    terminal.writer().println("  enterLeft: <text>           - Set the unsanitized left panel text (use \\n for newlines)");
                    terminal.writer().println("  printRight                  - Print the right panel sanitized text");
                    terminal.writer().println("  enterRight: <text>          - Set the right panel sanitized text which is the answer you received (use \\n for newlines)");
                    terminal.writer().println("  clickMoveRight              - Sanitize left panel and write to right panel");
                    terminal.writer().println("  clickMoveLeft               - Personalize right panel and write to left panel");
                    terminal.writer().println("  clickTildeButton            - Open the dictionary editor (~)");
                    terminal.writer().println("  clickAsteriskTildeButton    - Open the regex dictionary editor (*~)");
                } else {
                    terminal.writer().println("Unknown command.  Type 'help' for a list of commands.");
                }
                terminal.writer().flush();
            } catch (UserInterruptException | EndOfFileException e) {
                keepGoing = false;
            }
        }
    }
}

