package top.lanscarlos.vulpecular.kether.target.selector

import taboolib.module.kether.*

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:57
 * */
object SelectorSender : Selector() {
    override fun parameters(): List<String> {
        return listOf()
    }

    override fun run(frame: ScriptFrame, args: Map<String, Any>): Any? {
        return frame.script().sender
    }

    @KetherParser(["@Sender", "@sender", "@Self", "@self"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { parse(it) }
}