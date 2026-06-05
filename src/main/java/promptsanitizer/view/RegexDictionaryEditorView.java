/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.view;

import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.controller.ScrollPaneMouseAdapter;
import promptsanitizer.controller.CenteredCellEditor;
import promptsanitizer.controller.TableMouseAdapter;
import promptsanitizer.model.RegexDictionaryModel;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;

public class RegexDictionaryEditorView {
    public RegexDictionaryEditorView(String fileName, DictionaryEditorController controller, RegexDictionaryModel model) {
        this.fileName = fileName;
        this.controller = controller;
        this.model = model;
        addBtn = new JButton("Add Row");
        rmBtn = new JButton("Remove Row");
        sortBySensitiveBtn = new JButton("Sort By Regex");
        sortBySafeBtn = new JButton("Sort By Replacement");
        saveBtn = new JButton("Save to File");
        cancelBtn = new JButton("Cancel");
        table = new JTable(model);
        frame = new JFrame("Edit Your Personal Dictionary of Regex Snippets");
    }

    private final String fileName;

    private static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    private final RegexDictionaryModel model;
    private final JTable table;
    private final JButton addBtn;
    private final JButton rmBtn;
    private final JButton sortBySensitiveBtn;
    private final JButton sortBySafeBtn;
    private final JButton saveBtn;
    private final JButton cancelBtn;
    private final JFrame frame;
    private final DictionaryEditorController controller;

    public void createUI() {
        controller.init(fileName, model, table, frame);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
            tcm.getColumn(2).setPreferredWidth(250);
        }

        java.util.List<JButton> disableTheseWhenEditingTableCell = new ArrayList<>();
        disableTheseWhenEditingTableCell.add(sortBySafeBtn);
        disableTheseWhenEditingTableCell.add(sortBySensitiveBtn);
        disableTheseWhenEditingTableCell.add(addBtn);
        disableTheseWhenEditingTableCell.add(rmBtn);
        // Single-click to start editing a cell
        table.addMouseListener(new TableMouseAdapter(table, disableTheseWhenEditingTableCell));

        // Clicking on empty space in the scroll pane cancels editing and deselects the row.

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addMouseListener(new ScrollPaneMouseAdapter(table, disableTheseWhenEditingTableCell));
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
        cancelBtn.setFont(BUTTON_FONT);

        addBtn.setMaximumSize(addBtn.getPreferredSize());
        rmBtn.setMaximumSize(rmBtn.getPreferredSize());
        sortBySensitiveBtn.setMaximumSize(sortBySensitiveBtn.getPreferredSize());
        sortBySafeBtn.setMaximumSize(sortBySafeBtn.getPreferredSize());
        saveBtn.setMaximumSize(saveBtn.getPreferredSize());
        cancelBtn.setMaximumSize(cancelBtn.getPreferredSize());

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
        buttonBar.add(cancelBtn);
        buttonBar.add(Box.createHorizontalStrut(6));

        frame.add(buttonBar, BorderLayout.SOUTH);

        // --- Actions ---
        addBtn.addActionListener(e -> controller.addRow());

        rmBtn.addActionListener(e -> controller.removeRow());

        sortBySensitiveBtn.addActionListener(e -> controller.sortByFirstColumn());

        sortBySafeBtn.addActionListener(e -> controller.sortBySecondColumn());

        saveBtn.addActionListener(e -> controller.saveToFile());
        cancelBtn.addActionListener(e -> controller.cancel());

        // Size & position
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width / 2, screenSize.height / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
