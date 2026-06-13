/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.namespaces.NameSpaceResolver;
import promptsanitizer.view.SanitizerView;
import promptsanitizer.view.SanitizerJLinePromptLoop;
import java.awt.HeadlessException;
import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {
        NameSpaceResolver nameSpaceResolver = new NameSpaceResolver(System.getProperty("user.home"));
        String pathPrefix = nameSpaceResolver.resolveNameSpace(System.getenv("CURRENT_PROMPTSANITIZER_NAMESPACE")) + System.getProperty("file.separator");
        try {
            new SanitizerView(
                    pathPrefix + "personal_dictionary.json",
                    pathPrefix + "personal_regex_dictionary.json",
                    new SanitizerController(),
                    new SanitizerModel()
            ).createUI();
        } catch (HeadlessException he) {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            new SanitizerJLinePromptLoop(
                    pathPrefix + "personal_dictionary.json",
                    pathPrefix + "personal_regex_dictionary.json",
                    new SanitizerController(),
                    new SanitizerModel(),
                    terminal
            ).promptForWhatToDo();
        }
    }
}
