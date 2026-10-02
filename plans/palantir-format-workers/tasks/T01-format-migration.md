# T01 — Migrate and prove formatting

- Status: complete
- Blocked by: None
- Spec coverage: R1-R5 / AC1-AC5 (R5 publication and closeout follow implementation review)

## Delivers

Existing formatting commands and CI gate backed by the released rules module, graph checks and persistent workers; obsolete dependency plumbing removed.

## Acceptance criteria

- [x] AC1-AC4 delivered with coverage across all graph-owned source families and clean/dirty/repair/worker evidence.
- [x] AC5 implementation evidence committed for review; review/closeout/publication tracked in task map.

## Validation

- Verify module archive hash and prefix; resolve/lock Bazel modules.
- Inspect PalantirJavaFormat action inputs and worker execution/reuse.
- Exercise check failure/remediation/read-only behavior, write repair, watcher and staged safety, invalid mode.
- Run shell syntax/buildifier, dependency regeneration, tools/check.sh, unchanged generated outputs, git diff --check and final scope review.

## Evidence

- Release archive checksum independently matched sha256-4h/h0cVmPQ/7jkXnvW69rmABbfXOOzoXuBh3gMJDMk8= and prefix rules_palantir_java_format-0.1.1. BCR module endpoint returned 404; release v0.1.1 MODULE requires rules_java 9.9.0/rules_jvm_external 7.1 and formatter remains 2.93.0.
- `bazel mod deps --lockfile_mode=update` passed; `.bazelrc` target language/runtime set to Java 17 because external writer initially failed under default source 11. Existing tool language/runtime stay 25. No Java source changes or third_party/java BUILD regeneration changes.
- `bazel build //:java_format_check --worker_verbose --execution_log_json_file=...` passed. Execution log has 15 PalantirJavaFormat actions, every runner `worker`; unique workspace Java action inputs equal all 63 tracked Java files with no missing/extra sources. Worker creation log: one singleplex worker id 4.
- Deliberately unformatted package declarations in core main, JVM test, GWT smoke, J2CL BuildTest, example, and release Javadoc tool all rejected with file diagnostics and `tools/java_format.sh write`; check preserved dirty bytes. Default writer restored exact original bytes. Repaired check passed; worker PID 57520 unchanged through requests. Git index tree unchanged by checker/writer while a dirty probe was staged; temporary stage/source edits restored.
- `tools/java_format_watch.sh` startup announced roots core/examples/tools; repaired two BuildTest edits in watcher PID 70340, preserving staged Git index. Process stopped and temporary edits restored. Invalid mode exited 2.
- `bash -n` on wrappers/updater/check script, buildifier check, lockfile JSON parsing and `git diff --check` passed.
- Full gate `PATH=/tmp/zemeckis-worker-tools:$PATH tools/check.sh` passed after resume: buildifier + formatting, all 136 build targets including GWT/J2CL/release, all 8 test targets passed. Temporary PATH shim selects isolated output base; it is not repository code.
- `tools/update_java_deps.sh` repeated after the full gate: MODULE, lockfile and third_party/java BUILD remain byte-identical.
- Investigation history: first gate was stopped during slow checksum downloads, isolated cache seeded from completed local cache entries. An early GWT smoke action and focused attempt exited 1 without diagnostics; direct TRACE compilation, retained-sandbox TRACE and original WARN replay, and normal Bazel target all passed with no GWT code changes. Root cause unresolved; subsequent full gate passed. A later full run was intentionally canceled by user pause near J2CL link completion; resumed full run passed.
- Scope review removed obsolete formatter package/generated MODULE region/callers; no ahab or specialized generated Java consumers existed. Companion target is root-only visible. CI workflow and tools/check.sh unchanged.
