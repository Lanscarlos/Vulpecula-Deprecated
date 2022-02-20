package top.lanscarlos.vulpecular.kether.target.selector

import taboolib.module.kether.*

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:57
 * */
object SelectorSelf : Selector() {
    override fun parameters(): List<String> {
        return listOf()
    }

    override fun call(frame: ScriptFrame, args: Map<String, Any>): Any? {
        return frame.script().sender
    }

    @KetherParser(["@Self", "@self"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { parse(it) }
}