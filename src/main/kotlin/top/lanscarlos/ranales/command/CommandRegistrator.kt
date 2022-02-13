package top.lanscarlos.ranales.command

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang
import top.lanscarlos.ranales.internal.listener.ListenerRegistrator

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandRegistrator {

    val command = subCommand {
        literal("enable", literal = enable)
        literal("disable", literal = disable)
        literal("reload", literal = reload)
    }

    private val enable: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "id") {
            suggestion<ProxyCommandSender> { _, _ ->
                ListenerRegistrator.registrators.values.toSet().filter { !it.enable }.map { it.id }
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                ListenerRegistrator.get(arg)?.enable()
                sender.sendLang("Command-Registrator-Enable", arg)
            }
        }
    }

    private val disable: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "id") {
            suggestion<ProxyCommandSender> { _, _ ->
                listOf("all", *ListenerRegistrator.registrators.values.toSet().filter { it.enable }.map { it.id }.toTypedArray())
            }
            execute<ProxyCommandSender> { sender, _, arg ->
                if (arg == "all") {
                    ListenerRegistrator.registrators.values.toSet().forEach {
                        it.disable()
                    }
                    sender.sendLang("Command-Registrator-Disable-All")
                } else {
                    ListenerRegistrator.get(arg)?.disable()
                    sender.sendLang("Command-Registrator-Disable", arg)
                }
            }
        }
    }

    private val reload: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ListenerRegistrator.load(true).let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

}