package promptsanitizer.batchjob;

import java.io.IOException;
import java.util.Map;

public class MainBatchJobApp {
    public static void main(String[] args) throws IOException {
        Map<String, String> parsed = ArgsParse.parseArgs(args);
        boolean reverseDirection;
        if("forward".equals(parsed.get("direction"))) {
            reverseDirection = false;
        } else if("reverse".equals(parsed.get("direction"))) {
            reverseDirection = true;
        } else {
            throw new IllegalArgumentException("Must specify forward or reverse");
        }
        String personalDictionaryFileLocation =
                System.getProperty("user.home") +
                System.getProperty("file.separator") +
                "personal_dictionary.json";
        (new MergeIntoPersonalDictionaryTool(personalDictionaryFileLocation, "upserts.json")).updatePersonalDictionary();
        if(reverseDirection) {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, parsed.get("safe-file-loc"), parsed.get("sensitive-file-loc"), true)).executeUpdate();
        } else {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, parsed.get("sensitive-file-loc"), parsed.get("safe-file-loc"), false)).executeUpdate();
        }
    }
}
