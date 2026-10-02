# Palantir formatting worker migration

## Source

- Explicit authorized request: adopt rules_palantir_java_format, use worker actions, preserve existing CI, deliver a reviewed PR assigned to realityforge with auto-merge.
- Grill confirmation and workflow entry exception: discover answers in references/repository, document the complete tree, proceed without confirmation.
- Latest user clarification: only Java sources reachable through the target graph require coverage; no enumeration/scratch-copy check supplement.
- Evidence: tools/java_format.sh, tools/check.sh, .github/workflows/ci.yml, all workspace Java BUILD targets; rules v0.1.1 defs.bzl/format.bzl/MODULE.bazel, jdbt reference diff and rose wrappers.

## Problem

The existing gate launches a local formatter binary rather than reusable Bazel formatting workers and maintains duplicate formatter dependency plumbing.

## Required outcome and scope

Migrate formatting to the public rules module and persistent worker checks, keeping existing command conventions and CI enforcement. Include graph-owned workspace Java under core/examples/tools, including GWT, J2CL, JVM tests, and release tools. Exclude generated/external sources and Java absent from the graph. Remove obsolete local formatter configuration. Publish an assigned PR with auto-merge without bypassing CI or forcing a merge.

## Constraints

No glob() or cross-directory source lists added to BUILD targets. Preserve existing Java formatting version (2.93.0) and avoid formatting churn or unrelated upgrades. Existing CI calls tools/check.sh; keep that workflow. Run tools/check.sh before declaring implementation complete. Preserve the original checkout. No durable domain documentation directories exist.

## Requirements and acceptance criteria

- R1 / AC1: bazel build //:java_format_check checks graph-owned Java across all source families and fails with file diagnostics and tools/java_format.sh write remediation on deliberately dirty source; it must not alter source files.
- R2 / AC2: PalantirJavaFormat uses worker,local strategy and one worker instance; observed execution proves worker use and repeated requests reuse a worker.
- R3 / AC3: tools/java_format.sh default/write/check remain supported; watch and tools/java_format_watch.sh delegate to public external tools with explicit existing roots core/examples/tools. Verify write repair, invalid-mode exit, watcher behavior, and unchanged Git index during external tool operation. No specialized staged or generated fixture consumers exist locally.
- R4 / AC4: delete local tools/java-format binary/dependencies and depgen MODULE section/callers; regenerate lockfile without unrelated dependency or source changes; update Unreleased changelog; required tools/check.sh passes and no generated diff remains afterward.
- R5 / AC5: plan and evidence commits, read-only plan/implementation reviews, and closeout deletion are auditable; PR assigned to realityforge and auto-merge enabled with an allowed merge method. Report precise external blockers if encountered.

## Completed evidence-based design tree

1. Enforcement -> existing tools/check.sh -> tools/java_format.sh check -> root java_format_check Bazel build, because bazel test alone is insufficient.
2. Coverage -> graph only (explicit user decision) -> explicit entry targets -> all_tests + core + examples + GWT smoke + J2CL build_test + release binaries. Pinned J2CL targets do not expose JavaInfo (J2clInfo stores it privately); add a same-package ordinary java_library companion for BuildTest.java with core/jsinterop dependencies, and use it as an entry root. Audit actual action inputs; no enumeration supplement.
3. Execution -> released rules v0.1.1 -> release archive override (BCR 404) -> verified sha256-4h/h0cVmPQ/7jkXnvW69rmABbfXOOzoXuBh3gMJDMk8=, strip rules_palantir_java_format-0.1.1 -> worker,local / one worker.
4. Commands -> existing wrapper modes preserved -> external write/watch roots core/examples/tools -> watch convenience wrapper, safety inherited and exercised.
5. Dependencies -> delete local formatter-only region and package -> remove its depgen caller -> regenerate existing dependencies and Bazel lock -> MVS-required rules_java 9.9.0/rules_jvm_external 7.1 accepted as necessary module requirements; no other intentional upgrades.
6. Delivery -> isolated master worktree, no duplicate open PR -> one vertical task -> plan review/commit -> implementation/evidence/full gate/commit -> fresh implementation review -> closeout commit -> assigned auto-merge PR. Authorization includes all these steps.

## Significant decisions

| Decision | Rationale | Impact | User verification |
| --- | --- | --- | --- |
| Graph-only check with explicit entry targets and J2CL JVM companion | User clarification and public rule semantics | Unowned Java is outside the gate; the J2CL source gets JavaInfo ownership without changing its transpiler target | Review roots and action input audit |
| Release override v0.1.1, formatter stays 2.93.0 | Verified latest release and BCR absence | Pin reproducible archive; adopt transitive Bazel rule version requirements | Review module/lock diff |
| Preserve shell wrapper/CI and add external watch | Existing UX and reference settle commands | Existing gate becomes worker action; writer roots unchanged | Run documented modes |
| Remove obsolete local formatter plumbing | No remaining specialized consumers | One source of formatter implementation/dependencies | Inspect final diff |

## Testing decisions

Verify release integrity/strip prefix, module resolution, buildifier, shell syntax, action inputs versus graph-owned sources, clean/dirty/repaired checks, unchanged source hashes during check, worker strategy and process reuse, external writer and watcher staged-index safety, dependency regeneration, tools/check.sh, generated-file diff, and final diff review. Restore all temporary source edits and stage probes. No permanent benchmark or extra test framework is needed.

## Open questions

None; the user request and inspected evidence settle the frontier.
