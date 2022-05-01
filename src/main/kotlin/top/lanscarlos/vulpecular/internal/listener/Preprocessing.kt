package top.lanscarlos.vulpecular.internal.listener

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.hanging.HangingEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.vehicle.VehicleEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.event.world.WorldEvent
import org.bukkit.inventory.Inventory

/**
 * 预处理事件参数
 * @param deep 是否深度处理参数
 * */
fun Event.preprocessing(filter: Collection<String>): MutableMap<String, Any?> {
    if ("*" in filter) return mutableMapOf()

    val args = if ("event*" in filter) {
        mutableMapOf()
    } else if ("event.*" in filter) {
        mutableMapOf<String, Any?>("event" to this)
    } else {
        mutableMapOf<String, Any?>(
            "event" to this,
            "event.name" to this.eventName,
            "event.eventName" to this.eventName,
            "event.isAsynchronous" to this.isAsynchronous,
            "event.isCancelled" to ((this as? Cancellable)?.isCancelled ?: false)
        )
    }

    when (this) {
        is BlockEvent -> preprocessing(args, filter)
        is EntityEvent -> preprocessing(args, filter)
        is HangingEvent -> preprocessing(args, filter)
        is InventoryEvent -> preprocessing(args, filter)
        is PlayerEvent -> preprocessing(args, filter)
        is VehicleEvent -> preprocessing(args, filter)
        is WeatherEvent -> args["world"] = world
        is WorldEvent -> args["world"] = world
    }
    return args
}

private fun Block.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>, prefix: String = "block") {
    if ("$prefix*" in filter) return
    args[prefix] = this
    if ("$prefix.*" in filter) return
}

private fun Entity.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>, prefix: String = "entity") {
    if ("$prefix*" in filter) return
    args[prefix] = this
    if ("$prefix.*" in filter) return
    args["$prefix.name"] = this.name
}

private fun Inventory.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>, prefix: String = "inventory") {
    if ("$prefix*" in filter) return
    args[prefix] = this
    if ("$prefix.*" in filter) return
}

private fun Material.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>, prefix: String = "material") {
    if ("$prefix*" in filter) return
    args[prefix] = this
    if ("$prefix.*" in filter) return
    args["$prefix.name"] = this.name
    args["$prefix.blastResistance"] = this.blastResistance
    args["$prefix.craftingRemainingItem"] = this.craftingRemainingItem
    args["$prefix.equipmentSlot"] = this.equipmentSlot
    args["$prefix.hardness"] = this.hardness
//    args["$prefix.id"] = this.id
    args["$prefix.key"] = this.key
    args["$prefix.maxDurability"] = this.maxDurability
    args["$prefix.maxStackSize"] = this.maxStackSize
    args["$prefix.slipperiness"] = this.slipperiness
    args["$prefix.hasGravity"] = this.hasGravity()
    args["$prefix.isAir"] = this.isAir
    args["$prefix.isBlock"] = this.isBlock
    args["$prefix.isBurnable"] = this.isBurnable
    args["$prefix.isEdible"] = this.isEdible
    args["$prefix.isFlammable"] = this.isFlammable
    args["$prefix.isFuel"] = this.isFuel
    args["$prefix.isInteractable"] = this.isInteractable
    args["$prefix.isItem"] = this.isItem
    args["$prefix.isOccluding"] = this.isOccluding
    args["$prefix.isRecord"] = this.isRecord
    args["$prefix.isSolid"] = this.isSolid
}

private fun Player.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>, prefix: String = "player") {
    if ("$prefix*" in filter) return
    args[prefix] = this
    if ("$prefix.*" in filter) return
    args["$prefix.name"] = this.name
    args["$prefix.health"] = this.health
}

fun BlockEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    block.preprocessing(args, filter)
    if ("event*" in filter || "event.*" in filter) return
    when (this) {
        is BlockBreakEvent -> this.player.preprocessing(args, filter)
        is BlockPlaceEvent -> this.player.preprocessing(args, filter)
    }
}

fun EntityEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    entity.preprocessing(args, filter)
    (entity as? Player)?.preprocessing(args, filter)
    if ("event*" in filter || "event.*" in filter) return
}

private fun HangingEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    entity.preprocessing(args, filter)
    if ("event*" in filter || "event.*" in filter) return
}

