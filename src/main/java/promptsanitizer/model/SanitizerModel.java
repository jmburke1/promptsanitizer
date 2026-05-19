package promptsanitizer.model;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SanitizerModel {
    public void init(String fileName) {
        this.fileName = fileName;
    }
    private Map<String, String> dictionary;
    private String fileName;

    public boolean isValidDictionary() {
        return dictionary != null;
    }
    public boolean isStronglyValidDictionary() {
        return dictionary != null && !dictionary.isEmpty();
    }
    public void invalidateDictionary() {
        dictionary = null;
    }
    /** Load the personal dictionary from disk. Returns an empty map if the file doesn't exist. */
    public void loadDictionary() {
        File f = new File(fileName);
        if (!f.exists()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of(fileName)));
            dictionary = new HashMap<>();
            for (String k : json.keySet()) {
                dictionary.put(k, json.getString(k));
            }
        } catch (Exception ex) {
            dictionary = Map.of();
        }
    }

    /** Apply all replacements from the dictionary, in the appropriate direction, to the given text. */
    public String applyDictionary(String text, boolean isReverseDirection) {
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            if(isReverseDirection) {
                text = text.replace(entry.getValue(), entry.getKey());
            } else {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }
}
