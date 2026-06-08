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
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertEquals("upserts.json", context.arguments().get(2));
                            assertFalse((boolean)context.arguments().get(3));
                        }
                );
                MockedConstruction<PersonalDictionaryApplicator> personalDictApplMC = Mockito.mockConstruction(
                        PersonalDictionaryApplicator.class,
                        (mock, context) -> {
                            assertEquals(5, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertEquals("unsanitized_content.txt", context.arguments().get(2));
                            assertEquals("sanitized_content.txt", context.arguments().get(3));
                            assertFalse((boolean)context.arguments().get(4));
                        }
                )
        ) {
            String[] testArgs = {"forward"};
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
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertEquals("upserts.json", context.arguments().get(2));
                            assertFalse((boolean)context.arguments().get(3));
                        }
                );
                MockedConstruction<PersonalDictionaryApplicator> personalDictApplMC = Mockito.mockConstruction(
                        PersonalDictionaryApplicator.class,
                        (mock, context) -> {
                            assertEquals(5, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertEquals("sanitized_content.txt", context.arguments().get(2));
                            assertEquals("unsanitized_content.txt", context.arguments().get(3));
                            assertTrue((boolean)context.arguments().get(4));
                        }
                )
        ) {
            String[] testArgs = {"reverse"};
            MainBatchJobApp.main(testArgs);
            Mockito.verify(mgIntoPDectToolMC.constructed().getFirst()).updatePersonalDictionary();
            Mockito.verify(personalDictApplMC.constructed().getFirst()).executeUpdate();
        }
    }
    @Test
    void testMainForOtherDirection() throws IOException {
        boolean caught = false;
        String[] testArgs = {"somethingelse"};
        try {
            MainBatchJobApp.main(testArgs);
        } catch(IllegalArgumentException iae) {
            assertEquals("Must specify forward, reverse or upsertonly", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }
    @Test
    void testMainForUpsertOnly() throws IOException {
        try(
                MockedConstruction<MergeIntoPersonalDictionaryTool> mgIntoPDectToolMC = Mockito.mockConstruction(
                        MergeIntoPersonalDictionaryTool.class,
                        (mock, context) -> {
                            assertEquals(4, context.arguments().size());
                            assertTrue(((String)context.arguments().get(0)).contains("personal_dictionary.json"));
                            assertTrue(((String)context.arguments().get(1)).contains("personal_regex_dictionary.json"));
                            assertEquals("upserts.json", context.arguments().get(2));
                            assertTrue((boolean)context.arguments().get(3));
                        }
                );
                MockedConstruction<PersonalDictionaryApplicator> personalDictApplMC = Mockito.mockConstruction(
                        PersonalDictionaryApplicator.class
                )
        ) {
            String[] testArgs = {"upsertonly"};
            MainBatchJobApp.main(testArgs);
            Mockito.verify(mgIntoPDectToolMC.constructed().getFirst()).updatePersonalDictionary();
            assertEquals(0, personalDictApplMC.constructed().size());
        }
    }
    @Test
    void testMainForNoArgsProvided() throws IOException {
        boolean caught = false;
        String[] testArgs = {};
        try {
            MainBatchJobApp.main(testArgs);
        } catch(IllegalArgumentException iae) {
            assertEquals("The batch job takes one argument where you specify forward, reverse or upsertonly", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }

}