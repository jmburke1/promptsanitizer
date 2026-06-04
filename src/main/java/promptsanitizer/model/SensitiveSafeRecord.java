/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.List;

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

    public int contextCompareToOther(String context, ReplacementRecord other) {
        if(!(other instanceof SensitiveSafeRecord otherSS)) {
            throw new IllegalArgumentException("Must be comparing to a SensitiveSafeRecord");
        }
        if ("FIRST_COLUMN".equalsIgnoreCase(context)) {
            return sensitive.compareTo(otherSS.sensitive);
        } else if ("SECOND_COLUMN".equalsIgnoreCase(context)) {
            return safe.compareTo(otherSS.safe);
        } else {
            throw new IllegalArgumentException("Invalid context: " + context);
        }
    }

    public void pushIntoJSONObject(JSONObject result) {
        if (!sensitive.isEmpty() || safe.isEmpty()) result.put(sensitive, safe);  // otherwise, skip blank rows
    }

    public void pushIntoArrayList(String k, JSONObject json, List<ReplacementRecord> replacementValues) {
        sensitive = k;
        safe = json.getString(k);
        replacementValues.add(this);
    }
}
