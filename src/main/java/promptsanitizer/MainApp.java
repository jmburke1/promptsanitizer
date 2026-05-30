/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer;

import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.SanitizerView;

public class MainApp {

    public static void main(String[] args) {
        String pathPrefix = System.getProperty("user.home") + System.getProperty("file.separator");
                new SanitizerView(
                pathPrefix + "personal_dictionary.json",
                        pathPrefix + "personal_regex_dictionary.json",
                new SanitizerController(),
                new SanitizerModel()
        ).createUI();
    }
}
