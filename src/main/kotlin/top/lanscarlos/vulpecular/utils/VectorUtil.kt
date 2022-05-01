package top.lanscarlos.vulpecular.utils

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.utils
 *
 * @author Lanscarlos
 * @since 2022-03-04 10:05
 */

fun taboolib.common.util.Vector.toBukkit(): org.bukkit.util.Vector {
    return org.bukkit.util.Vector(x, y, z)
}

fun org.bukkit.util.Vector.toProxy(): taboolib.common.util.Vector {
    return taboolib.common.util.Vector(x, y, z)
}