package com.ostirotix.app.data

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/** Coût composite d'une amélioration : les premiers niveaux restent simples, puis la progression demande des ressources rares. */
data class UpgradeCost(
    val coins: Int,
    val pages: Int = 0,
    val ink: Int = 0,
)

/** Une amélioration de la bibliothèque (fiche d'archive). */
data class Upgrade(
    val id: String,
    val name: String,
    val desc: String,             // ce que fait l'amélioration
    val baseCost: Int,
    val maxLevel: Int = 25,
    val pageCostStart: Int = 5,
    val inkCostStart: Int = Int.MAX_VALUE,
    val effectText: (Int) -> String,  // effet au niveau donné
)

/** Pack de ressources déclaré côté app. Le vrai paiement sera branché via Google Play Billing. */
data class ResourcePack(
    val productId: String,
    val name: String,
    val desc: String,
    val coins: Int,
    val pages: Int,
    val ink: Int,
    val priceLabel: String,
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
        Upgrade("biblio", "Bibliothèque", "Rayonnages mieux classés : plus de pièces après chaque victoire.",
            baseCost = 90, pageCostStart = 6) { lvl -> "+${lvl * 2}% de pièces par victoire" },
        Upgrade("loupe", "Loupe sémantique", "Indices plus précis, utiles quand la piste devient étroite.",
            baseCost = 130, maxLevel = 12, pageCostStart = 4, inkCostStart = 8) { lvl ->
            "Premier indice jusqu'au rang ${hintTargetRank(1, lvl)}"
        },
        Upgrade("encrier", "Encrier", "Réduit le coût des indices et prolonge les longues recherches.",
            baseCost = 120, maxLevel = 12, pageCostStart = 5) { lvl -> "-${lvl * 5}% d'encre par indice" },
        Upgrade("scribe", "Scribe", "Un copiste reverse des pièces, même lors des parties ratées.",
            baseCost = 80, pageCostStart = 7) { lvl -> "+${lvl * 2} pièces par partie terminée" },
        Upgrade("archives", "Archives rares", "Augmente les chances de gagner des Pages rares.",
            baseCost = 150, maxLevel = 18, pageCostStart = 3, inkCostStart = 10) { lvl ->
            "${10 + lvl * 3}% de chance de Page rare"
        },
        Upgrade("registre", "Registre quotidien", "Renforce les séries et les récompenses du mot du jour.",
            baseCost = 140, maxLevel = 18, pageCostStart = 4) { lvl -> "+${lvl * 5}% de bonus de série" },
    )

    val resourcePacks = listOf(
        ResourcePack(
            productId = "ostirotix_pack_marque_page",
            name = "Marque-page du novice",
            desc = "Un petit appoint pour lancer quelques améliorations.",
            coins = 300,
            pages = 2,
            ink = 35,
            priceLabel = "1,99 €",
        ),
        ResourcePack(
            productId = "ostirotix_pack_encrier",
            name = "Encrier du scribe",
            desc = "De quoi acheter des indices et avancer la bibliothèque.",
            coins = 850,
            pages = 7,
            ink = 110,
            priceLabel = "4,99 €",
        ),
        ResourcePack(
            productId = "ostirotix_pack_archives",
            name = "Coffret des archives",
            desc = "Un gros lot pour les longues sessions de recherche.",
            coins = 2100,
            pages = 18,
            ink = 280,
            priceLabel = "9,99 €",
        ),
        ResourcePack(
            productId = "ostirotix_pack_grand_codex",
            name = "Grand Codex relié",
            desc = "Le pack premium pour accélérer une progression long terme.",
            coins = 5200,
            pages = 45,
            ink = 700,
            priceLabel = "19,99 €",
        ),
    )

    fun upgradeById(id: String) = upgrades.first { it.id == id }

    fun upgradeCost(u: Upgrade, level: Int): UpgradeCost {
        val nextLevel = level + 1
        val coins = (u.baseCost * 1.38.pow(level)).roundToInt()
        val pages = if (nextLevel >= u.pageCostStart) {
            1 + ((nextLevel - u.pageCostStart) / 3)
        } else 0
        val ink = if (nextLevel >= u.inkCostStart) {
            8 + ((nextLevel - u.inkCostStart) / 2) * 4
        } else 0
        return UpgradeCost(coins, pages, ink)
    }

    fun costText(cost: UpgradeCost): String = buildList {
        add("${cost.coins} pièces")
        if (cost.pages > 0) add("${cost.pages} pages")
        if (cost.ink > 0) add("${cost.ink} encre")
    }.joinToString(" · ")

    fun buyUpgrade(prefs: Prefs, id: String): Boolean {
        val u = upgradeById(id)
        val lvl = prefs.upgradeLevel(id)
        if (lvl >= u.maxLevel) return false
        val cost = upgradeCost(u, lvl)
        if (prefs.coins < cost.coins || prefs.pages < cost.pages || prefs.ink < cost.ink) return false
        prefs.coins -= cost.coins
        prefs.pages -= cost.pages
        prefs.ink -= cost.ink
        prefs.setUpgradeLevel(id, lvl + 1)
        return true
    }

    fun canAfford(prefs: Prefs, cost: UpgradeCost): Boolean =
        prefs.coins >= cost.coins && prefs.pages >= cost.pages && prefs.ink >= cost.ink

    fun missingText(prefs: Prefs, cost: UpgradeCost): String = buildList {
        if (prefs.coins < cost.coins) add("${cost.coins - prefs.coins} pièces")
        if (prefs.pages < cost.pages) add("${cost.pages - prefs.pages} pages")
        if (prefs.ink < cost.ink) add("${cost.ink - prefs.ink} encre")
    }.joinToString(" · ")

    /**
     * Placeholder propre pour Google Play Billing.
     * À connecter plus tard à BillingClient avec productId, achat signé, validation serveur
     * puis crédit des ressources seulement après confirmation officielle de Google Play.
     */
    fun requestResourcePackPurchase(pack: ResourcePack): Boolean {
        val productId = pack.productId
        return productId.isNotBlank() && false
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
