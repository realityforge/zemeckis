# Bazel Migration Implementation Plan

## Phase Sequence

1. Copy Braincheck's repository instructions.
2. Update the legacy dependency baseline and prove Buildr remains operational.
3. Add the Bazel 9.2 build, DepGen dependencies, Java/TestNG/JDepend tests, Grim processor, fixture verification, artifact targets, and initial check script.
4. Fix findings from the strict Error Prone policy.
5. Enable the strict Error Prone policy without NullAway.
6. Migrate nullness annotations to JSpecify and enable NullAway plus explicit null-marking checks while keeping Buildr operational.
7. Apply formatter-only Java changes.
8. Add Java-format tooling and expand the repository check gate.
9. Replace legacy IntelliJ metadata with Bazel tooling and add diagnostic fixture regeneration.
10. Add Maven Central release rules, builders, contract tests, and lifecycle scripts for `zemeckis-core`.
11. Add a latest pinned J2CL compile-and-link test.
12. Add production/development GWT compiler smoke targets and compile all three examples.
13. Add the GitHub Actions validation workflow while Buildr and Travis still exist.
14. Run implementation alignment review, resolve findings, then remove Buildr and all accepted legacy infrastructure together with this completed plan tree in the final commit.

## Delivery Approach

- Keep one task in progress and one focused commit per accepted phase boundary.
- Preserve Buildr until Bazel build, test, release, J2CL, GWT, IDE, and CI paths exist.
- Generate dependency declarations with DepGen; do not hand-maintain generated sections.
- Follow package-owned Bazel targets, explicit source lists, and no `glob()` usage.
- Copy Braincheck tooling where requested, adapting only project coordinates, source layout, dependencies, artifact topology, and accepted exclusions.
- Update the Buildr dependency declarations when source changes must remain dual-build compatible.
- Inspect status, unstaged diff, staged diff, and task evidence before every commit.

## Task Granularity Rules

- Keep policy-enabling commits separate from source-only remediation and formatting commits.
- Keep release, J2CL, GWT, IDE, and CI infrastructure independently reviewable.
- Record real commit hashes only after commits exist.
- Do not combine unrelated source refactors with migration changes.

## High-Risk Areas

- Published artifact parity:
  - Impact: missing sources, Grim metadata, GWT/JavaScript resources, POM dependencies, or classifiers would break consumers or Maven Central publication.
  - Mitigation: compare generated archive entry manifests and normalized POMs with `30-output-contract.md` and add release integration tests.
- J2CL with Akasha and latest upstream J2CL:
  - Impact: using the GWT Akasha artifact or an unpinned upstream revision can produce analysis success without a valid JavaScript link.
  - Mitigation: use `akasha-j2cl`, pin an exact observed J2CL commit, and build an optimized link target.
- GWT source and generated resource collection:
  - Impact: a target that analyzes but does not run the compiler would miss module/resource and Grim metadata failures.
  - Mitigation: build real compiler outputs for both core module modes and all three example entry points.
- Nullness and strict analysis migration:
  - Impact: public type-use annotations change and strict checks can expose contract errors across a large source surface.
  - Mitigation: separate Error Prone remediation, policy enablement, JSpecify/NullAway migration, and formatting into focused commits; keep both build systems passing until final cleanup.
- Release replacement:
  - Impact: removing Buildr before signed Maven Central bundle parity would strand releases.
  - Mitigation: retain Buildr until release lifecycle tests and dry-run packaging checks pass.

## Required Full Gates

`tools/check.sh`

Before `tools/check.sh` exists, use the task-specific Buildr and Bazel commands recorded in `20-task-board.yaml`.

## Completion Criteria

- All accepted implementation tasks have passing evidence and intentional commits.
- The implementation alignment review has no actionable findings.
- `tools/check.sh` passes with Bazel 9.2.0.
- Generated dependencies, formatting, and lock files are current.
- Maven artifacts satisfy the output contract.
- The J2CL link, core GWT modes, and all example GWT compiles pass.
- GitHub Actions owns validation.
- Buildr, Ruby, Travis, legacy IDEA metadata, API-difference reports, site publishing, and this plan tree are absent after the final commit.
- The worktree is clean.
