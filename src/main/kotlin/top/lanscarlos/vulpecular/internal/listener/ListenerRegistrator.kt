package top.lanscarlos.vulpecular.internal.listener

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.*
import taboolib.common5.compileJS
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.getMap
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
import javax.script.SimpleBindings

/**
 * 事件注册器
 *
 * @author Lanscarlos
 * @since 2022-2-11 16:49
 * */
class ListenerRegistrator(
    val id: String,
    val file: File,
    var enable: Boolean,
    val event: Class<*>,
    val aliases: List<String>,
    val priority: EventPriority,
    val ignoreCancelled: Boolean,
    val cooldown: Int,
    val runPriority: List<String>,
    val preprocessing: Triple<String?, String?, List<String>>,
    val args: Map<String, String>
) {

    private var listener: ProxyListener? = null
    private var record = -1L

    /**
     * 启动监听模块
     * */
    fun enable() {
        if (enable && registered()) return
        enable = true
        register()
    }

    /**
     * 关闭监听模块
     * */
    fun disable() {
        if (!enable && !registered()) return
        enable = false
        unregister()
    }

    fun handle(event: Event) {
        if (ignoreCancelled && (event as? Cancellable)?.isCancelled == true) return

        // 冷却机制判断
        if (cooldown > 0 ) {
            val time = System.currentTimeMillis()
            if (record > 0 && time < record + cooldown) return
            record = time
        }

        debug("show-details-event", event)
        debug("show-details-event-name", event.eventName)
        val args = event.preprocessing(this.args.filter { it.key.endsWith("*") && it.value == "false" }.map { it.key }).also {
            this.args.forEach { (k, v) ->
                if (k.contains("*") || v == "true" || v == "false") return@forEach
                try {
                    v.compileJS()?.eval(SimpleBindings(it))?.let { r -> it[k] = r }
                } catch (e: Exception) {
                    debug("failed-to-handle-arg", id, k, event.eventName, e.localizedMessage, e.stackTrace)
                }
            }
        }
        val parameters = args.toMutableMap().also { it["args"] = args }
        // 预处理
        if (runPriority.isEmpty()) {
            runJavaScript(event, parameters)
            runKether(event, parameters)
            runScript(event, parameters)
        } else {
            runPriority.forEach {
                when (it) {
                    "javascript", "js" -> runJavaScript(event, parameters)
                    "kether", "ke", "ks" -> runKether(event, parameters)
                    "script" -> runScript(event, parameters)
                }
            }
        }
        // 事件已被取消
        if (ignoreCancelled && (event as? Cancellable)?.isCancelled == true) return
        ListenerHandler.handle(this, event, args)
    }

    fun runJavaScript(event: Event, args: Map<String, Any?>) {
        preprocessing.first?.let {
            try {
                it.evalJS(args, true)
            } catch (e: Exception) {
                debug("failed-to-run-javascript", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    fun runKether(event: Event, args: Map<String, Any?>) {
        preprocessing.second?.let {
            try {
                it.evalKether(sender = args["player"], args = args, throws = true)
            } catch (e: Exception) {
                e.printKetherErrorMessage()
                debug("failed-to-run-kether", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    fun runScript(event: Event, args: Map<String, Any?>) {
        preprocessing.third.forEach {
            try {
                it.runScript(sender = args["player"], args = args, throws = true)
            } catch (e: Exception) {
                e.printKetherErrorMessage()
                debug("failed-to-run-script", id, it, event.eventName, e.localizedMessage, e.stackTrace)
            }
        }
    }

    fun registered(): Boolean {
        return listener != null
    }

    fun register(): ProxyListener {
        if (registered()) return listener!!
        return registerBukkitListener(event, priority, ignoreCancelled) {
            (it as? Event)?.let { event -> handle(event) }
        }.also { listener = it }
    }

    fun unregister() {
        listener?.let { unregisterListener(it) }
    }

    companion object {

        private val folder by lazy {
            File(Vulpecular.plugin.dataFolder, "listener/registrators")
        }

        internal val registrators = mutableMapOf<String, ListenerRegistrator>()

        fun get(id: String): ListenerRegistrator? {
            return registrators[id]
        }

        fun load(register: Boolean = false): String {
            return try {
                val start = timing()
                ListenerHandler.handlers.clear()
                if (registrators.isNotEmpty()) {
                    registrators.values.toSet().filter { it.enable }.forEach {
                        it.unregister()
                    }
                    registrators.clear()
                }
                folder.ifNotExists {
                    listOf(
                        "#example.yml",
                        "def.yml"
                    ).forEach { releaseResourceFile("listener/registrators/$it", true) }
                }.getFiles().forEach { file ->
                    file.addWatcher {
                        val record = timing()
                        var count = 0
                        // 清除原有文件的处理器
                        registrators.values.toSet().filter { this == it.file }.forEach {
                            info("检测到移除 ${it.id}...")
                            it.unregister()
                            registrators.remove(it.id)
                        }
                        this.toConfig().forEachSections { key, section ->
                            buildRegistrator(this, key, section)?.let {
                                registrators[key] = it
                                count += 1
                            }
                        }
                        console().sendLang("Registrators-Load-Automatic", file.name, count, timing(record))
                    }.toConfig().forEachSections { key, section ->
                        buildRegistrator(file, key, section)?.let { registrator ->
                            registrators[key] = registrator
                            registrator.aliases.forEach { registrators[it] = registrator }
                            debug("handler-loaded", key)
                        }
                    }
                }
                if (register) registerAll()
                console().asLangText("Registrators-Load-Succeeded", registrators.values.toSet().size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                console().asLangText("Registrators-Load-Failed", e.localizedMessage).also {
                    console().sendMessage(it)
                }
            }
        }

        fun registerAll() {
            registrators.values.toSet().filter { it.enable }.forEach {
                debug("register-listener", it.id)
                it.register()
            }
        }

        @Awake(LifeCycle.ACTIVE)
        fun onActive() {
            debug("on-active-run")
            // 注册事件
            registerAll()
        }

        fun buildRegistrator(file: File, key: String, section: ConfigurationSection): ListenerRegistrator? {
            debug("handler-loading", key)
            val enable = section.getBoolean("enable", true)
            val event = section.getString("class")?.let event@{
                try {
                    debug("trying-to-load-event-class", key, it)
                    Class.forName(it)
                } catch (e: ClassNotFoundException) {
                    debug("event-class-not-found", key, it)
                    return null
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            } ?: let event@{
                debug("event-class-undefined", key)
                return null
            }
            val aliases = section.getStringList("aliases")
            val priority = when(section.getString("priority")?.uppercase()) {
                "MONITOR" -> EventPriority.MONITOR
                "LOWEST" -> EventPriority.LOWEST
                "LOW" -> EventPriority.LOW
                "HIGH" -> EventPriority.HIGH
                "HIGHEST" -> EventPriority.HIGHEST
                else -> EventPriority.NORMAL
            }
            val ignoreCancelled = section.getBoolean("ignore-cancelled", true)
            val cooldown = section.getInt("cooldown", -1)

            val runPriority = section.getStringList("preprocessing.run-priority")
            val preprocessing = Triple(
                section.getString("preprocessing.javascript") ?: section.getString("preprocessing.js"),
                section.getString("preprocessing.kether") ?: section.getString("preprocessing.ke") ?: section.getString("preprocessing.ks"),
                section.getStringList("preprocessing.scripts")
            )
            val args = section.getMap<String, Any>("args").mapNotNull { if (it.value.toString().isNotEmpty()) it.key to it.value.toString() else null }.toMap()
            debug("show-details-parameters", args)
            return ListenerRegistrator(key, file, enable, event, aliases, priority, ignoreCancelled, cooldown, runPriority, preprocessing, args)
        }
    }
}