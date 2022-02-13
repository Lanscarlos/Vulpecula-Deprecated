package top.lanscarlos.ranales

import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import taboolib.platform.BukkitPlugin
import top.lanscarlos.ranales.internal.RanalesScript
import top.lanscarlos.ranales.internal.debug.Debug
import top.lanscarlos.ranales.internal.listener.ListenerHandler
import top.lanscarlos.ranales.internal.listener.ListenerRegistrator
import top.lanscarlos.ranales.utils.timing

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
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlin:kotlin-script-util:1.5.10"
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlin:kotlin-compiler:1.5.10"
    ),
    RuntimeDependency(
        value = "commons-cli:commons-cli:1.4"
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
        RanalesScript.load()
        info("Successfully running ExamplePlugin!")
    }

    fun reloadConfig(): String {
        return try {
            val start = timing()
            config.reload()
            Debug.load()
            console().asLangText("Config-Reload-Succeeded", timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Config-Reload-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }
}