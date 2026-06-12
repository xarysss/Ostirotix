package com.ostirotix.app.data.api

import com.ostirotix.app.data.Prefs
import com.ostirotix.app.data.model.LeaderEntry
import com.ostirotix.app.data.model.ProfileData
import com.ostirotix.app.data.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiException(message: String) : Exception(message)

/** Client REST + WebSocket vers le backend FastAPI (émulateur : http://10.0.2.2:8000). */
class ApiClient(private val prefs: Prefs) {

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private val json = "application/json; charset=utf-8".toMediaType()
    private fun base() = prefs.serverUrl

    private suspend fun get(path: String): JSONObject = withContext(Dispatchers.IO) {
        exec(Request.Builder().url(base() + path).build())
    }

    private suspend fun post(path: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        exec(Request.Builder().url(base() + path).post(body.toString().toRequestBody(json)).build())
    }

    private fun exec(req: Request): JSONObject {
        client.newCall(req).execute().use { resp ->
            val txt = resp.body?.string() ?: "{}"
            if (!resp.isSuccessful) {
                val detail = runCatching { JSONObject(txt).optString("detail") }.getOrNull()
                throw ApiException(if (!detail.isNullOrBlank()) detail else "Erreur serveur (${resp.code})")
            }
            return JSONObject(txt)
        }
    }

    suspend fun health(): Boolean = runCatching { get("/health").optString("status") == "ok" }.getOrDefault(false)

    suspend fun register(username: String): UserAccount {
        val o = post("/auth/register", JSONObject().put("username", username))
        return UserAccount(o.getString("id"), o.getString("username"), o.getInt("is_guest") == 1)
    }

    suspend fun guestLogin(): UserAccount {
        val o = post("/auth/guest", JSONObject())
        return UserAccount(o.getString("id"), o.getString("username"), true)
    }

    suspend fun createRoom(userId: String, ranked: Boolean, bot: Boolean = false): String =
        post("/rooms", JSONObject().put("userId", userId).put("ranked", ranked).put("bot", bot))
            .getString("roomCode")

    suspend fun joinRoom(code: String, userId: String): Boolean {
        post("/rooms/${code.uppercase()}/join", JSONObject().put("userId", userId))
        return true
    }

    suspend fun roomRanked(code: String, userId: String): Boolean =
        post("/rooms/${code.uppercase()}/join", JSONObject().put("userId", userId)).getBoolean("ranked")

    suspend fun leaderboard(): List<LeaderEntry> {
        val arr = get("/leaderboard").getJSONArray("players")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            LeaderEntry(o.getString("username"), o.getInt("rating"), o.getInt("games"), o.getInt("wins"))
        }
    }

    suspend fun profile(userId: String): ProfileData {
        val o = get("/profile/$userId")
        return ProfileData(o.getString("username"), o.getInt("rating"), o.getInt("games"),
            o.getInt("wins"), o.optDouble("winrate", 0.0), o.getInt("best_rating"))
    }

    /** Ouvre le WebSocket de room. Messages JSON transmis au callback. */
    fun openRoomSocket(
        code: String,
        userId: String,
        onMessage: (JSONObject) -> Unit,
        onClosed: (String) -> Unit,
    ): WebSocket {
        val wsUrl = base().replaceFirst("http", "ws") + "/ws/room/${code.uppercase()}?userId=$userId"
        val req = Request.Builder().url(wsUrl).build()
        return client.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching { onMessage(JSONObject(text)) }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onClosed(t.message ?: "Connexion perdue")
            }
            override fun onClosed(webSocket: WebSocket, codeWs: Int, reason: String) {
                onClosed(reason.ifBlank { "Connexion fermée" })
            }
        })
    }
}
