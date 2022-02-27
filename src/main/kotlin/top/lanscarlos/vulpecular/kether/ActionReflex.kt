package top.lanscarlos.vulpecular.kether

import org.tabooproject.reflex.ClassAnalyser
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.platform.function.warning
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

object ActionReflex {

    /**
     * @return 返回 path
     */
    fun parserActionGet(reader: QuestReader): ParsedAction<*> {
        return reader.next(ArgTypes.ACTION)
    }

    /**
     * @return 返回 path to value
     */
    fun parserActionSet(reader: QuestReader): Pair<ParsedAction<*>, ParsedAction<*>> {
        return reader.next(ArgTypes.ACTION) to reader.next(ArgTypes.ACTION)
    }

    /**
     * @return 返回 name to args
     */
    fun parserActionInvoke(reader: QuestReader): Pair<ParsedAction<*>, List<ParsedAction<*>>> {
        val name = reader.next(ArgTypes.ACTION)
        return try {
            reader.mark()
            reader.expect("with")
            name to reader.next(ArgTypes.listOf(ArgTypes.ACTION))
        } catch (e: Exception) {
            reader.reset()
            name to listOf()
        }
    }

    fun parser(reader: QuestReader): ScriptAction<Any?> {
        val obj = reader.next(ArgTypes.ACTION)
        val parsers = mutableListOf<Any>().also {
            fun handle() {
                try {
                    reader.mark()
                    it += when (val operation = reader.expects("fields", "get", "set", "invoke")) {
                        "fields" -> 0
                        "get" -> parserActionGet(reader)
                        "set" -> parserActionSet(reader)
                        "invoke" -> parserActionInvoke(reader)
                        else -> error("Unknown reflex operation $operation!")
                    }
                    handle()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    reader.reset()
                }
            }
            handle()
        }
        return object : ScriptAction<Any?>() {
            override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
                val operations = parsers.toMutableList()
                val future = CompletableFuture<Any?>()
                if (operations.isEmpty()) return future
                fun handle(obj: Any?) {
                    if (obj == null || obj is Unit || obj is Nothing || operations.isEmpty()) {
                        future.complete(obj)
                        return
                    }
                    when (val operation = operations.removeFirst()) {
                        is Int -> {
                            // fields
                            try {
                                future.complete(ClassAnalyser.analyse(obj::class.java).fields)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            return
                        }
                        is ParsedAction<*> -> {
                            // get
                            frame.newFrame(operation).run<Any>().thenApply { path ->
                                try {
                                    handle(obj.getProperty(path.toString(), false))
                                } catch (e: Exception) {
                                    warning("No such field \"$path\" at class ${obj::class.qualifiedName}!")
                                }
                            }
                        }
                        is Pair<*, *> -> {
                            when (operation.second) {
                                is ParsedAction<*> -> {
                                    // set 无返回值
                                    frame.newFrame(operation.first as ParsedAction<*>).run<Any>().thenApply { path ->
                                        frame.newFrame(operation.second as ParsedAction<*>).run<Any>().thenApply { value ->
                                            try {
                                                obj.setProperty(path.toString(), value, false)
                                            } catch (e: Exception) {
                                                warning("No such field \"$path\" at class ${obj::class.qualifiedName}!")
                                            }
                                        }
                                    }
                                    return
                                }
                                is List<*> -> {
                                    // invoke
                                    frame.newFrame(operation.first as ParsedAction<*>).run<Any>().thenApply { name ->
                                        val args = mutableListOf<Any>()
                                        (operation.second as List<*>).forEach { action ->
                                            frame.newFrame(action as ParsedAction<*>).run<Any>().thenApply { arg ->
                                                args += arg
                                            }
                                        }
                                        try {
                                            handle(obj.invokeMethod(name.toString(), *args.toTypedArray(), isStatic = false))
                                        } catch (e: Exception) {
                                            warning("No such method \"$name\" at class ${obj::class.qualifiedName}!")
                                        }
                                    }
                                }
                                else -> return
                            }
                        }
                    }
                }
                frame.newFrame(obj).run<Any>().thenApply { obj ->
                    handle(obj)
                }
                return future
            }
        }
    }


    @KetherParser(["reflex", "ref"], namespace = "vulpecular", shared = true)
    fun parser() = scriptParser {
        parser(it)
    }

}