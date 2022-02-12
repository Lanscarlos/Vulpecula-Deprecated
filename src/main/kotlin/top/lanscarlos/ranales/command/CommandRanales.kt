package top.lanscarlos.ranales.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import top.lanscarlos.ranales.Ranales

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:32
 * */
@CommandHeader(name = "ranales", aliases = ["rl"], permission = "ranales.command")
object CommandRanales {

    @CommandBody
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            Ranales.reload()
            sender.sendLang("Plugin-Reloaded")
        }
    }

    @CommandBody
    val script = CommandScript.command

    @CommandBody
    val debug = CommandDebug.command

}