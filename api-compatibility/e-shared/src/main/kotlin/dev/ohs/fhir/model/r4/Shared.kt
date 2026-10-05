// SPDX-License-Identifier: Apache-2.0
// New file written for this probe; not part of ohs-foundation/kotlin-fhir.

package dev.ohs.fhir.model.r4

import kotlinx.datetime.LocalDate

public data class FhirYear(public val `value`: Int) : FhirDate, FhirDateTime {
  override fun toString(): String = value.toString()
}

public data class FhirYearMonth(public val `value`: kotlinx.datetime.YearMonth) : FhirDate, FhirDateTime {
  override fun toString(): String = value.toString()
}

public data class FhirFullDate(public val date: LocalDate) : FhirDate, FhirDateTime {
  override fun toString(): String = date.toString()
}
