package top.lanscarlos.ranales.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.platform.util.sendLang
import top.lanscarlos.ranales.RanalesAPI

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandScript {

    val workspace by lazy { RanalesAPI.workspace }

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
                    execute<CommandSender> { sender, context, argument ->
                        RanalesAPI.runScript(
                            file = context.argument(-2),
                            sender = sender,
                            viewer = context.argument(-1),
                            *argument.split(" ").toTypedArray()
                        )
                    }
                }
                execute<CommandSender> { sender, context, argument ->
                    RanalesAPI.runScript(
                        file = context.argument(-1),
                        sender = sender,
                        viewer = argument
                    )
                }
            }
            execute<CommandSender> { sender, _, argument ->
                RanalesAPI.runScript(file = argument, sender = sender)
            }
        }
    }

    private val stop: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                workspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, argument ->
                RanalesAPI.stopScript(id = argument, sender = sender)
            }
        }
        execute<CommandSender> { sender, _, _ ->
            RanalesAPI.stopScript(sender = sender)
        }
    }

    private val list: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang("command-script-list-all",
                workspace.scripts.map { it.value.id }.joinToString(", "),
                workspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    private val reload: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            RanalesAPI.loadScript()
            sender.sendLang("command-script-reload-all")
        }
    }

    private val invoke: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "script") {
            execute<CommandSender> { sender, _, argument ->
                val result = RanalesAPI.eval(argument, sender).get()
                sender.sendMessage("§8[§3Ranales§8] §7Result: $result")
            }
        }
    }

}