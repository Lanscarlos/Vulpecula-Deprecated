package top.lanscarlos.ranales.kether.entity

import org.bukkit.entity.Entity
import taboolib.module.kether.ScriptFrame

object ActionEntityType: ActionEntity() {

    override fun parameters(): List<String> {
        return listOf()
    }

    override fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any>): Any {
        return entity.type.name.lowercase()
    }

}