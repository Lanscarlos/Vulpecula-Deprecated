package top.lanscarlos.ranales.internal.lisener

import taboolib.common.platform.function.console
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.Coerce
import taboolib.common5.compileJS
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import top.lanscarlos.ranales.Ranales
import top.lanscarlos.ranales.RanalesAPI
import top.lanscarlos.ranales.internal.Debug.Companion.debug
import top.lanscarlos.ranales.utils.getFiles
import java.io.File
import javax.script.SimpleBindings

/**
 * 事件注册器
 *
 * @author Lanscarlos
 * @since 2022-2-12 15:35
 * */
class ListenerHandler(
    val id: String,
    val listeners: Set<ListenerRegistrator>,
    val priority: Int,
    val namespace: List<String>,
    val runPriority: List<String>,
    val javascript: String?,
    val kether: String?,
    val scripts: List<String>
) {
    fun run(args: Map<String, Any?>) {
        if (runPriority.isEmpty()) {
            runJavaScript(args)
            runKether(args)
            runScript(args)
            return
        }
        runPriority.forEach {
            when (it) {
                "javascript", "js" -> runJavaScript(args)
                "kether", "ke", "ks" -> runKether(args)
                "script" -> runScript(args)
            }
        }
    }

    fun runJavaScript(args: Map<String, Any?>) {
        javascript?.compileJS()?.eval(SimpleBindings(args))
    }

    fun runKether(args: Map<String, Any?>) {
        debug("trying-to-run-kether", id, "$kether")
        kether?.let {
            debug("running-kether")
            debug("show-kether", it, args)
            RanalesAPI.eval(it, sender = args["player"], args = args)
        }
    }

    fun runScript(args: Map<String, Any?>) {
        scripts.forEach {
            RanalesAPI.runScript(file = it, sender = args["player"], args = args)
        }
    }

    companion object {

        private val folder by lazy {
            File(Ranales.plugin.dataFolder, "listener/handler")
        }

        val handlers = mutableMapOf<String, ListenerHandler>()

        fun handle(listener: ListenerRegistrator, args: Map<String, Any?>) {
            handlers.values.filter { listener in it.listeners }.sortedBy { it.priority }.also {
                debug("sorted-by-priority", it.map { handler -> handler.id })
            }.forEach {
                it.run(args)
            }
        }

        fun load() {
            try {
                val start = System.nanoTime()
                if (!folder.exists()) {
                    listOf(
                        "def.yml"
                    ).forEach { releaseResourceFile("listener/handler/$it", true) }
                }
                handlers.clear()
                getFiles(folder).map { Configuration.loadFromFile(it) }.forEach { config ->
                    config.getKeys(false).forEach { key ->
                        config.getConfigurationSection(key)?.let { section ->
                            if (!section.getBoolean("enable", true)) return@let
                            val listeners = section.getStringList("listeners").mapNotNull {
                                ListenerRegistrator.getRegistrator(it)
                            }.toSet().also { it.ifEmpty { return@let } }
                            val priority = section.getInt("priority", 0)
                            val namespace = section.getStringList("namespace")
                            val runPriority = section.getStringList("run-priority")
                            val javascript = section.getString("javascript") ?: config.getString("js")
                            val kether = section.getString("kether") ?: config.getString("ke") ?: config.getString("ks")
                            val scripts = section.getStringList("scripts")
                            handlers[key] = ListenerHandler(key, listeners, priority, namespace, runPriority, javascript, kether, scripts)
                        }
                    }
                }
                console().sendLang("Handlers-Loaded", handlers.size, Coerce.format((System.nanoTime() - start).div(1000000.0)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}