/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SanitizerModelTest {

    @Test
    void canApplyDictionaryInForwardDirection() throws Exception {
        SanitizerModel model = new SanitizerModel();
        Path tmpFile = Files.createTempFile("dict", ".json");
        try {
            Files.writeString(tmpFile, "{\"hello\": \"world\", \"foo\": \"bar\"}");
            model.init(tmpFile.toString());
            model.loadDictionary();
            String beforeSubstitutions = "hello foo clay drool foo dust hello graft";
            String expectedAfterSubstitutions = "world bar clay drool bar dust world graft";

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
        try {
            Files.writeString(tmpFile, "{\"hello\": \"world\", \"foo\": \"bar\"}");
            model.init(tmpFile.toString());
            model.loadDictionary();
            String beforeSubstitutions = "world bar clay drool bar dust world graft";
            String expectedAfterSubstitutions = "hello foo clay drool foo dust hello graft";

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
        try {
            Files.writeString(tmpFile, "{}");
            model.init(tmpFile.toString());

            model.loadDictionary();

            assertTrue(model.isValidDictionary());
            assertFalse(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
        }
    }

    @Test
    void loadingDictionaryFromNonExistentFileResultsInInvalidDictionary() throws Exception {
        SanitizerModel model = new SanitizerModel();

        try {
            model.init("/tmp/nonexistent/file.json");

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
        try {
            Files.writeString(tmpFile, "This is not valid JSON syntax.");
            model.init(tmpFile.toString());

            model.loadDictionary();

            assertTrue(model.isValidDictionary());
            assertFalse(model.isStronglyValidDictionary());
        } finally {
            model.invalidateDictionary();
            Files.delete(tmpFile);
        }
    }
}
