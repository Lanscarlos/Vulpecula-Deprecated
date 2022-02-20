package top.lanscarlos.vulpecular.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import top.lanscarlos.vulpecular.Vulpecular
import top.lanscarlos.vulpecular.internal.VulpecularScript
import top.lanscarlos.vulpecular.internal.listener.ListenerHandler
import top.lanscarlos.vulpecular.internal.listener.ListenerRegistrator

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
            Vulpecular.reloadConfig().let {
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
            VulpecularScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val all: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            Vulpecular.reloadConfig().let {
                if (sender is Player) sender.sendMessage(it)
            }
            ListenerRegistrator.load(true).let {
                if (sender is Player) sender.sendMessage(it)
            }
            ListenerHandler.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
            VulpecularScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

}