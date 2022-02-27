package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.script
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType
import top.lanscarlos.vulpecular.utils.asDouble
import top.lanscarlos.vulpecular.utils.toBoolean
import top.lanscarlos.vulpecular.utils.toDouble

/**
 *
 * entity damage 10
 * entity damage 10 by {damager} with {args...}
 * entity damage 10 with {args...}
 *
 * args:
 *      ignoreArmor, igArmor, iga, ia -> false 是否无视防御力
 *      ignoreImmunity, igIm, igm, im -> false 是否无视受伤间隔
 *      knockback, kb -> true 是否造成击退
 *
 * @author Lanscarlos
 * @since 2021-12-18 09:05
 * */
@ActionExpansion(ActionExpansionType.ENTITY, "damage")
object ActionEntityDamage : ActionEntity() {

    override fun parse(reader: QuestReader): Array<ParsedAction<*>?> {
        val meta = arrayOfNulls<ParsedAction<*>>(5)
        meta[0] = reader.next(ArgTypes.ACTION)
        meta[1] = try {
            reader.mark()
            reader.expect("by")
            reader.next(ArgTypes.ACTION)
        } catch (e: Exception) {
            reader.reset()
            null
        }
        try {
            reader.mark()
            reader.expect("with")
            meta[2] = reader.next(ArgTypes.ACTION)
            meta[3] = reader.next(ArgTypes.ACTION)
            meta[4] = reader.next(ArgTypes.ACTION)
        } catch (e: Exception) {
            reader.reset()
        }
        return meta
    }

    override fun run(frame: ScriptFrame, entity: Entity, meta: Any?): Any? {
        if (entity !is Damageable) error("Unknown or Illegal value \"entity\" in \"entity damage\" Action!")

        val data = (meta as? Array<*>)?.map { (it as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get() } } ?: error("Illegal Meta in \"entity damage\" Action!")
        val damage = data[0].toDouble(0.0)
        val damager = data[1] as? Entity ?: (frame.script().sender as? Entity)
        val ignoreArmor = data[2].toBoolean(false)
        val ignoreImmunity = data[3].toBoolean(false)
        val knockback = data[4].toBoolean(true)

        if (damage < 0) error("Illegal value \"damage\" in \"entity damage\" Action!")

        if (entity is LivingEntity && ignoreImmunity) entity.noDamageTicks = 0
        if (ignoreArmor) {
            if (entity.health - damage <= 0.0) {
                entity.health = 1E-4
            } else {
                entity.health -= damage
            }
            if (knockback && damager != null) entity.damage(0.1, damager) else entity.damage(0.1)
        } else if (knockback && damager != null) entity.damage(damage, damager) else entity.damage(damage)
        return null
    }
}