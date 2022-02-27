package top.lanscarlos.vulpecular.kether.entity

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.info
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation
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

        fun isAction(name: String): Boolean {
            return name in actions
        }

        fun getAction(name: String): ActionEntity? {
            return actions[name.lowercase()]
        }

        fun registerActionEntity(name: String, action: ActionEntity, vararg alias: String): ActionEntity {
            info("registerActionEntity $name")
            actions[name] = action
            alias.forEach {
                info("registerActionEntity $name alias -> $it")
                actions[it.lowercase()] = action
            }
            return action
        }

        /**
         * entity type
         * entity {entity} type
         * */
        @KetherParser(["entity"], namespace = "vulpecular", shared = true)
        fun ketherParser() = scriptParser {
            it.mark()
            var token = it.nextToken()
            val entity = if (!isAction(token)) {
                it.reset()
                it.next(ArgTypes.ACTION).apply {
                    token = it.nextToken()
                }
            } else null
            getAction(token)?.resolve(it, entity) ?: error("Unknown type \"$token\" of entity action")
        }
    }
}