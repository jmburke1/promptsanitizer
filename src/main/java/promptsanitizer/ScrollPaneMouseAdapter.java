package promptsanitizer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;

class ScrollPaneMouseAdapter extends MouseAdapter {
    private final JTable table;

    ScrollPaneMouseAdapter(JTable table) {
        this.table = table;
    }

    @Override public void mousePressed(MouseEvent e) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }
}
