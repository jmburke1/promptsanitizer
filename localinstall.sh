#!/usr/bin/env bash
set -euo pipefail

# -- Configuration -----------------------------------------------------
APP_NAME="promptsanitizer"
INSTALL_DIR="${HOME}/.local/opt/${APP_NAME}"
LIB_DIR="${INSTALL_DIR}/lib"
JAV_DIR="${INSTALL_DIR}/jav"
RUNNER_DIR="${INSTALL_DIR}/rnnr"
REPO_URL="https://github.com/jmburke1/promptsanitizer/archive/refs/heads/feature/jlinesupport.zip"
#REPO_URL="https://github.com/jmburke1/promptsanitizer/archive/refs/tags/v1.2.5.zip"
UNZIP_URL="https://raw.githubusercontent.com/jmburke1/promptsanitizer/feature/jlinesupport/Unzip.java"
#UNZIP_URL="https://raw.githubusercontent.com/jmburke1/promptsanitizer/v1.2.5/Unzip.java"
JSON_VERSION="20250107"
JSON_COORD="org.json:json:${JSON_VERSION}"
JLINE_VERSION="3.30.13"
JLINE_READER_COORD="org.jline:jline-reader:${JLINE_VERSION}"
JLINE_TERMINAL_COORD="org.jline:jline-terminal:${JLINE_VERSION}"
JLINE_CONSOLE_COORD="org.jline:jline-console:${JLINE_VERSION}"
JLINE_TERMINALJANSI_COORD="org.jline:jline-terminal-jansi:${JLINE_VERSION}"
JLINE_BUILTINS_COORD="org.jline:jline-builtins:${JLINE_VERSION}"

# -- Helpers -----------------------------------------------------------
die() { echo "[ERROR] $*" >&2; exit 1; }
info() { echo "[INFO]  $*"; }

# -- Step 1: Create install directory ----------------------------------
if [ -d "$INSTALL_DIR" ]; then
    info "Removing existing installation at ${INSTALL_DIR} ..."
    rm -rf "$INSTALL_DIR"
fi
mkdir -p "${LIB_DIR}"

# -- Step 1a: Put java in install directory ----------------------------------
DOWNLOAD_JAVA_URL=$(curl -sL "https://api.github.com/repos/adoptium/temurin21-binaries/releases/latest" | grep 'browser_download_url.*jdk_x64_linux_hotspot' | grep -v debug | head -1 | grep -o '"[^"]*tar\.gz"' | head -1 | tr -d '"')
info "Downloading JDK from Adoptium..."
curl -L -o /tmp/openjdk-21.tar.gz "$DOWNLOAD_JAVA_URL"

mkdir "${JAV_DIR}"
info "Created ${INSTALL_DIR}." # mkdir -p implicitly creates the INSTALL_DIR in addition to the LIB_DIR because of the -p option.
mv /tmp/openjdk-21.tar.gz ${JAV_DIR}
pushd ${JAV_DIR}
tar -xzf openjdk-21.tar.gz
popd
# Get the actual JDK folder name
JDK_FOLDER=$(ls ${JAV_DIR}/ | grep jdk | grep -v '^openjdk-21\.tar\.gz$')
info "JDK folder: ${JDK_FOLDER}"

# -- Step 2: Download the repo as a zip (no git required) ----------
info "Downloading repository from ${REPO_URL} ..."
if ! curl -fsSL "${REPO_URL}" -o "/tmp/downloaded.zip"; then
    die "Failed to download ${REPO_URL}. Is the URL correct?"
fi
mv /tmp/downloaded.zip ${INSTALL_DIR}
pushd ${INSTALL_DIR}
if ! curl -fsSL "${UNZIP_URL}" -o "/tmp/Unzip.java"; then
    die "Failed to download ${UNZIP_URL}. Is the URL correct?"
fi
mv /tmp/Unzip.java ./
${JAV_DIR}/${JDK_FOLDER}/bin/javac Unzip.java
${JAV_DIR}/${JDK_FOLDER}/bin/java Unzip
rm downloaded.zip

# -- Step 3: Download org.json jar -------------------------------------
JSON_JAR_URL="https://repo1.maven.org/maven2/org/json/json/${JSON_VERSION}/json-${JSON_VERSION}.jar"
JSON_JAR_PATH="${LIB_DIR}/json-${JSON_VERSION}.jar"
info "Downloading ${JSON_COORD} ..."
curl -fsSL -o "$JSON_JAR_PATH" "$JSON_JAR_URL" || die "Failed to download ${JSON_JAR_URL}"

