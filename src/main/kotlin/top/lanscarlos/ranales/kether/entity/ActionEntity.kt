package top.lanscarlos.ranales.kether.entity

import org.bukkit.entity.Entity
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import java.util.concurrent.CompletableFuture

abstract class ActionEntity {

    abstract fun parameters(): List<String>

    abstract fun call(frame: ScriptFrame, entity: Entity, args: Map<String, Any> = mapOf()): Any?

    fun parse(reader: QuestReader, entity: ParsedAction<*>): ScriptAction<Any?> {
        val actions = mutableListOf<Pair<String, ParsedAction<*>>>().apply {
            parameters().forEach {
                add(Pair(it, reader.next(ArgTypes.ACTION)))
            }
        }
        val args = mutableMapOf<String, Any>()

        return object : ScriptAction<Any?>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
                val future = CompletableFuture<Any?>()
                fun process(entity: Entity) {
                    if (actions.isNotEmpty()) {
                        val action = actions.removeFirst()
                        frame.newFrame(action.second).run<Any>().thenApply { value ->
                            args[action.first] = value
                            process(entity)
                        }
                    }else {
                        future.complete(call(frame, entity, args))
                    }
                }
                frame.newFrame(entity).run<Any>().thenApply { entity ->
                    when(entity) {
                        is BukkitPlayer -> process(entity.player)
                        is Entity -> process(entity)
                        else -> error("Unknown entity type! ${entity::class.qualifiedName}")
                    }
                }
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

        @KetherParser(["entity"], namespace = "ranales", shared = true)
        fun parser() = scriptParser {
            val entity = it.next(ArgTypes.ACTION)
            getAction(it.nextToken())?.parse(it, entity) ?: error("Unknown type of entity action")
        }
    }
}