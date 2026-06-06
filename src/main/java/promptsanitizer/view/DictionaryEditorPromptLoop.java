package promptsanitizer.view;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class DictionaryEditorPromptLoop {
    private final JButton addBtn;
    private final JButton rmBtn;
    private final JButton sortByFirstBtn;
    private final JButton sortBySecondBtn;
    private final JTable table;
    private final JButton saveBtn;
    private final PrintStream shouldBeSystemOut;
    private final PrintStream shouldBeSystemErr;
    private final InputStream shouldBeSystemIn;
    public DictionaryEditorPromptLoop(
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
        this.addBtn = addBtn;
        this.rmBtn = rmBtn;
        this.sortByFirstBtn = sortByFirstBtn;
        this.sortBySecondBtn = sortBySecondBtn;
        this.table = table;
        this.saveBtn = saveBtn;
        this.shouldBeSystemOut = shouldBeSystemOut;
        this.shouldBeSystemErr = shouldBeSystemErr;
        this.shouldBeSystemIn = shouldBeSystemIn;
    }
    public void promptForWhatToDo() {
        boolean keepGoing = true;
        Scanner scanner = new Scanner(shouldBeSystemIn);
        while(keepGoing) {
            shouldBeSystemOut.print(getRegexPrefix() + "DictionaryEditorPromptLoop ... What do you want to do: ");
            String command = scanner.hasNextLine() ? scanner.nextLine() : "clickCancel";
            if("clickCancel".equals(command)) {
                keepGoing = false;
            } else if("clickAdd".equals(command)) {
                addBtn.doClick();
            } else if("clickRemove".equals(command)) {
                shouldBeSystemOut.println("Enter row number (counting from zero'th row):");
                command = scanner.nextLine();
                int row;
                try {
                    row = Integer.parseInt(command);
                } catch(NumberFormatException nfe) {
                    shouldBeSystemErr.println("invalid, row doesn't parse as integer");
                    continue;
                }
                if(row < 0 || table.getRowCount() <= row) {
                    shouldBeSystemErr.println("invalid, row is out of range");
                    continue;
                }
                table.setRowSelectionInterval(row, row);
                rmBtn.doClick();
            } else if(getSortByFirstLabel().equals(command)) {
                sortByFirstBtn.doClick();
            } else if(getSortBySecondLabel().equals(command)) {
                sortBySecondBtn.doClick();
            } else if("printTable".equals(command)) {
                int rowCount = table.getRowCount();
                shouldBeSystemOut.println("**********************");
                for(int i=0; i<rowCount; i++) {
                    shouldBeSystemOut.println(table.getValueAt(i,0) + "\t\t\t" + table.getValueAt(i,1) + getThirdValue(i));
                }
                shouldBeSystemOut.println("**********************");
            } else if(command.equals("editCellContents")) {
                shouldBeSystemOut.println("Enter row number (counting from zero'th row):");
                command = scanner.nextLine();
                int row;
                try {
                    row = Integer.parseInt(command);
                } catch(NumberFormatException nfe) {
                    shouldBeSystemErr.println("invalid, row doesn't parse as integer");
                    continue;
                }
                int column;
                shouldBeSystemOut.println("Enter column number (counting from zero'th column):");
                command = scanner.nextLine();
                try {
                    column = Integer.parseInt(command);
                } catch(NumberFormatException nfe) {
                    shouldBeSystemErr.println("invalid, column doesn't parse as integer");
                    continue;
                }
                shouldBeSystemOut.println("Enter new value:");
                command = scanner.nextLine();
                if(!table.editCellAt(row, column)) {
                    shouldBeSystemErr.println("invalid, either row or column are out of range");
                    continue;
                }
                if(!isValidThirdColumn(column, command)) {
                    continue;
                }
                TableCellEditor editor = table.getCellEditor();
                ((JTextField)((DefaultCellEditor)editor).getComponent()).setText(command);
                editor.stopCellEditing();
            } else if("clickSaveToFile".equals(command)) {
                saveBtn.doClick();
            } else if("help".equals(command)) {
                printHelp();
            } else {
                shouldBeSystemOut.println("Unknown command.  Type 'help' for a list of commands.");
            }
        }
    }
    String getRegexPrefix() {
        return "";
    }
    String getSortByFirstLabel() {
        return "clickSortBySensitive";
    }
    String getSortBySecondLabel() {
        return "clickSortBySafe";
    }
    String getThirdValue(int i) {
        return "";
    }
    void printHelp() {
        shouldBeSystemOut.println("You are in the dictionary editor prompt loop.  Choices are:");
        shouldBeSystemOut.println("  clickCancel               - Close the dictionary editor and return to main loop");
        shouldBeSystemOut.println("  clickAdd                    - Add a new empty row to the dictionary");
        shouldBeSystemOut.println("  clickRemove                 - Remove the selected row (prompts for row number)");
        shouldBeSystemOut.println("  clickSortBySensitive        - Sort rows by the sensitive (left) column");
        shouldBeSystemOut.println("  clickSortBySafe             - Sort rows by the safe (right) column");
        shouldBeSystemOut.println("  printTable                  - Print the current dictionary table");
        shouldBeSystemOut.println("  editCellContents            - Edit a cell (prompts for row, column, and new value)");
        shouldBeSystemOut.println("  clickSaveToFile             - Save the dictionary to file");
    }
    boolean isValidThirdColumn(int column, String command) {
        return true;
    }
}

