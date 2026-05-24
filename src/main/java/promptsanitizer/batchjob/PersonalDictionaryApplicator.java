package promptsanitizer.batchjob;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class PersonalDictionaryApplicator {
    private final String personalDictionaryFileLocation;
    private final String sourceFileLocation;
    private final String sinkFileLocation;
    private final boolean reverseDirection;

    public PersonalDictionaryApplicator(
            String personalDictionaryFileLocation,
            String sourceFileLocation,
            String sinkFileLocation,
            boolean reverseDirection) throws IOException {
        this.personalDictionaryFileLocation = personalDictionaryFileLocation;
        this.sourceFileLocation = sourceFileLocation;
        this.sinkFileLocation = sinkFileLocation;
        this.reverseDirection = reverseDirection;
    }

    public void executeUpdate() throws IOException {
        File f = new File(personalDictionaryFileLocation);
        if(!f.exists()) {
            throw new IllegalStateException("You don't have a dictionary defined.");
        }
        JSONObject currentPersonalDictionary = new JSONObject(Files.readString(Path.of(personalDictionaryFileLocation)));
        f = new File(sourceFileLocation);
        if(!f.exists()) {
            System.out.println("No work piece to sanitize.  Returning.");
            return;
        }
        String fileContent = Files.readString(Path.of(sourceFileLocation));
        Iterator<String> personalDictionaryIterator = currentPersonalDictionary.keys();
        while(personalDictionaryIterator.hasNext()) {
            String key = personalDictionaryIterator.next();
            if(reverseDirection) {
                fileContent = fileContent.replace(currentPersonalDictionary.getString(key), key);
            } else {
                fileContent = fileContent.replace(key, currentPersonalDictionary.getString(key));
            }
        }
        Files.writeString(Path.of(sinkFileLocation), fileContent);
    }
}
