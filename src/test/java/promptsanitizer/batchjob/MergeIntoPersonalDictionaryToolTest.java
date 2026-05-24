package promptsanitizer.batchjob;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MergeIntoPersonalDictionaryToolTest {
    @Test
    void upsertShouldUpdatePersonalDictionaryWhenExists() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpUpsertDict = Files.createTempFile("upsertDict", ".json");
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
                    tmpUpsertDict,
                    "{" +
                            "    \"key2\": {" +
                            "        \"upsertType\":\"UPSERT\"," +
                            "        \"value\":\"value3\"" +
                            "    }," +
                            "    \"key3\": {" +
                            "        \"upsertType\":\"DELETE\"" +
                            "    }," +
                            "    \"key4\": {" +
                            "        \"upsertType\":\"UPSERT\"," +
                            "        \"value\":\"value5\"" +
                            "    }" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpUpsertDict.toString());

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", currentPersonalDictionary.getString("key1"));
            assertEquals("value3", currentPersonalDictionary.getString("key2"));
            assertFalse(currentPersonalDictionary.has("key3"));
            assertEquals("value5", currentPersonalDictionary.getString("key4"));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpUpsertDict);
        }
    }

    @Test
    void upsertShouldCreatePersonalDictionaryWhenNotExists() throws Exception {
        Path tmpUpsertDict = Files.createTempFile("upsertDict", ".json");
        Path tmpPersonalDict = Path.of(tmpUpsertDict.toString().replace("upsertDict", "personalDict"));
        try {
            Files.writeString(
                    tmpUpsertDict,
                    "{" +
                            "    \"key2\": {" +
                            "        \"upsertType\":\"UPSERT\"," +
                            "        \"value\":\"value3\"" +
                            "    }," +
                            "    \"key3\": {" +
                            "        \"upsertType\":\"DELETE\"" +
                            "    }," +
                            "    \"key4\": {" +
                            "        \"upsertType\":\"UPSERT\"," +
                            "        \"value\":\"value5\"" +
                            "    }" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpUpsertDict.toString());

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value3", currentPersonalDictionary.getString("key2"));
            assertFalse(currentPersonalDictionary.has("key3"));
            assertEquals("value5", currentPersonalDictionary.getString("key4"));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpUpsertDict);
        }
    }

    @Test
    void illegalArgumentExceptionWhenUpsertTypeNotRecognized() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpUpsertDict = Files.createTempFile("upsertDict", ".json");
        String caught = null;
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
                    tmpUpsertDict,
                    "{" +
                            "    \"key2\": {" +
                            "        \"upsertType\":\"UPSERT\"," +
                            "        \"value\":\"value3\"" +
                            "    }," +
                            "    \"key3\": {" +
                            "        \"upsertType\":\"DELETE\"" +
                            "    }," +
                            "    \"key4\": {" +
                            "        \"upsertType\":\"SOMETHING_ELSE\"," +
                            "        \"value\":\"value5\"" +
                            "    }" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpUpsertDict.toString());

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", currentPersonalDictionary.getString("key1"));
            assertEquals("value3", currentPersonalDictionary.getString("key2"));
            assertFalse(currentPersonalDictionary.has("key3"));
            assertEquals("value5", currentPersonalDictionary.getString("key4"));
        } catch(IllegalArgumentException iae) {
            caught = iae.getMessage();
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpUpsertDict);
        }
        assertEquals("upsertType should be either UPSERT or DELETE", caught);
    }
}