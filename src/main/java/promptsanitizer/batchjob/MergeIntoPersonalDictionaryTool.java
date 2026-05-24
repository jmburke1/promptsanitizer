package promptsanitizer.batchjob;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MergeIntoPersonalDictionaryTool {
    private final String personalDictionaryFileLocation;
    private final String updateFileLocation;
    public MergeIntoPersonalDictionaryTool(String personalDictionaryFileLocation, String updateFileLocation) {
        this.personalDictionaryFileLocation = personalDictionaryFileLocation;
        this.updateFileLocation = updateFileLocation;
    }
    public void updatePersonalDictionary() throws IOException {
        File f = new File(personalDictionaryFileLocation);
        Path writeToThisWhenDone = Path.of(personalDictionaryFileLocation);
        JSONObject currentPersonalDictionary;
        if(f.exists()) {
            currentPersonalDictionary = new JSONObject(Files.readString(writeToThisWhenDone));
        } else {
            currentPersonalDictionary = new JSONObject();
        }
        f = new File(updateFileLocation);
        if(!f.exists()) {
            return;
        }
        JSONObject thingToMergeIntoPersonalDictionary = new JSONObject(Files.readString(Path.of(updateFileLocation)));
        for (String k : thingToMergeIntoPersonalDictionary.keySet()) {
            JSONObject currentObject = thingToMergeIntoPersonalDictionary.getJSONObject(k);
            String upsertType = currentObject.getString("upsertType");
            if("UPSERT".equals(upsertType)) {
                currentPersonalDictionary.put(k, currentObject.getString("value"));
            } else if("DELETE".equals(upsertType)) {
                currentPersonalDictionary.remove(k);
            } else {
                throw new IllegalArgumentException("upsertType should be either UPSERT or DELETE");
            }
        }
        Files.writeString(writeToThisWhenDone, currentPersonalDictionary.toString(2));   // pretty-print with 2-space indent
    }
}
