package promptsanitizer.batchjob;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                            "    \"shouldBeUnchangedBecauseUnlisted\": \"value1\"," +
                            "    \"shouldBeModified\": \"value2\"," +
                            "    \"shouldBeDeleted\": \"value4\"," +
                            "    \"shouldBeUnchangedBecauseExactEqual\": \"value6\"" +
                            "}"
            );
            Files.writeString(
                    tmpUpsertDict,
                    "{" +
                            "    \"shouldBeModified\": \"value3\"," +
                            "    \"shouldBeDeleted\": null," +
                            "    \"shouldBeInserted\": \"value5\"," +
                            "    \"shouldBeUnchangedBecauseExactEqual\": \"value6\"" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpUpsertDict.toString(), false);

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", currentPersonalDictionary.getString("shouldBeUnchangedBecauseUnlisted"));
            assertEquals("value3", currentPersonalDictionary.getString("shouldBeModified"));
            assertFalse(currentPersonalDictionary.has("shouldBeDeleted"));
            assertEquals("value5", currentPersonalDictionary.getString("shouldBeInserted"));
            assertEquals("value6", currentPersonalDictionary.getString("shouldBeUnchangedBecauseExactEqual"));
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
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpUpsertDict.toString(), false);

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
    void upsertShouldUpdateRegexPersonalDictionaryWhenExists() throws Exception {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        Path tmpRegexPersonalDict = Path.of(tmpPersonalDict.toString().replace("personalDict", "regexPersonalDict"));
        Path tmpUpsertDict = Path.of(tmpPersonalDict.toString().replace("personalDict", "upsert"));
        try {
            Files.writeString(
                    tmpPersonalDict,
                    "{" +
                            "    \"shouldBeUnchanged\": \"value1\"," +
                            "    \"shouldBeModified\": \"value2\"," +
                            "    \"shouldBeDeleted\": \"value4\"," +
                            "    \"shouldConvertToARegex\": \"value6\"" +
                            "}"
            );
            Files.writeString(
                    tmpRegexPersonalDict,
                    "{" +
                            "    \"shouldConvertToASimpleReplacement(.*)\": {\"repl\": \"sayWhat$1\", \"dir\": \"<\"}," +
                            "    \"shouldBeModified(.*)\": {\"repl\": \"sayAgain$1\", \"dir\": \">\"}," +
                            "    \"shouldBeDeleted(.*)\": {\"repl\": \"really$1\", \"dir\": \"<\"}," +
                            "    \"shouldBeUnchangedBecauseUnlisted(.*)\": {\"repl\": \"yesReally$1\", \"dir\": \">\"}," +
                            "    \"shouldBeUnchangedBecauseExactlySameInUpserts(.*)\": {\"repl\": \"fourtyTwoIsMagicNumber$1\", \"dir\": \"<\"}" +
                            "}"
            );
            Files.writeString(
                    tmpUpsertDict,
                    "{" +
                            "    \"shouldBeModified\": \"value3\"," +
                            "    \"shouldBeDeleted\": null," +
                            "    \"shouldBeInserted\": \"value5\"," +
                            "    \"shouldConvertToASimpleReplacement(.*)\": \"12345\"," +
                            "    \"shouldBeModified(.*)\": {\"repl\": \"sayAgainAgain$1\", \"dir\": \"<\"}," +
                            "    \"shouldBeDeleted(.*)\": null," +
                            "    \"shouldConvertToARegex\": {\"repl\": \"technicallyARegex\", \"dir\": \">\"}," +
                            "    \"shouldBeInserted(.*)\": {\"repl\": \"aBrandNewRegex$1\", \"dir\": \"<\"}," +
                            "    \"shouldBeUnchangedBecauseExactlySameInUpserts(.*)\": {\"repl\": \"fourtyTwoIsMagicNumber$1\", \"dir\": \"<\"}" +
                            "}"
            );
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool(tmpPersonalDict.toString(), tmpRegexPersonalDict.toString(), tmpUpsertDict.toString(), false);

            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();

            JSONObject currentPersonalDictionary = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", currentPersonalDictionary.getString("shouldBeUnchanged"));
            assertEquals("value3", currentPersonalDictionary.getString("shouldBeModified"));
            assertFalse(currentPersonalDictionary.has("shouldBeDeleted"));
            assertEquals("value5", currentPersonalDictionary.getString("shouldBeInserted"));
            assertEquals("12345", currentPersonalDictionary.getString("shouldConvertToASimpleReplacement(.*)"));
            assertFalse(currentPersonalDictionary.has("shouldConvertToARegex"));

            JSONObject currentRegexPersonalDictionary = new JSONObject(Files.readString(tmpRegexPersonalDict));
            assertEquals("technicallyARegex", currentRegexPersonalDictionary.getJSONObject("shouldConvertToARegex").getString("repl"));
            assertEquals(">", currentRegexPersonalDictionary.getJSONObject("shouldConvertToARegex").getString("dir"));
            assertEquals("sayAgainAgain$1", currentRegexPersonalDictionary.getJSONObject("shouldBeModified(.*)").getString("repl"));
            assertEquals("<", currentRegexPersonalDictionary.getJSONObject("shouldBeModified(.*)").getString("dir"));
            assertEquals("yesReally$1", currentRegexPersonalDictionary.getJSONObject("shouldBeUnchangedBecauseUnlisted(.*)").getString("repl"));
            assertEquals(">", currentRegexPersonalDictionary.getJSONObject("shouldBeUnchangedBecauseUnlisted(.*)").getString("dir"));
            assertFalse(currentRegexPersonalDictionary.has("shouldBeDeleted(.*)"));
            assertFalse(currentRegexPersonalDictionary.has("shouldConvertToASimpleReplacement(.*)"));
            assertEquals("aBrandNewRegex$1", currentRegexPersonalDictionary.getJSONObject("shouldBeInserted(.*)").getString("repl"));
            assertEquals("<", currentRegexPersonalDictionary.getJSONObject("shouldBeInserted(.*)").getString("dir"));
            assertEquals("fourtyTwoIsMagicNumber$1", currentRegexPersonalDictionary.getJSONObject("shouldBeUnchangedBecauseExactlySameInUpserts(.*)").getString("repl"));
            assertEquals("<", currentRegexPersonalDictionary.getJSONObject("shouldBeUnchangedBecauseExactlySameInUpserts(.*)").getString("dir"));
        } finally {
            Files.delete(tmpPersonalDict);
            Files.delete(tmpRegexPersonalDict);
            Files.delete(tmpUpsertDict);
        }
    }

    @Test
    void upsertShouldReturnEarlyWhenUpsertsNotExistAndNotUpsertOnlyFlow() throws Exception {
        Path tmpUpsertDict = Files.createTempFile("upsert", ".json");
        Files.delete(tmpUpsertDict);
        boolean caught = false;
        try {
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool("/path/to/file.txt", "/path/to/other_file.txt", tmpUpsertDict.toString(), false);
            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();
        } catch(FileNotFoundException fnfe) {
            caught = true;
        }
        assertFalse(caught);
    }

    @Test
    void upsertShouldThrowFNFEWhenUpsertsNotExistAndActuallyIsUpsertOnlyFlow() throws Exception {
        Path tmpUpsertDict = Files.createTempFile("upsert", ".json");
        Files.delete(tmpUpsertDict);
        boolean caught = false;
        try {
            MergeIntoPersonalDictionaryTool mergeIntoPersonalDictionaryTool = new MergeIntoPersonalDictionaryTool("/path/to/file.txt", "/path/to/other_file.txt", tmpUpsertDict.toString(), true);
            mergeIntoPersonalDictionaryTool.updatePersonalDictionary();
        } catch(FileNotFoundException fnfe) {
            String excpMsg = fnfe.getMessage();
            assertTrue(excpMsg.startsWith("There is no upsert file "));
            assertTrue(excpMsg.endsWith(" to merge into the personal dictionary."));
            caught = true;
        }
        assertTrue(caught);
    }
}