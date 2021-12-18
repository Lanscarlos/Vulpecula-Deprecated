package top.lanscarlos.ranales.kether.target.filter

import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame

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