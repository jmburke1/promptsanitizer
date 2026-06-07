/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer;

import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.SanitizerView;
import promptsanitizer.view.SanitizerPromptLoop;
import java.awt.HeadlessException;

public class MainApp {

    public static void main(String[] args) {
        String pathPrefix = System.getProperty("user.home") + System.getProperty("file.separator");
        try {
            new SanitizerView(
                    pathPrefix + "personal_dictionary.json",
                    pathPrefix + "personal_regex_dictionary.json",
                    new SanitizerController(),
                    new SanitizerModel()
            ).createUI();
        } catch (HeadlessException he) {
            new SanitizerPromptLoop(
                    pathPrefix + "personal_dictionary.json",
                    pathPrefix + "personal_regex_dictionary.json",
                    new SanitizerController(),
                    new SanitizerModel(),
                    System.out,
                    System.err,
                    System.in
            ).promptForWhatToDo();
        }
    }
}
