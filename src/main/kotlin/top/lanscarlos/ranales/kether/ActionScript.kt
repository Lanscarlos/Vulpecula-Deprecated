package top.lanscarlos.ranales.kether

import taboolib.common.platform.function.info
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.script
import top.lanscarlos.ranales.RanalesAPI
import java.util.concurrent.CompletableFuture

object ActionScript {

    private val run: (frame: ScriptFrame, file: ParsedAction<*>) -> Unit = { frame, file ->
        frame.newFrame(file).run<Any>().thenApply {
            RanalesAPI.runScript(it.toString(), sender = frame.script().sender, args = frame.variables())
        }
    }

    private val stop: (frame: ScriptFrame, id: ParsedAction<*>?) -> Unit = { frame, id ->
        id?.let {
            frame.newFrame(id).run<Any>().thenApply {
                RanalesAPI.stopScript(it.toString(), frame.script().sender)
            }
        } ?: let {
            RanalesAPI.stopScript()
        }
    }

    private val load: () -> Unit = {
        RanalesAPI.loadScript()
    }

    private val list: () -> Collection<String> = {
        RanalesAPI.workspace.scripts.map { it.value.id }
    }

    private fun resolve(func: (frame: ScriptFrame) -> Any): ScriptAction<Any> {
        return object : ScriptAction<Any>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                return CompletableFuture.completedFuture(func(frame))
            }
        }
    }

    fun parse(reader: QuestReader): ScriptAction<Any> {
        return when(val token = reader.expects(
            "run", "stop", "reload", "list"
        )) {
            "run" -> {
                val file = reader.next(ArgTypes.ACTION)
                resolve {
                    run(it, file)
                }
            }
            "stop" -> {
                val id = try {
                    reader.mark()
                    reader.expect("with")
                    reader.next(ArgTypes.ACTION)
                }catch (e: Exception) {
                    reader.reset()
                    null
                }
                resolve { stop(it, id) }
            }
            "reload" -> resolve { load() }
            "list" -> resolve { list() }
            else -> error("Unknown script action type: $token")
        }
    }

}