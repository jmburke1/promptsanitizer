package promptsanitizer;

import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class CenteredCellEditorTest {

    @Test
    void shouldSetTextFieldMarginsWhenEditingStarts() {
        JTable table = new JTable(1, 1);
        CenteredCellEditor editor = new CenteredCellEditor(new JTextField());

        Component component = editor.getTableCellEditorComponent(
                table,
                null,
                true,
                0,
                0
        );

        assertInstanceOf(JTextField.class, component);
        JTextField textField = (JTextField) component;
        assertEquals(5, textField.getMargin().top);
        assertEquals(10, textField.getMargin().left);
        assertEquals(5, textField.getMargin().bottom);
        assertEquals(10, textField.getMargin().right);
    }
}