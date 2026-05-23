#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────
INSTALL_DIR="/opt/promptsanitizer"
LIB_DIR="${INSTALL_DIR}/lib"
APP_NAME="promptsanitizer"
REPO_URL="https://github.com/jmburke1/promptsanitizer/archive/refs/heads/main.zip"
JSON_VERSION="20250107"
JSON_COORD="org.json:json:${JSON_VERSION}"
JAVA_MIN=21

# ── Helpers ───────────────────────────────────────────────────────────
die() { echo "[ERROR] $*" >&2; exit 1; }
info() { echo "[INFO]  $*"; }

# ── Step 0: Check Java ────────────────────────────────────────────────
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

# ── Step 1: Create install directory ──────────────────────────────────
if [ -d "$INSTALL_DIR" ]; then
    info "Removing existing installation at ${INSTALL_DIR} ..."
    sudo rm -rf "$INSTALL_DIR"
fi

sudo mkdir -p "${LIB_DIR}"
info "Created ${INSTALL_DIR}." # mkdir -p implicitly creates the INSTALL_DIR in addition to the LIB_DIR because of the -p option.

# ── Step 2: Download the repo as a zip (no git required) ──────────

info "Downloading repository from ${REPO_URL} ..."
if ! curl -fsSL "${REPO_URL}" -o "downloaded.zip"; then
    die "Failed to download ${REPO_URL}. Is the URL correct?"
fi
sudo mv downloaded.zip ${INSTALL_DIR}
pushd ${INSTALL_DIR}
sudo unzip downloaded.zip
sudo rm downloaded.zip

# ── Step 3: Download org.json jar ─────────────────────────────────────
JAR_URL="https://repo1.maven.org/maven2/org/json/json/${JSON_VERSION}/json-${JSON_VERSION}.jar"
JAR_PATH="${LIB_DIR}/json-${JSON_VERSION}.jar"

if [ ! -f "$JAR_PATH" ]; then
    info "Downloading ${JSON_COORD} ..."
    sudo curl -fsSL -o "$JAR_PATH" "$JAR_URL" || die "Failed to download ${JAR_URL}"
fi

# ── Step 4: Compile MainApp and all main classes ─────────────────────
info "Compiling Java sources ..."
pushd promptsanitizer-main
sudo mv * ../
sudo mv .gitignore ../
sudo mv .gitattributes ../
popd
sudo rmdir promptsanitizer-main

# Collect all .java files under src/main/java
JAVA_FILES=$(find src/main/java -name '*.java' | sort)

if [ -z "$JAVA_FILES" ]; then
    die "No .java files found under src/main/java."
fi

sudo javac -d build -sourcepath src/main/java \
    -cp "${INSTALL_DIR}/lib/json-${JSON_VERSION}.jar" \
    $JAVA_FILES || die "Compilation failed."

info "Compilation successful."

# ── Step 6: Create the application runner script ──────────────────────
popd
RUNNER="run-${APP_NAME}"

cat > "$RUNNER" <<'RUNNER_EOF'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="/opt/promptsanitizer/"
CLASS_PATH="${SCRIPT_DIR}/build:$(find "${SCRIPT_DIR}/lib" -name '*.jar' | tr '\n' ':')"

exec java -cp "$CLASS_PATH" promptsanitizer.MainApp "$@"
RUNNER_EOF

sudo mv ${RUNNER} ${INSTALL_DIR}
sudo chmod +x "${INSTALL_DIR}/${RUNNER}"
info "Created runner script at ${RUNNER}."

# ── Step 7: Create symbolic link in /usr/local/bin ────────────────────
LINK_TARGET="/usr/local/bin/${APP_NAME}"

if [ -L "$LINK_TARGET" ] || [ -e "$LINK_TARGET" ]; then
    info "Removing existing ${LINK_TARGET} ..."
    sudo rm -f "$LINK_TARGET"
fi

sudo ln -sf "${INSTALL_DIR}/${RUNNER}" "$LINK_TARGET"
info "Created symlink: ${LINK_TARGET} -> ${INSTALL_DIR}/${RUNNER}"

# ── Done ──────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════════════"
echo "  Installation complete!"
echo "  Run '${APP_NAME}' from anywhere to start."
echo "  ONE IMPORTANT NOTE!  This install script currently"
echo "  has no way to check if you have the full JDK or"
echo "  only the headless one.  If you get a headless"
echo "  If you get a headless exception, simply install the"
echo "  full JDK."
echo "═══════════════════════════════════════════════════"
