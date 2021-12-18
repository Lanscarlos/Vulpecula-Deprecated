package top.lanscarlos.ranales.kether.target.selector

import taboolib.module.kether.*

object SelectorSelf : Selector() {
    override fun parameters(): List<String> {
        return listOf()
    }

    override fun call(frame: ScriptFrame, args: Map<String, Any>): Any? {
        return frame.script().sender
    }

    @KetherParser(["@Self", "@self"], namespace = "ranales", shared = true)
    fun parser() = scriptParser { parse(it) }
}