#!/bin/bash
# Javaファイル編集後にMavenコンパイルを実行するhookスクリプト
FILE=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty' 2>/dev/null)

if [ -z "$FILE" ]; then
  exit 0
fi

if ! echo "$FILE" | grep -qE '[.]java$'; then
  exit 0
fi

cd /Users/ak/Development/IdeaProjects/AssemblyBuilderForPC
if bash mvnw compile -q 2>&1; then
  printf '{"systemMessage": "compile OK"}\n'
else
  printf '{"systemMessage": "compile FAILED - check errors above"}\n'
fi
