package top.lanscarlos.ranales.kether.target

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.ranales.kether.target.filter.Filter
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-20 10:13
 * */
object TargetForeach {

    fun parse(reader: QuestReader): Pair<String, ParsedAction<*>> {
        val key = try {
            reader.mark()
            reader.expect("by")
            reader.nextToken()
        } catch (e: Exception) {
            reader.reset()
            "it"
        }
        return Pair(key, reader.next(ArgTypes.ACTION))
    }

    fun resolve(frame: ScriptFrame, args: Pair<String, ParsedAction<*>>, targets: Collection<Any>) {
        val key = args.first
        val action = args.second
        fun process(iterator: Iterator<Any>) {
            if (iterator.hasNext()) {
                val target = iterator.next()
                frame.variables()[key] = target
                frame.newFrame(action).run<Any>()
                process(iterator)
            }else {
                frame.variables().remove(key)
            }
        }
        process(targets.iterator())
    }

    fun resolve(reader: QuestReader): ScriptAction<Void> {
        val targets = reader.next(ArgTypes.ACTION)
        val args = parse(reader)
        return object : ScriptAction<Void>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Void> {
                frame.newFrame(targets).run<Any>().thenApply { targets ->
                    when(targets) {
                        is Collection<*> -> {
                            resolve(frame, args, targets.filterNotNull())
                        }
                    }
                }
                return CompletableFuture.completedFuture(null)
            }
        }
    }

}