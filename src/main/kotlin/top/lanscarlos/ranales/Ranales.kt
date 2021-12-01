package top.lanscarlos.ranales

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object Ranales : Plugin() {

    override fun onEnable() {
        info("Successfully running ExamplePlugin!")
    }
}