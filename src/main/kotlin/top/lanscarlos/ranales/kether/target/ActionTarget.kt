package top.lanscarlos.ranales.kether.target

import taboolib.library.kether.ArgTypes
import taboolib.module.kether.*
import top.lanscarlos.ranales.kether.target.filter.Filter
import top.lanscarlos.ranales.kether.target.selector.*
import java.util.concurrent.CompletableFuture

object ActionTarget {

    @KetherParser(["target"], namespace = "ranales", shared = true)
    fun parser() = scriptParser {
        when(it.expects(
            "sel", "select", "selector",
            "filter"
        )) {
            "sel", "select", "selector" -> Selector.getSelector(it.nextToken())?.parse(it) ?: error("unknown target selector!")
            "filter" -> {
                val targets = it.next(ArgTypes.ACTION)
                val filter = Filter.getFilter(it.nextToken())?.let { filter -> Pair(filter, filter.parse(it)) } ?: error("unknown target filter!")
                object : ScriptAction<Any>() {
                    override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                        val future = CompletableFuture<Any>()
                        frame.newFrame(targets).run<Any>().thenApply { targets ->
                            when(targets) {
                                is Collection<*> -> future.complete(filter.first.call(frame, filter.second, targets.filterNotNull()))
                                else -> future.complete(targets)
                            }
                        }
                        return future
                    }
                }
            }
            else -> error("unknown type!")
        }
    }
}