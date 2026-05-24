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
                            assertEquals("ContainsSecrets.md", context.arguments().get(1));
                            assertEquals("Safe.md", context.arguments().get(2));
                            assertFalse((boolean)context.arguments().get(3));
                        }
                )
        ) {
            String[] testArgs = {"--sensitive-file-loc", "ContainsSecrets.md", "--safe-file-loc", "Safe.md", "--direction", "forward"};
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
                            assertEquals("LLMAnswer.md", context.arguments().get(1));
                            assertEquals("WhatWeKnowItMeans.md", context.arguments().get(2));
                            assertTrue((boolean)context.arguments().get(3));
                        }
                )
        ) {
            String[] testArgs = {"--sensitive-file-loc", "WhatWeKnowItMeans.md", "--safe-file-loc", "LLMAnswer.md", "--direction", "reverse"};
            MainBatchJobApp.main(testArgs);
            Mockito.verify(mgIntoPDectToolMC.constructed().getFirst()).updatePersonalDictionary();
            Mockito.verify(personalDictApplMC.constructed().getFirst()).executeUpdate();
        }
    }
    @Test
    void testMainForOtherDirection() throws IOException {
        boolean caught = false;
        String[] testArgs = {"--sensitive-file-loc", "someMD.md", "--safe-file-loc", "someOtherMD.md", "--direction", "somethingelse"};
        try {
            MainBatchJobApp.main(testArgs);
        } catch(IllegalArgumentException iae) {
            assertEquals("Must specify forward or reverse", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }
}