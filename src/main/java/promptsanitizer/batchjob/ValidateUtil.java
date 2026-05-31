package promptsanitizer.batchjob;

import org.json.JSONObject;

class ValidateUtil {
    static void validateRegexPersonalDictEntry(JSONObject validateThisFirst) {
        if(!validateThisFirst.has("repl")) {
            throw new IllegalArgumentException("Personal regex dictionary entry must have \"repl\" key");
        }
        Object replValue = validateThisFirst.get("repl");
        if(!(replValue instanceof String)) {
            throw new IllegalArgumentException("Personal regex dictionary entry's \"repl\" entry must be a string");
        }
        if(!validateThisFirst.has("dir")) {
            throw new IllegalArgumentException("Personal regex dictionary must have \"dir\" key");
        }
        Object dirValue = validateThisFirst.get("dir");
        if(!(dirValue instanceof String dir)) {
            throw new IllegalArgumentException("Personal regex dictionary entry's \"dir\" entry must be a string");
        }
        if(!">".equals(dir) && !"<".equals(dir)) {
            throw new IllegalArgumentException("Direction must be either > or <");
        }
    }
}
