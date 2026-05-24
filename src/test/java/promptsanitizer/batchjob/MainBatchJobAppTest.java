/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.batchjob;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MainBatchJobAppTest {
    @Test
    void testMainForForwardDirection() throws IOException {
        try(
                MockedConstruction<MergeIntoPersonalDictionaryTool> mgIntoPDectToolMC = Mockito.mockConstruction(
                        MergeIntoPersonalDictionaryTool.class,
                        (mock, context) -> {
                            assertEquals(2, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertEquals("upserts.json", context.arguments().get(1));
                        }
                );
                MockedConstruction<PersonalDictionaryApplicator> personalDictApplMC = Mockito.mockConstruction(
                        PersonalDictionaryApplicator.class,
                        (mock, context) -> {
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertEquals("ContainsSecrets_Or_WhatWeKnowItMeans.md", context.arguments().get(1));
                            assertEquals("LLMAnswer_Or_Safe.md", context.arguments().get(2));
                            assertFalse((boolean)context.arguments().get(3));
                        }
                )
        ) {
            String[] testArgs = {"ContainsSecrets_Or_WhatWeKnowItMeans.md", "LLMAnswer_Or_Safe.md", "forward"};
            MainBatchJobApp.main(testArgs);
            Mockito.verify(mgIntoPDectToolMC.constructed().getFirst()).updatePersonalDictionary();
            Mockito.verify(personalDictApplMC.constructed().getFirst()).executeUpdate();
        }
    }

    @Test
    void testMainForReverseDirection() throws IOException {
        try(
                MockedConstruction<MergeIntoPersonalDictionaryTool> mgIntoPDectToolMC = Mockito.mockConstruction(
                        MergeIntoPersonalDictionaryTool.class,
                        (mock, context) -> {
                            assertEquals(2, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertEquals("upserts.json", context.arguments().get(1));
                        }
                );
                MockedConstruction<PersonalDictionaryApplicator> personalDictApplMC = Mockito.mockConstruction(
                        PersonalDictionaryApplicator.class,
                        (mock, context) -> {
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertEquals("LLMAnswer_Or_Safe.md", context.arguments().get(1));
                            assertEquals("ContainsSecrets_Or_WhatWeKnowItMeans.md", context.arguments().get(2));
                            assertTrue((boolean)context.arguments().get(3));
                        }
                )
        ) {
            String[] testArgs = {"ContainsSecrets_Or_WhatWeKnowItMeans.md", "LLMAnswer_Or_Safe.md", "reverse"};
            MainBatchJobApp.main(testArgs);
            Mockito.verify(mgIntoPDectToolMC.constructed().getFirst()).updatePersonalDictionary();
            Mockito.verify(personalDictApplMC.constructed().getFirst()).executeUpdate();
        }
    }
}