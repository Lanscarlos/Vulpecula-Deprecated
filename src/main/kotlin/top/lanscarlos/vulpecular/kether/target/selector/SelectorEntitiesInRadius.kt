package top.lanscarlos.vulpecular.kether.target.selector

import org.bukkit.entity.Entity
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * @author Lanscarlos
 * @since 2021-12-18 10:05
 * */
@ActionExpansion(ActionExpansionType.FILTER, "EntitiesInRadius", ["EIR"])
object SelectorEntitiesInRadius : ActionSelector() {

    /**
     * sel EIR loc &loc r 10
     * */
    override fun parse(reader: QuestReader): Pair<String, Any> {
        return when (val token = reader.expects(
            "location", "loc", "l",
            "radius", "range", "r"
        )) {
            "location", "loc", "l" -> "location" to reader.next(ArgTypes.ACTION)
            "radius", "range", "r" -> "radius" to reader.next(ArgTypes.ACTION)
            else -> error("Unknown parameter \"$token\"!")
        }
    }

    override fun run(frame: ScriptFrame, meta: Map<String, Any>): Collection<Entity> {
        val data = meta.mapValues { (it.value as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get() } }
        val location = (data["location"]?.toLocation() ?: frame.script().sender?.toLocation())?.toBukkitLocation() ?: error("Illegal location data!")
        val radius = data["radius"]?.toString()?.toDouble() ?: error("Illegal radius data!")
        return location.world?.getNearbyEntities(location, radius, radius, radius) ?: setOf()
    }

    @KetherParser(["@EIR"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser { resolve(it) }

}