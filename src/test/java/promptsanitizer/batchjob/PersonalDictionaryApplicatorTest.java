package promptsanitizer.batchjob;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonalDictionaryApplicatorTest {
    @Test
    void shouldSanitizePrompt() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
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
            Files.writeString(
                    tmpRegexPersonalDict,
                    "{" +
                            "    \"([a-z]*)_grorg_([a-z]*)\": {" +
                            "        \"repl\": \"$1_$2_grarg\"," +
                            "        \"dir\": \">\"" +
                            "    }," +
                            "    \"([a-z]*)_([a-z]*)_grarg\": {" +
                            "        \"repl\": \"$1_grorg_$2\"," +
                            "        \"dir\": \"<\"" +
                            "    }" +
                            "}"
            );
            Files.writeString(tmpContentToSanitize, "The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1.  act_grorg_weq says so and blibb_grorg_wuff has confirmed it.");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), false);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is value2, followed by value1 followed by value4 followed by another application of value1.  act_weq_grarg says so and blibb_wuff_grarg has confirmed it.", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpRegexPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }

    @Test
    void shouldSanitizePromptReverseDirection() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
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
            Files.writeString(
                    tmpRegexPersonalDict,
                    "{" +
                            "    \"([a-z]*)_grorg_([a-z]*)\": {" +
                            "        \"repl\": \"$1_$2_grarg\"," +
                            "        \"dir\": \">\"" +
                            "    }," +
                            "    \"([a-z]*)_([a-z]*)_grarg\": {" +
                            "        \"repl\": \"$1_grorg_$2\"," +
                            "        \"dir\": \"<\"" +
                            "    }" +
                            "}"
            );
            Files.writeString(tmpContentToSanitize, "The key to understanding is value2, followed by value1 followed by value4 followed by another application of value1.  act_weq_grarg says so and blibb_wuff_grarg has confirmed it.");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), true);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1.  act_grorg_weq says so and blibb_grorg_wuff has confirmed it.", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpRegexPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }

    @Test
    void shouldReturnEarlyWhenNothingToSanitize() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
        Path tmpContentToSanitize = Files.createTempFile("contentToSanitize", ".txt");
        Files.delete(tmpContentToSanitize);
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"key1\": \"value1\"," +
                            "    \"key2\": \"value2\"," +
                            "    \"key3\": \"value4\"," +
                            "}"
            );
            Files.writeString(
                    tmpRegexPersonalDict,
                    "{" +
                            "    \"([a-z]*)_([a-z]*)_grarg\": {" +
                            "        \"repl\": \"$1_grorg_$2\"," +
                            "        \"dir\": \"<\"" +
                            "    }" +
                            "}"
            );
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpContentToSanitize.toString(), null, false);

            personalDictionaryApplicator.executeUpdate();
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpRegexPersonalDict);
        }
    }

    @Test
    void shouldIllegalStateExceptionWhenPersonalDictionaryNotExist() throws Exception {
        Path dummyContent = Files.createTempFile("dummyContent", ".txt");
        Files.writeString(dummyContent, "This could be anything.");
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Files.delete(tmpPersonalDict);
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
        Files.delete(tmpRegexPersonalDict);
        String caught = null;
        try {
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), dummyContent.toString(), null, false);

            personalDictionaryApplicator.executeUpdate();
        } catch(IllegalStateException ise) {
            caught = ise.getMessage();
        } finally {
            Files.delete(dummyContent);
        }
        assertEquals("You don't have a dictionary defined.", caught);
    }

    @Test
    void shouldSanitizePromptRegexCaseOnly() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Files.delete(tmpPersonalDict);
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
        Path tmpContentToSanitize = Files.createTempFile("contentToSanitize", ".txt");
        Path tmpContentPostSanitize = Files.createTempFile("contentPostSanitize", ".txt");
        try {
            Files.writeString(
                    tmpRegexPersonalDict,
                    "{" +
                            "    \"([a-z]*)_grorg_([a-z]*)\": {" +
                            "        \"repl\": \"$1_$2_grarg\"," +
                            "        \"dir\": \">\"" +
                            "    }" +
                            "}"
            );
            Files.writeString(tmpContentToSanitize, "The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1.  act_grorg_weq says so and blibb_grorg_wuff has confirmed it.");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), false);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1.  act_weq_grarg says so and blibb_wuff_grarg has confirmed it.", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpRegexPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }

    @Test
    void shouldSanitizePromptStrictStringReplaceOnly() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpRegexPersonalDict = Files.createTempFile("regexPersonalDict", ".json");
        Files.delete(tmpRegexPersonalDict);
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
            Files.writeString(tmpContentToSanitize, "The key to understanding is key2, followed by key1 followed by key3 followed by another application of key1.  act_grorg_weq says so and blibb_grorg_wuff has confirmed it.");
            PersonalDictionaryApplicator personalDictionaryApplicator = new PersonalDictionaryApplicator(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpContentToSanitize.toString(), tmpContentPostSanitize.toString(), false);

            personalDictionaryApplicator.executeUpdate();

            assertEquals("The key to understanding is value2, followed by value1 followed by value4 followed by another application of value1.  act_grorg_weq says so and blibb_grorg_wuff has confirmed it.", Files.readString(tmpContentPostSanitize));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpContentToSanitize);
            Files.delete(tmpContentPostSanitize);
        }
    }
}