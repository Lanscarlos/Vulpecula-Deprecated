package top.lanscarlos.vulpecular.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader

/**
 * @author Lanscarlos
 * @since 2021-12-20 19:32
 * */
@CommandHeader(name = "vulpecular", aliases = ["vul", "vl"], permission = "vulpecular.command")
object CommandVulpecular {

    @CommandBody
    val reload = CommandReload.command

    @CommandBody
    val script = CommandScript.command

    @CommandBody
    val debug = CommandDebug.command

    @CommandBody
    val registrator = CommandRegistrator.command

    @CommandBody
    val handler = CommandHandler.command

}