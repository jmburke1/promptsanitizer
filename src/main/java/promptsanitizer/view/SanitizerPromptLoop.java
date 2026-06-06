package promptsanitizer.view;

import javax.swing.JTextArea;
import javax.swing.JButton;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class SanitizerPromptLoop {
    private final JTextArea leftArea;
    private final JTextArea rightArea;
    private final JButton moveRightButton;
    private final JButton moveLeftButton;
    private final JButton tildeButton;
    private final JButton asteriskTildeButton;
    private final PrintStream shouldBeSystemOut;
    private final PrintStream shouldBeSystemErr;
    private final InputStream shouldBeSystemIn;
    public SanitizerPromptLoop(JTextArea leftArea,
                               JTextArea rightArea,
                               JButton moveRightButton,
                               JButton moveLeftButton,
                               JButton tildeButton,
                               JButton asteriskTildeButton,
                               PrintStream shouldBeSystemOut,
                               PrintStream shouldBeSystemErr,
                               InputStream shouldBeSystemIn
    ) {
        this.leftArea = leftArea;
        this.rightArea = rightArea;
        this.moveRightButton = moveRightButton;
        this.moveLeftButton = moveLeftButton;
        this.tildeButton = tildeButton;
        this.asteriskTildeButton = asteriskTildeButton;
        this.shouldBeSystemOut = shouldBeSystemOut;
        this.shouldBeSystemErr = shouldBeSystemErr;
        this.shouldBeSystemIn = shouldBeSystemIn;
    }
    public void promptForWhatToDo() {
        boolean keepGoing = true;
        Scanner scanner = new Scanner(shouldBeSystemIn);
        while(keepGoing) {
            shouldBeSystemOut.print("SanitizerPromptLoop ... What do you want to do: ");
            String command = scanner.hasNextLine() ? scanner.nextLine() : "exit";
            if("exit".equals(command)) {
                keepGoing = false;
            } else if("printLeft".equals(command)) {
                shouldBeSystemOut.println(leftArea.getText());
            } else if(command.startsWith("enterLeft")) {
                if(!command.startsWith("enterLeft: ")) {
                    shouldBeSystemErr.println("invalid.  Expected format is \"enterLeft: <text you want to enter with just type \\n for newlines>");
                    continue;
                }
                String actualText = command.substring(11).replace("\\n", "\n");
                leftArea.setText(actualText);
            } else if("printRight".equals(command)) {
                shouldBeSystemOut.println(rightArea.getText());
            } else if(command.startsWith("enterRight")) {
                if(!command.startsWith("enterRight: ")) {
                    shouldBeSystemErr.println("invalid.  Expected format is \"enterRight: <text you want to enter with just type \\n for newlines>");
                    continue;
                }
                String actualText = command.substring(12).replace("\\n", "\n");
                rightArea.setText(actualText);
            } else if("clickMoveLeft".equals(command)) {
                moveLeftButton.doClick();
            } else if("clickMoveRight".equals(command)) {
                moveRightButton.doClick();
            } else if("clickTildeButton".equals(command)) {
                tildeButton.doClick();
            } else if("clickAsteriskTildeButton".equals(command)) {
                asteriskTildeButton.doClick();
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

