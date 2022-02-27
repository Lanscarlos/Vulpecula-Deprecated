package top.lanscarlos.vulpecular.kether.target.selector

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecular.utils.iterator
import top.lanscarlos.vulpecular.utils.parse
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:35
 * */
abstract class ActionSelector {

    /**
     * 自行解析 Kether 语句
     * 并返回相对应的 ParsedAction 或 集合等
     * */
    abstract fun parse(reader: QuestReader, meta: Map<String, Any>): Pair<String, Any>?

    /**
     * 运行 Kether 动作
     * */
    abstract fun run(frame: ScriptFrame, meta: Map<String, Any>): Any?

    fun resolve(reader: QuestReader): ScriptAction<Any?> {
        val meta = reader.parse { it, meta -> parse(it, meta) }
        return object : ScriptAction<Any?>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
                return CompletableFuture.completedFuture(run(frame, meta)?.let { frame.iterator(it) })
            }
        }
    }

    companion object {

        private val selectors = mutableMapOf<String, ActionSelector>()

        fun getSelector(name: String): ActionSelector? {
            return selectors[name.lowercase()]
        }

        fun registerSelector(name: String, selector: ActionSelector, vararg alias: String): ActionSelector {
            selectors[name.lowercase()] = selector
            alias.forEach {
                selectors[it.lowercase()] = selector
            }
            return selector
        }

        fun Any.toLocation(): taboolib.common.util.Location? {
            return when(this) {
                is taboolib.common.util.Location -> this
                is org.bukkit.Location -> this.toProxyLocation()
                is Entity -> this.location.toProxyLocation()
                is ProxyPlayer -> this.location
                else -> null
            }
        }

        @KetherParser(["selector", "select", "sel"], namespace = "vulpecular", shared = true)
        fun parser() = scriptParser {
            val token = it.nextToken()
            getSelector(token)?.resolve(it) ?: error("Unknown selector type \"$token\"!")
        }

    }

}