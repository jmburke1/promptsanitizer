package promptsanitizer.namespaces;

import java.util.function.Function;

class NameSpaceValidator {
    private final Function<String, ValidationResult> additionalRejectionCriteria;
    private final String sep;

    NameSpaceValidator(Function<String, ValidationResult> additionalRejectionCriteria, String sep) {
        this.additionalRejectionCriteria = additionalRejectionCriteria;
        this.sep = sep;
    }

    ValidationResult isValidNameSpace(String proposedNameSpace) {
        String[] splits = proposedNameSpace.split("\\.");
        boolean validPerRegex = true;

        for(String part : splits) {
            if(!part.matches("[a-z0-9_]+")) {
                validPerRegex = false;
            }
        }
        String joined = String.join(sep, splits);
        if(!validPerRegex) {
            return new ValidationResult(joined, false, "Namespace names must be lowercase alphanumeric with underscores allowed");
        }
        return additionalRejectionCriteria.apply(joined);
    }
}
