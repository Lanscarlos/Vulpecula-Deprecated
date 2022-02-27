package top.lanscarlos.vulpecular.kether

import taboolib.common.LifeCycle
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import top.lanscarlos.vulpecular.kether.entity.ActionEntity
import top.lanscarlos.vulpecular.kether.target.filter.ActionFilter
import top.lanscarlos.vulpecular.kether.target.selector.ActionSelector
import java.util.function.Supplier

/**
 * Vulpecular
 * top.lanscarlos.vulpecular.kether.entity
 *
 * @author Lanscarlos
 * @since 2022-02-27 11:38
 */
@Awake
object ActionLoader : Injector.Classes {

    override val lifeCycle: LifeCycle
        get() = LifeCycle.LOAD

    override val priority: Byte
        get() = 0

    override fun inject(clazz: Class<*>, instance: Supplier<*>) {
    }

    override fun postInject(clazz: Class<*>, instance: Supplier<*>) {
        if (!clazz.isAnnotationPresent(ActionExpansion::class.java)) return
        val annotation = clazz.getAnnotation(ActionExpansion::class.java)
        when (annotation.type) {
            ActionExpansionType.SELECTOR -> {
                if (!ActionSelector::class.java.isAssignableFrom(clazz)) return
                ActionSelector.registerSelector(annotation.name, instance.get() as ActionSelector, *annotation.alias)
            }
            ActionExpansionType.FILTER -> {
                if (!ActionFilter::class.java.isAssignableFrom(clazz)) return
                ActionFilter.registerSelector(annotation.name, instance.get() as ActionFilter, *annotation.alias)
            }
            ActionExpansionType.ENTITY -> {
                if (!ActionEntity::class.java.isAssignableFrom(clazz)) return
                ActionEntity.registerActionEntity(annotation.name, instance.get() as ActionEntity, *annotation.alias)
            }
        }
    }
}