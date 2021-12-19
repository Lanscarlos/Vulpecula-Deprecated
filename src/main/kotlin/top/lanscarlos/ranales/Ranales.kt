package top.lanscarlos.ranales

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

/**
 * @author Lanscarlos
 * @since 2021-11-29 17:20
 * */
object Ranales : Plugin() {

    override fun onEnable() {
        info("Successfully running ExamplePlugin!")
    }
}