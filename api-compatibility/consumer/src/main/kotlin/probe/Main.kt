// SPDX-License-Identifier: Apache-2.0
// The caller used by this probe; not part of ohs-foundation/kotlin-fhir.

package probe

import dev.ohs.fhir.model.r4.FhirDate
import dev.ohs.fhir.model.r4.FhirDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset

fun probe(name: String, block: () -> Any?) {
  val result = try { "ok    " + block() } catch (t: Throwable) { "FAIL  " + t::class.simpleName + ": " + (t.message ?: "").take(90) }
  println("  %-34s %s".format(name, result))
}

fun label(v: FhirDateTime): String =
  when (v) {
    is FhirDateTime.Year -> "year"
    is FhirDateTime.YearMonth -> "year-month"
    is FhirDateTime.Date -> "date"
    is FhirDateTime.DateTime -> "date-time"
  }

fun main(args: Array<String>) {
  println("== caller compiled against v1, run against " + args[0])
  val noonUtc = { FhirDateTime.DateTime(LocalDateTime(2024, 1, 1, 12, 0, 0), UtcOffset.ZERO) }
  probe("construct DateTime(a, b)") { noonUtc() }
  probe("read dateTime and utcOffset") { val d = FhirDateTime.fromString("2024-01-01T12:00:00Z") as FhirDateTime.DateTime; "" + d.dateTime + " " + d.utcOffset }
  probe("copy(utcOffset = ...)") { noonUtc().copy(utcOffset = UtcOffset(hours = 1)) }
  probe("destructure (a, b)") { val (a, b) = FhirDateTime.fromString("2024-01-01T12:00:00Z") as FhirDateTime.DateTime; "" + a + " " + b }
  probe("when over a parsed date-time") { label(FhirDateTime.fromString("2024-01-01T12:00:00.000Z")) }
  probe("construct FhirDateTime.Year") { FhirDateTime.Year(1985) }
  probe("when over a parsed year") { label(FhirDateTime.fromString("1985")) }
  probe("construct FhirDate.Year") { FhirDate.Year(1985) }
  probe("12:00Z == 07:00-05:00") { FhirDateTime.fromString("2024-01-01T12:00:00Z") == FhirDateTime.fromString("2024-01-01T07:00:00-05:00") }
  probe("round trip ...12:00:00.000+00:00") { FhirDateTime.fromString("2024-01-01T12:00:00.000+00:00").toString() }
  probe("round trip, then copy()") { (FhirDateTime.fromString("2024-01-01T12:00:00.000+00:00") as FhirDateTime.DateTime).copy().toString() }
  probe("parsed .000Z == parsed Z") { FhirDateTime.fromString("2024-01-01T12:00:00.000Z") == FhirDateTime.fromString("2024-01-01T12:00:00Z") }
}
