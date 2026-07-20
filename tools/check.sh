#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT}"

tools/update_java_deps.sh
bazel run //:buildifier_check
tools/java_format.sh check
bazel build //...
bazel build //core/src/test/j2cl:zemeckis_j2cl_smoke
bazel build //core/src/test/gwt:zemeckis_gwt_assets //core/src/test/gwt:zemeckis_dev_gwt_assets //core/src/test/gwt:basic_example_gwt_assets //core/src/test/gwt:pulse_task_example_gwt_assets //core/src/test/gwt:repeating_task_example_gwt_assets
bazel test //... --release_version=0.0
