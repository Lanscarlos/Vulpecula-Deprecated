package top.lanscarlos.vulpecular.kether.target.filter

import org.bukkit.entity.Entity
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture

/**
 * @author Lanscarlos
 * @since 2021-12-18 15:36
 * */
object FilterEntityType: Filter() {

    override fun parse(reader: QuestReader): Any {
        return reader.next(ArgTypes.listOf(ArgTypes.ACTION)).toMutableList()
    }

    override fun call(frame: ScriptFrame, arg: Any, targets: Collection<Any>, func: (targets: Collection<Any>) -> Collection<Any>): Collection<Any> {
        val types = (arg as? List<*>)?.map { it }?.toMutableList() ?: error("Illegal Filter Data!")
        val legalTypes = mutableSetOf<String>()
        val illegalTypes = mutableSetOf<String>()
        val future = CompletableFuture<Collection<Any>>()
        fun process() {
            if (types.isNotEmpty()) {
                val type = types.removeFirst() as? ParsedAction<*> ?: return
                frame.newFrame(type).run<String>().thenApply { type ->
                    if (type.startsWith("!")) {
                        illegalTypes += type.substring(1).lowercase()
                    }else {
                        legalTypes += type.lowercase()
                    }
                    process()
                }
            }else {
                future.complete(
                    func(
                        targets.filter {
                            val type = when(it) {
                                is ProxyPlayer -> "ProxyPlayer".lowercase()
                                is Entity -> it.type.name.lowercase()
                                else -> it::class.simpleName?.lowercase() ?: return@filter false
                            }
                            type !in illegalTypes && ( if (legalTypes.isNotEmpty()) type in legalTypes else true )
                        }
                    )
                )
            }
        }
        process()
        return future.get()
    }
}