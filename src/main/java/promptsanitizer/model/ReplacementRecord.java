package promptsanitizer.model;

interface ReplacementRecord {
    String getColumnValue(int c);
    ReplacementRecord createOther(String s, int c);
    int contextCompareToOther(String context, ReplacementRecord other);
}
