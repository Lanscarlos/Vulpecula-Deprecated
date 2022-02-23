package top.lanscarlos.vulpecular.kether

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

object ActionEvent {

    private val cancel: (frame: ScriptFrame, event: Event, cancel: ParsedAction<*>?) -> Unit = { frame, event, cancel ->
        val result = cancel?.let {
            frame.newFrame(it).run<Any>().thenApply { result ->
                !result.toString().equals("false", true)
            }.get()
        } ?: true
        (event as? Cancellable)?.isCancelled = result
    }

    private val isCancelled: (frame: ScriptFrame, event: Event) -> Boolean = { _, event ->
        (event as? Cancellable)?.isCancelled ?: false
    }

    private val eventName: (frame: ScriptFrame, event: Event) -> String = { _, event ->
        event.eventName
    }

    private fun resolve(source: ParsedAction<*>?, func: (frame: ScriptFrame, event: Event) -> Any): ScriptAction<Any> {
        return object : ScriptAction<Any>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any> {
                val event = frame.variables().get<Event?>("event").orElseGet {
                    source?.let {
                        frame.newFrame(it).run<Event?>().get()
                    }
                } ?: return CompletableFuture.completedFuture(null)
                return CompletableFuture.completedFuture(func(frame, event))
            }
        }
    }

    /**
     * event &it cancel
     * event cancel
     * */
    @KetherParser(["event"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser {
        it.mark()
        val event = if (it.nextToken() !in arrayOf("cancel", "isCancelled", "cancelled", "eventName", "name")) {
            it.reset()
            it.next(ArgTypes.ACTION)
        } else {
            it.reset()
            null
        }
        it.switch {
            case("cancel") {
                it.mark()
                val cancel = try {
                    it.expect("to")
                    it.next(ArgTypes.ACTION)
                } catch (e: Exception) {
                    it.reset()
                    null
                }
                resolve(event) { frame, event -> cancel(frame, event, cancel) }
            }
            case("isCancelled", "cancelled") {
                resolve(event) { frame, event -> isCancelled(frame, event) }
            }
            case("eventName", "name") {
                resolve(event) { frame, event -> eventName(frame, event) }
            }
        }
    }


}