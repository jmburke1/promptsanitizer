package promptsanitizer.view;

import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class SanitizerPromptLoop {
    private final PrintStream shouldBeSystemOut;
    private final PrintStream shouldBeSystemErr;
    private final InputStream shouldBeSystemIn;
    private String leftAreaText;
    private String rightAreaText;
    private final SanitizerController controller;
    private final SanitizerModel model;
    private final String fileName;
    private final String regexFileName;
    public SanitizerPromptLoop(String fileName,
                               String regexFileName,
                               SanitizerController controller,
                               SanitizerModel model,
                               PrintStream shouldBeSystemOut,
                               PrintStream shouldBeSystemErr,
                               InputStream shouldBeSystemIn
    ) {
        this.shouldBeSystemOut = shouldBeSystemOut;
        this.shouldBeSystemErr = shouldBeSystemErr;
        this.shouldBeSystemIn = shouldBeSystemIn;
        this.controller = controller;
        this.model = model;
        this.fileName = fileName;
        this.regexFileName = regexFileName;
    }

    public void promptForWhatToDo() {
        controller.init(model, fileName, regexFileName, (title, message) -> shouldBeSystemOut.println("[%s] %s"));
        boolean keepGoing = true;
        Scanner scanner = new Scanner(shouldBeSystemIn);
        while(keepGoing) {
            shouldBeSystemOut.print("SanitizerPromptLoop ... What do you want to do: ");
            String command = scanner.hasNextLine() ? scanner.nextLine() : "exit";
            if("exit".equals(command)) {
                keepGoing = false;
            } else if("printLeft".equals(command)) {
                shouldBeSystemOut.println(leftAreaText);
            } else if(command.startsWith("enterLeft")) {
                if(!command.startsWith("enterLeft: ")) {
                    shouldBeSystemErr.println("invalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>");
                    continue;
                }
                leftAreaText = command.substring(11).replace("\\n", "\n");
            } else if("printRight".equals(command)) {
                shouldBeSystemOut.println(rightAreaText);
            } else if(command.startsWith("enterRight")) {
                if(!command.startsWith("enterRight: ")) {
                    shouldBeSystemErr.println("invalid.  Expected format is \"enterRight: <text you want to enter with just type \\n for newlines>");
                    continue;
                }
                rightAreaText = command.substring(12).replace("\\n", "\n");
            } else if("clickMoveLeft".equals(command)) {
                controller.moveText(() -> rightAreaText, s -> leftAreaText = s, s -> rightAreaText = s, true);
            } else if("clickMoveRight".equals(command)) {
                controller.moveText(() -> leftAreaText, s -> rightAreaText = s, s -> leftAreaText = s, false);
            } else if("clickTildeButton".equals(command)) {
                controller.handleTilde();
            } else if("clickAsteriskTildeButton".equals(command)) {
                controller.handleAsteriskTilde();
            } else if("help".equals(command)) {
                shouldBeSystemOut.println("You are in the sanitizer prompt loop.  Choices are:");
                shouldBeSystemOut.println("  exit                        - Exit the application");
                shouldBeSystemOut.println("  printLeft                   - Print the left panel text");
                shouldBeSystemOut.println("  enterLeft: <text>           - Set the left panel text (use \\n for newlines)");
                shouldBeSystemOut.println("  printRight                  - Print the right panel text");
                shouldBeSystemOut.println("  enterRight: <text>          - Set the right panel text (use \\n for newlines)");
                shouldBeSystemOut.println("  clickMoveRight              - Sanitize left panel and write to right panel");
                shouldBeSystemOut.println("  clickMoveLeft               - Restore right panel and write to left panel");
                shouldBeSystemOut.println("  clickTildeButton            - Open the dictionary editor (~)");
                shouldBeSystemOut.println("  clickAsteriskTildeButton    - Open the regex dictionary editor (*~)");
            } else {
                shouldBeSystemOut.println("Unknown command.  Type 'help' for a list of commands.");
            }
        }
    }
}

