package top.lanscarlos.vulpecular.internal.debug

import taboolib.common.io.deepDelete
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getJarFile
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecular.Vulpecular
import top.lanscarlos.vulpecular.utils.deleteDeep
import top.lanscarlos.vulpecular.utils.getFiles
import java.io.File
import java.util.jar.JarFile

class Debug(
    val name: String,
    val file: File,
    config: Configuration
) {

    val prefix = (config.getString("prefix") ?: "&8[Vulpecular-Debug&8]&r ").colored()
    val debug = mutableMapOf<String, Pair<Int, Any>>()

    init {
        config.getConfigurationSection("debug")?.let { section ->
            section.getKeys(false).forEach { key ->
                section.getConfigurationSection(key)?.let it@{
                    val msg = if (it.isList("message")) {
                        it.getStringList("message").map { msg -> msg.colored() }
                    } else {
                        it.getString("message")?.colored()
                    } ?: return@it
                    debug[key] = it.getInt("level", 0) to msg
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
            if (it.first <= level) {
                if (it.second is List<*>) {
                    (it.second as List<*>).forEach { msg ->
                        console().sendMessage(prefix + msg.toString().replaceWithOrder(*args))
                    }
                } else {
                    console().sendMessage(prefix + it.second.toString().replaceWithOrder(*args))
                }
            }
        }
    }

    companion object {

        val folder by lazy {
            File(Vulpecular.plugin.dataFolder, "debug")
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

        /**
         * 关闭调试模块并释放资源
         * */
        fun remove(name: String) {
            modules[name]?.file?.deepDelete()
            modules.remove(name)
        }

        /**
         * 关闭所有调试模块并释放资源
         * */
        fun clear() {
            modules.clear()
            folder.deleteDeep()
        }

        fun load() {
            // 加载所有资源文件
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
            enable = Vulpecular.config.getBoolean("debug-setting.enable")
            level = Vulpecular.config.getInt("debug-setting.level")

            modules.clear()
            folder.getFiles().forEach {
                val name = it.name.substringBeforeLast('.')
                modules[name] = Debug(name, it, Configuration.loadFromFile(it))
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