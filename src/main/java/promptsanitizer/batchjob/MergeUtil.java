package promptsanitizer.batchjob;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class MergeUtil {
    static boolean removeIfHas(JSONObject checkIfHasKey, String key) {
        if(checkIfHasKey.has(key)) {
            checkIfHasKey.remove(key);
            return true;
        }
        return false;
    }
    static boolean putIfNotHasOrDifferent(JSONObject checkIfHasKey, String key, Object putThisThere) {
        if(checkIfHasKey.has(key)) {
            Object alreadyThere = checkIfHasKey.get(key);
            if(aEqualsBForMergePurposes(putThisThere, alreadyThere)) {
                return false;
            }
        }
        checkIfHasKey.put(key, putThisThere);
        return true;
    }
    static void guardedWriteFile(boolean wroteAtLeastOneThingToJSONObject, Path updatePath, JSONObject jsonObjectInQuestion) throws IOException {
        if(wroteAtLeastOneThingToJSONObject) {
            Files.writeString(updatePath, jsonObjectInQuestion.toString(2));   // pretty-print with 2-space indent
        }
    }
    static boolean aEqualsBForMergePurposes(Object putThisThere, Object alreadyThere) {
        if(putThisThere.equals(alreadyThere)) {
            return true;
        }
        if((putThisThere instanceof JSONObject putThisThereJO) && (alreadyThere instanceof JSONObject alreadyThereJO)) {
            ValidateUtil.validateRegexPersonalDictEntry(putThisThereJO);
            ValidateUtil.validateRegexPersonalDictEntry(alreadyThereJO);
            return putThisThereJO.getString("repl").equals(alreadyThereJO.getString("repl")) && putThisThereJO.getString("dir").equals(alreadyThereJO.getString("dir"));
        }
        return false;
    }
}
