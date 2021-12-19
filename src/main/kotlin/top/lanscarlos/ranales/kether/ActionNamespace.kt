package top.lanscarlos.ranales.kether

import taboolib.common.platform.function.warning
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.library.kether.SimpleReader
import taboolib.module.kether.*

/**
 * @author Lanscarlos
 * @since 2021-12-17 13:30
 * */
object ActionNamespace {

    @KetherParser(["namespace"], shared = true)
    fun parser() = scriptParser {
        val namespace: MutableList<String> = when(it) {
            is SimpleReader -> it.namespace
            is RemoteQuestReader -> it.source.getProperty("namespace")
            else -> null
        } ?: error("Can not access namespace!")
        when(it.expects(
            "add", "remove", "list"
        )) {
            "add" -> {
                val name = it.nextToken().lowercase()
                if (name !in namespace) namespace += name
            }
            "remove" -> {
                val name = it.nextToken().lowercase()
                if (name == "kether") warning("Can not remove default namespace \"kether\"!")
                if (name in namespace) namespace -= name
            }
            "list" -> {  }
            else -> warning("Unknown type of namespace action!")
        }
        actionNow { namespace }
    }
}