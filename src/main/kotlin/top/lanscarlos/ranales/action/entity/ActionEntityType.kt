package top.lanscarlos.ranales.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.module.kether.ScriptFrame

object ActionEntityType: ActionEntity() {

    override fun parameters(): List<String> {
        return listOf()
    }

    override fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any>): Any {
        return entity.type.name.lowercase()
    }

}