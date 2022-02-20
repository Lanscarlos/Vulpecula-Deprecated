package top.lanscarlos.vulpecular.utils

import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * 过滤有效文件
 * */
fun File.getFiles(file : File = this, filter : String = "#") : List<File> {
    if (!file.exists()) return listOf()
    return mutableListOf<File>().apply {
        if(file.isDirectory) {
            file.listFiles()?.forEach {
                addAll(getFiles(it))
            }
        } else if (!file.name.startsWith(filter)) {
            add(file)
        }
    }
}

/**
 * 删除此文件及其以内的所有文件
 * */
fun File.deleteDeep() {
    if (!exists()) return
    if (isDirectory) {
        // 先删除子目录内的文件
        listFiles()?.forEach {
            it.deleteDeep()
        }
    }
    delete()
}

fun File.toConfig(): ConfigFile {
    return Configuration.loadFromFile(this)
}