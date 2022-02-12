package top.lanscarlos.ranales

import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import top.lanscarlos.ranales.internal.Debug
import top.lanscarlos.ranales.internal.lisener.ListenerHandler
import top.lanscarlos.ranales.internal.lisener.ListenerRegistrator

/**
 * @author Lanscarlos
 * @since 2021-11-29 17:20
 * */
@RuntimeDependencies(
    RuntimeDependency(
        value = "org.tabooproject.reflex:analyser:1.0.6",
        test = "org.tabooproject.reflex.ClassAnalyser",
        repository = "https://repo.tabooproject.org/repository/releases/",
        transitive = false
    ),
    RuntimeDependency(
        value = "org.tabooproject.reflex:reflex:1.0.6",
        test = "org.tabooproject.reflex.Reflex",
        repository = "https://repo.tabooproject.org/repository/releases/",
        transitive = false
    )
)
object Ranales : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    @Config("config.yml")
    lateinit var config: Configuration
        private set

    override fun onEnable() {
        Debug.load()
        ListenerRegistrator.load()
        ListenerHandler.load()
        info("Successfully running ExamplePlugin!")
    }

    fun reload() {
        config.reload()
        Debug.load()
        ListenerRegistrator.load(true)
        ListenerHandler.load()
    }
}