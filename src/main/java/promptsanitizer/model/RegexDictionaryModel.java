/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

/** Lightweight model backed by a Map<Integer, String>. */
public class RegexDictionaryModel extends AbstractDictionaryModel {
    protected ReplacementRecord createReplacementRecord() {
        return new RegexReplaceRecord("", "", ">");
    }
    @Override public int getColumnCount()           { return 3; }
    @Override public String getColumnName(int c)    { return c == 0 ? "Regex" : (c == 1 ? "Replacement" : "Direction"); }
}

