package promptsanitizer.batchjob;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ValidateUtilTest {
    @Test
    void invalidDirectionShouldIllegalArgumentException() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": \"any$1body@gmail.com\", \"dir\": \"huh?\"}");
        IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
        assertEquals("Direction must be either > or <", caught.getMessage());
    }
    @Test
    void directionNotStringShouldIllegalArgumentException() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": \"any$1body@gmail.com\", \"dir\": 561}");
        IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
        assertEquals("Personal regex dictionary entry's \"dir\" entry must be a string", caught.getMessage());
    }
    @Test
    void doesNotHaveDirectionIndicatorShouldIllegalArgumentException() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": \"any$1body@gmail.com\", \"direction\": \">\"}");
        IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
        assertEquals("Personal regex dictionary must have \"dir\" key", caught.getMessage());
    }
    @Test
    void replPropertyNotStringShouldIllegalArgumentException() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": 341, \"dir\": \">\"}");
        IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
        assertEquals("Personal regex dictionary entry's \"repl\" entry must be a string", caught.getMessage());
    }
    @Test
    void replPropertyNotPresentShouldIllegalArgumentException() throws Exception {
        JSONObject validateThis = new JSONObject("{\"replacement\": \"any$1body@gmail.com\", \"dir\": \">\"}");
        IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
        assertEquals("Personal regex dictionary entry must have \"repl\" key", caught.getMessage());
    }
    @Test
    void validForDirectionLessThanSymbol() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": \"any$1body@gmail.com\", \"dir\": \"<\"}");
        assertDoesNotThrow(() -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
    }
    @Test
    void validForDirectionGreaterThanSymbol() throws Exception {
        JSONObject validateThis = new JSONObject("{\"repl\": \"any$1body@gmail.com\", \"dir\": \">\"}");
        assertDoesNotThrow(() -> {
            ValidateUtil.validateRegexPersonalDictEntry(validateThis);
        });
    }
}