package promptsanitizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;

import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;
import java.util.Collections;

// compile with: javac -cp json-20250107.jar DictionaryEditor.java
// run with:    java -cp json-20250107.jar:. DictionaryEditor
public class DictionaryEditor {

    private static final String FILE_NAME = "personal_dictionary.json";
    private static final String[] COLUMN_NAMES = {"Sensitive", "Safe"};

    /** Cell editor that centers its text field vertically in the cell. */
    private static class CenteredCellEditor extends javax.swing.DefaultCellEditor {
        public CenteredCellEditor(JTextField tf) {
            super(tf);
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(
                javax.swing.JTable table, Object value, boolean isSelected, int row, int col) {
            java.awt.Component c = super.getTableCellEditorComponent(table, value, isSelected, row, col);
            if (c instanceof javax.swing.JTextField tf) {
                tf.setMargin(new Insets(5, 10, 5, 10));
            }
            return c;
        }
    }

    /** Lightweight model backed by a Map<Integer, String>. */
    private static class DictionaryModel extends javax.swing.table.AbstractTableModel {
        private final List<SensitiveSafeRecord> sensitiveSafes   = new ArrayList<>();

        @Override public int getRowCount()              { return sensitiveSafes.size(); }
        @Override public int getColumnCount()           { return 2; }
        @Override public String getColumnName(int c)    { return COLUMN_NAMES[c]; }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override public Object getValueAt(int r, int c) {
            if (c == 0) return sensitiveSafes.get(r).sensitive();
            if (c == 1) return sensitiveSafes.get(r).safe();
            return null;
        }

        @Override public void setValueAt(Object v, int r, int c) {
            String s = (v == null) ? "" : v.toString();
            SensitiveSafeRecord senSafRec;
            if (c == 0) {
                senSafRec = new SensitiveSafeRecord(s, sensitiveSafes.get(r).safe());
            } else {
                senSafRec = new SensitiveSafeRecord(sensitiveSafes.get(r).sensitive(), s);
            }
            sensitiveSafes.set(r, senSafRec);
            fireTableCellUpdated(r, c);
        }

        @Override public boolean isCellEditable(int r, int c) { return true; }

        /** Add a blank row and return its row index. */
        public int addRow() {
            sensitiveSafes.add(new SensitiveSafeRecord("", ""));
            fireTableRowsInserted(sensitiveSafes.size() - 1, sensitiveSafes.size() - 1);
            return sensitiveSafes.size() - 1;
        }

        /** Remove the given row index (shifts subsequent entries). */
        public void removeRow(int r) {
            sensitiveSafes.remove(r);

            fireTableDataChanged();
        }

        /** Sort the JTable by sensitive values. */
        public void sortBySensitive() {
            Collections.sort(sensitiveSafes, (ss1, ss2) -> ss1.sensitive().compareTo(ss2.sensitive()));
            fireTableDataChanged();
        }

        /** Sort the JTable by safe values. */
        public void sortBySafe() {
            Collections.sort(sensitiveSafes, (ss1, ss2) -> ss1.safe().compareTo(ss2.safe()));
            fireTableDataChanged();
        }

        /** Load all entries from the JSON file into this model. */
        public void load(JSONObject json) {
            sensitiveSafes.clear();
            for (String k : json.keySet()) {
                sensitiveSafes.add(new SensitiveSafeRecord(k, json.getString(k)));
            }
            fireTableDataChanged();
        }

