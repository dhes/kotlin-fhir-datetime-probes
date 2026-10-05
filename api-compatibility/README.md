# API compatibility of changes to `FhirDateTime`

This folder tests six ways of changing `FhirDateTime` and reports, for each, whether code that already uses the class
keeps working. The question, the summary table and the limits are in the [front page](../README.md).

## How it works

- **`v1/`** holds `FhirDateTime.kt` and `FhirDate.kt` copied unchanged from kotlin-fhir.
- **`a-field/`, `a-field-hidden/`, `b-body/`, `c-subtype/`, `d-equals/`, `e-shared/`** each hold a copy with one
  change. They are changes 1, 1b, 2, 3, 4 and 5 below.
- **`consumer/`** is the program under test, [`Main.kt`](consumer/src/main/kotlin/probe/Main.kt). It is compiled once,
  against `v1`. What it does:
  - constructs a `FhirDateTime.DateTime` from two values
  - parses a string and casts the result to `DateTime`, then reads its two properties and destructures it
  - calls `copy` with one named argument and with none, both of which compile to a call through `copy$default`
  - matches on the four subtypes in a `when` used as an expression, with no `else`
  - compares two parsed values
  - parses a string and prints it back
- **`recompile-*/`** are build files only. Each compiles the same `Main.kt` against one of the folders.

`./run.sh` does two things:

1. **Binary compatibility.** It runs the compiled `consumer` seven times. The first run has `v1` on the class path, as
   at compile time, and is the baseline. Each of the other six has one changed folder there instead. Each line
   of output is one operation, marked `ok` with the value it produced or `FAIL` with the error.
2. **Source compatibility.** It compiles `Main.kt` afresh against each folder and reports whether it compiles.

[`results.txt`](results.txt) holds the output of the run made here. `./run.sh` prints to the terminal and does not
write to that file.

Every statement under "Behaviour" below is taken from that output, except where it says otherwise.

## The class today

`FhirDateTime` is a sealed interface with four subtypes: `Year`, `YearMonth`, `Date` and `DateTime`. The changes for
#101 are to `DateTime`:

```kotlin
public data class DateTime(
  public val dateTime: LocalDateTime,
  public val utcOffset: UtcOffset,
) : FhirDateTime {
  override fun toString(): String = dateTime.format(LocalDateTime.Formats.ISO) + utcOffset.toString()
}
```

In changes 1, 1b, 2 and 3 the parser also passes along the string it was given, so that it can be printed back
unchanged. That string is called `wire` here, after the property of that name in kotlin-fhir's `FhirDecimal`.

## The six changes

### 1. A third property in the constructor (`a-field/`)

```kotlin
public data class DateTime(
  public val dateTime: LocalDateTime,
  public val utcOffset: UtcOffset,
  public val wire: String? = null,                 // added
) : FhirDateTime {
  override fun toString(): String = wire ?: /* built from the two parts, as today */
}
```

- **Already built:** fails. The program was compiled to call a constructor taking two values and a `copy` taking two;
  neither exists any more. `NoSuchMethodError`. Reading the properties, destructuring and `when` still work.
- **Rebuilt:** compiles, because the new property has a default value.
- **Behaviour:** a string parsed and printed back keeps `.000` and `+00:00`. A value parsed from `2024-01-01T12:00:00.000Z` no longer
  equals one parsed from `2024-01-01T12:00:00Z`, because a constructor property takes part in equality.
- **A caution about this version (read from the code, not run):** because the property holds a string, a `copy` that
  changes the time would carry the old string along. A numeric precision field, as #101 suggests, would not have that
  problem.
- **Not tried:** overriding `equals` and `hashCode` to leave `wire` out, which would keep equality as it is today.

### 1b. The same, with the old constructor and `copy` kept hidden (`a-field-hidden/`)

As change 1, plus two declarations that source code cannot see but already-built code can still call:

```kotlin
@Deprecated("Kept so that code compiled against the two-property class still runs", level = DeprecationLevel.HIDDEN)
public constructor(dateTime: LocalDateTime, utcOffset: UtcOffset) : this(dateTime, utcOffset, null)

@Deprecated("Kept so that code compiled against the two-property class still runs", level = DeprecationLevel.HIDDEN)
public fun copy(
  dateTime: LocalDateTime = this.dateTime,
  utcOffset: UtcOffset = this.utcOffset,
): DateTime = DateTime(dateTime, utcOffset, null)
```

