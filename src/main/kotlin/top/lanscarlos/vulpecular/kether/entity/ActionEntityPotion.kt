package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Entity
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.kether.entity
 *
 * @author Lanscarlos
 * @since 2022-02-27 11:26
 */
@ActionExpansion(ActionExpansionType.ENTITY, "potion")
object ActionEntityPotion : ActionEntity() {

    /**
     * potion
     * */
    override fun parse(reader: QuestReader): Any {
        return when (val token = reader.expects(
            "location", "loc", "l",
            "radius", "range", "r"
        )) {
            "location", "loc", "l" -> "location" to reader.next(ArgTypes.ACTION)
            "radius", "range", "r" -> "radius" to reader.next(ArgTypes.ACTION)
            else -> error("Unknown parameter \"$token\"!")
        }
    }

    override fun run(frame: ScriptFrame, entity: Entity, meta: Any?): Any? {
        TODO("Not yet implemented")
    }

}