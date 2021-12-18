package top.lanscarlos.ranales.action.target.filter

import taboolib.common.platform.function.info
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.QuestReader
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.ranales.action.target.selector.Selector
import top.lanscarlos.ranales.action.target.selector.SelectorEntitiesInRadius
import top.lanscarlos.ranales.action.target.selector.SelectorPlayer
import top.lanscarlos.ranales.action.target.selector.SelectorSelf
import java.util.concurrent.CompletableFuture

abstract class Filter {

    abstract fun parse(reader: QuestReader): Any

    abstract fun call(frame: ScriptFrame, arg: Any, targets: Collection<Any>): Collection<Any>

    companion object {

        private val filters = mutableMapOf<String, Filter>()

        init {
            registerSelector("Type", FilterEntityType)
            registerSelector("Foreach", FilterForeach)
        }

        fun getFilter(name: String): Filter? {
            return filters[name.lowercase()]
        }

        fun registerSelector(name: String, filter: Filter, vararg alias: String): Filter {
            filters[name.lowercase()] = filter
            alias.forEach {
                filters[it.lowercase()] = filter
            }
            return filter
        }

        /**
         * 此 filter 语句 容易与上一行的 target sel 起冲突
         * */
//        @KetherParser(["filter"], namespace = "ranales", shared = true)
//        fun parser() = scriptParser {
//            val targets = it.next(ArgTypes.ACTION)
//            val filter = Filter.getFilter(it.nextToken())?.let { filter -> Pair(filter, filter.parse(it)) } ?: error("Unknown filter type!")
//            object : ScriptAction<Any>() {
//                override fun run(frame: ScriptFrame): CompletableFuture<Any> {
//                    val future = CompletableFuture<Any>()
//                    frame.newFrame(targets).run<Any>().thenApply { targets ->
//                        when(targets) {
//                            is Collection<*> -> future.complete(filter.first.call(frame, filter.second, targets.filterNotNull()))
//                            else -> future.complete(targets)
//                        }
//                    }
//                    return future
//                }
//            }
//        }

    }

}