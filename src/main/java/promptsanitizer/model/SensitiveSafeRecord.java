/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

class SensitiveSafeRecord implements ReplacementRecord {
    private String sensitive;
    private String safe;

    SensitiveSafeRecord(String sensitive, String safe) {
        this.sensitive = sensitive;
        this.safe = safe;
    }

    public String getColumnValue(int c) {
        if (c == 0) return sensitive;
        if (c == 1) return safe;
        return null;
    }

    public SensitiveSafeRecord createOther(String s, int c) {
        SensitiveSafeRecord other = new SensitiveSafeRecord(sensitive, safe);
        if (c == 0) {
            other.sensitive = s;
        } else {
            other.safe = s;
        }
        return other;
    }

    @Deprecated
    String sensitive() {
        return sensitive;
    }

    @Deprecated
    String safe() {
        return safe;
    }
}
