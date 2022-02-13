package top.lanscarlos.ranales.internal

import org.bukkit.Bukkit
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.kether.QuestContext
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.Workspace
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.ranales.api.RanalesAPI.runScript
import top.lanscarlos.ranales.internal.listener.ListenerHandler
import top.lanscarlos.ranales.utils.timing
import java.io.File
import java.util.concurrent.CompletableFuture

object RanalesScript {

    val workspace by lazy {
        val directory = File(getDataFolder(), "script")
        if (!directory.exists()) {
            listOf(
                "example.ks"
            ).forEach { releaseResourceFile("script/$it", true) }
        }
        Workspace(directory, namespace = listOf("ranales"))
    }

    fun eval(script: String, sender: Any?, namespace: List<String> = listOf("ranales"), args: Map<String, Any?>? = null, throws: Boolean = false): CompletableFuture<Any?> {
        val func = {
            KetherShell.eval(script, sender = sender?.let { adaptCommandSender(it) }, namespace = namespace, context= {
                args?.forEach { (k, v) -> set(k, v) }
            })
        }
        return if (throws) func()
        else try {
            func()
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        }
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, vararg args: String, throws: Boolean = false): String? {
        return runScript(file, sender, viewer, args.mapIndexed { i, arg -> i.toString() to arg as Any }.toMap(), throws)
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, args: QuestContext.VarTable, throws: Boolean = false): String? {
        val data = args.keys().filter { args.get<Any>(it).isPresent }.associateWith { args.get<Any>(it).get() }
        return runScript(file, sender, viewer, data, throws)
    }

    fun runScript(file: String, sender: Any? = null, viewer: String? = null, args: Map<String, Any?>? = null, throws: Boolean = false): String? {
        val script = workspace.scripts[file]
        if (script != null) {
            val context = ScriptContext.create(script) {
                (viewer?.let { Bukkit.getPlayerExact(it) } ?: sender)?.let { this.sender = (it as? ProxyCommandSender) ?: adaptCommandSender(it) }
                args?.forEach { (k, v) -> set(k, v) }
            }
            if (throws) {
                workspace.runScript(file, context)
            } else {
                try {
                    workspace.runScript(file, context)
                } catch (t: Throwable) {
                    t.printKetherErrorMessage()
                    return (sender as? ProxyCommandSender ?: console()).asLangText("Command-Script-Error", t.localizedMessage)
                }
            }
        }else {
            return (sender as? ProxyCommandSender ?: console()).asLangText("Command-Script-Not-Found")
        }
        return null
    }

    fun stopScript(id: String? = null, sender: Any? = null): String? {
        id?.let {
            val script = workspace.getRunningScript().filter { it.quest.id == id }
            if (script.isNotEmpty()) {
                script.forEach {
                    workspace.terminateScript(it)
                }
            } else {
                return (sender as? ProxyCommandSender ?: console()).asLangText("Command-Script-Not-Running")
            }
        } ?: let {
            workspace.getRunningScript().forEach { workspace.terminateScript(it) }
        }
        return null
    }

    fun load(): String {
        return try {
            val start = timing()
            workspace.cancelAll()
            workspace.loadAll()
            console().asLangText("Scripts-Load-Succeeded", ListenerHandler.handlers.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Scripts-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }
}