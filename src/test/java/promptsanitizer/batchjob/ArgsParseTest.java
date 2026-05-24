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

class ArgsParseTest {
    @Test
    void shouldThrowForNullArgs() {
        boolean caught = false;
        try {
            ArgsParse.parseArgs(null);
        } catch(IllegalArgumentException iae) {
            assertEquals("Please specify options for --direction, --sensitive-file-loc and --safe-file-loc", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    void shouldThrowForArgsLengthWrong() {
        boolean caught = false;
        String[] testArgs = {"a", "b", "c", "d", "e", "f", "g"};
        try {
            ArgsParse.parseArgs(testArgs);
        } catch(IllegalArgumentException iae) {
            assertEquals("Please specify options for --direction, --sensitive-file-loc and --safe-file-loc", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    void shouldThrowForArgsWrongFormat() {
        boolean caught = false;
        String[] testArgs = {"a", "b", "c", "d", "e", "f"};
        try {
            ArgsParse.parseArgs(testArgs);
        } catch(IllegalArgumentException iae) {
            assertEquals("Please specify options for --direction, --sensitive-file-loc and --safe-file-loc", iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }
}