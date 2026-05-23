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

### 3. Bring it home

1. Paste the LLM's response into the right panel.
2. Hit **<** - every placeholder gets swapped back to the original sensitive value.
3. The fully restored answer appears on the left.

### 4. How it works
1. If you've ever copy-pasted a prompt into notepad and replaced words before sending it off to an LLM, you get the basic idea.  Prompt sanitization is just text replacement, at the end of the day.
2. But this application adds the convenience that you have a locally configured dictionary, kept between uses of the application, where the text replacements get applied one after the other.

---

## Example dictionary

```json
{
  "sk-proj-abc123": "[OPENAI_KEY]",
  "AKIAxxxxxxxxxxxxxx": "[AWS_ACCESS_KEY]",
  "john.doe@megacorp.com": "[REDACTED_EMAIL]",
  "Project Chimera": "[CLASSIFIED_PROJECT]",
  "SSN: 123-45-6789": "[REDACTED_SSN]"
}
```

Paste a prompt containing `AKIAxxxxxxxxxxxxxx` → get `[AWS_ACCESS_KEY]` → send to Claude → receive answer → hit **<** → restore the original key in your local copy.

## Don't define your dictionary with cycles

```json
{
  "abcde": "fghij",
  "fghij": "abcde"
}
```

"I saw an abcde go to the fghij." becomes either "I saw an fghij go to the fghij." or "I saw an abcde go to the abcde.".  It doesn't become "I saw an fghij go to the abcde."  So don't do that.  The general rule is, this will do the same as if you had just clicked the "replace all", one-by-one, for each replacement you want to make, in something like notepad.

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

## License

MIT - do whatever you want with your downloaded copy of it.

---

**Built because your prompts shouldn't be anyone else's training data.**
