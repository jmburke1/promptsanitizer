/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

/** Lightweight model backed by a Map<Integer, String>. */
public class DictionaryModel extends AbstractDictionaryModel {
    ReplacementRecord createReplacementRecord() {
        return new SensitiveSafeRecord("", "");
    }
    @Override public int getColumnCount()           { return 2; }
    @Override public String getColumnName(int c)    { return c == 0 ? "Sensitive" : "Safe"; }
}

