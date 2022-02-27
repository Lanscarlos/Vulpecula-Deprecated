package top.lanscarlos.vulpecular.kether.target.filter

import taboolib.library.kether.QuestReader
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecular.utils.iterator
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 11:20
 * */
abstract class ActionFilter {

    abstract fun parse(reader: QuestReader): Pair<String, Any>?

    abstract fun run(frame: ScriptFrame, targets: Collection<Any>, meta: Map<String, Any>): Collection<Any>

    fun resolve(reader: QuestReader): ScriptAction<Any> {
        val meta = mutableMapOf<String, Any>()
        try {
            while (true) {
                reader.mark()
                val parsed = parse(reader) ?: break
                if (parsed.first in meta) {
                    reader.reset()
                    break
                }
                meta[parsed.first] = parsed.second
            }
        } catch (e: Exception) {
            reader.reset()
        }
        return object : ScriptAction<Any>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                val targets = frame.iterator() as? Collection<*> ?: error("Illegal targets data!")
                return CompletableFuture.completedFuture(frame.iterator(run(frame, targets.mapNotNull { it }, meta)))
            }
        }
    }

    companion object {

        private val filters = mutableMapOf<String, ActionFilter>()

        fun getFilter(name: String): ActionFilter? {
            return filters[name.lowercase()]
        }

        fun registerSelector(name: String, filter: ActionFilter, vararg alias: String): ActionFilter {
            filters[name.lowercase()] = filter
            alias.forEach {
                filters[it.lowercase()] = filter
            }
            return filter
        }

        /**
         * filter {type} {args...}
         * */
        @KetherParser(["filter"], namespace = "vulpecular", shared = true)
        fun parser() = scriptParser {
            val token = it.nextToken()
            getFilter(token)?.resolve(it) ?: error("Unknown filter type \"$token\"!")
        }

    }

}