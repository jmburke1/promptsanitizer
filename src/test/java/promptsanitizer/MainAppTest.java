/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;
import promptsanitizer.view.SanitizerView;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MainAppTest {
    @Test
    void testMain() throws IOException {
        try(
                MockedConstruction<SanitizerView> sanitizerMC = Mockito.mockConstruction(
                        SanitizerView.class,
                        (mock, context) -> {
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertInstanceOf(SanitizerController.class, context.arguments().get(2));
                            assertInstanceOf(SanitizerModel.class, context.arguments().get(3));
                        }
                )
        ) {
            String[] testArgs = {"test", "args"};
            MainApp.main(testArgs);
            Mockito.verify(sanitizerMC.constructed().getFirst()).createUI();
        }
    }

}