package top.lanscarlos.ranales.action.target.filter

import taboolib.common.platform.function.info
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.script
import java.util.concurrent.CompletableFuture

object FilterForeach: Filter() {

    /*
    * target filter {targets} {type} [ {parameter} ]
    * target filter &targets foreach i to check entity &i type == *husk
    * */
    override fun parse(reader: QuestReader): Any {
        return mutableMapOf<String, Any>().also {
            it["key"] = reader.nextToken()
            reader.expect("by")
            it["condition"] = reader.next(ArgTypes.ACTION)
        }
    }

    override fun call(frame: ScriptFrame, arg: Any, targets: Collection<Any>): Collection<Any> {
        val map = arg as? Map<*, *> ?: error("Illegal Filter Data!")
        val key = map["key"]?.toString() ?: error("Filter Data key cannot be null!")
        val condition = map["condition"] as? ParsedAction<*> ?: error("Filter Data condition cannot be null!")
        val future = CompletableFuture<Collection<Any>>()
        val set = targets.toMutableSet()
        fun process(iterator: MutableIterator<Any>) {
            if (iterator.hasNext()) {
                val target = iterator.next()
                frame.variables()[key] = target
                frame.newFrame(condition).run<Any>().thenApply { result ->
                    if (!result.toString().equals("true", true)) iterator.remove()
                }
                process(iterator)
            }else {
                future.complete(set)
            }
        }
        process(set.iterator())
        return future.get()
    }

}