fun InventoryEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    inventory.preprocessing(args, filter)
    (view.player as? Player)?.preprocessing(args, filter)
    if ("event*" in filter || "event.*" in filter) return
}

fun PlayerEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    player.preprocessing(args, filter)
    when (eventName) {
        "AsyncPlayerChatEvent" -> {
            val e = this as org.bukkit.event.player.AsyncPlayerChatEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.format"] = e.format
            args["event.message"] = e.message
            args["event.recipients"] = e.recipients
        }
        "PlayerLoginEvent" -> {
            val e = this as org.bukkit.event.player.PlayerLoginEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.address"] = e.address
            args["event.hostAddress"] = e.address.hostAddress
            args["event.address.hostAddress"] = e.address.hostAddress
            args["event.hostName"] = e.address.hostName
            args["event.address.hostName"] = e.address.hostName
            args["event.canonicalHostName"] = e.address.canonicalHostName
            args["event.address.canonicalHostName"] = e.address.canonicalHostName
            args["event.hostname"] = e.hostname
            args["event.kickMessage"] = e.kickMessage
        }
        "PlayerAdvancementDoneEvent" -> {
            val e = this as org.bukkit.event.player.PlayerAdvancementDoneEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.advancement"] = e.advancement
        }
        "PlayerAnimationEvent" -> {
            val e = this as org.bukkit.event.player.PlayerAnimationEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.animationType"] = e.animationType.name
        }
        "PlayerArmorStandManipulateEvent" -> {
            val e = this as org.bukkit.event.player.PlayerArmorStandManipulateEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.armorStandItem"] = e.armorStandItem
            args["event.playerItem"] = e.playerItem
            args["event.rightClicked"] = e.rightClicked
            args["event.slot"] = e.slot.name
        }
        "PlayerBedEnterEvent" -> {
            val e = this as org.bukkit.event.player.PlayerBedEnterEvent
            e.bed.preprocessing(args, filter, "bed")
            if ("event*" in filter || "event.*" in filter) return
            args["event.bed"] = e.bed
            args["event.bedEnterResult"] = e.bedEnterResult.name
            args["event.useBed"] = e.useBed().name
        }
        "PlayerBedLeaveEvent" -> {
            val e = this as org.bukkit.event.player.PlayerBedLeaveEvent
            e.bed.preprocessing(args, filter, "bed")
            if ("event*" in filter || "event.*" in filter) return
            args["event.bed"] = e.bed
            args["event.shouldSetSpawnLocation"] = e.shouldSetSpawnLocation()
        }
        "PlayerBucketEmptyEvent", "PlayerBucketFillEvent" -> {
            val e = this as org.bukkit.event.player.PlayerBucketEvent
            e.bucket.preprocessing(args, filter, "bucket")
            e.block.preprocessing(args, filter, "block")
            e.blockClicked.preprocessing(args, filter, "blockClicked")
            if ("event*" in filter || "event.*" in filter) return
            args["event.bucket"] = e.bucket.name
            args["event.block"] = e.block
            args["event.blockFace"] = e.blockFace
            args["event.blockClicked"] = e.blockClicked
        }
        "PlayerBucketEntityEvent" -> {
            val e = this as org.bukkit.event.player.PlayerBucketEntityEvent
            e.entity.preprocessing(args, filter)
            if ("event*" in filter || "event.*" in filter) return
            args["event.bukkit"] = e.entityBucket
            args["event.entityBucket"] = e.entityBucket
            args["event.originalBucket"] = e.originalBucket
        }
        "PlayerJoinEvent" -> {
            val e = this as org.bukkit.event.player.PlayerJoinEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.message"] = e.joinMessage
            args["event.joinMessage"] = e.joinMessage
        }
        "PlayerKickEvent" -> {
            val e = this as org.bukkit.event.player.PlayerKickEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.message"] = e.leaveMessage
            args["event.leaveMessage"] = e.leaveMessage
            args["event.reason"] = e.reason
        }
        "PlayerQuitEvent" -> {
            val e = this as org.bukkit.event.player.PlayerQuitEvent
            if ("event*" in filter || "event.*" in filter) return
            args["event.message"] = e.quitMessage
            args["event.quitMessage"] = e.quitMessage
        }
    }
}

fun VehicleEvent.preprocessing(args: MutableMap<String, Any?>, filter: Collection<String>) {
    vehicle.preprocessing(args, filter)
    if ("event*" in filter || "event.*" in filter) return
}