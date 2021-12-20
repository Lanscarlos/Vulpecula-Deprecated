package top.lanscarlos.ranales.kether.target.filter

import taboolib.common.platform.function.info
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 19:56
 * */
object FilterForeach: Filter() {

    /*
    * target filter {targets} {type} [ {parameter} ]
    * target filter &targets foreach by i { check entity &i type == *husk }
    * */
    override fun parse(reader: QuestReader): Any {
        return mutableMapOf<String, Any>().also {
            try {
                reader.mark()
                reader.expect("by")
                it["key"] = reader.nextToken()
            } catch (e: Exception) {
                reader.reset()
            }
            it["condition"] = reader.next(ArgTypes.ACTION)
        }
    }

    override fun call(frame: ScriptFrame, arg: Any, targets: Collection<Any>, func: (targets: Collection<Any>) -> Collection<Any>): Collection<Any> {
        val map = arg as? Map<*, *> ?: error("Illegal Filter Data!")
        val key = map["key"]?.toString() ?: "it"
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
                frame.variables().remove(key)
                future.complete(
                    func(
                        set
                    )
                )
            }
        }
        process(set.iterator())
        return future.get()
    }

}