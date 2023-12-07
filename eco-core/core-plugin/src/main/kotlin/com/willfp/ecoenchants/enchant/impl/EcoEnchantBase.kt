package com.willfp.ecoenchants.enchant.impl

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecoenchants.EcoEnchantsPlugin
import com.willfp.ecoenchants.enchant.EcoEnchant
import com.willfp.ecoenchants.enchant.EcoEnchantLevel
import com.willfp.ecoenchants.rarity.EnchantmentRarities
import com.willfp.ecoenchants.rarity.EnchantmentRarity
import com.willfp.ecoenchants.target.EnchantmentTargets
import com.willfp.ecoenchants.type.EnchantmentType
import com.willfp.ecoenchants.type.EnchantmentTypes
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

abstract class EcoEnchantBase(
    final override val id: String,
    final override val plugin: EcoEnchantsPlugin,
) : EcoEnchant {
    protected val context = ViolationContext(plugin, "enchantment $id")

    final override val config by lazy { loadConfig() }

    private val levels = mutableMapOf<Int, EcoEnchantLevel>()

    private val conflictIds = config.getStrings("conflicts").toSet()

    override val key = NamespacedKey.minecraft(id)

    override val rawDisplayName = config.getString("display-name")

    override val maxLevel = config.getInt("max-level")

    override val targets = config.getStrings("targets")
        .mapNotNull { EnchantmentTargets[it] }
        .toSet()

    override val type: EnchantmentType = config.getString("type")
        .let { EnchantmentTypes[it] }
        ?: EnchantmentTypes.values().first()

    override val rarity: EnchantmentRarity = config.getString("rarity")
        .let { EnchantmentRarities[it] }
        ?: EnchantmentRarities.values().first()

    override val conditions = Conditions.compile(
        config.getSubsections("conditions"),
        context.with("conditions")
    )

    override val isEnchantable = config.getBool("enchantable")

    override val isTradeable = config.getBool("tradeable")

    override val isDiscoverable = config.getBool("discoverable")

    /**
     * Load the config for this enchant.
     */
    protected abstract fun loadConfig(): Config

    override fun getLevel(level: Int): EcoEnchantLevel {
        return levels.getOrPut(level) {
            createLevel(level)
        }
    }

    protected abstract fun createLevel(level: Int): EcoEnchantLevel

    override fun conflictsWithDirectly(other: Enchantment): Boolean {
        return other.key.key in conflictIds
    }
}
