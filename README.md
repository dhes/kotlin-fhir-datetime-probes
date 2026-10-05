# kotlin-fhir date-time probes

Small tests that anyone can run, about the date-time classes in
[ohs-foundation/kotlin-fhir](https://github.com/ohs-foundation/kotlin-fhir). A probe here means a one-off test written
to answer one question. There is one so far. It was written while looking at two open issues:

- [#101](https://github.com/ohs-foundation/kotlin-fhir/issues/101): when a date-time is read and written back,
  trailing zeros in the seconds are dropped and `+00:00` becomes `Z`.
- [#87](https://github.com/ohs-foundation/kotlin-fhir/issues/87): equality of date-time values, and a refactor
  [sketched in a comment](https://github.com/ohs-foundation/kotlin-fhir/issues/87#issuecomment-4568831387) so that
  `FhirDate` and `FhirDateTime` share their `Year`, `YearMonth` and `Date` classes.

This repository is independent work by a kotlin-fhir contributor. It is not part of kotlin-fhir or of the Open Health
Stack project.

## The question

kotlin-fhir is at release candidate 1.0.0-rc03. Once 1.0 is out, people will expect an upgrade not to break code that
uses it. So: if `FhirDateTime` is changed after 1.0 to address either issue, is the change binary compatible and
source compatible?

## The answer

It depends on how the change is made. Six ways of changing the class were tried. They are numbered 1, 1b and 2 to 5,
because 1b is a variation on 1. Each was tested for two things:

- **Binary compatibility:** a small program that uses `FhirDateTime` was compiled against the class as it is today,
  and then run, without being rebuilt, against the changed class. This is what happens when a library is upgraded
  underneath code that is not recompiled.
- **Source compatibility:** the same program's source was compiled afresh against the changed class.

| # | Folder | Change tried | Issue | Already-built program still runs | Source still compiles | What else changes |
|---|---|---|---|---|---|---|
| 1 | `a-field` | A third property in the constructor of `FhirDateTime.DateTime`, holding the string exactly as it was parsed | #101 | **No:** `NoSuchMethodError` on the constructor and on `copy` | Yes | Equality: a value parsed from `2024-01-01T12:00:00.000Z` no longer equals one parsed from `2024-01-01T12:00:00Z` |
| 1b | `a-field-hidden` | The same, with the old two-argument constructor and `copy` kept as `@Deprecated(level = HIDDEN)` declarations | #101 | Yes | Yes | Equality, as in 1. The old `copy` returns a value without the kept string |
| 2 | `b-body` | That string kept in a property in the class body, outside the constructor | #101 | Yes | Yes | `copy()` returns a value without the kept string |
| 3 | `c-subtype` | A new subtype of `FhirDateTime`, which the parser returns for every full date-time | #101 | **No:** `NoWhenBranchMatchedException`, `ClassCastException` | **No:** `when` is no longer exhaustive | A parsed full date-time is no longer a `DateTime` |
| 4 | `d-equals` | `equals` and `hashCode` compare the moment in time | #87 | Yes | Yes | `12:00:00Z == 07:00:00-05:00` goes from false to true |
| 5 | `e-shared` | `Year`, `YearMonth` and `Date` shared between `FhirDate` and `FhirDateTime` | #87 | **No:** `NoClassDefFoundError` | Yes | Not examined |

What follows from the table:

- **Of the changes tried for #101, two are binary compatible: 1b and 2.** A property added to the constructor needs
  the old constructor and `copy` kept as hidden declarations (1b); without them it breaks already-built code (1).
- **The refactor for #87, in the form tried (change 5), is source compatible for this program but not binary
  compatible.** Source that uses the old names still compiles; already-built code fails. Other forms were not tried.
  One thing can be said without a test: on the JVM a class has a single name, so if `FhirDate.Year` and
  `FhirDateTime.Year` become one class, at most one of those two names can remain a real class, and code compiled
  against the other will not find it.
- **Changes 1, 1b and 4 change what equality answers,** with no error to signal it. For 1 and 1b that could be
  avoided by overriding `equals` and `hashCode` to leave the new property out; that was not tried.

What each change looks like in code is in [`api-compatibility/`](api-compatibility/README.md). The full output of the run made here is in
[`api-compatibility/results.txt`](api-compatibility/results.txt); running the test yourself prints to the terminal and
leaves that file alone.

## Run it

```
cd api-compatibility
./run.sh
```

It needs a JDK; it was run here with JDK 21. The Gradle wrapper in the folder downloads Gradle and the dependencies on
first use.

## Limits

- **JVM only.** kotlin-fhir also builds for Android, JS, Wasm and iOS; none of those was tested.
- **Only what the test program does was tested.** It constructs, reads, copies, destructures, matches with `when`,
  casts, compares, and parses and prints. Other uses of the class are not covered, and no dump of the public API was
  compared. In particular, "source still compiles" for change 5 is as far as this program goes: code that overloads
  on both `FhirDate.Year` and `FhirDateTime.Year`, or tells them apart with `is`, would be affected.
- **Only these forms of each change were tried.** Another way of making the same change could behave differently, as
  1 and 1b show. For change 3, a form in which the parser returns the new subtype only on request was not tried.
- **The class under test is a copy.** `FhirDateTime.kt` and `FhirDate.kt` were copied from kotlin-fhir's `main` at
  commit `0a2ab88` (2026-10-02). Both files are the same in release 1.0.0-rc03: in a clone of kotlin-fhir,
  `git diff v1.0.0-rc03 0a2ab88 -- fhir-model-r4/src/commonMain/kotlin/dev/ohs/fhir/model/r4/FhirDateTime.kt` prints
  nothing, and likewise for `FhirDate.kt`.
- **Changes 1 and 1b store the parsed string, where #101 suggests "a precision field" on the date-time classes.** The
  compatibility result depends on the constructor's shape, not on what the property holds.
- **Change 5 is a simplified version of the sketch in #87.** Its class names are placeholders, and it leaves out the
  common parent type that the sketch proposes. It uses nested type aliases, which compiled here with Kotlin 2.3.20, the
  version kotlin-fhir builds with, and no extra compiler flag.

## Licence

Apache License 2.0; see [LICENSE](LICENSE) and [NOTICE](NOTICE). The files under
`api-compatibility/*/src/main/kotlin/dev/ohs/fhir/model/r4/` are copied from kotlin-fhir, which is copyright Open
Health Stack Foundation and under the same licence. Each copy says at its top whether it was modified and how.
