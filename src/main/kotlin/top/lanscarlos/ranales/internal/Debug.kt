package top.lanscarlos.ranales.internal

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getJarFile
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import taboolib.module.configuration.util.getStringColored
import top.lanscarlos.ranales.Ranales
import top.lanscarlos.ranales.internal.lisener.ListenerRegistrator
import top.lanscarlos.ranales.utils.deleteDeep
import top.lanscarlos.ranales.utils.getFiles
import java.io.File
import java.util.jar.JarFile

class Debug(
    val name: String,
    val file: File,
    config: Configuration
) {

    val prefix = (config.getString("prefix") ?: "&8[Ranales-Debug&8]&r ").colored()
    val debug = mutableMapOf<String, Pair<Int, String>>()

    init {
        config.getConfigurationSection("debug")?.let { section ->
            section.getKeys(false).forEach { key ->
                section.getConfigurationSection(key)?.let it@{
                    val msg = if (it.isList("message")) {
                        it.getStringList("message").joinToString(separator = "\n$prefix")
                    } else {
                        it.getString("message")
                    } ?: return@it
                    debug[key] = it.getInt("level", 0) to msg.colored()
                }
            }
        }
    }

    fun debug(level: Int, id: String, vararg args: Any) {
        if (!file.exists()) {
            modules.remove(name)
            return
        }
        debug[id]?.let {
            if (it.first <= level) console().sendMessage(prefix + it.second.replaceWithOrder(*args))
        }
    }

    companion object {

        val folder by lazy {
            File(Ranales.plugin.dataFolder, "debug")
        }

        internal var enable = false
        internal var level = 0
        internal val modules = mutableMapOf<String, Debug>()
        internal val resources = mutableMapOf<String, String>()

        fun load(name: String): Boolean {
            resources[name]?.let {
                val file = releaseResourceFile(it, false)
                modules[name] = Debug(name, file, Configuration.loadFromFile(file))
                return true
            } ?: return false
        }

        fun clear() {
            modules.clear()
            folder.deleteDeep()
        }

        fun load() {

            if (resources.isEmpty()) {
                JarFile(getJarFile()).use { jar ->
                    jar.entries().iterator().forEachRemaining {
                        if (it.name.startsWith("debug/") && it.name.endsWith(".yml")) {
                            // debug 文件
                            resources[it.name.substringAfter("debug/").substringBefore(".yml")] = it.name
                        }
                    }
                }
            }

            enable = Ranales.config.getBoolean("debug-setting.enable")
            level = Ranales.config.getInt("debug-setting.level")

            modules.clear()
            if (folder.exists()) {
                getFiles(folder).forEach {
                    val name = it.name.substringBeforeLast('.')
                    modules[name] = Debug(name, it, Configuration.loadFromFile(it))
                }
            }
        }

        /**
         * 调试
         * @param debug 调试信息 id
         */
        fun Any.debug(debug: String, vararg args: Any) {
            val module = this::class.java.name.let { it.substring(it.indexOfLast { c -> c == '.' } + 1) }
            modules[module]?.debug(level, debug, *args)
        }
    }
}