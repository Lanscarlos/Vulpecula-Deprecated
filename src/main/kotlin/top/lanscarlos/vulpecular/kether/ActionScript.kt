package top.lanscarlos.vulpecular.kether

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.script
import top.lanscarlos.vulpecular.api.VulpecularAPI.runScript
import top.lanscarlos.vulpecular.internal.VulpecularScript
import java.util.concurrent.CompletableFuture

object ActionScript {

    private val run: (frame: ScriptFrame, file: ParsedAction<*>) -> Unit = { frame, file ->
        frame.newFrame(file).run<Any>().thenApply {
            it.toString().runScript(sender = frame.script().sender, args = frame.variables())
        }
    }

    private val stop: (frame: ScriptFrame, id: ParsedAction<*>?) -> Unit = { frame, id ->
        id?.let {
            frame.newFrame(id).run<Any>().thenApply {
                VulpecularScript.stopScript(it.toString(), frame.script().sender)
            }
        } ?: let {
            VulpecularScript.stopScript()
        }
    }

    private val load: (frame: ScriptFrame) -> Unit = { frame ->
        frame.script().sender?.sendMessage(VulpecularScript.load())
    }

    private val list: () -> Collection<String> = {
        VulpecularScript.workspace.scripts.map { it.value.id }
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
            "reload" -> resolve { load(it) }
            "list" -> resolve { list() }
            else -> error("Unknown script action type: $token")
        }
    }

}