package promptsanitizer.batchjob;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MergeIntoPersonalDictionaryTool {
    private final String personalDictionaryFileLocation;
    private final String regexPersonalDictionaryFileLocation;
    private final String updateFileLocation;
    public MergeIntoPersonalDictionaryTool(String personalDictionaryFileLocation, String regexPersonalDictionaryFileLocation, String updateFileLocation) {
        this.personalDictionaryFileLocation = personalDictionaryFileLocation;
        this.regexPersonalDictionaryFileLocation = regexPersonalDictionaryFileLocation;
        this.updateFileLocation = updateFileLocation;
    }
    public void updatePersonalDictionary() throws IOException {
        File checkExists = new File(updateFileLocation);
        if(!checkExists.exists()) {
            return;
        }
        checkExists = new File(personalDictionaryFileLocation);
        Path personalDictionaryUpdatePath = Path.of(personalDictionaryFileLocation);
        JSONObject currentPersonalDictionary;
        if(checkExists.exists()) {
            currentPersonalDictionary = new JSONObject(Files.readString(personalDictionaryUpdatePath));
        } else {
            currentPersonalDictionary = new JSONObject();
        }
        checkExists = new File(regexPersonalDictionaryFileLocation);
        Path regexPersonalDictionaryUpdatePath = Path.of(regexPersonalDictionaryFileLocation);
        JSONObject currentRegexPersonalDictionary;
        if(checkExists.exists()) {
            currentRegexPersonalDictionary = new JSONObject(Files.readString(regexPersonalDictionaryUpdatePath));
        } else {
            currentRegexPersonalDictionary = new JSONObject();
        }
        JSONObject thingToMergeIntoPersonalDictionary = new JSONObject(Files.readString(Path.of(updateFileLocation)));
        boolean wroteAtLeastOneRegex = false;
        boolean wroteAtLeastOneSimpleReplacement = false;
        for (String k : thingToMergeIntoPersonalDictionary.keySet()) {
            if(thingToMergeIntoPersonalDictionary.isNull(k)) {
                wroteAtLeastOneSimpleReplacement |= MergeUtil.removeIfHas(currentPersonalDictionary, k);
                wroteAtLeastOneRegex |= MergeUtil.removeIfHas(currentRegexPersonalDictionary, k);
            } else {
                Object o = thingToMergeIntoPersonalDictionary.get(k);
                if(o instanceof String) {
                    wroteAtLeastOneSimpleReplacement |= MergeUtil.putIfNotHasOrDifferent(currentPersonalDictionary, k, o);
                    wroteAtLeastOneRegex |= MergeUtil.removeIfHas(currentRegexPersonalDictionary, k);
                } else if(o instanceof JSONObject validateThisFirst) {
                    wroteAtLeastOneSimpleReplacement |= MergeUtil.removeIfHas(currentPersonalDictionary, k);
                    wroteAtLeastOneRegex |= MergeUtil.putIfNotHasOrDifferent(currentRegexPersonalDictionary, k, o);
                }
            }
        }
        MergeUtil.guardedWriteFile(wroteAtLeastOneSimpleReplacement, personalDictionaryUpdatePath, currentPersonalDictionary);
        MergeUtil.guardedWriteFile(wroteAtLeastOneRegex, regexPersonalDictionaryUpdatePath, currentRegexPersonalDictionary);
    }
}
