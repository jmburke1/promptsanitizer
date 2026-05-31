package promptsanitizer.batchjob;

import org.json.JSONObject;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.io.IOException;


public class MergeUtilTest {
    @Test
    void removeIfHasShouldReturnTrueIfWasThereToRemove() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertTrue(MergeUtil.removeIfHas(jo, "key2"));
        assertEquals("value1", jo.getString("key1"));
        assertFalse(jo.has("key2"));
    }

    @Test
    void removeIfHasShouldReturnFalseIfNotThereToRemove() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertFalse(MergeUtil.removeIfHas(jo, "key3"));
        assertEquals("value1", jo.getString("key1"));
        assertEquals("value2", jo.getString("key2"));
    }

    @Test
    void putIfNotHasOrDifferentShouldReturnTrueIfNotThere() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertTrue(MergeUtil.putIfNotHasOrDifferent(jo, "key3", "value3"));
        assertEquals("value1", jo.getString("key1"));
        assertEquals("value2", jo.getString("key2"));
        assertEquals("value3", jo.getString("key3"));
    }

    @Test
    void putIfNotHasOrDifferentShouldReturnTrueIfAlreadyThereButDifferent() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertTrue(MergeUtil.putIfNotHasOrDifferent(jo, "key2", "value3"));
        assertEquals("value1", jo.getString("key1"));
        assertEquals("value3", jo.getString("key2"));
    }

    @Test
    void putIfNotHasOrDifferentShouldReturnFalseIfAlreadyThereAndSame() {
        JSONObject jo = new JSONObject("{\"key1\": \"value1\", \"key2\": \"value2\"}");
        assertFalse(MergeUtil.putIfNotHasOrDifferent(jo, "key2", "value2"));
        assertEquals("value1", jo.getString("key1"));
        assertEquals("value2", jo.getString("key2"));
    }

    @Test
    void aEqualsBForMergePurposesShouldTrueWhenObjectEqualsReturnsTrue() {
        assertTrue(MergeUtil.aEqualsBForMergePurposes("abcde", "abcde"));
    }

    @Test
    void aEqualsBForMergePurposesShouldFalseWhenAIsNotJSONObjectAndBIs() {
        String a = "not a json object";
        JSONObject b = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        assertFalse(MergeUtil.aEqualsBForMergePurposes(a, b));
    }
    @Test
    void aEqualsBForMergePurposesShouldFalseWhenAIsJSONObjectAndBIsNot() {
        JSONObject a = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        String b = "not a json object";
        assertFalse(MergeUtil.aEqualsBForMergePurposes(a, b));
    }
    @Test
    void aEqualsBForMergePurposesShouldFalseWhenReplValuesMismatch() {
        JSONObject a = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        JSONObject b = new JSONObject("{\"repl\": \"value2\", \"dir\": \"<\"}");
        assertFalse(MergeUtil.aEqualsBForMergePurposes(a, b));
    }
    @Test
    void aEqualsBForMergePurposesShouldFalseWhenDirValuesMismatch() {
        JSONObject a = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        JSONObject b = new JSONObject("{\"repl\": \"value1\", \"dir\": \">\"}");
        assertFalse(MergeUtil.aEqualsBForMergePurposes(a, b));
    }
    @Test
    void aEqualsBForMergePurposesShouldTrueWhenReplAndDirValuesMatch() {
        JSONObject a = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        JSONObject b = new JSONObject("{\"repl\": \"value1\", \"dir\": \"<\"}");
        assertTrue(MergeUtil.aEqualsBForMergePurposes(a, b));
    }
    @Test
    void guardedWriteShouldWriteWhenTrue() throws IOException {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        JSONObject jo = new JSONObject("{\"key1\": \"value1\"}");
        try {
            MergeUtil.guardedWriteFile(true, tmpPersonalDict, jo);
            JSONObject joRetrieved = new JSONObject(Files.readString(tmpPersonalDict));
            assertEquals("value1", joRetrieved.getString("key1"));
        } finally {
            Files.delete(tmpPersonalDict);
        }
    }
    @Test
    void guardedWriteShouldRefrainFromWriteWhenFalse() throws IOException {
        Path tmpPersonalDict = Files.createTempFile("personalDict", ".json");
        JSONObject jo = new JSONObject("{\"key1\": \"value1\"}");
        try {
            MergeUtil.guardedWriteFile(false, tmpPersonalDict, jo);
            assertEquals("", Files.readString(tmpPersonalDict));
        } finally {
            Files.delete(tmpPersonalDict);
        }
    }
}
