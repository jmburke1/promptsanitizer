package promptsanitizer.view;

import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.AbstractDictionaryModel;

import javax.swing.*;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class DictionaryEditorPromptLoop {
    private boolean keepGoing;
    private DictionaryEditorController controller;
    private AbstractDictionaryModel model;
    private int selectedRow;
    private final PrintStream shouldBeSystemOut;
    private final PrintStream shouldBeSystemErr;
    private final InputStream shouldBeSystemIn;
    public DictionaryEditorPromptLoop(
            String fileName,
            DictionaryEditorController controller,
            AbstractDictionaryModel model,
            PrintStream shouldBeSystemOut,
            PrintStream shouldBeSystemErr,
            InputStream shouldBeSystemIn
    ) {
        this.controller = controller;
        this.model = model;
        this.shouldBeSystemOut = shouldBeSystemOut;
        this.shouldBeSystemErr = shouldBeSystemErr;
        this.shouldBeSystemIn = shouldBeSystemIn;
        selectedRow = -1;
        keepGoing = true;
        model.initBehaviors(this::printTable, this::printTable, () -> printTable(-1), (r, c) -> System.out.println("Cell contents changed to: " + model.getValueAt(r, c)));
        controller.init(
                fileName,
                model,
                this::getSelectedRow,
                this::setSelectedRow,
                () -> setSelectedRow(-1),
                () -> keepGoing = false,
                (title, message) -> System.err.println(String.format("[%s] %s", title, message))
        );
    }
    public void promptForWhatToDo() {
        Scanner scanner = new Scanner(shouldBeSystemIn);
        while(keepGoing) {
            shouldBeSystemOut.print(getRegexPrefix() + "DictionaryEditorPromptLoop ... What do you want to do: ");
            String command = scanner.hasNextLine() ? scanner.nextLine() : "clickCancel";
            if("clickCancel".equals(command)) {
                controller.cancel();
            } else if("clickAdd".equals(command)) {
                controller.addRow();
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
                if(row < 0 || model.getRowCount() <= row) {
                    shouldBeSystemErr.println("invalid, row is out of range");
                    continue;
                }
                selectedRow = row;
                controller.removeRow();
            } else if(getSortByFirstLabel().equals(command)) {
                controller.sortByFirstColumn();
            } else if(getSortBySecondLabel().equals(command)) {
                controller.sortBySecondColumn();
            } else if("printTable".equals(command)) {
                printTable(-1);
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
                if(model.getValueAt(row, column) == null) {
                    shouldBeSystemErr.println("invalid, either row or column are out of range");
                    continue;
                }
                if(!isValidThirdColumn(column, command)) {
                    continue;
                }
                model.setValueAt(command, row, column);
            } else if("clickSaveToFile".equals(command)) {
                controller.saveToFile();
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
    private void printTable(int index) {
        int rowCount = model.getRowCount();
        shouldBeSystemOut.println("**********************");
        for(int i=0; i<rowCount; i++) {
            shouldBeSystemOut.println(model.getValueAt(i,0) + "\t\t\t" + model.getValueAt(i,1) + getThirdValue(i) + (i == index ? "<<<<<" : ""));
        }
        shouldBeSystemOut.println("**********************" + (rowCount == index ? "<<<<<" : ""));
    }
    private int getSelectedRow() {
        return selectedRow;
    }
    private void setSelectedRow(int value) {
        selectedRow = value;
    }
}

