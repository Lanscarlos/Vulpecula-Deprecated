package top.lanscarlos.vulpecular.api

import taboolib.common5.compileJS
import taboolib.library.kether.QuestContext
import top.lanscarlos.vulpecular.internal.VulpecularScript
import java.util.concurrent.CompletableFuture
import javax.script.SimpleBindings

/**
 * @author Lanscarlos
 * @since 2021-12-19 14:48
 * */
object VulpecularAPI {

    fun String.evalJS(args: Map<String, Any?> = mapOf(), throws: Boolean = false): Any? {
        return if (throws) compileJS()?.eval(SimpleBindings(args))
        else try {
            compileJS()?.eval(SimpleBindings(args))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun String.evalKether(sender: Any?, namespace: List<String> = listOf("vulpecular"), args: Map<String, Any?>? = null, throws: Boolean = false): CompletableFuture<Any?> {
        return VulpecularScript.eval(this, sender, namespace, args, throws)
    }

    fun String.runScript(sender: Any? = null, viewer: String? = null, vararg args: String, throws: Boolean = false) {
        runScript(sender, viewer, args.mapIndexed { i, arg -> i.toString() to arg as Any }.toMap(), throws)
    }

    fun String.runScript(sender: Any? = null, viewer: String? = null, args: QuestContext.VarTable, throws: Boolean = false) {
        val data = args.keys().filter { args.get<Any>(it).isPresent }.associateWith { args.get<Any>(it).get() }
        runScript(sender, viewer, data, throws)
    }

    fun String.runScript(sender: Any? = null, viewer: String? = null, args: Map<String, Any?>? = null, throws: Boolean = false) {
        VulpecularScript.runScript(this, sender, viewer, args, throws)
    }

}
