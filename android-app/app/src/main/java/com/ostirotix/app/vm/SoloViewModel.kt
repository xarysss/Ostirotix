package com.ostirotix.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.Economy
import com.ostirotix.app.data.Rewards
import com.ostirotix.app.data.SemanticEngine
import com.ostirotix.app.data.model.Guess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SoloMode { DAILY, TRAINING }

data class SoloUiState(
    val loading: Boolean = true,
    val mode: SoloMode = SoloMode.DAILY,
    val secretIndex: Int = -1,
    val guesses: List<Guess> = emptyList(),
    val best: Guess? = null,
    val attempts: Int = 0,
    val hintsUsed: Int = 0,
    val hintWord: String? = null,
    val finished: Boolean = false,
    val won: Boolean = false,
    val revealedWord: String? = null,
    val last: Guess? = null,
    val message: String? = null,
    val streak: Int = 0,
    // économie
    val coins: Int = 0,
    val pages: Int = 0,
    val ink: Int = 0,
    val hintCost: Int = 15,
    val rewards: Rewards? = null,     // rempli à la victoire
    val lossConsolation: Int = 0,     // pièces du Scribe en cas d'abandon
)

class SoloViewModel : ViewModel() {
    private val engine = ServiceLocator.engine
    private val prefs = ServiceLocator.prefs

    private val _state = MutableStateFlow(SoloUiState())
    val state: StateFlow<SoloUiState> = _state

    val maxHints = 3
    private var startedAt = 0L

    private fun resources(s: SoloUiState) = s.copy(
        coins = prefs.coins, pages = prefs.pages, ink = prefs.ink,
        hintCost = Economy.hintInkCost(prefs), streak = prefs.streak,
    )

    fun start(mode: SoloMode) {
        _state.value = resources(SoloUiState(loading = true, mode = mode))
        startedAt = System.currentTimeMillis()
        viewModelScope.launch {
            engine.ensureLoaded()
            val index = if (mode == SoloMode.DAILY) engine.dailyIndex() else engine.randomIndex()
            _state.update { it.copy(loading = false, secretIndex = index) }
        }
    }

    fun submit(rawWord: String) {
        val s = _state.value
        if (s.loading || s.finished) return
        val word = rawWord.trim()
        if (word.isEmpty()) return
        val n = SemanticEngine.norm(word)
        if (s.guesses.any { SemanticEngine.norm(it.word) == n }) {
            _state.update { it.copy(message = "Mot déjà inscrit au registre.") }
            return
        }
        val r = engine.guess(s.secretIndex, word)
        val isNewBest = r.recognized && (s.best == null || r.temp > s.best.result.temp)
        val g = Guess(word, r, s.attempts + 1, isNewBest)
        val won = r.exact
        var rewards: Rewards? = null
        if (won) {
            // Anti-farm : rejouer le mot du jour déjà gagné ne rapporte rien.
            val dailyRepeat = s.mode == SoloMode.DAILY && prefs.dailyAlreadyWonToday()
            prefs.soloPlayed += 1
            prefs.soloWon += 1
            if (!dailyRepeat &&
                (prefs.bestAttempts == 0 || s.attempts + 1 < prefs.bestAttempts)
            ) prefs.bestAttempts = s.attempts + 1
            if (s.mode == SoloMode.DAILY) prefs.registerDailyWin()
            if (!dailyRepeat) {
                val bestTemps = (s.guesses.map { it.result.temp } + 100.0)
                    .sortedDescending().take(5)
                rewards = Economy.applyWinReward(
                    prefs,
                    attempts = s.attempts + 1,
                    seconds = (System.currentTimeMillis() - startedAt) / 1000,
                    isDaily = s.mode == SoloMode.DAILY,
                    bestTempsAvg = bestTemps.average(),
                )
            }
        }
        _state.update {
            resources(it.copy(
                guesses = listOf(g) + it.guesses,
                best = if (isNewBest || won) g else it.best,
                attempts = it.attempts + 1,
                last = g,
                message = if (!r.recognized) "Mot absent du registre. Il compte glacial." else null,
                finished = won,
                won = won,
                revealedWord = if (won) engine.secretWord(s.secretIndex) else null,
                rewards = rewards,
            ))
        }
    }

    /** Indice : coûte de l'encre, la Loupe améliore le rang révélé. */
    fun useHint() {
        val s = _state.value
        if (s.finished || s.hintsUsed >= maxHints) return
        val cost = Economy.hintInkCost(prefs)
        if (prefs.ink < cost) {
            _state.update { it.copy(message = "Encre insuffisante. Recharge à la Bibliothèque.") }
            return
        }
        prefs.ink -= cost
        val loupe = prefs.upgradeLevel("loupe")
        val rank = Economy.hintTargetRank(s.hintsUsed + 1, loupe)
        val w = engine.hintAtRank(s.secretIndex, rank)
        _state.update { resources(it.copy(hintsUsed = it.hintsUsed + 1, hintWord = w)) }
    }

    fun giveUp() {
        val s = _state.value
        if (s.finished) return
        prefs.soloPlayed += 1
        val consolation = Economy.applyLossReward(prefs)
        _state.update {
            resources(it.copy(finished = true, won = false,
                revealedWord = engine.secretWord(s.secretIndex), lossConsolation = consolation))
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
