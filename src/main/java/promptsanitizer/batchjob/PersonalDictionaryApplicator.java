package promptsanitizer.batchjob;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class PersonalDictionaryApplicator {
    private final String personalDictionaryFileLocation;
    private final String regexPersonalDictionaryFileLocation;
    private final String sourceFileLocation;
    private final String sinkFileLocation;
    private final boolean reverseDirection;

    public PersonalDictionaryApplicator(
            String personalDictionaryFileLocation,
            String regexPersonalDictionaryFileLocation,
            String sourceFileLocation,
            String sinkFileLocation,
            boolean reverseDirection) throws IOException {
        this.personalDictionaryFileLocation = personalDictionaryFileLocation;
        this.regexPersonalDictionaryFileLocation = regexPersonalDictionaryFileLocation;
        this.sourceFileLocation = sourceFileLocation;
        this.sinkFileLocation = sinkFileLocation;
        this.reverseDirection = reverseDirection;
    }

    public void executeUpdate() throws IOException {
        File sourceFile = new File(sourceFileLocation);
        if(!sourceFile.exists()) {
            throw new FileNotFoundException(String.format("There is no %s to read data from and process into its counterpart.", sourceFileLocation));
        }
        File personalDictionaryFile = new File(personalDictionaryFileLocation);
        File regexPersonalDictionaryFile = new File(regexPersonalDictionaryFileLocation);
        JSONObject currentPersonalDictionary;
        if(personalDictionaryFile.exists()) {
            currentPersonalDictionary = new JSONObject(Files.readString(Path.of(personalDictionaryFileLocation)));
        } else {
            currentPersonalDictionary = new JSONObject();
        }
        JSONObject currentRegexPersonalDictionary;
        if(regexPersonalDictionaryFile.exists()) {
            currentRegexPersonalDictionary = new JSONObject(Files.readString(Path.of(regexPersonalDictionaryFileLocation)));
        } else {
            currentRegexPersonalDictionary = new JSONObject();
        }
        if(currentPersonalDictionary.isEmpty() && currentRegexPersonalDictionary.isEmpty()) {
            throw new IllegalStateException("You don't have a dictionary defined.");
        }
        String fileContent = Files.readString(Path.of(sourceFileLocation));
        if(reverseDirection) {
            Iterator<String> regexPersonalDictionaryIterator = currentRegexPersonalDictionary.keys();
            while(regexPersonalDictionaryIterator.hasNext()) {
                String key = regexPersonalDictionaryIterator.next();
                if("<".equals(currentRegexPersonalDictionary.getJSONObject(key).getString("dir"))) {
                    fileContent = fileContent.replaceAll(key, currentRegexPersonalDictionary.getJSONObject(key).getString("repl"));
                }
            }
        }
        Iterator<String> personalDictionaryIterator = currentPersonalDictionary.keys();
        while(personalDictionaryIterator.hasNext()) {
            String key = personalDictionaryIterator.next();
            if(reverseDirection) {
                fileContent = fileContent.replace(currentPersonalDictionary.getString(key), key);
            } else {
                fileContent = fileContent.replace(key, currentPersonalDictionary.getString(key));
            }
        }
        if(!reverseDirection) {
            Iterator<String> regexPersonalDictionaryIterator = currentRegexPersonalDictionary.keys();
            while(regexPersonalDictionaryIterator.hasNext()) {
                String key = regexPersonalDictionaryIterator.next();
                if(">".equals(currentRegexPersonalDictionary.getJSONObject(key).getString("dir"))) {
                    fileContent = fileContent.replaceAll(key, currentRegexPersonalDictionary.getJSONObject(key).getString("repl"));
                }
            }
        }
        Files.writeString(Path.of(sinkFileLocation), fileContent);
    }
}
