#!/usr/bin/env bash
set -euo pipefail

# -- Configuration -----------------------------------------------------
APP_NAME="promptsanitizer"
INSTALL_DIR="/opt/${APP_NAME}"
LIB_DIR="${INSTALL_DIR}/lib"
REPO_URL="https://github.com/jmburke1/promptsanitizer/archive/refs/heads/feature/jlinesupport.zip"
#REPO_URL="https://github.com/jmburke1/promptsanitizer/archive/refs/tags/v1.2.5.zip"
JSON_VERSION="20250107"
JSON_COORD="org.json:json:${JSON_VERSION}"
JLINE_VERSION="3.30.13"
JLINE_READER_COORD="org.jline:jline-reader:${JLINE_VERSION}"
JLINE_TERMINAL_COORD="org.jline:jline-terminal:${JLINE_VERSION}"
JAVA_MIN=21

# -- Helpers -----------------------------------------------------------
die() { echo "[ERROR] $*" >&2; exit 1; }
info() { echo "[INFO]  $*"; }

# -- Step 0: Check Java ------------------------------------------------
if ! command -v java &>/dev/null; then
    die "java not found. Please install JDK ${JAVA_MIN}+ and try again."
fi

java_version=$(java -version 2>&1 | head -n 1 | sed 's/"//g' | awk '{print $3}')
javac_version=$(javac -version 2>&1 | awk '{print $2}')

# Both java and javac report versions like "21.0.2" or "21"
parse_major() { echo "$1" | cut -d. -f1; }

java_major=$(parse_major "$java_version")
javac_major=$(parse_major "$javac_version")

if (( java_major < JAVA_MIN || javac_major < JAVA_MIN )); then
    die "Java version must be at least ${JAVA_MIN}. Found java=${java_major}, javac=${javac_major}."
fi

info "Java version OK (java=${java_major}, javac=${javac_major})."

# -- Step 1: Create install directory ----------------------------------
if [ -d "$INSTALL_DIR" ]; then
    info "Removing existing installation at ${INSTALL_DIR} ..."
    sudo rm -rf "$INSTALL_DIR"
fi

sudo mkdir -p "${LIB_DIR}"
info "Created ${INSTALL_DIR}." # mkdir -p implicitly creates the INSTALL_DIR in addition to the LIB_DIR because of the -p option.

# -- Step 2: Download the repo as a zip (no git required) ----------

info "Downloading repository from ${REPO_URL} ..."
if ! curl -fsSL "${REPO_URL}" -o "downloaded.zip"; then
    die "Failed to download ${REPO_URL}. Is the URL correct?"
fi
sudo mv downloaded.zip ${INSTALL_DIR}
pushd ${INSTALL_DIR}
sudo unzip downloaded.zip
sudo rm downloaded.zip

# -- Step 3: Download org.json jar -------------------------------------
JSON_JAR_URL="https://repo1.maven.org/maven2/org/json/json/${JSON_VERSION}/json-${JSON_VERSION}.jar"
JSON_JAR_PATH="${LIB_DIR}/json-${JSON_VERSION}.jar"

if [ ! -f "$JSON_JAR_PATH" ]; then
    info "Downloading ${JSON_COORD} ..."
    sudo curl -fsSL -o "$JSON_JAR_PATH" "$JSON_JAR_URL" || die "Failed to download ${JSON_JAR_URL}"
fi

JLINE_READER_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-reader/${JLINE_VERSION}/jline-reader-${JLINE_VERSION}.jar"
JLINE_READER_JAR_PATH="${LIB_DIR}/jline-reader-${JLINE_VERSION}.jar"

if [ ! -f "$JLINE_READER_JAR_PATH" ]; then
    info "Downloading ${JLINE_READER_COORD} ..."
    sudo curl -fsSL -o "$JLINE_READER_JAR_PATH" "$JLINE_READER_JAR_URL" || die "Failed to download ${JLINE_READER_JAR_URL}"
fi

JLINE_TERMINAL_JAR_URL="https://repo1.maven.org/maven2/org/jline/jline-terminal/${JLINE_VERSION}/jline-terminal-${JLINE_VERSION}.jar"
JLINE_TERMINAL_JAR_PATH="${LIB_DIR}/jline-terminal-${JLINE_VERSION}.jar"

