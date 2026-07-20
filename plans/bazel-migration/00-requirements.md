# Bazel Migration Requirements

## Objective

Replace the Buildr-based build, test, IDE, release, and CI infrastructure with Bazel while following the recently completed migration in `/Users/peter/Code/realityforge/braincheck`.

## Baseline

- Preserve the current `master` baseline at commit `df43abf`.
- Use Bazel 9.2.0.
- Use remote JDK 25 for Bazel tooling and retain Java 17 source and bytecode compatibility for published libraries.
- Import Bazel, dependency generation, formatting, IntelliJ, release, J2CL, GWT, and GitHub Actions conventions from Braincheck's current `master`.
- Keep Buildr operational until the final migration commit.
- Deliver the migration as focused local commits on `codex/bazel-migration`.

## Required Outcomes

1. Add `AGENTS.md` matching Braincheck's repository instructions.
2. Use DepGen-generated Java dependency repositories and targets without `glob()` and with package-owned `BUILD.bazel` files.
3. Build and test the core library with Bazel, including the released Grim 0.09 annotation processor output, TestNG tests, JDepend checks, and diagnostic-message fixture verification.
4. Add an explicit Bazel-backed command that regenerates the diagnostic-message fixture.
5. Enable Braincheck's strict Error Prone, JSpecify, NullAway, explicit null-marking, and Palantir Java Format policies.
6. Update Braincheck to 1.35.0, Akasha to 0.34, and align TestNG to Braincheck's 6.11; retain Grim 0.09 and JDepend 2.9.5.
7. Replace legacy IntelliJ metadata with Bazel BSP and managed project configuration.
8. Preserve the Maven Central contract for `zemeckis-core` defined in `30-output-contract.md` and add release artifact/lifecycle tests.
9. Add a latest pinned J2CL compile-and-link test using the J2CL-specific Akasha dependency.
10. Add real GWT compiler validation for `Zemeckis`, `ZemeckisDev`, and the basic, pulse-task, and repeating-task examples.
11. Add GitHub Actions validation for pushes, pull requests, and manual dispatches.
12. Remove Buildr, Ruby, Travis, superseded Rake tasks, API-difference reports, site publishing, and stale README references only in the final commit.

## Compatibility Decisions

- Retain the two-component release sequence (`0.14` to `0.15`).
- Preserve existing dependency versions unless explicitly updated above or required by Bazel, Java 17, JSpecify, J2CL, GWT, or strict analysis.
- Retain released Grim 0.09 and configure Bazel to run its existing processor; do not build or publish Grim.
- Preserve JDepend and its architecture test.
- Preserve the source-bearing main JAR required by GWT consumers, including generated Grim metadata and JavaScript/GWT resources.
- Continue producing main, sources, Javadoc, and POM artifacts for Maven Central.
- Do not port API-difference checking or retain its stale reports.
- Do not port GitHub Pages/site publishing, Javadoc cleanup, or release TODO scanning.
- Compile all three examples as validation targets, but do not add deployment or browser-launch support.
- Keep releases manual and local; GitHub Actions performs validation only.
- Remove the stale Codecov badge and Website reference when legacy infrastructure is removed.
- The accepted requirement that Buildr removal be the final commit overrides the workflow's usual deletion-only plan-cleanup commit: the active plan tree is removed in that final cleanup commit.

## Validation

- Each phase has focused validation recorded in `20-task-board.yaml` and an intentional local commit.
- `tools/check.sh` becomes the full repository gate once introduced and must pass before completion.
- CI runs `tools/check.sh` and verifies generated files are current.
- Release validation compares archive entry manifests and normalized POMs with `30-output-contract.md`.
- J2CL validation compiles and links, rather than merely analyzing, the core library.
- GWT validation runs the real compiler for production, development, and example modules.
- The final published classes remain Java 17 classfile version 61.

## Open Questions

All material questions were resolved with the user before implementation. The plan was approved on 2026-07-20.
