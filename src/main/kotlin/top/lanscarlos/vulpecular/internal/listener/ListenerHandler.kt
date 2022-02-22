package top.lanscarlos.vulpecular.internal.listener

import org.bukkit.event.Event
import taboolib.common.platform.function.console
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecular.Vulpecular
import top.lanscarlos.vulpecular.api.VulpecularAPI.evalJS
import top.lanscarlos.vulpecular.api.VulpecularAPI.evalKether
import top.lanscarlos.vulpecular.api.VulpecularAPI.runScript
import top.lanscarlos.vulpecular.internal.debug.Debug.Companion.debug
import top.lanscarlos.vulpecular.utils.getFiles
import top.lanscarlos.vulpecular.utils.timing
import top.lanscarlos.vulpecular.utils.toConfig
import java.io.File

/**
 * 事件注册器
 *
 * @author Lanscarlos
 * @since 2022-2-12 15:35
 * */
class ListenerHandler(
    val id: String,
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
                if (!folder.exists()) {
                    listOf(
                        "#example.yml",
                        "def.yml"
                    ).forEach { releaseResourceFile("listener/handler/$it", true) }
                }
                handlers.clear()
                folder.getFiles().map { it.toConfig() }.forEach { config ->
                    config.getKeys(false).forEach { key ->
                        config.getConfigurationSection(key)?.let { section ->
                            val enable = section.getBoolean("enable", true)
                            val listeners = section.getStringList("listeners").mapNotNull {
                                ListenerRegistrator.get(it)
                            }.toSet().also { it.ifEmpty { return@let } }
                            val priority = section.getInt("priority", 0)
                            val namespace = section.getStringList("namespace")
                            val runPriority = section.getStringList("run-priority")
                            val javascript = section.getString("javascript") ?: section.getString("js")
                            val kether = section.getString("kether") ?: section.getString("ke") ?: section.getString("ks")
                            val scripts = section.getStringList("scripts")
                            handlers[key] = ListenerHandler(key, enable, listeners, priority, namespace, runPriority, javascript, kether, scripts)
                        }
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
    }
}