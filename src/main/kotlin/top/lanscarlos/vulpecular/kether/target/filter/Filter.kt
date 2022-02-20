package top.lanscarlos.vulpecular.kether.target.filter

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 11:20
 * */
abstract class Filter {

    abstract fun parse(reader: QuestReader): Any

    abstract fun call(frame: ScriptFrame, arg: Any, targets: Collection<Any>, func: (targets: Collection<Any>) -> Collection<Any>): Collection<Any>

    companion object {

        private val filters = mutableMapOf<String, Filter>()

        init {
            registerSelector("IsInstance", FilterEntityIsInstance, "Instance", "Inst")
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

        fun parser(reader: QuestReader): List<Pair<Filter, Any>> {
            return mutableListOf<Pair<Filter, Any>>().also {
                val name = reader.nextToken()
                it += getFilter(name)?.let { filter -> Pair(filter, filter.parse(reader)) } ?: error("Unknown target filter: $name!")
                fun process() {
                    try {
                        reader.mark()
                        reader.expect("filter")
                        val token = reader.nextToken()
                        it += getFilter(token)?.let { filter -> Pair(filter, filter.parse(reader)) } ?: error("Unknown target filter: $token!")
                        process()
                    }catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }catch (e: Exception) {
                        reader.reset()
                    }
                }
                process()
            }
        }

        fun resolve(frame: ScriptFrame, filters: MutableList<Pair<Filter, Any>>, targets: Collection<Any>): Collection<Any> {
            val filter = filters.removeFirst()
            return filter.first.call(frame, filter.second, targets) { collection ->
                if (filters.isNotEmpty()) {
                    resolve(frame, filters, collection)
                }else {
                    collection
                }
            }
        }

        fun resolve(reader: QuestReader): ScriptAction<Any> {
            val targets = reader.next(ArgTypes.ACTION)
            val filters = parser(reader)
            return object : ScriptAction<Any>() {
                override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                    val future = CompletableFuture<Any>()
                    frame.newFrame(targets).run<Any>().thenApply { targets ->
                        when(targets) {
                            is Collection<*> -> {
                                future.complete(
                                    resolve(
                                        frame,
                                        filters.map { it }.toMutableList(),
                                        targets.filterNotNull()
                                    )
                                )
                            }
                            else -> future.complete(targets)
                        }
                    }
                    return future
                }
            }
        }

        /**
         * 此 filter 语句 容易与上一行的 target sel 起冲突
         * */
//        @KetherParser(["filter"], namespace = "vulpecular", shared = true)
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