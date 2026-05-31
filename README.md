# PromptSanitizer

**Your prompts, your rules - zero sensitive data hits the wire.**

PromptSanitizer is a lightweight desktop application that sits between you and frontier LLMs (Claude, ChatGPT, Gemini, etc.). It lets you define a **personal dictionary** of sensitive words and phrases, then instantly swaps them out with safe placeholders before your prompt ever leaves your machine - and puts them back when the answer comes home.

No plugins. No browser extensions. No cloud uploads. Just a fast, local Java desktop app that gives you full control over what your AI sees.

---

## Why does this exist?

When you paste a prompt into an LLM you're often including things you'd never put in an email: API keys, passwords, internal URLs, customer PII, proprietary code snippets, medical snippets, legal strategy. Most LLM providers store those interactions for training or auditing. PromptSanitizer (when used responsibly) solves the core problem - **you can't always trust what leaves your screen** - by keeping sensitive content local while still letting you use powerful models.

---

## How it works

### 1. Build your dictionary

Click the **~** button to open the dictionary editor. Each entry is a pair:

| Sensitive (your data) | Safe (placeholder) |
|-----------------------|---------------------|
| `my-secret-api-key`   | `[API_KEY]`         |
| `john.doe@corp.com`   | `[REDACTED_EMAIL]`  |
| `Project Chimera`     | `[CLASSIFIED_PROJECT]` |

Entries are saved as a simple JSON file in your home directory.

### 2. Paste & sanitize

1. Paste your raw prompt into the **Unsanitized Prompt** panel (left).
2. Hit **\>** - every word from your dictionary gets replaced with its safe counterpart.
3. The result appears in the **Sanitized Prompt** panel (right), ready to copy and send.
4. Copy the sanitized prompt from the right panel into the cloud LLM you are connected to.
5. This manual, click-driven approach means you always see exactly what leaves your machine — no automation surprises.

### 3. Bring it home

1. Copy-paste the LLM's response into the right panel.
2. Hit **<** - every placeholder gets swapped back to the original sensitive value.
3. The restored answer, based on making all of the text replacements in reverse, appears on the left.

### 4. Under the hood

PromptSanitizer is sequential text replacement — the same thing you'd do in Notepad's Find & Replace, but automated from a persistent dictionary. Each entry is applied one after another, in order. No network calls, no telemetry, nothing leaves your machine except what you explicitly copy.

---

## Example dictionary

Here's an example with exact text replacements:

```json
{
  "sk-proj-abc123": "[OPENAI_KEY]",
  "AKIAxxxxxxxxxxxxxx": "[AWS_ACCESS_KEY]",
  "john.doe@megacorp.com": "[REDACTED_EMAIL]",
  "Project Chimera": "[CLASSIFIED_PROJECT]",
  "SSN: 123-45-6789": "[REDACTED_SSN]"
}
```

And here's one with regular expression replacements:
```json
{
  "([a-z]*)\\Q@gmail.com\\E": {"repl": "$1@proton.me", "dir": "<"},
  "([a-z]*)\\Q@proton.me\\E": {"repl": "$1@gmail.com", "dir": ">"}
}
```

Paste a prompt containing `AKIAxxxxxxxxxxxxxx` → get `[AWS_ACCESS_KEY]` → send to Claude → receive answer → hit **<** → restore the original key in your local copy.

## Avoid Cyclic Dictionaries

```json
{
  "abcde": "fghij",
  "fghij": "abcde"
}
```

Replacements are applied sequentially — not simultaneously. So `"I saw an abcde go to the fghij."` becomes `"I saw an fghij go to the fghij."` (or the reverse, depending on order). It won't do both in one pass. The rule of thumb: think of it as clicking "Replace All" one entry at a time, like in Notepad.

---

## Features

- **Personal dictionary editor** - sortable, editable table with add/remove rows.
   - Sorting the rows aren't saved.  The sort is just a visual convenience.
- **Bidirectional replacement** - sanitize outgoing prompts and personalize incoming answers with one click.
- **JSON persistence** - dictionary saved to `~/personal_dictionary.json` between sessions.
- **Zero dependencies on remote services** - everything runs locally, offline.
- **Java AWT/Swing desktop app** - no browser required, works headless if you're automating.

