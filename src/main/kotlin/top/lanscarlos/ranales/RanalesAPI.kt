package top.lanscarlos.ranales

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import top.lanscarlos.ranales.command.CommandRanales
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-19 14:48
 * */
object RanalesAPI {

    val workspace by lazy {
        val directory = File(getDataFolder(), "script")
        if (!directory.exists()) {
            listOf(
                "example.ks"
            ).forEach { releaseResourceFile("script/$it", true) }
        }
        val workspace = Workspace(directory, namespace = listOf("ranales"))
        workspace.loadAll()
        workspace
    }

    fun eval(script: String, sender: Any?, namespace: List<String> = listOf("ranales"), data: Map<String, Any> = mapOf()): CompletableFuture<Any?> {
        return try {
            KetherShell.eval(script, sender = sender?.let { adaptCommandSender(it) }, namespace = namespace, context= {
                data.forEach { (k, v) -> set(k, v) }
            })
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, vararg args: String) {
        runScript(file, sender, viewer, args.mapIndexed { i, arg -> i.toString() to arg as Any }.toMap())
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, args: QuestContext.VarTable) {
        runScript(file, sender, viewer, args.keys().associateWith { args.get<Any>(it) })
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, args: Map<String, Any>) {
        val script = workspace.scripts[file]
        if (script != null) {
            val context = ScriptContext.create(script) {
                (viewer?.let { Bukkit.getPlayerExact(it) } ?: sender)?.let { this.sender = (it as? ProxyCommandSender) ?: adaptCommandSender(it) }
                args.forEach { (k, v) -> set(k, v) }
            }
            try {
                workspace.runScript(file, context)
            } catch (t: Throwable) {
                (sender as? ProxyCommandSender)?.sendLang("command-script-error", t.localizedMessage)
                t.printKetherErrorMessage()
            }
        }else {
            (sender as? ProxyCommandSender)?.sendLang("command-script-not-found")
        }
    }

    fun stopScript(id: String? = null, sender: Any? = null) {
        id?.let {
            val script = workspace.getRunningScript().filter { it.quest.id == id }
            if (script.isNotEmpty()) {
                script.forEach {
                    workspace.terminateScript(it)
                }
            } else {
                (sender as? ProxyCommandSender)?.sendLang("command-script-not-running")
            }
        } ?: let {
            workspace.getRunningScript().forEach { workspace.terminateScript(it) }
        }
    }

    fun loadScript() {
        workspace.cancelAll()
        workspace.loadAll()
    }

}