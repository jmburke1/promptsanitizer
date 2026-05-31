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
        Path tmpRegexPersonalDict = Path.of(tmpPersonalDict.toString().replace("personalDict", "regexPersonalDict"));
        Path tmpUpsertDict = Path.of(tmpPersonalDict.toString().replace("personalDict", "upsert"));
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"shouldBeUnchanged\": \"value1\"," +
                            "    \"shouldBeModified\": \"value2\"," +
                            "    \"shouldBeDeleted\": \"value4\"" +
                            "}"
            );
            Files.writeString(
                    tmpUpsertDict,
                    "{" +
                            "    \"shouldBeModified\": \"value3\"," +
                            "    \"shouldBeDeleted\": null," +
                            "    \"shouldBeInserted\": \"value5\"" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpUpsertDict.toString());

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", currentPersonalDictionary.getString("shouldBeUnchanged"));
            assertEquals("value3", currentPersonalDictionary.getString("shouldBeModified"));
            assertFalse(currentPersonalDictionary.has("shouldBeDeleted"));
            assertEquals("value5", currentPersonalDictionary.getString("shouldBeInserted"));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpUpsertDict);
        }
    }

    @Test
    void upsertShouldCreatePersonalDictionaryWhenNotExists() throws Exception {
        Path tmpUpsertDict = Files.createTempFile("upsertDict", ".json");
        Path tmpPersonalDict = Path.of(tmpUpsertDict.toString().replace("upsertDict", "personalDict"));
        Path tmpRegexPersonalDict = Path.of(tmpUpsertDict.toString().replace("upsertDict", "regexPersonalDict"));
        try {
            Files.writeString(
                    tmpUpsertDict,
                    "{" +
                            "    \"key2\": \"value3\"," +
                            "    \"key3\": null," +
                            "    \"key4\": \"value5\"" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpUpsertDict.toString());

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
}