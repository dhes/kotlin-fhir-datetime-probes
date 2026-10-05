pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }
dependencyResolutionManagement { repositories { mavenCentral() } }
rootProject.name = "abi-probe"
include("consumer")
include("v1")
include("recompile-v1")
include("a-field")
include("recompile-a-field")
include("a-field-hidden")
include("recompile-a-field-hidden")
include("b-body")
include("recompile-b-body")
include("c-subtype")
include("recompile-c-subtype")
include("d-equals")
include("recompile-d-equals")
include("e-shared")
include("recompile-e-shared")
