package top.lanscarlos.ranales.kether

import taboolib.module.kether.*

object ActionRanales {

    @KetherParser(["ranales", "rl"], namespace = "ranales", shared = true)
    fun parser() = scriptParser {
        it.switch {
            case("script") { ActionScript.parse(it) }
        }
    }

}