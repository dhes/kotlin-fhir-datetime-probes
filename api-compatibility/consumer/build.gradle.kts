plugins { kotlin("jvm") }
dependencies { compileOnly(project(":v1")) }

val rt0 by configurations.creating
dependencies { rt0(project(":v1")) }
tasks.register<JavaExec>("runAgainst-v1") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt0 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("v1")
  isIgnoreExitValue = true
}

val rt1 by configurations.creating
dependencies { rt1(project(":a-field")) }
tasks.register<JavaExec>("runAgainst-a-field") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt1 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("a-field")
  isIgnoreExitValue = true
}

val rt2 by configurations.creating
dependencies { rt2(project(":b-body")) }
tasks.register<JavaExec>("runAgainst-b-body") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt2 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("b-body")
  isIgnoreExitValue = true
}

val rt3 by configurations.creating
dependencies { rt3(project(":c-subtype")) }
tasks.register<JavaExec>("runAgainst-c-subtype") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt3 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("c-subtype")
  isIgnoreExitValue = true
}

val rt4 by configurations.creating
dependencies { rt4(project(":d-equals")) }
tasks.register<JavaExec>("runAgainst-d-equals") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt4 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("d-equals")
  isIgnoreExitValue = true
}

val rt5 by configurations.creating
dependencies { rt5(project(":e-shared")) }
tasks.register<JavaExec>("runAgainst-e-shared") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rt5 + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("e-shared")
  isIgnoreExitValue = true
}

val rtHidden by configurations.creating
dependencies { rtHidden(project(":a-field-hidden")) }
tasks.register<JavaExec>("runAgainst-a-field-hidden") {
  dependsOn("classes")
  mainClass.set("probe.MainKt")
  classpath = sourceSets["main"].output + rtHidden + configurations["compileClasspath"].filter { it.name.startsWith("kotlin-stdlib") }
  args("a-field-hidden")
  isIgnoreExitValue = true
}
