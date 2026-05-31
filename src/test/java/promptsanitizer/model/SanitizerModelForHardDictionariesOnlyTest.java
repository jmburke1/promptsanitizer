/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SanitizerModelForHardDictionariesOnlyTest {

    @Test
    void canApplyDictionaryInForwardDirection() throws Exception {
        SanitizerModel model = new SanitizerModel();
        Path tmpFile = Files.createTempFile("dict", ".json");
        Path tmpFileRegex = Files.createTempFile("dict_regex", ".json");
        Files.delete(tmpFileRegex);
        try {
            Files.writeString(tmpFile, "{\"hello\": \"world\", \"foo\": \"bar\"}");
            model.init(tmpFile.toString(), tmpFileRegex.toString());
            model.loadDictionary();
            String beforeSubstitutions = "hello foo graft1 drool foo dust hello graft2";
            String expectedAfterSubstitutions = "world bar graft1 drool bar dust world graft2";

            String actualAfterSubstitutions = model.applyDictionary(beforeSubstitutions, false);

            assertEquals(expectedAfterSubstitutions, actualAfterSubstitutions);
            assertTrue(model.isValidDictionary());
            assertTrue(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
        }
        assertFalse(model.isValidDictionary());
        assertFalse(model.isStronglyValidDictionary());
    }

    @Test
    void canApplyDictionaryInReverseDirection() throws Exception {
        SanitizerModel model = new SanitizerModel();
        Path tmpFile = Files.createTempFile("dict", ".json");
        Path tmpFileRegex = Files.createTempFile("dict_regex", ".json");
        Files.delete(tmpFileRegex);
        try {
            Files.writeString(tmpFile, "{\"hello\": \"world\", \"foo\": \"bar\"}");
            model.init(tmpFile.toString(), tmpFileRegex.toString());
            model.loadDictionary();
            String beforeSubstitutions = "world bar graft1 drool bar dust world graft2";
            String expectedAfterSubstitutions = "hello foo graft1 drool foo dust hello graft2";

            String actualAfterSubstitutions = model.applyDictionary(beforeSubstitutions, true);

            assertEquals(expectedAfterSubstitutions, actualAfterSubstitutions);
            assertTrue(model.isValidDictionary());
            assertTrue(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
        }
        assertFalse(model.isValidDictionary());
        assertFalse(model.isStronglyValidDictionary());
    }

    @Test
    void dictionaryCanBeValidWithoutStronglyValid() throws Exception {
        SanitizerModel model = new SanitizerModel();

        Path tmpFile = Files.createTempFile("dict", ".json");
        Path tmpFileRegex = Files.createTempFile("dict_regex", ".json");
        try {
            Files.writeString(tmpFile, "{}");
            model.init(tmpFile.toString(), tmpFileRegex.toString());

            model.loadDictionary();

            assertTrue(model.isValidDictionary());
            assertFalse(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
            Files.delete(tmpFileRegex);
        }
    }

    @Test
    void loadingDictionaryFromNonExistentFileResultsInInvalidDictionary() {
        SanitizerModel model = new SanitizerModel();

        try {
            model.init("/tmp/nonexistent/file.json", "/tmp/nonexistent/file_regex.json");

            model.loadDictionary();

            assertFalse(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
        }
    }

    @Test
    void loadingDictionaryCausingExceptionResultsInInvalidDictionary() throws Exception {
        SanitizerModel model = new SanitizerModel();
        Path tmpFile = Files.createTempFile("dict", ".json");
        Path tmpFileRegex = Files.createTempFile("dict_regex", ".json");
        Files.delete(tmpFileRegex);
        try {
            Files.writeString(tmpFile, "This is not valid JSON syntax.");
            model.init(tmpFile.toString(), tmpFileRegex.toString());

            model.loadDictionary();

            assertTrue(model.isValidDictionary());
            assertFalse(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
        }
    }
}
