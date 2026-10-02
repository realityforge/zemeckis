#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${1:-write}"

cd "${ROOT}"

case "${MODE}" in
  write)
    exec bazel run @rules_palantir_java_format//:java_format -- --root=core --root=examples --root=tools
    ;;
  check)
    exec bazel build //:java_format_check
    ;;
  watch)
    exec bazel run @rules_palantir_java_format//:java_format_watch -- --root=core --root=examples --root=tools
    ;;
  *)
    echo "usage: tools/java_format.sh [write|check|watch]" >&2
    exit 2
    ;;
esac