---

## Installation

### Quick install (Linux / macOS)

```bash
curl -fsSL https://raw.githubusercontent.com/jmburke1/promptsanitizer/main/install.sh | bash
```

This downloads the source, compiles it with Java 21+, and installs a `promptsanitizer` command to `/usr/local/bin`.

### Build From source (Gradle)

```bash
git clone https://github.com/jmburke1/promptsanitizer.git
cd promptsanitizer
./gradlew build   # requires JDK 21+
```

If you're building from Gradle, you're a power user.  Consider creating the artifacts in the `/opt/` folder and the link in the `/usr/local/bin`.

### Run directly

```bash
java -cp build/classes/java/main:lib/json-20250107.jar promptsanitizer.MainApp
```

---

## Requirements

| Item       | Version |
|------------|---------|
| Java       | 21+     |
| Build tool | Gradle (optional, for source builds) |
| GUI        | Java AWT + Swing (not headless JRE) |

---

## Tech stack

- **Language:** Java 21 (records, pattern matching, var)
- **GUI:** Java AWT / Swing
- **JSON:** org.json 20250107
- **Tests:** JUnit 5 + Mockito
- **Build:** Gradle (Java plugin)

---

## Batch Job Interface

For headless or automated use — such as a local AI agent running inside a Docker container — the batch mode sanitizes files via command line with no GUI required.

### How to Run

```bash
# Forward:  unsanitized_content.txt → sanitized_content.txt
promptsanitizer forward

# Reverse:  sanitized_content.txt → unsanitized_content.txt
promptsanitizer reverse

# Upsert dictionaries only (no sanitization)
promptsanitizer upsertonly
```

### Replacement Order

| Direction | Step 1 | Step 2 |
|-----------|--------|--------|
| **Forward** (`forward`) | Regex rules with `"dir": ">"` | Exact-match entries |
| **Reverse** (`reverse`) | Exact-match entries (reversed) | Regex rules with `"dir": "<"` |

Both your exact-match dictionary (`personal_dictionary.json`) and regex dictionary (`regex_personal_dictionary.json`) are applied automatically. The file `unsanitized_content.txt` is read for forward mode; `sanitized_content.txt` is read for reverse mode. Output goes to the opposite file.

### Dictionary Upserts

Before any sanitization, the batch job checks for `upserts.json` in the working directory. Use it to programmatically add, modify, or remove dictionary entries:

| JSON Format | Effect |
|-------------|--------|
| `"key": "value"` | Insert or overwrite an exact-match entry |
| `"regex_pattern": {"repl": "...", "dir": ">"}` | Add a forward regex rule |
| `"regex_pattern": {"repl": "...", "dir": "<"}` | Add a reverse regex rule |
| `"key": null` | Delete the entry from both dictionaries |

**Example `upserts.json`:**

```json
{
  "my-secret-api-key": "[API_KEY]",
  "(\\d{3}-\\d{2}-\\d{4})": {"repl": "[SSN]", "dir": ">"},
  "old-entry-to-remove": null
}
```

### Workflow

The batch mode is designed under the assumption of a click-ops workflow: your Docker container's filesystem is mounted so you can inspect files from the host. The typical flow:

1. Place your raw prompt in `unsanitized_content.txt`.
2. Run (or have your local LLM run) `promptsanitizer forward` — sanitized output appears in `sanitized_content.txt`.
3. Copy the sanitized text into your cloud LLM.
4. Paste the LLM's response back into `sanitized_content.txt`.
5. Run `promptsanitizer reverse` — restored original values appear in `unsanitized_content.txt`.

The advantage: you're leveraging a local AI to surface sensitive information you might have missed, while nothing ever leaves your machine unredacted.

## License

MIT - do whatever you want with your downloaded copy of it.

---

**Built because your prompts shouldn't be anyone else's training data.**

## Screen Captures

This README.md is already long enough.  See the EXAMPLE_WITH_SCREEN_CAPTURES.md for more details.  Also, for regular expression support, see REGEX_EXAMPLE_WITH_SCREEN_CAPTURES.md.
