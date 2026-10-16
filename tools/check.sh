#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT}"

tools/update_java_deps.sh
bazel run //tools/buildifier:buildifier_check
tools/java_format.sh check
bazel build //...
bazel test //...
