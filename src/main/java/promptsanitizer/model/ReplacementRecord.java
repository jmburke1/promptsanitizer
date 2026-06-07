package promptsanitizer.model;

import org.json.JSONObject;

import java.util.List;

public interface ReplacementRecord {
    String getColumnValue(int c);
    ReplacementRecord createOther(String s, int c);
    int contextCompareToOther(String context, ReplacementRecord other);
    void pushIntoJSONObject(JSONObject result);
    void pushIntoArrayList(String k, JSONObject json, List<ReplacementRecord> replacementValues);
}
