rootProject.name = "blps-lab"

include("shared")
include(":api")
include(":transcriptions")

project(":api").projectDir = File("./services/api")
project(":transcriptions").projectDir = File("./services/transcriptions")
