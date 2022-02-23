package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Entity
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import top.lanscarlos.vulpecular.utils.variable
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 08:51
 * */
abstract class ActionEntity {

    /**
     * 解析 Kether 语句
     * */
    abstract fun parse(reader: QuestReader): Any?

    /**
     * 运行 Kether 动作
     * */
    abstract fun run(frame: ScriptFrame, entity: Entity, meta: Any?): Any?

    fun resolve(reader: QuestReader, source: ParsedAction<*>?): ScriptAction<Any?> {
        val args = parse(reader)
        return object : ScriptAction<Any?>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
                val future = CompletableFuture<Any?>()
                val entity = source?.let {
                    frame.newFrame(it).run<Any?>().get()
                } ?: frame.variable("entity") ?: error("Cannot found entity target!")
                future.complete(
                    when(entity) {
                        is BukkitPlayer -> run(frame, entity.player, args)
                        is Entity -> run(frame, entity, args)
                        else -> error("Unknown entity type! ${entity::class.qualifiedName}")
                    }
                )
                return future
            }
        }
    }

    companion object {

        private val actions = mutableMapOf<String, ActionEntity>()

        init {
            registerActionEntity("damage", ActionEntityDamage)
            registerActionEntity("type", ActionEntityType)
        }

        fun getAction(name: String): ActionEntity? {
            return actions[name.lowercase()]
        }

        fun registerActionEntity(name: String, action: ActionEntity, vararg alias: String): ActionEntity {
            actions[name] = action
            alias.forEach {
                actions[it.lowercase()] = action
            }
            return action
        }

        @KetherParser(["entity"], namespace = "vulpecular", shared = true)
        fun ketherParser() = scriptParser {
            it.mark()
            val entity = it.nextToken().let { token ->
                if (token.startsWith('&') || token.startsWith("get")) {
                    it.reset()
                    it.next(ArgTypes.ACTION)
                } else {
                    it.reset()
                    null
                }
            }
            getAction(it.nextToken())?.resolve(it, entity) ?: error("Unknown type of entity action")
        }
    }
}