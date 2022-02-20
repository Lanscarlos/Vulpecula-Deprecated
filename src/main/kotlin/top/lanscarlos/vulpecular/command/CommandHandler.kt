package top.lanscarlos.vulpecular.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecular.internal.listener.ListenerHandler

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandHandler {

    val command = subCommand {
        literal("enable", literal = enable)
        literal("disable", literal = disable)
        literal("reload", literal = reload)
    }

    private val enable: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "id") {
            suggestion<ProxyCommandSender> { _, _ ->
                ListenerHandler.handlers.values.filter { !it.enable }.map { it.id }
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                ListenerHandler.get(arg)?.enable()
                sender.sendLang("Command-Handler-Enable", arg)
            }
        }
    }

    private val disable: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "id") {
            suggestion<ProxyCommandSender> { _, _ ->
                listOf("all", *ListenerHandler.handlers.values.filter { it.enable }.map { it.id }.toTypedArray())
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                if (arg == "all") {
                    ListenerHandler.handlers.values.forEach {
                        it.disable()
                    }
                    sender.sendLang("Command-Handler-Disable-All")
                } else {
                    ListenerHandler.get(arg)?.disable()
                    sender.sendLang("Command-Handler-Disable", arg)
                }
            }
        }
    }

    private val reload: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ListenerHandler.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

}