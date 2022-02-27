package top.lanscarlos.vulpecular.kether.target.filter

import org.bukkit.entity.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecular.kether.ActionExpansion
import top.lanscarlos.vulpecular.kether.ActionExpansionType

/**
 * @author Lanscarlos
 * @since 2021-12-19 10:16
 * */
@ActionExpansion(ActionExpansionType.FILTER, "isInstance", ["instance", "inst"])
object FilterEntityIsInstance: ActionFilter() {

    override fun parse(reader: QuestReader): Pair<String, Any> {
        return "instance" to reader.next(ArgTypes.ACTION)
    }

    override fun run(frame: ScriptFrame, targets: Collection<Any>, meta: Map<String, Any>): Collection<Any> {
        val instance = (meta["instance"] as? ParsedAction<*>)?.let { action -> frame.newFrame(action).run<Any>().get() }
        return targets.filterIsInstance(getClasses(instance.toString()))
    }

    private fun getClasses(name: String): Class<*> {
        if (name.contains(".")) return Class.forName(name)
        return when(name.lowercase()) {
            "AbstractArrow".lowercase() -> AbstractArrow::class.java
            "AbstractHorse".lowercase() -> AbstractHorse::class.java
            "AbstractSkeleton".lowercase() -> AbstractSkeleton::class.java
            "AbstractVillager".lowercase() -> AbstractVillager::class.java
            "Ageable".lowercase() -> Ageable::class.java
            "Ambient".lowercase() -> Ambient::class.java
            "Animals".lowercase() -> Animals::class.java
            "AnimalTamer".lowercase() -> AnimalTamer::class.java
            "AreaEffectCloud".lowercase() -> AreaEffectCloud::class.java
            "ArmorStand".lowercase() -> ArmorStand::class.java
            "Arrow".lowercase() -> Arrow::class.java
            "Axolotl".lowercase() -> Axolotl::class.java
            "Bat".lowercase() -> Bat::class.java
            "Bee".lowercase() -> Bee::class.java
            "Blaze".lowercase() -> Blaze::class.java
            "Boat".lowercase() -> Boat::class.java
            "Boss".lowercase() -> Boss::class.java
            "Breedable".lowercase() -> Breedable::class.java
            "Cat".lowercase() -> Cat::class.java
            "CaveSpider".lowercase() -> CaveSpider::class.java
            "ChestedHorse".lowercase() -> ChestedHorse::class.java
            "Chicken".lowercase() -> Chicken::class.java
            "Cod".lowercase() -> Cod::class.java
            "ComplexEntityPart".lowercase() -> ComplexEntityPart::class.java
            "ComplexLivingEntity".lowercase() -> ComplexLivingEntity::class.java
            "Cow".lowercase() -> Cow::class.java
            "Creature".lowercase() -> Creature::class.java
            "Creeper".lowercase() -> Creeper::class.java
            "Damageable".lowercase() -> Damageable::class.java
            "Dolphin".lowercase() -> Dolphin::class.java
            "Donkey".lowercase() -> Donkey::class.java
            "DragonFireball".lowercase() -> DragonFireball::class.java
            "Drowned".lowercase() -> Drowned::class.java
            "Egg".lowercase() -> Egg::class.java
            "ElderGuardian".lowercase() -> ElderGuardian::class.java
            "EnderCrystal".lowercase() -> EnderCrystal::class.java
            "EnderDragon".lowercase() -> EnderDragon::class.java
            "EnderDragonPart".lowercase() -> EnderDragonPart::class.java
            "Enderman".lowercase() -> Enderman::class.java
            "Endermite".lowercase() -> Endermite::class.java
            "EnderPearl".lowercase() -> EnderPearl::class.java
            "EnderSignal".lowercase() -> EnderSignal::class.java
            "Entity".lowercase() -> Entity::class.java
            "Evoker".lowercase() -> Evoker::class.java
            "EvokerFangs".lowercase() -> EvokerFangs::class.java
            "ExperienceOrb".lowercase() -> ExperienceOrb::class.java
            "Explosive".lowercase() -> Explosive::class.java
            "FallingBlock".lowercase() -> FallingBlock::class.java
            "Fireball".lowercase() -> Fireball::class.java
            "Firework".lowercase() -> Firework::class.java
            "Fish".lowercase() -> Fish::class.java
            "FishHook".lowercase() -> FishHook::class.java
            "Flying".lowercase() -> Flying::class.java
            "Fox".lowercase() -> Fox::class.java
            "Ghast".lowercase() -> Ghast::class.java
            "Giant".lowercase() -> Giant::class.java
            "GlowItemFrame".lowercase() -> GlowItemFrame::class.java
            "GlowSquid".lowercase() -> GlowSquid::class.java
            "Goat".lowercase() -> Goat::class.java
            "Golem".lowercase() -> Golem::class.java
            "Guardian".lowercase() -> Guardian::class.java
            "Hanging".lowercase() -> Hanging::class.java
            "Hoglin".lowercase() -> Hoglin::class.java
            "Horse".lowercase() -> Horse::class.java
            "HumanEntity".lowercase() -> HumanEntity::class.java
            "Husk".lowercase() -> Husk::class.java
            "Illager".lowercase() -> Illager::class.java
            "Illusioner".lowercase() -> Illusioner::class.java
            "IronGolem".lowercase() -> IronGolem::class.java
            "Item".lowercase() -> Item::class.java
            "ItemFrame".lowercase() -> ItemFrame::class.java
            "LargeFireball".lowercase() -> LargeFireball::class.java
            "LeashHitch".lowercase() -> LeashHitch::class.java
            "LightningStrike".lowercase() -> LightningStrike::class.java
            "LingeringPotion".lowercase() -> LingeringPotion::class.java
            "LivingEntity".lowercase() -> LivingEntity::class.java
            "Llama".lowercase() -> Llama::class.java
            "LlamaSpit".lowercase() -> LlamaSpit::class.java
            "MagmaCube".lowercase() -> MagmaCube::class.java
            "Marker".lowercase() -> Marker::class.java
            "Minecart".lowercase() -> Minecart::class.java
            "Mob".lowercase() -> Mob::class.java
            "Monster".lowercase() -> Monster::class.java
            "Mule".lowercase() -> Mule::class.java
            "MushroomCow".lowercase() -> MushroomCow::class.java
            "NPC".lowercase() -> NPC::class.java
            "Ocelot".lowercase() -> Ocelot::class.java
            "Painting".lowercase() -> Painting::class.java
            "Panda".lowercase() -> Panda::class.java
            "Parrot".lowercase() -> Parrot::class.java
            "Phantom".lowercase() -> Phantom::class.java
            "Pig".lowercase() -> Pig::class.java
            "Piglin".lowercase() -> Piglin::class.java
            "PiglinAbstract".lowercase() -> PiglinAbstract::class.java
            "PiglinBrute".lowercase() -> PiglinBrute::class.java
            "PigZombie".lowercase() -> PigZombie::class.java
            "Pillager".lowercase() -> Pillager::class.java
            "Player".lowercase() -> Player::class.java
            "PolarBear".lowercase() -> PolarBear::class.java
            "Projectile".lowercase() -> Projectile::class.java
            "PufferFish".lowercase() -> PufferFish::class.java
            "Rabbit".lowercase() -> Rabbit::class.java
            "Raider".lowercase() -> Raider::class.java
            "Ravager".lowercase() -> Ravager::class.java
            "Salmon".lowercase() -> Salmon::class.java
            "Sheep".lowercase() -> Sheep::class.java
            "Shulker".lowercase() -> Shulker::class.java
            "ShulkerBullet".lowercase() -> ShulkerBullet::class.java
            "Silverfish".lowercase() -> Silverfish::class.java
            "Sittable".lowercase() -> Sittable::class.java
            "SizedFireball".lowercase() -> SizedFireball::class.java
            "Skeleton".lowercase() -> Skeleton::class.java
            "SkeletonHorse".lowercase() -> SkeletonHorse::class.java
            "Slime".lowercase() -> Slime::class.java
            "SmallFireball".lowercase() -> SmallFireball::class.java
            "Snowball".lowercase() -> Snowball::class.java
            "Snowman".lowercase() -> Snowman::class.java
            "SpectralArrow".lowercase() -> SpectralArrow::class.java
            "Spellcaster".lowercase() -> Spellcaster::class.java
            "Spider".lowercase() -> Spider::class.java
            "SplashPotion".lowercase() -> SplashPotion::class.java
            "Squid".lowercase() -> Squid::class.java
            "Steerable".lowercase() -> Steerable::class.java
            "Stray".lowercase() -> Stray::class.java
            "Strider".lowercase() -> Strider::class.java
            "Tameable".lowercase() -> Tameable::class.java
            "ThrowableProjectile\t".lowercase() -> ThrowableProjectile	::class.java
            "ThrownExpBottle".lowercase() -> ThrownExpBottle::class.java
            "ThrownPotion".lowercase() -> ThrownPotion::class.java
            "TippedArrow".lowercase() -> TippedArrow::class.java
            "TNTPrimed".lowercase() -> TNTPrimed::class.java
            "TraderLlama".lowercase() -> TraderLlama::class.java
            "Trident".lowercase() -> Trident::class.java
            "TropicalFish".lowercase() -> TropicalFish::class.java
            "Turtle".lowercase() -> Turtle::class.java
            "Vehicle".lowercase() -> Vehicle::class.java
            "Vex".lowercase() -> Vex::class.java
            "Villager".lowercase() -> Villager::class.java
            "Vindicator".lowercase() -> Vindicator::class.java
            "WanderingTrader".lowercase() -> WanderingTrader::class.java
            "WaterMob".lowercase() -> WaterMob::class.java
            "Witch".lowercase() -> Witch::class.java
            "Wither".lowercase() -> Wither::class.java
            "WitherSkeleton".lowercase() -> WitherSkeleton::class.java
            "WitherSkull".lowercase() -> WitherSkull::class.java
            "Wolf".lowercase() -> Wolf::class.java
            "Zoglin".lowercase() -> Zoglin::class.java
            "Zombie".lowercase() -> Zombie::class.java
            "ZombieHorse".lowercase() -> ZombieHorse::class.java
            "ZombieVillager".lowercase() -> ZombieVillager::class.java
            else -> Class.forName("org.bukkit.entity.$name")
        }
    }
}