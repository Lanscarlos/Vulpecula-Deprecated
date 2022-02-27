package top.lanscarlos.vulpecular.kether.target.filter

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * @author Lanscarlos
 * @since 2021-12-18 19:56
 * */
@ActionExpansion(ActionExpansionType.FILTER, "foreach")
object FilterForeach: ActionFilter() {

    override fun parse(reader: QuestReader): Pair<String, Any> {
        return when (val token = reader.expects(
            "by", "then"
        )) {
            "by" -> "key" to reader.next(ArgTypes.ACTION)
            "then" -> "condition" to reader.next(ArgTypes.ACTION)
            else -> error("Unknown parameter \"$token\"!")
        }
    }

    override fun run(frame: ScriptFrame, targets: Collection<Any>, meta: Map<String, Any>): Collection<Any> {
        val key = meta["key"]?.toString() ?: "it"
        val condition = meta["condition"] as? ParsedAction<*> ?: error("Illegal condition data")
        return targets.filter {
            frame.variables()[key] = it
            frame.newFrame(condition).run<Any>().get().toString().equals("true", true)
        }
    }

}