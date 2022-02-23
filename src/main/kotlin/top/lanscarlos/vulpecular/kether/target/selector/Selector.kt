package top.lanscarlos.vulpecular.kether.target.selector

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecular.kether.target.TargetForeach
import top.lanscarlos.vulpecular.kether.target.filter.Filter
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:35
 * */
abstract class Selector {

    abstract fun parameters(): List<String>

    abstract fun run(frame: ScriptFrame, args: Map<String, Any> = mapOf()): Any?

    fun parse(reader: QuestReader): ScriptAction<Any> {
        val actions = mutableListOf<Pair<String, ParsedAction<*>>>().apply {
            parameters().forEach {
                add(Pair(it, reader.next(ArgTypes.ACTION)))
            }
        }
        val args = mutableMapOf<String, Any>()

        val filters = try {
            reader.mark()
            reader.expect("filter")
            Filter.parser(reader)
        } catch (e: Exception) {
            reader.reset()
            null
        }

        val foreach = try {
            reader.mark()
            reader.expect("foreach")
            TargetForeach.parse(reader)
        } catch (e: Exception) {
            reader.reset()
            null
        }

        return object : ScriptAction<Any>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                val future = CompletableFuture<Any>()
                fun process() {
                    if (actions.isNotEmpty()) {
                        val action = actions.removeFirst()
                        frame.newFrame(action.second).run<Any>().thenApply { value ->
                            args[action.first] = value
                            process()
                        }
                    }else {
                        val targets = run(frame, args) ?: return
                        when(targets) {
                            is Collection<*> -> {
//                                future.complete(filter?.first?.call(frame, filter.second, targets.filterNotNull()) ?: targets)
                                future.complete(
                                    (filters?.let {
                                        Filter.resolve(
                                            frame,
                                            filters.map { it }.toMutableList(),
                                            targets.filterNotNull()
                                        )
                                    } ?: targets).also {
                                        foreach?.let { args -> TargetForeach.resolve(frame, args, it.filterNotNull()) }
                                    }
                                )
                            }
                            else -> future.complete(targets)
                        }
                    }
                }
                process()
                return future
            }
        }
    }

    companion object {

        private val selectors = mutableMapOf<String, Selector>()

        init {
            registerSelector("Self", SelectorSender)
            registerSelector("Player", SelectorPlayer)
            registerSelector("EntitiesInRadius", SelectorEntitiesInRadius, "EIR")
        }

        fun getSelector(name: String): Selector? {
            return selectors[name.lowercase()]
        }

        fun registerSelector(name: String, selector: Selector, vararg alias: String): Selector {
            selectors[name.lowercase()] = selector
            alias.forEach {
                selectors[it.lowercase()] = selector
            }
            return selector
        }

    }

}