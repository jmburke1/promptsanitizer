package promptsanitizer.view;

import promptsanitizer.controller.DictionaryEditorController;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.AbstractDictionaryModel;
import promptsanitizer.model.SanitizerModel;

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
    public static void initSanitizerController(
            SanitizerController controller,
            SanitizerModel model,
            String fileName,
            String regexFileName) {
        controller.init(model, fileName, regexFileName, (title, message) -> JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE));
    }
}
