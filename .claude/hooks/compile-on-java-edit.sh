#!/bin/bash
# Javaファイル編集後にMavenコンパイルを実行するhookスクリプト

if ! command -v jq >/dev/null 2>&1; then
  printf '{"systemMessage": "compile skipped - jq is not installed"}\n'
  exit 0
fi

FILE=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty' 2>/dev/null)

if [ -z "$FILE" ]; then
  exit 0
fi

if ! echo "$FILE" | grep -qE '[.]java$'; then
  exit 0
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=${REPO_ROOT:-$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel 2>/dev/null)}

if [ -z "$REPO_ROOT" ]; then
  REPO_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
fi

if ! cd "$REPO_ROOT"; then
  printf '{"systemMessage": "compile FAILED - could not resolve repo root"}\n'
  exit 1
fi

MAVEN_LOG=$(mktemp)
trap 'rm -f "$MAVEN_LOG"' EXIT

if bash mvnw compile -q >"$MAVEN_LOG" 2>&1; then
  printf '{"systemMessage": "compile OK"}\n'
  exit 0
else
  cat "$MAVEN_LOG" >&2
  printf '{"systemMessage": "compile FAILED - see stderr for Maven output"}\n'
  exit 1
fi
