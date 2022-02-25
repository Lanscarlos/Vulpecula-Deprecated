package top.lanscarlos.vulpecular.kether.target

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.QuestReader
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecular.utils.iterator
import top.lanscarlos.vulpecular.utils.removeIterator
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-20 10:13
 * */
object ActionTargetForeach {

    fun resolve(reader: QuestReader): ScriptAction<Any> {
        val key = try {
            reader.mark()
            reader.expect("by")
            reader.nextToken()
        } catch (e: Exception) {
            reader.reset()
            "it"
        }
        reader.expect("then")
        val action = reader.next(ArgTypes.ACTION)
        return object : ScriptAction<Any>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                val targets = frame.iterator() as? Collection<*> ?: error("Illegal targets data!")
                targets.forEach {
                    frame.variables()[key] = it
                    frame.newFrame(action).run<Any>()
                }
                frame.removeIterator()
                return CompletableFuture.completedFuture(targets)
            }
        }
    }

    /**
     * filter {type} {args...}
     * */
    @KetherParser(["foreach"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { resolve(it) }

}