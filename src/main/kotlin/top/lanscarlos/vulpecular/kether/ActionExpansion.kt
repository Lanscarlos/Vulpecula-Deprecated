package top.lanscarlos.vulpecular.kether

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.kether
 *
 * @author Lanscarlos
 * @since 2022-02-27 13:53
 */
annotation class ActionExpansion(
    val type: ActionExpansionType,
    val name: String,
    val alias: Array<String> = []
)
