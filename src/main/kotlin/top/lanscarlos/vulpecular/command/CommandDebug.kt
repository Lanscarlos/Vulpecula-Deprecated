package top.lanscarlos.vulpecular.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecular.internal.debug.Debug

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:46
 * */
object CommandDebug {

    val command = subCommand {
        literal("on", literal = on)
        literal("off", literal = off)
        literal("remove", literal = remove)
        literal("clear", literal = clear)
    }

    private val on: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "file", optional = true) {
            suggestion<ProxyCommandSender> { _, _ ->
                Debug.resources.filter { it.key !in Debug.modules }.map { it.key }
            }
            execute<ProxyCommandSender> { sender, _, argument ->
                if (argument in Debug.modules) {
                    sender.sendLang("command-debug-on-already", argument)
                    return@execute
                }
                if (Debug.load(argument)) {
                    // 加载文件成功
                    Debug.enable = true
                    sender.sendLang("command-debug-on-success", argument)
                } else {
                    // 加载失败
                    sender.sendLang("command-debug-on-fail", argument)
                }
            }
        }
        execute<ProxyCommandSender> { sender, _, _ ->
            Debug.enable = true
            sender.sendLang("command-debug-on")
        }
    }

    private val off: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            Debug.enable = false
            sender.sendLang("command-debug-off")
        }
    }

    private val remove: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        dynamic(commit = "file", optional = false) {
            suggestion<ProxyCommandSender> { _, _ ->
                Debug.modules.map { it.key }
            }
            execute<ProxyCommandSender> { sender, _, argument ->
                Debug.remove(argument)
                sender.sendLang("command-debug-remove", argument)
            }
        }
    }

    private val clear: CommandBuilder.CommandComponentLiteral.() -> Unit = {
        execute<ProxyCommandSender> { sender, _, _ ->
            Debug.enable = false
            Debug.clear()
            sender.sendLang("command-debug-clear")
        }
    }

}