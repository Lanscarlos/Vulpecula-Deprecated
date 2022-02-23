package top.lanscarlos.vulpecular.kether.target.selector

import org.bukkit.Bukkit
import taboolib.module.kether.*

/**
 * @author Lanscarlos
 * @since 2021-12-18 09:47
 * */
object SelectorPlayer : Selector() {

    override fun parameters(): List<String> {
        return listOf("name")
    }

    override fun run(frame: ScriptFrame, args: Map<String, Any>): Any? {
        val name = args["name"] ?: error("Unknown player")
        return Bukkit.getPlayerExact(name.toString())
    }

    @KetherParser(["@Player", "@player"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { parse(it) }

}