#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────
INSTALL_DIR="/opt/promptsanitizer"
APP_NAME="promptsanitizer"

# ── Helpers ───────────────────────────────────────────────────────────
info() { echo "[INFO]  $*"; }

# ── Step 1: Remove symbolic link in /usr/local/bin ────────────────────
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

# ── Step 2: Remove install directory ──────────────────────────────────
if [ -d "$INSTALL_DIR" ]; then
    info "Removing existing installation at ${INSTALL_DIR} ..."
    sudo rm -rf "$INSTALL_DIR"
fi

# ── Done ──────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════════════"
echo "  Removal complete!"
echo "═══════════════════════════════════════════════════"
