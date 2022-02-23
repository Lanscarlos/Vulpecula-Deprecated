package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Entity
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame

/**
 *
 * entity type
 *
 * @author Lanscarlos
 * @since 2021-12-18 18:43
 * */
object ActionEntityType: ActionEntity() {

    override fun parse(reader: QuestReader): Any? = null

    override fun run(frame: ScriptFrame, entity: Entity, args: Any?): Any {
        return entity.type.name.lowercase()
    }

}