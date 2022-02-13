package top.lanscarlos.ranales.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.platform.util.sendLang
import top.lanscarlos.ranales.Ranales
import top.lanscarlos.ranales.internal.RanalesScript
import top.lanscarlos.ranales.internal.listener.ListenerHandler
import top.lanscarlos.ranales.internal.listener.ListenerRegistrator

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandReload {

    val command = subCommand {
        literal("config", literal = config)
        literal("registrator", literal = registrator)
        literal("handler", literal = handler)
        literal("all", literal = all)
    }

    private val config: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            Ranales.reloadConfig().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val registrator: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ListenerRegistrator.load(true).let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val handler: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            ListenerHandler.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val script: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            RanalesScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val all: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            Ranales.reloadConfig().let {
                if (sender is Player) sender.sendMessage(it)
            }
            ListenerRegistrator.load(true).let {
                if (sender is Player) sender.sendMessage(it)
            }
            ListenerHandler.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
            RanalesScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

}