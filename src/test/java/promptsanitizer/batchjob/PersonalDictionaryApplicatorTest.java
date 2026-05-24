package promptsanitizer.batchjob;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonalDictionaryApplicatorTest {
    @Test
    void shouldSanitizePrompt() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpContentToSanitize = Files.createTempFile("contentToSanitize", ".txt");
        Path tmpContentPostSanitize = Files.createTempFile("contentPostSanitize", ".txt");
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"key1\": \"value1\"," +
                            "    \"key2\": \"value2\"," +
                            "    \"key3\": \"value4\"," +
                            "}"
            );
            Files.writeString(tmpContentToSanitize, "The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), false);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is value2, followed by value1 followed by value4 followed by another application of value1", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }

    @Test
    void shouldSanitizePromptReverseDirection() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpContentToSanitize = Files.createTempFile("contentToSanitize", ".txt");
        Path tmpContentPostSanitize = Files.createTempFile("contentPostSanitize", ".txt");
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"key1\": \"value1\"," +
                            "    \"key2\": \"value2\"," +
                            "    \"key3\": \"value4\"," +
                            "}"
            );
            Files.writeString(tmpContentToSanitize, "The key to understanding is value2, followed by value1 followed by value4 followed by another application of value1");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), true);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }

    @Test
    void shouldReturnEarlyWhenNothingToSanitize() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpContentToSanitize = Files.createTempFile("contentToSanitize", ".txt");
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"key1\": \"value1\"," +
                            "    \"key2\": \"value2\"," +
                            "    \"key3\": \"value4\"," +
                            "}"
            );
            Files.delete(tmpContentToSanitize);
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpContentToSanitize.toString(), null, false);

            personalDictionaryApplicator.executeUpdate();
        } finally {
            Files.delete(tmpPersonalDict);
        }
    }

    @Test
    void shouldIllegalStateExceptionWhenPersonalDictionaryNotExist() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Files.delete(tmpPersonalDict);
        String caught = null;
        try {
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), null, null, false);

            personalDictionaryApplicator.executeUpdate();
        } catch(IllegalStateException ise) {
            caught = ise.getMessage();
        }
        assertEquals("You don't have a dictionary defined.", caught);
    }
}