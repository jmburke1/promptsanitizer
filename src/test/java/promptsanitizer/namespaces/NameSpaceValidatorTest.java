package promptsanitizer.namespaces;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameSpaceValidatorTest {
    NameSpaceValidator nameSpaceValidator;
    @BeforeEach
    public void setup() {
        nameSpaceValidator = new NameSpaceValidator(
                descendents -> new ValidationResult(descendents, !descendents.equals("ab_3c/xyz_2"), "ab_3c followed by xyz_2 not valid"),
                "/"
        );

    }

    @Test
    public void shouldBeInvalidWhenRegexesInvalid() {
        ValidationResult validationResult = nameSpaceValidator.isValidNameSpace("^ab_3c.xyz_2");
        assertFalse(validationResult.valid());
        assertEquals("Namespace names must be lowercase alphanumeric with underscores allowed", validationResult.reason());
        validationResult = nameSpaceValidator.isValidNameSpace("ab_3c.x^yz_2");
        assertFalse(validationResult.valid());
        assertEquals("Namespace names must be lowercase alphanumeric with underscores allowed", validationResult.reason());
    }

    @Test
    public void shouldBeInvalidWhenAddlCriteriaInvalid() {
        ValidationResult validationResult = nameSpaceValidator.isValidNameSpace("ab_3c.xyz_2");
        assertFalse(validationResult.valid());
        assertEquals("ab_3c followed by xyz_2 not valid", validationResult.reason());
    }

    @Test
    public void shouldBeValidWhenRegexesAndAddlCriteriaValid() {
        ValidationResult validationResult = nameSpaceValidator.isValidNameSpace("ab_34.xyz_2");
        assertTrue(validationResult.valid());
        assertEquals("ab_34/xyz_2", validationResult.rejoinedPath());
    }
}
