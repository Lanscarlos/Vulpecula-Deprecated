package top.lanscarlos.vulpecular.kether.target.filter

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * @author Lanscarlos
 * @since 2021-12-18 15:36
 * */
@ActionExpansion(ActionExpansionType.FILTER, "type")
object FilterEntityType: ActionFilter() {

    override fun parse(reader: QuestReader, meta: Map<String, Any>): Pair<String, Any> {
        return "types" to reader.next(ArgTypes.listOf(ArgTypes.ACTION))
    }

    override fun run(frame: ScriptFrame, targets: Collection<Any>, meta: Map<String, Any>): Collection<Any> {
        val legal = mutableSetOf<String>()
        val illegal = mutableSetOf<String>()
        (meta["types"] as? List<*>)?.forEach {
            val type = (it as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get().toString() } ?: return@forEach
            if (type.startsWith("!")) {
                illegal += type.substring(1).lowercase()
            } else {
                legal += type.lowercase()
            }
        }
        return targets.filter {
            val type = when(it) {
                is ProxyPlayer -> "ProxyPlayer".lowercase()
                is Entity -> it.type.name.lowercase()
                else -> it::class.simpleName?.lowercase() ?: return@filter false
            }
            type !in illegal && ( if (legal.isNotEmpty()) type in legal else true )
        }
    }
}