JLINE_READER_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-reader/${JLINE_VERSION}/jline-reader-${JLINE_VERSION}.jar"
JLINE_READER_JAR_PATH="${LIB_DIR}/jline-reader-${JLINE_VERSION}.jar"
info "Downloading ${JLINE_READER_COORD} ..."
curl -fsSL -o "$JLINE_READER_JAR_PATH" "$JLINE_READER_JAR_URL" || die "Failed to download ${JLINE_READER_JAR_URL}"

JLINE_TERMINAL_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-terminal/${JLINE_VERSION}/jline-terminal-${JLINE_VERSION}.jar"
JLINE_TERMINAL_JAR_PATH="${LIB_DIR}/jline-terminal-${JLINE_VERSION}.jar"

info "Downloading ${JLINE_TERMINAL_COORD} ..."
curl -fsSL -o "$JLINE_TERMINAL_JAR_PATH" "$JLINE_TERMINAL_JAR_URL" || die "Failed to download ${JLINE_TERMINAL_JAR_URL}"

JLINE_CONSOLE_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-console/${JLINE_VERSION}/jline-console-${JLINE_VERSION}.jar"
JLINE_CONSOLE_JAR_PATH="${LIB_DIR}/jline-console-${JLINE_VERSION}.jar"
info "Downloading ${JLINE_CONSOLE_COORD} ..."
curl -fsSL -o "$JLINE_CONSOLE_JAR_PATH" "$JLINE_CONSOLE_JAR_URL" || die "Failed to download ${JLINE_CONSOLE_JAR_URL}"

JLINE_TERMINALJANSI_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-terminal-jansi/${JLINE_VERSION}/jline-terminal-jansi-${JLINE_VERSION}.jar"
JLINE_TERMINALJANSI_JAR_PATH="${LIB_DIR}/jline-terminal-jansi-${JLINE_VERSION}.jar"
info "Downloading ${JLINE_TERMINALJANSI_COORD} ..."
curl -fsSL -o "$JLINE_TERMINALJANSI_JAR_PATH" "$JLINE_TERMINALJANSI_JAR_URL" || die "Failed to download ${JLINE_TERMINALJANSI_JAR_URL}"

JLINE_BUILTINS_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-builtins/${JLINE_VERSION}/jline-builtins-${JLINE_VERSION}.jar"
JLINE_BUILTINS_JAR_PATH="${LIB_DIR}/jline-builtins-${JLINE_VERSION}.jar"
info "Downloading ${JLINE_BUILTINS_COORD} ..."
curl -fsSL -o "$JLINE_BUILTINS_JAR_PATH" "$JLINE_BUILTINS_JAR_URL" || die "Failed to download ${JLINE_BUILTINS_JAR_URL}"

