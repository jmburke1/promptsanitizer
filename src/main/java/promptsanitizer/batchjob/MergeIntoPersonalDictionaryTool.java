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
            if(thingToMergeIntoPersonalDictionary.isNull(k)) {
                currentPersonalDictionary.remove(k);
            } else {
                currentPersonalDictionary.put(k, thingToMergeIntoPersonalDictionary.getString(k));
            }
        }
        Files.writeString(writeToThisWhenDone, currentPersonalDictionary.toString(2));   // pretty-print with 2-space indent
    }
}
