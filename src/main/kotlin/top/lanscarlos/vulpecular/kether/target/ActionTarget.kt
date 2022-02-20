package top.lanscarlos.vulpecular.kether.target

import taboolib.module.kether.*
import top.lanscarlos.vulpecular.kether.target.filter.Filter
import top.lanscarlos.vulpecular.kether.target.selector.*

/**
 * @author Lanscarlos
 * @since 2021-12-17 15:07
 * */
object ActionTarget {

    @KetherParser(["target"], namespace = "vulpecular", shared = true)
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