# T01 — Migrate and prove formatting

- Status: pending
- Blocked by: None
- Spec coverage: R1-R5 / AC1-AC5 (R5 publication and closeout follow implementation review)

## Delivers

Existing formatting commands and CI gate backed by the released rules module, graph checks and persistent workers; obsolete dependency plumbing removed.

## Acceptance criteria

- [ ] AC1-AC4 delivered with coverage across all graph-owned source families and clean/dirty/repair/worker evidence.
- [ ] AC5 implementation evidence committed for review; review/closeout/publication tracked in task map.

## Validation

- Verify module archive hash and prefix; resolve/lock Bazel modules.
- Inspect PalantirJavaFormat action inputs and worker execution/reuse.
- Exercise check failure/remediation/read-only behavior, write repair, watcher and staged safety, invalid mode.
- Run shell syntax/buildifier, dependency regeneration, tools/check.sh, unchanged generated outputs, git diff --check and final scope review.

## Evidence

Pending.
