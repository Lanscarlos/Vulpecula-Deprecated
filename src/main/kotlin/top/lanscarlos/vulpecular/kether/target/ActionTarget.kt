package top.lanscarlos.vulpecular.kether.target

import taboolib.module.kether.*
import top.lanscarlos.vulpecular.kether.target.filter.ActionFilter
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
            "sel", "select", "selector" -> {
                val token = it.nextToken()
                ActionSelector.getSelector(token)?.resolve(it) ?: error("Unknown selector type \"$token\"!")
            }
            "filter" -> {
                val token = it.nextToken()
                ActionFilter.getFilter(token)?.resolve(it) ?: error("Unknown filter type \"$token\"!")
            }
            "foreach" -> ActionTargetForeach.resolve(it)
            else -> error("unknown target action type!")
        }
    }

}