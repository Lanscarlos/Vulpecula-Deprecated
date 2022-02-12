package top.lanscarlos.ranales.internal.lisener

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.hanging.HangingEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.vehicle.VehicleEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.event.world.WorldEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.*
import taboolib.common5.Coerce
import taboolib.common5.compileJS
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
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
 * @since 2022-2-11 16:49
 * */
class ListenerRegistrator(
    val id: String,
    val event: Class<*>,
    val priority: EventPriority,
    val ignoreCancelled: Boolean,
    val runPriority: List<String>,
    val preprocessing: Triple<String?, String?, List<String>>,
    val parameters: Map<String, String>
) {

    private var listener: ProxyListener? = null

    fun handle(event: Event) {
        if (ignoreCancelled && (event as? Cancellable)?.isCancelled == true) return
        val args = mutableMapOf<String, Any?>("evevt" to event)
        when (event) {
            is BlockEvent -> args["block"] = event.block
            is EntityEvent -> args["entity"] = event.entity
            is HangingEvent -> args["entity"] = event.entity
            is InventoryEvent -> args["inventory"] = event.inventory
            is PlayerEvent -> args["player"] = event.player
            is VehicleEvent -> args["vehicle"] = event.vehicle
            is WeatherEvent -> args["player"] = event.world
            is WorldEvent -> args["world"] = event.world
        }
        val parameters = args.toMutableMap().also { it["args"] = args }
        // 预处理
        if (runPriority.isEmpty()) {
            runJavaScript(parameters)
            runKether(parameters)
            runScript(parameters)
        } else {
            runPriority.forEach {
                when (it) {
                    "javascript", "js" -> runJavaScript(parameters)
                    "kether", "ke", "ks" -> runKether(parameters)
                    "script" -> runScript(parameters)
                }
            }
        }
        // 事件已被取消
        if ((event as? Cancellable)?.isCancelled == true) return
        ListenerHandler.handle(this, args)
    }

    fun runJavaScript(args: Map<String, Any?>) {
        preprocessing.first?.compileJS()?.eval(SimpleBindings(args))
    }

    fun runKether(args: Map<String, Any?>) {
        preprocessing.second?.let {
            RanalesAPI.eval(it, sender = args["player"], args = args)
        }
    }

    fun runScript(args: Map<String, Any?>) {
        preprocessing.third.forEach {
            RanalesAPI.runScript(file = it, sender = args["player"], args = args)
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
            File(Ranales.plugin.dataFolder, "listener/registrators")
        }

        private val registrators = mutableMapOf<String, ListenerRegistrator>()

        fun getRegistrator(id: String): ListenerRegistrator? {
            return registrators[id]
        }

        fun load(register: Boolean = false) {
            try {
                val start = System.nanoTime()
                ListenerHandler.handlers.clear()
                if (!folder.exists()) {
                    listOf(
                        "def.yml"
                    ).forEach { releaseResourceFile("listener/registrators/$it", true) }
                }
                if (registrators.isNotEmpty()) {
                    registrators.values.toSet().forEach {
                        it.unregister()
                    }
                    registrators.clear()
                }
                getFiles(folder).map { Configuration.loadFromFile(it) }.forEach { config ->
                    config.getKeys(false).forEach { key ->
                        config.getConfigurationSection(key)?.let { section ->
                            debug("handler-loading", key)
                            if (!section.getBoolean("enable", true)) return@let
                            debug("handler-enable", key)
                            val event = Class.forName(section.getString("class"))
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

                            val runPriority = section.getStringList("preprocessing.run-priority")
                            val preprocessing = Triple(
                                section.getString("preprocessing.javascript") ?: section.getString("preprocessing.js"),
                                section.getString("preprocessing.kether") ?: section.getString("preprocessing.ke") ?: section.getString("preprocessing.ks"),
                                section.getStringList("preprocessing.scripts")
                            )
                            val parameters = section.getMap<String, String>("parameters")
                            val registrator = ListenerRegistrator(key, event, priority, ignoreCancelled, runPriority, preprocessing, parameters)
                            registrators[key] = registrator
                            aliases.forEach { registrators[it] = registrator }
                            debug("handler-loaded", key)
                        }
                    }
                }
                console().sendLang("Registrators-Loaded", registrators.values.toSet().size, Coerce.format((System.nanoTime() - start).div(1000000.0)))
                if (register) registerAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun registerAll() {
            registrators.values.toSet().forEach {
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
    }
}