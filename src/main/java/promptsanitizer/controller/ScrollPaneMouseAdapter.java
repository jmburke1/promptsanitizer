package promptsanitizer.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

public class ScrollPaneMouseAdapter extends MouseAdapter {
    private final JTable table;
    private final List<JButton> enableThese;

    public ScrollPaneMouseAdapter(JTable table, List<JButton> enableThese) {
        this.table = table;
        this.enableThese = enableThese;
    }

    @Override public void mousePressed(MouseEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        enableThese.forEach(jBtn -> jBtn.setEnabled(true));
    }
}
