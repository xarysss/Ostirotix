package com.ostirotix.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.model.Guess
import com.ostirotix.app.data.model.GuessResult
import com.ostirotix.app.data.model.MatchPlayerResult
import com.ostirotix.app.data.model.PlayerInfo
import com.ostirotix.app.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import org.json.JSONObject

enum class MultiStep { SETUP, LOBBY, PLAYING, RESULT }

data class MultiUiState(
    val account: UserAccount? = null,
    val step: MultiStep = MultiStep.SETUP,
    val busy: Boolean = false,
    val error: String? = null,
    val roomCode: String = "",
    val ranked: Boolean = false,
    val isHost: Boolean = false,
    val players: List<PlayerInfo> = emptyList(),
    val events: List<String> = emptyList(),
    val myGuesses: List<Guess> = emptyList(),
    val last: Guess? = null,
    val attempts: Int = 0,
    val endgameWinner: String? = null,
    val secondsLeft: Int? = null,
    val results: List<MatchPlayerResult> = emptyList(),
    val secret: String? = null,
)

class MultiViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val prefs = ServiceLocator.prefs

    private val _state = MutableStateFlow(MultiUiState(account = ServiceLocator.prefs.account))
    val state: StateFlow<MultiUiState> = _state

    private var ws: WebSocket? = null

    // --- Compte ---
    fun register(name: String) = launchBusy {
        val acc = api.register(name).copy(isGuest = false)
        prefs.account = acc
        _state.update { it.copy(account = acc) }
    }

    fun guestLogin() = launchBusy {
        val acc = api.guestLogin()
        prefs.account = acc
        _state.update { it.copy(account = acc) }
    }

    fun logout() {
        ServiceLocator.auth.logout()
        _state.update { it.copy(account = null) }
    }

    fun syncAccount() {
        _state.update { it.copy(account = ServiceLocator.auth.currentUser()) }
    }

    // --- Rooms ---
    fun createRoom(ranked: Boolean, bot: Boolean = false) = launchBusy {
        val acc = authenticatedAccount()
        val code = api.createRoom(acc.id, ranked, bot)
        _state.update { it.copy(roomCode = code, ranked = ranked, isHost = true) }
        connect(code, acc.id)
    }

    fun joinRoom(code: String) = launchBusy {
        val acc = authenticatedAccount()
        val ranked = api.roomRanked(code, acc.id)
        _state.update { it.copy(roomCode = code.uppercase(), ranked = ranked, isHost = false) }
        connect(code, acc.id)
    }

    private fun authenticatedAccount(): UserAccount =
        ServiceLocator.auth.currentUser() ?: throw Exception("Connexion requise")

    private fun connect(code: String, userId: String) {
        ws?.close(1000, null)
        ws = api.openRoomSocket(code, userId,
            onMessage = { msg -> viewModelScope.launch { handle(msg) } },
            onClosed = { reason ->
                _state.update { st ->
                    if (st.step == MultiStep.RESULT || st.step == MultiStep.SETUP) st
                    else st.copy(error = reason, step = MultiStep.SETUP)
                }
            })
        _state.update { it.copy(step = MultiStep.LOBBY, events = emptyList(), myGuesses = emptyList(),
            attempts = 0, last = null, results = emptyList(), secret = null,
            endgameWinner = null, secondsLeft = null) }
    }

    fun startGame() {
        ws?.send(JSONObject().put("type", "start").toString())
    }

    fun sendGuess(word: String) {
        if (word.isBlank()) return
        ws?.send(JSONObject().put("type", "guess").put("word", word.trim()).toString())
    }

    fun leaveRoom() {
        ws?.close(1000, null)
        ws = null
        _state.update { it.copy(step = MultiStep.SETUP, roomCode = "", players = emptyList()) }
    }

    private fun handle(msg: JSONObject) {
        when (msg.optString("type")) {
            "players" -> {
                val arr = msg.getJSONArray("players")
                val list = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    PlayerInfo(o.getString("id"), o.getString("username"), o.getDouble("maxTemp"),
                        o.getInt("points"), o.getInt("attempts"), o.getBoolean("found"))
                }
                _state.update { st ->
                    val hostId = msg.optString("hostId", "")
                    st.copy(players = list,
                        isHost = if (hostId.isNotEmpty()) st.account?.id == hostId else st.isHost)
                }
            }
            "game_started" -> _state.update { it.copy(step = MultiStep.PLAYING,
                events = listOf("La partie commence ! Trouvez le mot secret.")) }
            "guess_result" -> {
                val r = GuessResult(msg.getDouble("temp"), msg.getInt("rank"),
                    msg.getInt("progression"), msg.getBoolean("recognized"), msg.getBoolean("exact"))
                _state.update { st ->
                    val best = st.myGuesses.maxByOrNull { it.result.temp }
                    val g = Guess(msg.getString("word"), r, st.attempts + 1,
                        r.recognized && (best == null || r.temp > best.result.temp))
                    st.copy(myGuesses = listOf(g) + st.myGuesses, attempts = st.attempts + 1, last = g)
                }
            }
            "guess_rejected" -> addEvent(msg.optString("reason"))
            "player_progress" -> addEvent("${msg.optString("username")} a atteint ${msg.optDouble("maxTemp").toInt()}°")
            "milestone" -> addEvent("${msg.optString("username")} premier à ${msg.optInt("temp")}° !")
            "endgame_started" -> {
                _state.update { it.copy(endgameWinner = msg.optString("winner"),
                    secondsLeft = msg.optInt("secondsLeft", 30)) }
                addEvent("${msg.optString("winner")} a trouvé ! ${msg.optInt("secondsLeft", 30)}s pour marquer des points.")
            }
            "match_end" -> {
                val arr = msg.getJSONArray("results")
                val res = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    MatchPlayerResult(o.getInt("pos"), o.getString("username"), o.getInt("perfScore"),
                        o.getDouble("maxTemp"), o.getInt("points"), o.getBoolean("found"),
                        o.getInt("eloDelta"), o.getInt("attempts"))
                }
                _state.update { it.copy(step = MultiStep.RESULT, results = res,
                    secret = msg.optString("secret")) }
                ws?.close(1000, null); ws = null
            }
            "error" -> _state.update { it.copy(error = msg.optString("message")) }
        }
    }

    private fun addEvent(e: String) =
        _state.update { it.copy(events = (listOf(e) + it.events).take(4)) }

    private fun launchBusy(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try { block() } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Erreur réseau — backend lancé ?") }
            }
            _state.update { it.copy(busy = false) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        ws?.close(1000, null)
    }
}
