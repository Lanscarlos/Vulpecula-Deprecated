package top.lanscarlos.ranales.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.console
import taboolib.platform.util.sendLang
import top.lanscarlos.ranales.api.RanalesAPI.evalKether
import top.lanscarlos.ranales.api.RanalesAPI.runScript
import top.lanscarlos.ranales.internal.RanalesScript

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandScript {

    val workspace by lazy { RanalesScript.workspace }

    /*
    * rl script run/stop/list/reload/invoke
    * */
    val command = subCommand {
        literal("run", literal = run)
        literal("stop", literal = stop)
        literal("list", literal = list)
        literal("reload", literal = reload)
        literal("invoke", literal = invoke)
    }

    private val run: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "file") {
            suggestion<CommandSender> { _, _ ->
                workspace.scripts.map { it.value.id }
            }
            // viewer
            dynamic(commit = "viewer", optional = true) {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                // args
                dynamic(commit = "args", optional = true) {
                    execute<CommandSender> { sender, context, args ->
                        RanalesScript.runScript(
                            file = context.argument(-2),
                            sender = sender,
                            viewer = context.argument(-1),
                            *args.split(" ").toTypedArray()
                        )?.let {
                            sender.sendMessage(it)
                        }
                    }
                }
                execute<CommandSender> { sender, context, viewer ->
                    RanalesScript.runScript(
                        file = context.argument(-1),
                        sender = sender,
                        viewer = viewer
                    )?.let {
                        sender.sendMessage(it)
                    }
                }
            }
            execute<CommandSender> { sender, _, script ->
                RanalesScript.runScript(file = script, sender = sender)?.let {
                    sender.sendMessage(it)
                }
            }
        }
    }

    private val stop: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                workspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, id ->
                RanalesScript.stopScript(id = id, sender = sender)?.let {
                    sender.sendMessage(it)
                }
            }
        }
        execute<CommandSender> { sender, _, _ ->
            RanalesScript.stopScript(sender = sender)?.let {
                sender.sendMessage(it)
            }
        }
    }

    private val list: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang("Command-Script-List-All",
                workspace.scripts.map { it.value.id }.joinToString(", "),
                workspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    private val reload: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            RanalesScript.load().let {
                if (sender is Player) sender.sendMessage(it)
            }
        }
    }

    private val invoke: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "script") {
            execute<CommandSender> { sender, _, arg ->
                val result = arg.evalKether(sender).get()
                sender.sendMessage("§8[§3Ranales§8] §7Result: $result")
            }
        }
    }

}