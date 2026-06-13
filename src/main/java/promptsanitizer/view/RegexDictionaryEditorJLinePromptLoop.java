package promptsanitizer.view;

import org.jline.terminal.Terminal;
import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.AbstractDictionaryModel;

import java.io.PrintStream;
import java.util.Scanner;

public class RegexDictionaryEditorJLinePromptLoop extends DictionaryEditorJLinePromptLoop {
    private final Terminal terminal;
    public RegexDictionaryEditorJLinePromptLoop(
            String fileName,
            DictionaryEditorController controller,
            AbstractDictionaryModel model,
            Terminal terminal
    ) {
        super(fileName, controller, model, terminal);
        this.model = model;
        this.terminal = terminal;
    }
    String getRegexPrefix() {
        return "Regex";
    }
    String getSortByFirstLabel() {
        return "clickSortByRegex";
    }
    String getSortBySecondLabel() {
        return "clickSortByReplacement";
    }
    String getThirdValue(int i) {
        return "\t" + model.getValueAt(i, 2);
    }
    void printHelp() {
        terminal.writer().println("You are in the regex dictionary editor prompt loop.  Choices are:");
        terminal.writer().println("  clickCancel               - Close the regex dictionary editor and return to main loop");
        terminal.writer().println("  clickAdd                    - Add a new empty row to the regex dictionary");
        terminal.writer().println("  clickRemove                 - Remove the selected row (prompts for row number)");
        terminal.writer().println("  clickSortByRegex            - Sort rows by the regex column");
        terminal.writer().println("  clickSortByReplacement      - Sort rows by the replacement column");
        terminal.writer().println("  printTable                  - Print the current regex dictionary table");
        terminal.writer().println("  editCellContents            - Edit a cell (prompts for row, column, and new value)");
        terminal.writer().println("  clickSaveToFile             - Save the regex dictionary to file and closes the regex dictionary editor to return to main loop");
    }
    boolean isValidThirdColumn(int column, String command) {
        if(column != 2 || ">".equals(command) || "<".equals(command)) {
            return true;
        }
        terminal.writer().println("invalid, direction column must be either < or >");
        return false;
    }
}

