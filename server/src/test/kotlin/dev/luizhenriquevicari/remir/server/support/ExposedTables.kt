package dev.luizhenriquevicari.remir.server.support

import org.jetbrains.exposed.v1.core.Table
import java.io.File

/** Every Exposed `Table` object under the package scanned by `generateMigrations`. */
object ExposedTables {
    private const val TABLES_PACKAGE = "dev.luizhenriquevicari.remir.server.features"

    fun all(): List<Table> {
        val loader = Thread.currentThread().contextClassLoader
        return loader
            .getResources(TABLES_PACKAGE.replace('.', '/'))
            .toList()
            .filter { it.protocol == "file" }
            .flatMap { classNamesUnder(File(it.toURI())) }
            .mapNotNull { tableObjectOrNull(it, loader) }
    }

    private fun classNamesUnder(root: File): List<String> =
        root
            .walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .map {
                "$TABLES_PACKAGE." +
                    it
                        .relativeTo(root)
                        .path
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
            }.toList()

    /** Kotlin `object`s expose their instance in the static `INSTANCE` field. */
    private fun tableObjectOrNull(
        className: String,
        loader: ClassLoader,
    ): Table? =
        runCatching { Class.forName(className, true, loader).getField("INSTANCE").get(null) }.getOrNull() as? Table
}