if [ ! -f "$JLINE_TERMINAL_JAR_PATH" ]; then
    info "Downloading ${JLINE_TERMINAL_COORD} ..."
    sudo curl -fsSL -o "$JLINE_TERMINAL_JAR_PATH" "$JLINE_TERMINAL_JAR_URL" || die "Failed to download ${JLINE_TERMINAL_JAR_URL}"
fi

# -- Step 4: Compile MainApp and all main classes ---------------------
info "Compiling Java sources ..."
pushd promptsanitizer-feature-jlinesupport
#pushd promptsanitizer-1.2.5
sudo mv * ../
sudo mv .gitignore ../
sudo mv .gitattributes ../
popd
sudo rmdir promptsanitizer-feature-jlinesupport
#sudo rmdir promptsanitizer-1.2.5

# Collect all .java files under src/main/java
sudo javac -d build -sourcepath src/main/java \
    -cp "${JSON_JAR_PATH}:${JLINE_READER_JAR_PATH}:${JLINE_TERMINAL_JAR_PATH}" \
    src/main/java/promptsanitizer/MainApp.java \
    src/main/java/promptsanitizer/batchjob/MainBatchJobApp.java \
    || die "Compilation failed."

info "Compilation successful."

# -- Step 6: Create the application runner script ----------------------
popd
RUNNER="run-${APP_NAME}"

cat > "$RUNNER" <<'RUNNER_EOF'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="/opt/promptsanitizer/"
CLASS_PATH="${SCRIPT_DIR}/build:$(find "${SCRIPT_DIR}/lib" -name '*.jar' | tr '\n' ':')"

exec java -cp "$CLASS_PATH" promptsanitizer.MainApp "$@"
RUNNER_EOF

RUNNER_BATCH="run-${APP_NAME}-batch"

cat > "$RUNNER_BATCH" <<'RUNNER_BATCH_EOF'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="/opt/promptsanitizer/"
CLASS_PATH="${SCRIPT_DIR}/build:$(find "${SCRIPT_DIR}/lib" -name '*.jar' | tr '\n' ':')"

exec java -cp "$CLASS_PATH" promptsanitizer.batchjob.MainBatchJobApp "$@"
RUNNER_BATCH_EOF

sudo mv ${RUNNER} ${INSTALL_DIR}
sudo chmod +x "${INSTALL_DIR}/${RUNNER}"
info "Created runner script at ${RUNNER}."

sudo mv ${RUNNER_BATCH} ${INSTALL_DIR}
sudo chmod +x "${INSTALL_DIR}/${RUNNER_BATCH}"
info "Created runner script at ${RUNNER_BATCH}."

# -- Step 7: Create symbolic link in /usr/local/bin --------------------
LINK_TARGET="/usr/local/bin/${APP_NAME}"
LINK_TARGET_BATCH="/usr/local/bin/${APP_NAME}batch"

if [ -L "$LINK_TARGET" ] || [ -e "$LINK_TARGET" ]; then
    info "Removing existing ${LINK_TARGET} ..."
    sudo rm -f "$LINK_TARGET"
fi

if [ -L "$LINK_TARGET_BATCH" ] || [ -e "$LINK_TARGET_BATCH" ]; then
    info "Removing existing ${LINK_TARGET_BATCH} ..."
    sudo rm -f "$LINK_TARGET_BATCH"
fi

sudo ln -sf "${INSTALL_DIR}/${RUNNER}" "$LINK_TARGET"
info "Created symlink: ${LINK_TARGET} -> ${INSTALL_DIR}/${RUNNER}"

sudo ln -sf "${INSTALL_DIR}/${RUNNER_BATCH}" "$LINK_TARGET_BATCH"
info "Created symlink: ${LINK_TARGET_BATCH} -> ${INSTALL_DIR}/${RUNNER_BATCH}"

# -- Done --------------------------------------------------------------
echo ""
echo "==================================================="
echo "  Installation complete!"
echo "  Run '${APP_NAME}' from anywhere to start."
echo "  'promptsanitizerbatch' for headless environments."
echo "==================================================="
