package promptsanitizer;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import java.awt.Component;
import java.awt.Insets;

class CenteredCellEditor extends DefaultCellEditor {
    CenteredCellEditor(JTextField tf) {
        super(tf);
    }

    @Override
    public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int col) {
        JTextField tf = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, col);

        tf.setMargin(new Insets(5, 10, 5, 10));

        return tf;
    }
}