- **Already built:** every operation works, including both calls to `copy`, which find the old `copy$default` that the
  hidden declaration provides.
- **Rebuilt:** compiles. Source calls resolve to the three-property constructor and `copy`, since the hidden ones are
  invisible to source.
- **Behaviour:** as change 1. The hidden `copy`, as written here, returns a value without the kept string.

### 2. The text as written kept outside the constructor (`b-body/`)

```kotlin
public data class DateTime(
  public val dateTime: LocalDateTime,
  public val utcOffset: UtcOffset,
) : FhirDateTime {
  public var wire: String? = null                  // added, in the class body
    private set

  public constructor(dateTime: LocalDateTime, utcOffset: UtcOffset, wire: String?) :
    this(dateTime, utcOffset) { this.wire = wire } // added, for the parser

  override fun toString(): String = wire ?: /* built from the two parts, as today */
}
```

- **Already built:** every operation works.
- **Rebuilt:** compiles.
- **Behaviour:** a string parsed and printed back keeps `.000` and `+00:00`. Equality is as it is today, because a
  property in the class body takes no part in it. `copy()` returns a value without the kept text, so two equal values
  can print differently.
- **A cost:** the class gains a `var`, though its setter is private.

### 3. A new subtype (`c-subtype/`)

`DateTime` is unchanged. The parser returns the new class instead.

```kotlin
public data class PreciseDateTime(                 // added beside DateTime
  public val dateTime: LocalDateTime,
  public val utcOffset: UtcOffset,
  public val wire: String,
) : FhirDateTime {
  override fun toString(): String = wire
}
```

- **Already built:** the operations on a value the program constructs itself still work. Two things fail on a value
  that came from the parser: a `when` over the four subtypes meets a fifth (`NoWhenBranchMatchedException`), and a cast
  to `DateTime` fails (`ClassCastException`).
- **Rebuilt:** does not compile: `'when' expression must be exhaustive`.
- **Not tried:** a form in which the parser returns the new subtype only when asked to.

### 4. Equality by the moment in time (`d-equals/`)

Two functions added to `DateTime`, so that two values are equal when they name the same moment, whatever their
offsets:

```kotlin
override fun equals(other: Any?): Boolean =
  other is DateTime && dateTime.toInstant(utcOffset) == other.dateTime.toInstant(other.utcOffset)

override fun hashCode(): Int = dateTime.toInstant(utcOffset).hashCode()
```

- **Already built:** every operation works.
- **Rebuilt:** compiles.
- **Behaviour:** `12:00:00Z == 07:00:00-05:00` changes from false to true. Nothing fails, so code that relies on the
  old answer (values kept in a set, duplicates removed, a check for whether something changed) gets a different
  result without warning.

### 5. Shared classes (`e-shared/`)

This follows the refactor
[sketched in a comment on #87](https://github.com/ohs-foundation/kotlin-fhir/issues/87#issuecomment-4568831387), in
simplified form. The class names are placeholders, and the common parent type that the sketch proposes is left out.

```kotlin
// new top-level classes, each belonging to both families
public data class FhirYear(public val value: Int) : FhirDate, FhirDateTime
public data class FhirYearMonth(public val value: kotlinx.datetime.YearMonth) : FhirDate, FhirDateTime
public data class FhirFullDate(public val date: LocalDate) : FhirDate, FhirDateTime

// inside FhirDate and inside FhirDateTime, replacing the three nested classes of those names
public typealias Year = FhirYear
public typealias YearMonth = FhirYearMonth
public typealias Date = FhirFullDate
```

- **Already built:** the operations on `DateTime` still work. Anything that touches `Year`, including a `when` over
  the subtypes, fails: the program was compiled against classes named `FhirDateTime.Year` and `FhirDate.Year`, which
  no longer exist as classes. `NoClassDefFoundError`.
- **Rebuilt:** compiles, because the type aliases keep those names valid in source.
- **Behaviour (not tested here):** a `FhirDate.Year` and a `FhirDateTime.Year` become the same class, which is the
  purpose of the refactor.
- **Why this is hard to avoid (reasoning, not a test):** on the JVM a class has a single name. If the two become one
  class, at most one of `FhirDate.Year` and `FhirDateTime.Year` can remain a real class, and code compiled against
  the other will not find it. Other forms of the refactor were not tried.

## Versions

Kotlin 2.3.20, kotlinx-datetime 0.8.0, Gradle 9.5.1, run with JDK 21 on macOS.
