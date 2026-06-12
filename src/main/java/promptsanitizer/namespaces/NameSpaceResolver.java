package promptsanitizer.namespaces;

import java.io.File;
import java.nio.file.Path;

public class NameSpaceResolver {
    private final String basePath;
    private final NameSpaceValidator validator;

    public NameSpaceResolver(String basePath) {
        this.basePath = basePath;
        this.validator = new NameSpaceValidator(
                this::validPerFileExistsAndIsDirectory,
                System.getProperty("file.separator")
        );
    }

    public String resolveNameSpace(String proposedNameSpace) {
        if(proposedNameSpace == null || proposedNameSpace.isEmpty()) {
            return basePath;
        }
        ValidationResult validationResult = validator.isValidNameSpace(proposedNameSpace);
        if(!validationResult.valid()) {
            throw new IllegalArgumentException(validationResult.reason() + " : " + validationResult.rejoinedPath());
        }
        return validationResult.rejoinedPath();
    }

    private ValidationResult validPerFileExistsAndIsDirectory(String joined) {
        String resolved = Path.of(basePath).resolve(joined).toString();
        File f = new File(resolved);
        return new ValidationResult(resolved, f.exists() && f.isDirectory(), "Directory path based on namespace must exist and be a directory");
    }
}
