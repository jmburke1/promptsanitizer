package promptsanitizer;

import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.SanitizerView;

public class MainApp {

    public static void main(String[] args) {
        new SanitizerView(
                System.getProperty("user.home") +
                System.getProperty("file.separator") +
                "personal_dictionary.json",
                new SanitizerController(),
                new SanitizerModel()
        ).createUI();
    }
}
