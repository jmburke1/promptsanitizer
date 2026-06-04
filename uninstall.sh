#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────
APP_NAME="promptsanitizer"
INSTALL_DIR="${HOME}/.local/opt/${APP_NAME}"

# ── Helpers ───────────────────────────────────────────────────────────
info() { echo "[INFO]  $*"; }

# ── Step 1: Remove symbolic link in /usr/local/bin ────────────────────
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

# ── Step 2: Remove install directory ──────────────────────────────────
if [ -d "$INSTALL_DIR" ]; then
    info "Removing existing installation at ${INSTALL_DIR} ..."
    rm -rf "$INSTALL_DIR"
fi

# ── Done ──────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════════════"
echo "  Removal complete!"
echo "═══════════════════════════════════════════════════"
