package com.ostirotix.app.data

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/** Une amélioration de la bibliothèque (fiche d'archive). */
data class Upgrade(
    val id: String,
    val name: String,
    val desc: String,             // ce que fait l'amélioration
    val baseCost: Int,
    val maxLevel: Int = 25,
    val effectText: (Int) -> String,  // effet au niveau donné
)

/** Récompenses d'une partie gagnée. */
data class Rewards(
    val coins: Int,
    val pages: Int,
    val ink: Int,
    val streakBonus: Int,
)

/** Économie du jeu : pièces, pages rares, encre, améliorations. */
object Economy {

    val upgrades = listOf(
        Upgrade("biblio", "Bibliothèque", "Augmente les pièces gagnées après une victoire.",
            baseCost = 120) { lvl -> "+${lvl * 2}% de pièces par victoire" },
        Upgrade("loupe", "Loupe sémantique", "Les indices révèlent des mots plus proches du secret.",
            baseCost = 160, maxLevel = 10) { lvl -> "Indices de rang ${hintTargetRank(1, lvl)} et mieux" },
        Upgrade("encrier", "Encrier", "Réduit le coût en encre des indices.",
            baseCost = 140, maxLevel = 10) { lvl -> "-${lvl * 5}% d'encre par indice" },
        Upgrade("scribe", "Scribe", "Un copiste te reverse des pièces à chaque partie terminée.",
            baseCost = 100) { lvl -> "+${lvl * 2} pièces par partie" },
        Upgrade("archives", "Archives rares", "Augmente les chances de trouver une Page rare.",
            baseCost = 220, maxLevel = 15) { lvl -> "${10 + lvl * 3}% de chance de Page rare" },
        Upgrade("registre", "Registre quotidien", "Améliore les récompenses de série quotidienne.",
            baseCost = 180, maxLevel = 15) { lvl -> "+${lvl * 5}% de bonus de série" },
    )

    fun upgradeById(id: String) = upgrades.first { it.id == id }

    fun upgradeCost(u: Upgrade, level: Int): Int =
        (u.baseCost * 1.5.pow(level)).roundToInt()

    fun buyUpgrade(prefs: Prefs, id: String): Boolean {
        val u = upgradeById(id)
        val lvl = prefs.upgradeLevel(id)
        if (lvl >= u.maxLevel) return false
        val cost = upgradeCost(u, lvl)
        if (prefs.coins < cost) return false
        prefs.coins -= cost
        prefs.setUpgradeLevel(id, lvl + 1)
        return true
    }

    /** Rang du voisin révélé par l'indice n (1..3), amélioré par la Loupe. */
    fun hintTargetRank(hintNo: Int, loupeLevel: Int): Int {
        val base = when (hintNo) { 1 -> 50; 2 -> 25; else -> 10 }
        return (base - loupeLevel * 3).coerceAtLeast(2)
    }

    /** Coût en encre d'un indice, réduit par l'Encrier. */
    fun hintInkCost(prefs: Prefs): Int {
        val reduction = (prefs.upgradeLevel("encrier") * 5).coerceAtMost(50)
        return (15 * (100 - reduction) / 100).coerceAtLeast(5)
    }

    /** Petit gain de consolation quand on abandonne (Scribe uniquement). */
    fun applyLossReward(prefs: Prefs): Int {
        val scribe = prefs.upgradeLevel("scribe") * 2
        prefs.coins += scribe
        return scribe
    }

    /**
     * Récompense de victoire. Dépend : essais, temps, série, température moyenne des
     * meilleurs mots, améliorations. Applique directement sur les prefs.
     */
    fun applyWinReward(
        prefs: Prefs,
        attempts: Int,
        seconds: Long,
        isDaily: Boolean,
        bestTempsAvg: Double,
    ): Rewards {
        val base = 50
        val efficiency = ((40 - attempts).coerceAtLeast(0)) * 3          // peu d'essais
        val speed = when { seconds < 120 -> 30; seconds < 300 -> 15; else -> 0 }
        val mastery = ((bestTempsAvg.coerceAtLeast(0.0)) / 4).roundToInt() // chauffe maîtrisée
        var streakBonus = 0
        if (isDaily && prefs.streak > 0) {
            val registre = prefs.upgradeLevel("registre") * 5
            streakBonus = (prefs.streak.coerceAtMost(10) * 8 * (100 + registre)) / 100
        }
        val biblio = prefs.upgradeLevel("biblio") * 2
        val scribe = prefs.upgradeLevel("scribe") * 2
        var coins = ((base + efficiency + speed + mastery) * (100 + biblio)) / 100
        coins += streakBonus + scribe

        val pageChance = 10 + prefs.upgradeLevel("archives") * 3
        val pages = if (Random.nextInt(100) < pageChance) 1 else 0

        val ink = 8

        prefs.coins += coins
        prefs.pages += pages
        prefs.ink += ink
        return Rewards(coins, pages, ink, streakBonus)
    }

    /** Recharge d'encre à la boutique : 50 pièces → 25 encre. */
    const val INK_REFILL_COST = 50
    const val INK_REFILL_AMOUNT = 25
    fun buyInk(prefs: Prefs): Boolean {
        if (prefs.coins < INK_REFILL_COST) return false
        prefs.coins -= INK_REFILL_COST
        prefs.ink += INK_REFILL_AMOUNT
        return true
    }
}
