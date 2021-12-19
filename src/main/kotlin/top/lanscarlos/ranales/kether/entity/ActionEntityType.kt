package top.lanscarlos.ranales.kether.entity

import org.bukkit.entity.Entity
import taboolib.module.kether.ScriptFrame

/**
 * @author Lanscarlos
 * @since 2021-12-18 18:43
 * */
object ActionEntityType: ActionEntity() {

    override fun parameters(): List<String> {
        return listOf()
    }

    override fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any>): Any {
        return entity.type.name.lowercase()
    }

}