# -- Step 4: Compile MainApp and all main classes ---------------------
EXTRACTED_FOLDER=$(ls -d promptsanitizer-*/ | head -1 | tr -d '\n')
mv ${EXTRACTED_FOLDER}/* ./
mv ${EXTRACTED_FOLDER}/.gitignore ./ 2>/dev/null || true
mv ${EXTRACTED_FOLDER}/.gitattributes ./ 2>/dev/null || true
rmdir ${EXTRACTED_FOLDER}

# Compile
info "Compiling Java sources ..."
${JAV_DIR}/${JDK_FOLDER}/bin/javac -d build -sourcepath src/main/java \
    -cp "${JSON_JAR_PATH}:${JLINE_BUILTINS_JAR_URL}:${JLINE_READER_JAR_PATH}:${JLINE_TERMINALJANSI_JAR_URL}:${JLINE_CONSOLE_JAR_URL}:${JLINE_TERMINAL_JAR_PATH}" \
    src/main/java/promptsanitizer/MainApp.java \
    src/main/java/promptsanitizer/batchjob/MainBatchJobApp.java \
    || die "Compilation failed."

info "Compilation successful."

# -- Step 6: Create the application runner script ----------------------
popd
mkdir ${RUNNER_DIR}
RUNNER="run-${APP_NAME}"

cat > "$RUNNER" <<'RUNNER_EOF'
#!/usr/bin/env bash
set -euo pipefail
# Get the actual JDK folder name
JDK_FOLDER=$(ls ${HOME}/.local/opt/promptsanitizer/jav/ | grep jdk | grep -v '^openjdk-21\.tar\.gz$')

SCRIPT_DIR="${HOME}/.local/opt/promptsanitizer/"
CLASS_PATH="${SCRIPT_DIR}/build:$(find "${SCRIPT_DIR}/lib" -name '*.jar' | tr '\n' ':')"
export JAVA_HOME=${HOME}/.local/opt/promptsanitizer/jav/${JDK_FOLDER}
exec ${HOME}/.local/opt/promptsanitizer/jav/${JDK_FOLDER}/bin/java -cp "$CLASS_PATH" promptsanitizer.MainApp "$@"
RUNNER_EOF

RUNNER_BATCH="run-${APP_NAME}-batch"

cat > "$RUNNER_BATCH" <<'RUNNER_BATCH_EOF'
#!/usr/bin/env bash
set -euo pipefail
# Get the actual JDK folder name
JDK_FOLDER=$(ls ${HOME}/.local/opt/promptsanitizer/jav/ | grep jdk | grep -v '^openjdk-21\.tar\.gz$')

SCRIPT_DIR="${HOME}/.local/opt/promptsanitizer/"
CLASS_PATH="${SCRIPT_DIR}/build:$(find "${SCRIPT_DIR}/lib" -name '*.jar' | tr '\n' ':')"
export JAVA_HOME=${HOME}/.local/opt/promptsanitizer/jav/${JDK_FOLDER}
exec ${HOME}/.local/opt/promptsanitizer/jav/${JDK_FOLDER}/bin/java -cp "$CLASS_PATH" promptsanitizer.batchjob.MainBatchJobApp "$@"
RUNNER_BATCH_EOF

mv ${RUNNER} ${RUNNER_DIR}
chmod u+x "${RUNNER_DIR}/${RUNNER}"
info "Created runner script at ${RUNNER}."

mv ${RUNNER_BATCH} ${RUNNER_DIR}
chmod u+x "${RUNNER_DIR}/${RUNNER_BATCH}"
info "Created runner script at ${RUNNER_BATCH}."

# -- Step 7: Create symbolic link in /usr/local/bin --------------------
# -- Step 7a: See if we need to create the local .bin first ------------

created_local_bin=false

if [ ! -d "${HOME}/.local/bin" ]; then
    mkdir -p "${HOME}/.local/bin"
    created_local_bin=true
fi

# -- Step 7b: Now create the actual link ------------
LINK_TARGET="${HOME}/.local/bin/${APP_NAME}"
LINK_TARGET_BATCH="${HOME}/.local/bin/${APP_NAME}batch"

if [ -L "$LINK_TARGET" ] || [ -e "$LINK_TARGET" ]; then
    info "Removing existing ${LINK_TARGET} ..."
    rm -f "$LINK_TARGET"
fi

if [ -L "$LINK_TARGET_BATCH" ] || [ -e "$LINK_TARGET_BATCH" ]; then
    info "Removing existing ${LINK_TARGET_BATCH} ..."
    rm -f "$LINK_TARGET_BATCH"
fi

ln -sf "${RUNNER_DIR}/${RUNNER}" "$LINK_TARGET"
info "Created symlink: ${LINK_TARGET} -> ${RUNNER_DIR}/${RUNNER}"

ln -sf "${RUNNER_DIR}/${RUNNER_BATCH}" "$LINK_TARGET_BATCH"
info "Created symlink: ${LINK_TARGET_BATCH} -> ${RUNNER_DIR}/${RUNNER_BATCH}"

# -- Done --------------------------------------------------------------
echo ""
echo "==================================================="
echo "  Installation complete!"
echo "  Run '${APP_NAME}' from anywhere to start."
echo "==================================================="

# -- Step 7c: Let the user know they will have to log out and log back in --

if $created_local_bin; then
    echo ""
    echo "==========================================================="
    echo "Log out and log back in for the installation to take"
    echo "effect.  Alternatively, run source ~/.profile if you"
    echo "do not want to log out and log back in (but you'll"
    echo "have to do it every time you open a bash shell).  If"
    echo "you are an AI agent running inside a docker container,"
    echo "and you do not have a .profile in your home directory,"
    echo "just make sure that ~/.local/bin is in your PATH variable."
    echo "==========================================================="
fi

