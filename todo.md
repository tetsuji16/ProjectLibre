# Remaining Tasks

- [x] Fix `Project` baseline cost/work visibility so project-level timescaled baseline cells hide only when all rolled-up children hide.
- [x] Fix `EnterpriseResource` baseline cost/work visibility so resource rollups reuse assignment-level baseline visibility rules.
- [x] Verify the `Task Usage` / `Resource Usage` / histogram availability path and close the audit item.

## Verification Notes

- `Task Usage` / `Resource Usage` / histogram availability still contains an old "untested / broken" comment in `Assignment.java`, but current code inspection did not reveal a concrete live failure to fix in this pass.
- The suspicious availability path is wired through `Field.resourceAvailability`, but no compile-time breakage or newly confirmed runtime regression was found while validating the current build.
