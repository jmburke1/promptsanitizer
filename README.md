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
4. Copy-paste it from the **Sanitized Prompt** panel (right), into the prompt input in whatever LLM you are using.
5. This is actually your classic click-ops type of solution.  You copy-paste everything!  So you are making 100% sure it is exactly what you want.

### 3. Bring it home

1. Copy-paste the LLM's response into the right panel.
2. Hit **<** - every placeholder gets swapped back to the original sensitive value.
3. The restored answer, based on making all of the text replacements in reverse, appears on the left.

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

## Batch Job Interface

This is for a local AI running inside a docker container (for example, openmonoagent.ai), which will probably only have access to a headless java.

It only takes a single command line argument for the direction.  And the values are expected to be either "forward", "reverse" or "upsertonly".  If the direction is forward, it will write to a file called "sanitized_content.txt" with the contents of the "unsanitized_content.txt" replaced with their corresponding safe values from both your regex personal dictionary followed by your exact personal dictionary.  If the direction is specified as reverse, everything is done in reverse.  It will write to the file "unsanitized_content.txt" with the contents of the "sanitized_content.txt" after making the regex dictionary replacements followed by the exact ones.

Finally, before it does any of this, it checks for a file called upserts.json in the directory that the application is running from.  In it, if it finds json strings either of the form `{"key": "value"}` or of the form `{"some_(.*)_regex": {"repl": "captured_$1", "dir": ">"}}` or of the form `{"some_(.*)_regex": {"repl": "captured_$1", "dir": "<"}}` then it will update the correct personal dictionary so that the value is either overwritten or inserted.  And, if the upserts.json contains `{"key": null}` then that is taken as an instruction to delete "key" out of both of your personal dictionaries altogether.

This part of it is also a click-ops solution.  The assumption is that the hard drive, in your docker container, is mounted in such a way that the human outside of it can open the text file in the corresponding real location on their real hard drive and copy-paste text from the sanitized output into whatever LLM they are using in the cloud.  And that they can copy-paste the answer from that back into the same text file.  The advantage, in this case, is that you're leveraging your local AI to maybe think of things that are sensitive info that you didn't.

## License

MIT - do whatever you want with your downloaded copy of it.

---

**Built because your prompts shouldn't be anyone else's training data.**

## Screen Captures

This README.md is already long enough.  See the EXAMPLE_WITH_SCREEN_CAPTURES.md for more details.  Also, for regular expression support, see REGEX_EXAMPLE_WITH_SCREEN_CAPTURES.md.
