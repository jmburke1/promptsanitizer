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
        String personalDictionaryFileLocation =
                System.getProperty("user.home") +
                System.getProperty("file.separator") +
                "personal_dictionary.json";
        (new MergeIntoPersonalDictionaryTool(personalDictionaryFileLocation, "upserts.json")).updatePersonalDictionary();
        if(reverseDirection) {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, "sanitized_content.txt", "unsanitized_content.txt", true)).executeUpdate();
        } else {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, "unsanitized_content.txt", "sanitized_content.txt", false)).executeUpdate();
        }
    }
}
