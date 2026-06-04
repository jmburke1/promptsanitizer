package promptsanitizer.model;

import org.json.JSONObject;

interface ReplacementRecord {
    String getColumnValue(int c);
    ReplacementRecord createOther(String s, int c);
    int contextCompareToOther(String context, ReplacementRecord other);
    void pushIntoJSONObject(JSONObject result);
}
