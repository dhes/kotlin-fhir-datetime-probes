plugins { kotlin("jvm") }
kotlin { sourceSets["main"].kotlin.srcDir(rootProject.file("consumer/src/main/kotlin")) }
dependencies { implementation(project(":c-subtype")) }
