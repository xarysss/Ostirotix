package com.ostirotix.app.data

import android.content.Context
import com.ostirotix.app.data.model.UserAccount
import java.time.LocalDate

/** Préférences locales : tutoriel, réglages, compte, stats solo et streak. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("ostirotix", Context.MODE_PRIVATE)

    var tutorialSeen: Boolean
        get() = sp.getBoolean("tutorialSeen", false)
        set(v) = sp.edit().putBoolean("tutorialSeen", v).apply()

    var hapticsEnabled: Boolean
        get() = sp.getBoolean("haptics", true)
        set(v) = sp.edit().putBoolean("haptics", v).apply()

    var language: String
        get() = sp.getString("language", "fr")!!
        set(v) = sp.edit().putString("language", v).apply()

    /** RGPD : efface toutes les données locales (réglages, économie, stats, compte). */
    fun clearAll() = sp.edit().clear().apply()

    var serverUrl: String
        get() = sp.getString("serverUrl", "http://10.0.2.2:8000")!!
        set(v) = sp.edit().putString("serverUrl", v.trim().trimEnd('/')).apply()

    var account: UserAccount?
        get() {
            val id = sp.getString("accId", null) ?: return null
            return UserAccount(id, sp.getString("accName", "?")!!, sp.getBoolean("accGuest", true))
        }
        set(v) {
            if (v == null) sp.edit().remove("accId").remove("accName").remove("accGuest").apply()
            else sp.edit().putString("accId", v.id).putString("accName", v.username)
                .putBoolean("accGuest", v.isGuest).apply()
        }

    // --- Économie : pièces, pages rares, encre ---
    var coins: Int
        get() = sp.getInt("coins", 100)
        set(v) = sp.edit().putInt("coins", v.coerceAtLeast(0)).apply()
    var pages: Int
        get() = sp.getInt("pages", 0)
        set(v) = sp.edit().putInt("pages", v.coerceAtLeast(0)).apply()
    var ink: Int
        get() = sp.getInt("ink", 45)
        set(v) = sp.edit().putInt("ink", v.coerceAtLeast(0)).apply()

    fun upgradeLevel(id: String): Int = sp.getInt("upg_$id", 0)
    fun setUpgradeLevel(id: String, lvl: Int) = sp.edit().putInt("upg_$id", lvl).apply()

    // --- Stats solo ---
    var soloPlayed: Int
        get() = sp.getInt("soloPlayed", 0)
        set(v) = sp.edit().putInt("soloPlayed", v).apply()
    var soloWon: Int
        get() = sp.getInt("soloWon", 0)
        set(v) = sp.edit().putInt("soloWon", v).apply()
    var bestAttempts: Int
        get() = sp.getInt("bestAttempts", 0) // 0 = jamais gagné
        set(v) = sp.edit().putInt("bestAttempts", v).apply()

    // --- Daily + streak non punitive (rater un jour ne remet pas à zéro l'affichage du record) ---
    var dailyWonDate: String?
        get() = sp.getString("dailyWonDate", null)
        set(v) = sp.edit().putString("dailyWonDate", v).apply()
    var streak: Int
        get() = sp.getInt("streak", 0)
        set(v) = sp.edit().putInt("streak", v).apply()
    var bestStreak: Int
        get() = sp.getInt("bestStreak", 0)
        set(v) = sp.edit().putInt("bestStreak", v).apply()

    fun dailyAlreadyWonToday() = dailyWonDate == LocalDate.now().toString()

    /** À appeler quand le mot du jour est trouvé. */
    fun registerDailyWin() {
        val today = LocalDate.now()
        if (dailyWonDate == today.toString()) return
        streak = if (dailyWonDate == today.minusDays(1).toString()) streak + 1 else 1
        if (streak > bestStreak) bestStreak = streak
        dailyWonDate = today.toString()
    }
}
