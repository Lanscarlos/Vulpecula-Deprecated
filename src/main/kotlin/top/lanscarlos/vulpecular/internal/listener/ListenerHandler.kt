package top.lanscarlos.vulpecular.internal.listener

import org.bukkit.event.Event
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecular.Vulpecular
import top.lanscarlos.vulpecular.api.VulpecularAPI.evalJS
import top.lanscarlos.vulpecular.api.VulpecularAPI.evalKether
import top.lanscarlos.vulpecular.api.VulpecularAPI.runScript
import top.lanscarlos.vulpecular.internal.debug.Debug.Companion.debug
import top.lanscarlos.vulpecular.utils.*
import java.io.File

/**
 * 事件注册器
 *
 * @author Lanscarlos
 * @since 2022-2-12 15:35
 * */
class ListenerHandler(
    val id: String,
    val file: File,
    var enable: Boolean,
    val listeners: Set<ListenerRegistrator>,
    val priority: Int,
    val namespace: List<String>,
    val runPriority: List<String>,
    val javascript: String?,
    val kether: String?,
    val scripts: List<String>
) {

    /**
     * 启动监听模块
     * */
    fun enable() {
        if (enable) return
        enable = true
    }

    /**
     * 关闭监听模块
     * */
    fun disable() {
        if (!enable) return
        enable = false
    }

    fun run(event: Event, args: Map<String, Any?>) {
        if (runPriority.isEmpty()) {
            runJavaScript(event, args)
            runKether(event, args)
            runScript(event, args)
            return
        }
        runPriority.forEach {
            when (it) {
                "javascript", "js" -> runJavaScript(event, args)
                "kether", "ke", "ks" -> runKether(event, args)
                "script" -> runScript(event, args)
            }
        }
    }

    fun runJavaScript(event: Event, args: Map<String, Any?>) {
        javascript?.let {
            try {
                it.evalJS(args, true)
            } catch (e: Exception) {
                debug("failed-to-run-javascript", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    fun runKether(event: Event, args: Map<String, Any?>) {
        debug("trying-to-run-kether", id, "$kether")
        kether?.let {
            try {
                it.evalKether(sender = args["player"], args = args, throws = true)
            } catch (e: Exception) {
                e.printKetherErrorMessage()
                debug("failed-to-run-kether", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    fun runScript(event: Event, args: Map<String, Any?>) {
        scripts.forEach {
            try {
                it.runScript(sender = args["player"], args = args, throws = true)
            } catch (e: Exception) {
                e.printKetherErrorMessage()
                debug("failed-to-run-script", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    companion object {

        private val folder by lazy {
            File(Vulpecular.plugin.dataFolder, "listener/handler")
        }

        val handlers = mutableMapOf<String, ListenerHandler>()

        fun get(id: String): ListenerHandler? {
            return handlers[id]
        }

        fun handle(listener: ListenerRegistrator, event: Event, args: Map<String, Any?>) {
            handlers.values.filter { listener in it.listeners }.sortedBy { it.priority }.also {
                debug("sorted-by-priority", it.map { handler -> handler.id })
            }.forEach {
                it.run(event, args)
            }
        }

        fun load(): String {
            return try {
                val start = timing()
                handlers.clear()
                folder.ifNotExists {
                    listOf(
                        "#example.yml",
                        "def.yml"
                    ).forEach { releaseResourceFile("listener/handler/$it", true) }
                }.getFiles().forEach { file ->
                    file.addWatcher {
                        // 添加文件监听器
                        val record = timing()
                        var count = 0
                        // 清除原有文件的处理器
                        handlers.filter { this == it.value.file }.forEach {
                            info("检测到移除 ${it.key}...")
                            handlers.remove(it.key)
                        }
                        this.toConfig().forEachSections { key, section ->
                            buildHandler(this, key, section)?.let {
                                handlers[key] = it
                                count += 1
                            }
                        }
                        console().sendLang("Handlers-Load-Automatic", this.name, count, timing(record))
                    }.toConfig().forEachSections { key, section ->
                        // 获取所有 section
                        buildHandler(file, key, section)?.let { handlers[key] = it }
                    }
                }
                console().asLangText("Handlers-Load-Succeeded", handlers.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                console().asLangText("Handlers-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

        fun buildHandler(file: File, key: String, section: ConfigurationSection): ListenerHandler? {
            val enable = section.getBoolean("enable", true)
            val listeners = section.getStringList("listeners").mapNotNull {
                ListenerRegistrator.get(it)
            }.toSet().ifEmpty { return null }
            val priority = section.getInt("priority", 0)
            val namespace = section.getStringList("namespace")
            val runPriority = section.getStringList("run-priority")
            val javascript = section.getString("javascript") ?: section.getString("js")
            val kether = section.getString("kether") ?: section.getString("ke") ?: section.getString("ks")
            val scripts = section.getStringList("scripts")
            return ListenerHandler(key, file, enable, listeners, priority, namespace, runPriority, javascript, kether, scripts)
        }
    }
}