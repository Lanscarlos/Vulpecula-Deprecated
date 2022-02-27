package top.lanscarlos.vulpecular.utils

import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import java.lang.Exception

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.utils
 *
 * @author Lanscarlos
 * @since 2022-02-23 17:35
 */

fun QuestContext.Frame.variable(key: String): Any? {
    return variables().get<Any?>(key).let { if (it.isPresent) it.get() else null }
}

fun QuestContext.Frame.variable(key: String, value: Any?): Any? {
    variables()[key] = value
    return value
}

fun QuestContext.Frame.iterator(): Any? {
    return variable("@Iterator")
}

fun QuestContext.Frame.iterator(iterator: Any): Any {
    return variable("@Iterator", iterator)!!
}

fun QuestContext.Frame.removeIterator(){
    variable("@Iterator", null)
}

fun QuestReader.parse(func: ((reader: QuestReader, meta: Map<String, Any>) -> Pair<String, Any>?)): Map<String, Any> {
    val meta = mutableMapOf<String, Any>()
    try {
        while (true) {
            mark()
            val parsed = func(this, meta) ?: break
            if (parsed.first in meta) {
                reset()
                break
            }
            meta[parsed.first] = parsed.second
        }
    } catch (e: Exception) {
        reset()
    }
    return meta
}