package top.lanscarlos.vulpecular.kether

import taboolib.module.kether.*

object ActionVulpecular {

    @KetherParser(["vulpecular", "vl"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser {
        it.switch {
            case("script") { ActionScript.parse(it) }
        }
    }

}