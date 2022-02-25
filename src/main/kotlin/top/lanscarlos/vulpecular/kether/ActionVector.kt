package top.lanscarlos.vulpecular.kether

import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.scriptParser
import top.lanscarlos.vulpecular.kether.entity.ActionEntity

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.kether.vector
 *
 * @author Lanscarlos
 * @since 2022-02-23 22:16
 */
object ActionVector {



    @KetherParser(["vector"], namespace = "vulpecular", shared = true)
    fun ketherParser() = scriptParser {
        it.mark()
        var token = it.nextToken()
        val entity = if (!ActionEntity.isAction(token)) {
            it.reset()
            it.next(ArgTypes.ACTION).apply {
                token = it.nextToken()
            }
        } else null
        ActionEntity.getAction(token)?.resolve(it, entity) ?: error("Unknown type \"$token\" of entity action")
    }
}