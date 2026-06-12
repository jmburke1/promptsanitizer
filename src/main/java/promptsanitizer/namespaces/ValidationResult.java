package promptsanitizer.namespaces;

record ValidationResult(String rejoinedPath, boolean valid, String reason) {
}
