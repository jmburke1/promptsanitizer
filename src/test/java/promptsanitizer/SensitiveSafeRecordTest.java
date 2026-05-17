package promptsanitizer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SensitiveSafeRecordTest {

    @Test void record_components_match_constructor() {
        var rec = new SensitiveSafeRecord("secret", "replacement");
        assertEquals("secret", rec.sensitive());
        assertEquals("replacement", rec.safe());
    }

    @Test void equals_and_hashCode() {
        var a = new SensitiveSafeRecord("key", "val");
        var b = new SensitiveSafeRecord("key", "val");
        var c = new SensitiveSafeRecord("other", "val");

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test void toString_contains_both_fields() {
        var rec = new SensitiveSafeRecord("alpha", "beta");
        String s = rec.toString();
        assertTrue(s.contains("alpha"));
        assertTrue(s.contains("beta"));
    }
}
