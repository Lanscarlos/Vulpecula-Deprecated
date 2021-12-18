package top.lanscarlos.ranales.kether.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.warning
import taboolib.module.kether.ScriptFrame

object ActionEntityDamage: ActionEntity() {

    override fun parameters(): List<String> {
        return listOf("damage")
    }

    override fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any>): Any? {
        val damage = args["damage"]?.toString()?.toDouble() ?: return null
        if (entity is LivingEntity) {
            entity.damage(damage)
        }else {
            warning("Cannot damage entity exclude LivingEntity")
        }
        return null
    }

}