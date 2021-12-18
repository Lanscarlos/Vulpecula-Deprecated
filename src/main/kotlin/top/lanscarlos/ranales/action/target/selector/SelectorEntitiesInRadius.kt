package top.lanscarlos.ranales.action.target.selector

import org.bukkit.entity.Entity
import taboolib.module.kether.*
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.toBukkitLocation

object SelectorEntitiesInRadius : Selector() {

    override fun parameters(): List<String> {
        return listOf("location", "radius")
    }

    override fun call(frame: ScriptFrame, args: Map<String, Any>): Any? {
        val radius = args["radius"]?.toString()?.toDouble() ?: 1.0
        val location = when(val it = args["location"]) {
            is taboolib.common.util.Location -> it.toBukkitLocation()
            is org.bukkit.Location -> it
            is Entity -> it.location
            is BukkitPlayer -> it.player.location
            else -> error("location is null")
        }
        return location.world?.getNearbyEntities(location, radius, radius, radius)?.toSet()
    }

    @KetherParser(["@EIR"], namespace = "ranales", shared = true)
    fun parser() = scriptParser { parse(it) }

}