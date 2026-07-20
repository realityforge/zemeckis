#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT}"

java_tool_options="${JAVA_TOOL_OPTIONS:+${JAVA_TOOL_OPTIONS} }-Dzemeckis.diagnostic_messages_file=${ROOT}/core/src/test/java/zemeckis/diagnostic_messages.json"
bazel run \
  --test_env="JAVA_TOOL_OPTIONS=${java_tool_options}" \
  //core/src/test/java/zemeckis:update_diagnostic_messages
