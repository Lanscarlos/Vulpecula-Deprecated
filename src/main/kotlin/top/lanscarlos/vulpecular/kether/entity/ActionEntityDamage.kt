package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
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
        return mutableMapOf<String, Any>().apply {
            put("damage", reader.next(ArgTypes.ACTION))
            reader.mark()
            try {
                reader.expect("by")
                put("damager", reader.next(ArgTypes.ACTION))
            } catch (e: Exception) {
                reader.reset()
            }
            try {
                reader.mark()
                reader.expect("with")
                while (true) {
                    reader.mark()
                    put(
                        when (val arg = reader.expects(
                            "ignoreArmor", "igArmor", "iga", "ia",
                            "ignoreImmunity", "igIm", "igm", "im",
                            "knockback", "kb"
                        )) {
                            "ignoreArmor", "igArmor", "iga", "ia" -> "ignoreArmor"
                            "ignoreImmunity", "igIm", "igm", "im" -> "ignoreImmunity"
                            "knockback", "kb" -> "knockback"
                            else -> error("Unknown Arg Type \"$arg\" in \"entity damage\" Action!")
                        },
                        reader.next(ArgTypes.ACTION)
                    )
                }
            } catch (e: Exception) {
                reader.reset()
            }
        }
    }

    override fun run(frame: ScriptFrame, entity: Entity, args: Any?): Any? {
        val data = (args as? Map<*, *>)?.mapNotNull { (it.key as String) to frame.newFrame(it.value as ParsedAction<*>).run<Any?>().get() }?.toMap() ?: error("Illegal \"entity damage\" Action Data!")
        val damager = data["damager"] as? Entity ?: (frame.script().sender as? Entity)
        val damage = data["damage"]?.toString()?.toDouble() ?: return null
        val ignoreArmor = data["ignoreArmor"]?.toString()?.equals("true", true) ?: false
        val ignoreImmunity = data["ignoreImmunity"]?.toString()?.equals("true", true) ?: false
        val knockback = data["knockback"]?.toString()?.equals("true", true) ?: true

        if (damage < 0 || entity !is Damageable) return null
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

    @KetherParser(["damage"], namespace = "vulpecular", shared = true)
    fun ketherParser() = scriptParser {
        val args = parse(it)
        object : ScriptAction<Any?>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
                val future = CompletableFuture<Any?>()
                val entity = frame.variables().get<Any?>("@Before").orElseGet {
                    frame.variables().get<Any?>("entity")
                } ?: return future
                future.complete(
                    when(entity) {
                        is BukkitPlayer -> run(frame, entity.player, args)
                        is Entity -> run(frame, entity, args)
                        else -> error("Unknown entity type! ${entity::class.qualifiedName}")
                    }
                )
                entity to ""
                return future
            }
        }
    }

}