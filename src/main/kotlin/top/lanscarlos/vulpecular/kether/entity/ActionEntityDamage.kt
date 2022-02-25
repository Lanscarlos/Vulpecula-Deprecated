package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecular.utils.iterator
import java.util.concurrent.CompletableFuture

/**
 *
 * entity damage 10
 * entity damage 10 by {damager} with {arg} {value}
 * entity damage 10 with {arg} {value}
 *
 * args:
 *      ignoreArmor, igArmor, iga, ia -> false 是否无视防御力
 *      ignoreImmunity, igIm, igm, im -> false 是否无视受伤间隔
 *      knockback, kb -> true 是否造成击退
 *
 * @author Lanscarlos
 * @since 2021-12-18 09:05
 * */
object ActionEntityDamage: ActionEntity() {

    override fun parse(reader: QuestReader): Any {
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
            while (true) {
                reader.mark()
                when (val arg = reader.expects(
                    "ignoreArmor", "igArmor", "iga", "ia",
                    "ignoreImmunreadery", "igIm", "igm", "im",
                    "knockback", "kb"
                )) {
                    "ignoreArmor", "igArmor", "iga", "ia" -> meta[2] = reader.next(ArgTypes.ACTION)
                    "ignoreImmunreadery", "igIm", "igm", "im" -> meta[3] = reader.next(ArgTypes.ACTION)
                    "knockback", "kb" -> meta[4] = reader.next(ArgTypes.ACTION)
                    else -> error("Unknown Arg Type \"$arg\" in \"entreadery damage\" Action!")
                }
            }
        } catch (e: Exception) {
            reader.reset()
        }
        return meta
    }

    override fun run(frame: ScriptFrame, entity: Entity, meta: Any?): Any? {
        val data = (meta as? Array<*>)?.map { (it as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get() } } ?: error("Illegal Meta in \"${this@ActionEntityDamage::class.simpleName}\" Action!")
        val damage = data[0]?.toString()?.toDouble() ?: error("Illegal value \"damage\" in \"${this@ActionEntityDamage::class.simpleName}\" Action!")
        val damager = data[1] as? Entity ?: (frame.script().sender as? Entity)
        val ignoreArmor = data[2]?.toString()?.equals("true", true) ?: false
        val ignoreImmunity = data[3]?.toString()?.equals("true", true) ?: false
        val knockback = data[4]?.toString()?.equals("true", true) ?: true

        if (entity !is Damageable) error("Unknown or Illegal value \"entity\" in \"${this@ActionEntityDamage::class.simpleName}\" Action!")
        if (damage < 0) error("Illegal value \"damage\" in \"${this@ActionEntityDamage::class.simpleName}\" Action!")

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