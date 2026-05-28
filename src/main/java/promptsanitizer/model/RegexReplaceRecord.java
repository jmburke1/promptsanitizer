/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.model;

import java.util.Comparator;

record RegexReplaceRecord(String regex, String replacement, String direction) {
}
