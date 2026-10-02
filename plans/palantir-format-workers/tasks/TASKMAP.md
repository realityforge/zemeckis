# Task Map

- Spec: [SPEC.md](../SPEC.md)
- Status: `implementation-review`
- Current frontier: implementation review
- Planning reviewer: `/root/planning_reviewer` (`1/3` rounds, Findings: none)
- Plan checkpoint: automatic (completed evidence-based grill tree, explicit confirmation/entry exception, passing planning review)
- Implementation reviewer: pending (`0/5` rounds)

## Full-scope validation

- Gate: `tools/check.sh` and generated diff verification
- Evidence: `tools/check.sh` passed after resume (all build targets and 8/8 test targets); repeated dependency regeneration was byte-stable; shell syntax, buildifier and diff checks passed. Focused coverage, dirty/read-only/repair, worker reuse, watcher and index evidence recorded in T01.
- Earlier GWT smoke failure had no diagnostics; exact replay and later full gate passed without code changes. Cause remains unresolved; not represented as a fixed bug.

## Tasks

| ID | Task | Status | Blocked by |
| --- | --- | --- | --- |
| T01 | [Migrate and prove formatting](T01-format-migration.md) | complete | None |

## Sequencing notes

One coherent vertical migration owns dependency changes, command UX, coverage, negative behavior, worker evidence, changelog, and full validation. Review/publishing/closeout follow the workflow after implementation.

## Promoted knowledge

Not required: no docs/adr, docs/glossary, docs/specs, docs/deferred or .domain-modeling.toml exists.

## Publication and closeout

- Plan commit: `56017c0`.
- Implementation/evidence commit: pending.
- Review/closeout/publication: pending.
- User resumed work and requested archival after confirmed merge. Master is unprotected; wait for every actual check on the exact PR head before requesting `gh pr merge --auto --merge --match-head-commit`, without admin bypass. Preserve plan history with a merge commit. Archive this chat only after GitHub confirms merge.
