/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import org.json.JSONObject;

import java.util.List;

class RegexReplaceRecord implements ReplacementRecord {
    private String regex;
    private String replacement;
    private String direction;

    RegexReplaceRecord(String regex, String replacement, String direction) {
        this.regex = regex;
        this.replacement = replacement;
        this.direction = direction;
    }

    public String getColumnValue(int c) {
        if (c == 0) return regex;
        if (c == 1) return replacement;
        if (c == 2) return direction;
        return null;
    }

    public RegexReplaceRecord createOther(String s, int c) {
        RegexReplaceRecord other = new RegexReplaceRecord(regex, replacement, direction);
        if (c == 0) {
            other.regex = s;
        } else if (c == 1) {
            other.replacement = s;
        } else {
            if(!"<".equals(s) && !">".equals(s)) {
                return this;
            }
            other.direction = s;
        }
        return other;
    }

    public int contextCompareToOther(String context, ReplacementRecord other) {
        RegexReplaceRecord otherRR = (RegexReplaceRecord)other;
        int directionCompareResult = direction.compareTo(otherRR.direction);
        if(directionCompareResult == 0) {
            if ("FIRST_COLUMN".equalsIgnoreCase(context)) {
                return regex.compareTo(otherRR.regex);
            } else {
                return replacement.compareTo(otherRR.replacement);
            }
        }
        return directionCompareResult;
    }
    public void pushIntoJSONObject(JSONObject result) {
        if (regex.isEmpty()) return;
        JSONObject jo = new JSONObject();
        jo.put("repl", replacement);
        jo.put("dir", direction);
        result.put(regex, jo);
    }

    public void pushIntoArrayList(String k, JSONObject json, List<ReplacementRecord> replacementValues) {
        JSONObject jo = json.getJSONObject(k);
        String dir = jo.getString("dir");
        if(!"<".equals(dir) && !">".equals(dir)) return;
        regex = k;
        replacement = jo.getString("repl");
        direction = dir;
        replacementValues.add(this);
    }
}
