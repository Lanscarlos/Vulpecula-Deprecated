package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType
import top.lanscarlos.vulpecular.utils.toBoolean
import top.lanscarlos.vulpecular.utils.toInt

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
     * potion &asd give type slow time 100
     * potion &abc
     * */
    override fun parse(reader: QuestReader): Any? {
        return when (val arg = reader.expects("add", "give", "remove", "clear")) {
            "add", "give" -> {
                val meta = arrayOfNulls<ParsedAction<*>>(6)
                meta[0] = reader.next(ArgTypes.ACTION)
                meta[1] = reader.next(ArgTypes.ACTION)
                meta[2] = reader.next(ArgTypes.ACTION)
                try {
                    reader.mark()
                    reader.expect("with")
                    meta[3] = reader.next(ArgTypes.ACTION)
                    meta[4] = reader.next(ArgTypes.ACTION)
                    meta[5] = reader.next(ArgTypes.ACTION)
                } catch (e: Exception) {
                    reader.reset()
                }
                meta
            }
            "remove" -> reader.next(ArgTypes.ACTION)
            "clear" -> null
            else -> error("Unknown Arg Type \"$arg\" in \"entreadery damage\" Action!")
        }
    }

    override fun run(frame: ScriptFrame, entity: Entity, meta: Any?): Any? {
        if (entity !is LivingEntity) error("Unknown or Illegal value \"entity\" in \"entity damage\" Action!")
        when (meta) {
            is Array<*> -> {
                // 给予药水
                meta.map { (it as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any?>().get() } }.let {
                    PotionEffect(
                        PotionEffectType.getByName(it[0]?.toString()?.uppercase() ?: "SLOW") ?: PotionEffectType.SLOW,
                        it[1].toInt(200),
                        it[2].toInt(0),
                        it[3].toBoolean(false),
                        it[4].toBoolean(false),
                        it[5].toBoolean(true)
                    )
                }.also { entity.addPotionEffect(it) }
            }
            is ParsedAction<*> -> {
                // remove 移除药水
                PotionEffectType.getByName(frame.newFrame(meta).run<Any?>().get()?.toString()?.uppercase() ?: "SLOW")?.also {
                    entity.removePotionEffect(it)
                }
            }
            else -> {
                // clear 清除药水
                entity.activePotionEffects.forEach {
                    entity.removePotionEffect(it.type)
                }
            }
        }
        return null
    }

    @KetherParser(["potion"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser {
        it.mark()
        val entity = if (it.nextToken() !in arrayOf("add", "give", "remove", "clear")) {
            it.reset()
            it.next(ArgTypes.ACTION)
        } else {
            it.reset()
            null
        }
        resolve(it, entity)
    }

}