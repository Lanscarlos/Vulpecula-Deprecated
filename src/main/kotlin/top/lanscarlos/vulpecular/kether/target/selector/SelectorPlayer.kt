package top.lanscarlos.vulpecular.kether.target.selector

import org.bukkit.Bukkit
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:47
 * */
@ActionExpansion(ActionExpansionType.FILTER, "Player")
object SelectorPlayer : ActionSelector() {

    override fun parse(reader: QuestReader): Pair<String, Any> {
        reader.expect("name")
        return "name" to reader.next(ArgTypes.ACTION)
    }

    override fun run(frame: ScriptFrame, meta: Map<String, Any>): Any? {
        val name = (meta["name"] as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get() } ?: error("Unknown player name")
        return Bukkit.getPlayerExact(name.toString())
    }

    @KetherParser(["@Player", "@player"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { resolve(it) }

}