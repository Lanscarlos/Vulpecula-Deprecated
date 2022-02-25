package top.lanscarlos.vulpecular.kether.target.selector

import taboolib.library.kether.QuestReader
import taboolib.module.kether.*

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:57
 * */
object SelectorSender : ActionSelector() {

    override fun parse(reader: QuestReader): Pair<String, Any>? = null

    override fun run(frame: ScriptFrame, meta: Map<String, Any>): Any? {
        return frame.script().sender
    }

    @KetherParser(["@Sender", "@sender", "@Self", "@self"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { resolve(it) }
}