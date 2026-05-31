package promptsanitizer.batchjob;

import java.io.IOException;

public class MainBatchJobApp {
    public static void main(String[] args) throws IOException {
        boolean reverseDirection;
        if("forward".equals(args[0])) {
            reverseDirection = false;
        } else if("reverse".equals(args[0])) {
            reverseDirection = true;
        } else {
            throw new IllegalArgumentException("Must specify forward or reverse");
        }
        String personalDictionaryFileLocationPrefix =
                System.getProperty("user.home") +
                System.getProperty("file.separator");
        String personalDictionaryFileLocation =
                personalDictionaryFileLocationPrefix +
                        "personal_dictionary.json";
        String regexPersonalDictionaryFileLocation =
                personalDictionaryFileLocationPrefix +
                        "personal_regex_dictionary.json";
        (new MergeIntoPersonalDictionaryTool(personalDictionaryFileLocation, regexPersonalDictionaryFileLocation, "upserts.json")).updatePersonalDictionary();
        if(reverseDirection) {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, regexPersonalDictionaryFileLocation, "sanitized_content.txt", "unsanitized_content.txt", true)).executeUpdate();
        } else {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, regexPersonalDictionaryFileLocation, "unsanitized_content.txt", "sanitized_content.txt", false)).executeUpdate();
        }
    }
}
