package top.lanscarlos.ranales.kether.target.selector

import org.bukkit.Bukkit
import taboolib.module.kether.*

object SelectorPlayer : Selector() {

    override fun parameters(): List<String> {
        return listOf("name")
    }

    override fun call(frame: ScriptFrame, args: Map<String, Any>): Any? {
        val name = args["name"] ?: error("Unknown player")
        return Bukkit.getPlayerExact(name.toString())
    }

    @KetherParser(["@Player", "@player"], namespace = "ranales", shared = true)
    fun parser() = scriptParser { parse(it) }

}