package promptsanitizer.view;

import org.jline.console.Printer;
import org.jline.console.impl.DefaultPrinter;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.AbstractDictionaryModel;

import java.util.*;

public class DictionaryEditorJLinePromptLoop {
    private boolean keepGoing;
    private final DictionaryEditorController controller;
    protected AbstractDictionaryModel model;
    private int selectedRow;
    private final Terminal terminal;
    public DictionaryEditorJLinePromptLoop(
            String fileName,
            DictionaryEditorController controller,
            AbstractDictionaryModel model,
            Terminal terminal
    ) {
        this.controller = controller;
        this.model = model;
        this.terminal = terminal;
        selectedRow = -1;
        keepGoing = true;
        model.initBehaviors(this::printTable, this::printTable, () -> printTable(-1), (r, c) -> terminal.writer().println("Cell contents changed to: " + model.getValueAt(r, c)));
        controller.init(
                fileName,
                model,
                this::getSelectedRow,
                this::setSelectedRow,
                () -> setSelectedRow(-1),
                () -> keepGoing = false,
                (title, message) -> terminal.writer().println(String.format("[%s] %s", title, message))
        );
    }
    public void promptForWhatToDo() {

        Completer commandCompleter = new StringsCompleter(
                "clickCancel",
                "clickAdd",
                "clickRemove",
                getSortByFirstLabel(),
                getSortBySecondLabel(),
                "printTable",
                "editCellContents",
                "clickSaveToFile"
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
                String command = reader.readLine(getRegexPrefix() + "DictionaryEditorPromptLoop ... What do you want to do: ").trim();
                if("clickCancel".equals(command)) {
                    controller.cancel();
                } else if("clickAdd".equals(command)) {
                    controller.addRow();
                } else if("clickRemove".equals(command)) {
                    terminal.writer().println("Enter row number (counting from zero'th row):");
                    command = reader.readLine().trim();
                    int row;
                    try {
                        row = Integer.parseInt(command);
                    } catch(NumberFormatException nfe) {
                        terminal.writer().println("invalid, row doesn't parse as integer");
                        continue;
                    }
                    if(row < 0 || model.getRowCount() <= row) {
                        terminal.writer().println("invalid, row is out of range");
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
                    terminal.writer().println("Enter row number (counting from zero'th row):");
                    command = reader.readLine().trim();
                    int row;
                    try {
                        row = Integer.parseInt(command);
                    } catch(NumberFormatException nfe) {
                        terminal.writer().println("invalid, row doesn't parse as integer");
                        continue;
                    }
                    int column;
                    terminal.writer().println("Enter column number (counting from zero'th column):");
                    command = reader.readLine().trim();
                    try {
                        column = Integer.parseInt(command);
                    } catch(NumberFormatException nfe) {
                        terminal.writer().println("invalid, column doesn't parse as integer");
                        continue;
                    }
                    terminal.writer().println("Enter new value:");
                    command = reader.readLine().trim();
                    if(model.isOutOfRange(row, column)) {
                        terminal.writer().println("invalid, either row or column are out of range");
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
                    terminal.writer().println("Unknown command.  Type 'help' for a list of commands.");
                }
                terminal.writer().flush();
            } catch (UserInterruptException | EndOfFileException e) {
                keepGoing = false;
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
        terminal.writer().println("You are in the dictionary editor prompt loop.  Choices are:");
        terminal.writer().println("  clickCancel               - Close the dictionary editor and return to main loop");
        terminal.writer().println("  clickAdd                    - Add a new empty row to the dictionary");
        terminal.writer().println("  clickRemove                 - Remove the selected row (prompts for row number)");
        terminal.writer().println("  clickSortBySensitive        - Sort rows by the sensitive (left) column");
        terminal.writer().println("  clickSortBySafe             - Sort rows by the safe (right) column");
        terminal.writer().println("  printTable                  - Print the current dictionary table");
        terminal.writer().println("  editCellContents            - Edit a cell (prompts for row, column, and new value)");
        terminal.writer().println("  clickSaveToFile             - Save the dictionary to file and closes the dictionary editor to return to main loop");
    }
    boolean isValidThirdColumn(int column, String command) {
        return true;
    }
    private void printTable(int index) {
        Printer printer = new TerminalPrinter(terminal);
        List<Map<String, String>> rows = new ArrayList<>();

        int rowCount = model.getRowCount();
        terminal.writer().println("**********************");
        for(int i=0; i<rowCount; i++) {
            rows.add(row(model.getValueAt(i,0), model.getValueAt(i,1), getThirdValue(i), (i == index ? "<<<<<" : "")));
        }
        Map<String, Object> options = new HashMap<>();
        options.put(Printer.COLUMNS, columnPrintOrder());
        printer.println(options, rows);
        terminal.writer().println("**********************" + (rowCount == index ? "<<<<<" : ""));
    }
    private Map<String, String> row(
            String zerothColumn,
            String firstColumn,
            String secondColumnIfApplicable,
            String thirdOrFourthColumn
    ) {
        Map<String, String> row = new HashMap<>();
        row.put(model.getColumnName(0), zerothColumn);
        row.put(model.getColumnName(1), firstColumn);
        if(!secondColumnIfApplicable.isEmpty()) {
            row.put(model.getColumnName(2), secondColumnIfApplicable);
        }
        row.put("", thirdOrFourthColumn);
        return row;
    }

    private List<String> columnPrintOrder() {
        List<String> options = new ArrayList<>();
        options.add(model.getColumnName(0));
        options.add(model.getColumnName(1));
        if(model.getColumnCount() > 2) {
            options.add(model.getColumnName(2));
        }
        options.add("");
        return options;
    }

    private int getSelectedRow() {
        return selectedRow;
    }
    private void setSelectedRow(int value) {
        selectedRow = value;
    }
    /**
     * DefaultPrinter expects to know what Terminal it is printing to.
     * JLine's own example does this by subclassing DefaultPrinter
     * and overriding terminal().
     */
    private static class TerminalPrinter extends DefaultPrinter {
        private final Terminal terminal;

        TerminalPrinter(Terminal terminal) {
            super(null);
            this.terminal = terminal;
        }

        @Override
        protected Terminal terminal() {
            return terminal;
        }
    }
}

