package promptsanitizer.controller;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;

public class TableMouseAdapter extends MouseAdapter {
    private final JTable table;
    private final List<JButton> disableThese;

    public TableMouseAdapter(JTable table, List<JButton> disableThese) {
        this.table = table;
        this.disableThese = disableThese;
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && table.getSelectedRow() >= 0) {
            int col = table.columnAtPoint(e.getPoint());
            int row = table.rowAtPoint(e.getPoint());
            if (col >= 0 && row >= 0) {
                table.editCellAt(row, col);
                disableThese.forEach(jBtn -> jBtn.setEnabled(false));
            }
        }
    }
}
