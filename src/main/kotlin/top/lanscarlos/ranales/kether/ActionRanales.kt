package top.lanscarlos.ranales.kether

import taboolib.common.platform.function.adaptCommandSender
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

object ActionRanales {

    @KetherParser(["ranales"], namespace = "ranales", shared = true)
    fun parser() = scriptParser {
        it.switch {
            case("script") { ActionScript.parse(it) }
        }
    }

}