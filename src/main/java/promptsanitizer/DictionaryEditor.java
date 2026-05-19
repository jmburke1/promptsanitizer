package promptsanitizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.JTextField;

import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;
import promptsanitizer.component.CenteredCellEditor;
import promptsanitizer.controller.TableMouseAdapter;

// compile with: javac -cp json-20250107.jar DictionaryEditor.java
// run with:    java -cp json-20250107.jar:. DictionaryEditor
public class DictionaryEditor {
    public DictionaryEditor(String fileName) {
        this.fileName = fileName;
    }

    private final String fileName;

    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    private final DictionaryModel model = new DictionaryModel();
    private final JTable    table   = new JTable(model);
    private final JButton   addBtn  = new JButton("Add Row");
    private final JButton   rmBtn   = new JButton("Remove Row");
    private final JButton   sortBySensitiveBtn   = new JButton("Sort By Sensitive Words/Phrases");
    private final JButton   sortBySafeBtn   = new JButton("Sort By Safe Words/Phrases");
    private final JButton   saveBtn = new JButton("Save to File");
    private JFrame          frame;

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
        table.addMouseListener(new TableMouseAdapter(table));

        // Clicking on empty space in the scroll pane cancels editing and deselects the row.

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addMouseListener(new ScrollPaneMouseAdapter(table));
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
        File f = new File(fileName);
        if (!f.exists()) return;
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(fileName)));
            model.load(json);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Could not read " + fileName + ":\n" + ex.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Serialize the table back to JSON and write it to disk. */
    private void saveToFile() {
        Path p = Path.of(fileName);
        try {
            JSONObject json = model.toJSON();
            Files.writeString(p, json.toString(2));   // pretty-print with 2-space indent
            frame.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                "Could not save to " + fileName + ":\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
