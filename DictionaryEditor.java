import java.awt.BorderLayout;
import java.awt.Dimension;
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

import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

// compile with: javac -cp json-20250107.jar DictionaryEditor.java
// run with:    java -cp json-20250107.jar:. DictionaryEditor
public class DictionaryEditor {

    private static final String FILE_NAME = "personal_dictionary.json";
    private static final String[] COLUMN_NAMES = {"Key", "Value"};

    /** Lightweight model backed by a Map<Integer, String>. */
    private static class DictionaryModel extends javax.swing.table.AbstractTableModel {
        private final List<String> keys   = new ArrayList<>();
        private final List<String> values = new ArrayList<>();

        @Override public int getRowCount()              { return keys.size(); }
        @Override public int getColumnCount()           { return 2; }
        @Override public String getColumnName(int c)    { return COLUMN_NAMES[c]; }

        @Override public Object getValueAt(int r, int c) {
            if (c == 0) return keys.get(r);
            if (c == 1) return values.get(r);
            return null;
        }

        @Override public void setValueAt(Object v, int r, int c) {
            String s = (v == null) ? "" : v.toString();
            if (c == 0) keys.set(r, s);
            else        values.set(r, s);
            fireTableCellUpdated(r, c);
        }

        @Override public boolean isCellEditable(int r, int c) { return true; }

        /** Add a blank row and return its row index. */
        public int addRow() {
            keys.add("");
            values.add("");
            fireTableRowsInserted(keys.size() - 1, keys.size() - 1);
            return keys.size() - 1;
        }

        /** Remove the given row index (shifts subsequent entries). */
        public void removeRow(int r) {
            keys.remove(r);
            values.remove(r);

            fireTableDataChanged();
        }

        /** Move row up one position. Returns false if already at top. */
        public boolean moveRowUp(int r) {
            /*if (r <= 0) return false;
            swap(r - 1);*/
            return true;
        }

        /** Move row down one position. Returns false if already at bottom. */
        public boolean moveRowDown(int r) {
            /*if (r >= getRowCount() - 1) return false;
            swap(r);*/
            return true;
        }

        private void swap(int a) {
            /*int b = a + 1;

            String kA   = keys.remove(a);
            String kB   = keys.remove(b);
            String vA   = values.remove(a);
            String vB   = values.remove(b);

            keys.put(a, kB);
            keys.put(b, kA);
            values.put(a, vB);
            values.put(b, vA);

            fireTableRowsUpdated(Math.min(a, b), Math.max(a, b));*/
        }

        /** Load all entries from the JSON file into this model. */
        public void load(JSONObject json) {
            keys.clear();
            values.clear();
            for (String k : json.keySet()) {
                keys.add(k);
                values.add(json.getString(k));
            }
            fireTableDataChanged();
        }

        /** Serialize this model back into a JSONObject. */
        public JSONObject toJSON() {
            JSONObject result = new JSONObject();
            for (int i = 0; i < keys.size() ; i++) {
                String k = keys.get(i);
                String v = values.get(i);
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
    private final JButton   upBtn   = new JButton("▲ Up");
    private final JButton   dnBtn   = new JButton("▼ Down");
    private final JButton   saveBtn = new JButton("Save to File");

    public static void main(String[] args) {
        new DictionaryEditor().createUI();
    }

    private void createUI() {
        JFrame frame = new JFrame("Dictionary Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Table with bigger font and headers
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumnModel tcm = table.getColumnModel();
        if (tcm.getColumnCount() > 0) {
            tcm.getColumn(0).setPreferredWidth(180); // Key column wider
            tcm.getColumn(1).setPreferredWidth(320);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Button bar at the bottom
        JPanel buttonBar = new JPanel();
        buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
        buttonBar.setAlignmentX(0.0f);

        addBtn.setFont(BUTTON_FONT);
        rmBtn.setFont(BUTTON_FONT);
        upBtn.setFont(BUTTON_FONT);
        dnBtn.setFont(BUTTON_FONT);
        saveBtn.setFont(BUTTON_FONT);

        addBtn.setMaximumSize(addBtn.getPreferredSize());
        rmBtn.setMaximumSize(rmBtn.getPreferredSize());
        upBtn.setMaximumSize(upBtn.getPreferredSize());
        dnBtn.setMaximumSize(dnBtn.getPreferredSize());
        saveBtn.setMaximumSize(saveBtn.getPreferredSize());

        buttonBar.add(Box.createHorizontalStrut(6));
        buttonBar.add(addBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(rmBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(upBtn);
        buttonBar.add(Box.createHorizontalStrut(4));
        buttonBar.add(dnBtn);
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

        upBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r > 0 && model.moveRowUp(r)) {
                table.setRowSelectionInterval(r - 1, r - 1);
            }
        });

        dnBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0 && r < model.getRowCount() - 1 && model.moveRowDown(r)) {
                table.setRowSelectionInterval(r + 1, r + 1);
            }
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
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                "Could not save to " + FILE_NAME + ":\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
