package promptsanitizer.view;

import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.model.AbstractDictionaryModel;

import javax.swing.*;

public class ViewSetupUtil {
    public static void initDictionaryEditorControllerWithSwingComponents(
            DictionaryEditorController controller,
            String fileName,
            AbstractDictionaryModel model,
            JTable table,
            JFrame frame
    ) {
        controller.init(
                fileName,
                model,
                table::getSelectedRow,
                idx -> {
                    table.setRowSelectionInterval(idx, idx);
                    table.editCellAt(idx, 0);
                },
                table::clearSelection,
                frame::dispose,
                (title, message) -> JOptionPane.showMessageDialog(null,
                        message,
                        title, JOptionPane.ERROR_MESSAGE)
        );
    }
}
