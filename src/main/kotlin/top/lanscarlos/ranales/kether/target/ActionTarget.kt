package top.lanscarlos.ranales.kether.target

import taboolib.library.kether.ArgTypes
import taboolib.module.kether.*
import top.lanscarlos.ranales.kether.target.filter.Filter
import top.lanscarlos.ranales.kether.target.selector.*
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-17 15:07
 * */
object ActionTarget {

    fun foreach() {

    }

    @KetherParser(["target"], namespace = "ranales", shared = true)
    fun parser() = scriptParser {
        when(it.expects(
            "sel", "select", "selector",
            "filter",
            "foreach"
        )) {
            "sel", "select", "selector" -> Selector.getSelector(it.nextToken())?.parse(it) ?: error("unknown target selector!")
            "filter" -> Filter.resolve(it)
            "foreach" -> TargetForeach.resolve(it)
            else -> error("unknown target action type!")
        }
    }

}