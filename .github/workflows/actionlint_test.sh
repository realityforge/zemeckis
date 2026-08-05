#!/usr/bin/env bash

set -euo pipefail

actionlint="$1"
shellcheck="$2"
shift 2

exec "${actionlint}" "-shellcheck=${shellcheck}" -pyflakes= "$@"
