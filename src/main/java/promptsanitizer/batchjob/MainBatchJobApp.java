package promptsanitizer.batchjob;

import java.io.IOException;

public class MainBatchJobApp {
    public static void main(String[] args) throws IOException {
        boolean reverseDirection;
        if("forward".equals(args[2])) {
            reverseDirection = false;
        } else if("reverse".equals(args[2])) {
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
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, args[1], args[0], true)).executeUpdate();
        } else {
            (new PersonalDictionaryApplicator(personalDictionaryFileLocation, args[0], args[1], false)).executeUpdate();
        }
    }
}
