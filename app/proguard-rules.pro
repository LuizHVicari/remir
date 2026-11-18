# R8 rules for release builds (isMinifyEnabled = true).
#
# Add a rule only when a release build fails or crashes because R8 removed or renamed something that is
# reached by reflection. Libraries (kotlinx.serialization, Ktor, Compose, Clerk) ship their own consumer rules.
