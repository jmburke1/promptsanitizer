package promptsanitizer.view;

import javax.swing.*;
import java.io.InputStream;
import java.io.PrintStream;

public class RegexDictionaryEditorPromptLoop extends DictionaryEditorPromptLoop {
    private final JTable table;
    private final PrintStream shouldBeSystemOut;
    private final PrintStream shouldBeSystemErr;
    public RegexDictionaryEditorPromptLoop(
            JButton addBtn,
            JButton rmBtn,
            JButton sortByFirstBtn,
            JButton sortBySecondBtn,
            JButton saveBtn,
            JTable table,
            PrintStream shouldBeSystemOut,
            PrintStream shouldBeSystemErr,
            InputStream shouldBeSystemIn
    ) {
        super(addBtn, rmBtn, sortByFirstBtn, sortBySecondBtn, saveBtn, table, shouldBeSystemOut, shouldBeSystemErr, shouldBeSystemIn);
        this.table = table;
        this.shouldBeSystemOut = shouldBeSystemOut;
        this.shouldBeSystemErr = shouldBeSystemErr;
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
        return "\t" + table.getValueAt(i, 2);
    }
    void printHelp() {
        shouldBeSystemOut.println("You are in the regex dictionary editor prompt loop.  Choices are:");
        shouldBeSystemOut.println("  clickCancel               - Close the regex dictionary editor and return to main loop");
        shouldBeSystemOut.println("  clickAdd                    - Add a new empty row to the regex dictionary");
        shouldBeSystemOut.println("  clickRemove                 - Remove the selected row (prompts for row number)");
        shouldBeSystemOut.println("  clickSortByRegex            - Sort rows by the regex column");
        shouldBeSystemOut.println("  clickSortByReplacement      - Sort rows by the replacement column");
        shouldBeSystemOut.println("  printTable                  - Print the current regex dictionary table");
        shouldBeSystemOut.println("  editCellContents            - Edit a cell (prompts for row, column, and new value)");
        shouldBeSystemOut.println("  clickSaveToFile             - Save the regex dictionary to file");
    }
    boolean isValidThirdColumn(int column, String command) {
        if(column != 2 || ">".equals(command) || "<".equals(command)) {
            return true;
        }
        shouldBeSystemErr.println("invalid, direction column must be either < or >");
        return false;
    }
}

