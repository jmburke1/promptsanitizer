package promptsanitizer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;

class TableMouseAdapter extends MouseAdapter {
    private final JTable table;

    TableMouseAdapter(JTable table) {
        this.table = table;
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && table.getSelectedRow() >= 0) {
            int col = table.columnAtPoint(e.getPoint());
            int row = table.rowAtPoint(e.getPoint());
            if (col >= 0 && row >= 0) {
                table.editCellAt(row, col);
            }
        }
    }
}
