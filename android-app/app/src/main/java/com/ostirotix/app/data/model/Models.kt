package com.ostirotix.app.data.model

/** Résultat d'un essai (mêmes champs que le backend). */
data class GuessResult(
    val temp: Double,
    val rank: Int,          // 0 = exact, -1 = hors liste
    val progression: Int,   // ‰ (0..1000)
    val recognized: Boolean,
    val exact: Boolean,
)

/** Essai affiché dans l'historique. */
data class Guess(
    val word: String,
    val result: GuessResult,
    val order: Int,
    val isNewBest: Boolean,
)

data class UserAccount(val id: String, val username: String, val isGuest: Boolean)

data class PlayerInfo(
    val id: String,
    val username: String,
    val maxTemp: Double,
    val points: Int,
    val attempts: Int,
    val found: Boolean,
)

data class MatchPlayerResult(
    val pos: Int,
    val username: String,
    val perfScore: Int,
    val maxTemp: Double,
    val points: Int,
    val found: Boolean,
    val eloDelta: Int,
    val attempts: Int,
)

data class ProfileData(
    val username: String,
    val rating: Int,
    val games: Int,
    val wins: Int,
    val winrate: Double,
    val bestRating: Int,
)

data class LeaderEntry(val username: String, val rating: Int, val games: Int, val wins: Int)