        /** Serialize this model back into a JSONObject. */
        public JSONObject toJSON() {
            JSONObject result = new JSONObject();
            for (int i = 0; i < sensitiveSafes.size() ; i++) {
                String k = sensitiveSafes.get(i).sensitive();
                String v = sensitiveSafes.get(i).safe();
                if (k.isEmpty() && v.isEmpty()) continue; // skip blank rows
                result.put(k, v);
            }
            return result;
        }
    }

    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    private final DictionaryModel model = new DictionaryModel();
    private final JTable    table   = new JTable(model);
    private final JButton   addBtn  = new JButton("Add Row");
    private final JButton   rmBtn   = new JButton("Remove Row");
    private final JButton   sortBySensitiveBtn   = new JButton("Sort By Sensitive Words/Phrases");
    private final JButton   sortBySafeBtn   = new JButton("Sort By Safe Words/Phrases");
    private final JButton   saveBtn = new JButton("Save to File");
    private JFrame          frame;

    public static void main(String[] args) {
        new DictionaryEditor().createUI();
    }

    public void createUI() {
        frame = new JFrame("Edit Your Personal Dictionary of Sensitive Snippets");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the editor vertically so the cursor is visible
        table.setDefaultEditor(String.class, new CenteredCellEditor(new JTextField()));
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumnModel tcm = table.getColumnModel();
        if (tcm.getColumnCount() > 0) {
            tcm.getColumn(0).setPreferredWidth(180); // Key column wider
            tcm.getColumn(1).setPreferredWidth(320);
        }

        // Single-click to start editing a cell
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && table.getSelectedRow() >= 0) {
                    int col = table.columnAtPoint(e.getPoint());
                    int row = table.rowAtPoint(e.getPoint());
                    if (col >= 0 && row >= 0) {
                        table.editCellAt(row, col);
                    }
                }
            }
        });

        // Clicking on empty space in the scroll pane cancels editing and deselects the row.

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });
        frame.add(scrollPane, BorderLayout.CENTER);

        // Button bar at the bottom
        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
        buttonBar.setAlignmentX(0.0f);

        addBtn.setFont(BUTTON_FONT);
        rmBtn.setFont(BUTTON_FONT);
        sortBySensitiveBtn.setFont(BUTTON_FONT);
        sortBySafeBtn.setFont(BUTTON_FONT);
        saveBtn.setFont(BUTTON_FONT);

        addBtn.setMaximumSize(addBtn.getPreferredSize());
        rmBtn.setMaximumSize(rmBtn.getPreferredSize());
        sortBySensitiveBtn.setMaximumSize(sortBySensitiveBtn.getPreferredSize());
        sortBySafeBtn.setMaximumSize(sortBySafeBtn.getPreferredSize());
        saveBtn.setMaximumSize(saveBtn.getPreferredSize());

        buttonBar.add(Box.createHorizontalStrut(6));
        buttonBar.add(addBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(rmBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(sortBySensitiveBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(sortBySafeBtn);
        buttonBar.add(Box.createHorizontalGlue());
        buttonBar.add(saveBtn);
        buttonBar.add(Box.createHorizontalStrut(6));

        frame.add(buttonBar, BorderLayout.SOUTH);

        // --- Actions ---
        addBtn.addActionListener(e -> {
            int idx = model.addRow();
            table.setRowSelectionInterval(idx, idx);
            table.editCellAt(idx, 0);
        });

        rmBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) return;
            model.removeRow(r);
        });

        sortBySensitiveBtn.addActionListener(e -> {
            model.sortBySensitive();
            table.clearSelection();
        });

        sortBySafeBtn.addActionListener(e -> {
            model.sortBySafe();
            table.clearSelection();
        });

        saveBtn.addActionListener(e -> saveToFile());

        // Size & position
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width / 2, screenSize.height / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Load existing data (empty if file doesn't exist or is {})
        loadFromFile();
    }

    /** Read the JSON file and populate the table. */
    private void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(FILE_NAME)));
            model.load(json);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Could not read " + FILE_NAME + ":\n" + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Serialize the table back to JSON and write it to disk. */
    private void saveToFile() {
        Path p = Path.of(FILE_NAME);
        try {
            JSONObject json = model.toJSON();
            Files.writeString(p, json.toString(2));   // pretty-print with 2-space indent
            frame.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                "Could not save to " + FILE_NAME + ":\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
