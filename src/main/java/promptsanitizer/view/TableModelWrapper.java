package promptsanitizer.view;

import javax.swing.table.AbstractTableModel;
import promptsanitizer.model.AbstractDictionaryModel;

public class TableModelWrapper extends AbstractTableModel {
    private AbstractDictionaryModel model;

    public TableModelWrapper(AbstractDictionaryModel model) {
        this.model = model;
        model.initBehaviors(
                i -> this.fireTableRowsInserted(i, i),
                d -> this.fireTableRowsDeleted(d, d),
                this::fireTableDataChanged,
                this::fireTableCellUpdated
        );
    }
    @Override
    public int getRowCount() {
        return model.getRowCount();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return model.getColumnCount();
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return model.getValueAt(i, i1);
    }
    @Override public void setValueAt(Object v, int r, int c) {
        model.setValueAt((String)v, r, c);
    }
    @Override public boolean isCellEditable(int r, int c) { return true; }
    @Override public String getColumnName(int c) {return model.getColumnName(c);}
}