package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import taboolib.common.platform.function.warning
import taboolib.module.kether.ScriptFrame

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:05
 * */
object ActionEntityDamage: ActionEntity() {

    override fun parameters(): List<String> {
        return listOf("damage")
    }

    override fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any>): Any? {
        val damage = args["damage"]?.toString()?.toDouble() ?: return null
        if (entity is Damageable) {
            entity.damage(damage)
        }else {
            warning("Cannot damage entity exclude LivingEntity")
        }
        return null
    }

}