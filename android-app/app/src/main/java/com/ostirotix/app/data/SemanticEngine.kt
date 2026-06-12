package com.ostirotix.app.data

import android.content.Context
import com.ostirotix.app.data.model.GuessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import java.text.Normalizer
import java.time.LocalDate
import kotlin.random.Random

/**
 * Moteur sémantique local (hors-ligne). Charge semantic_pack_demo.json depuis les assets.
 * Formules identiques au backend (semantic.py) pour cohérence solo/multi.
 * Remplaçable plus tard par un appel backend word2vec : même interface guess().
 */
class SemanticEngine(private val context: Context) {

    private class Secret(val word: String, val norm: String, val ranks: Map<String, Pair<Int, String>>)

    private var secrets: List<Secret> = emptyList()
    private var dico: Set<String> = emptySet()
    private val mutex = Mutex()
    val isLoaded get() = secrets.isNotEmpty()

    suspend fun ensureLoaded() = mutex.withLock {
        if (isLoaded) return@withLock
        withContext(Dispatchers.IO) {
            val txt = context.assets.open("semantic_pack_demo.json")
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(txt)
            val secArr = root.getJSONArray("secrets")
            val list = ArrayList<Secret>(secArr.length())
            for (i in 0 until secArr.length()) {
                val o = secArr.getJSONObject(i)
                val word = o.getString("word")
                val neigh = o.getJSONArray("neighbors")
                val ranks = HashMap<String, Pair<Int, String>>(neigh.length())
                for (j in 0 until neigh.length()) {
                    val pair = neigh.getJSONArray(j)
                    val w = pair.getString(0)
                    ranks[norm(w)] = Pair(j + 1, w)
                }
                list.add(Secret(word, norm(word), ranks))
            }
            val dicArr = root.getJSONArray("dictionary")
            val d = HashSet<String>(dicArr.length())
            for (i in 0 until dicArr.length()) d.add(norm(dicArr.getString(i)))
            secrets = list
            dico = d
        }
    }

    fun secretCount() = secrets.size

    /** Même formule que le backend : ordinal proleptique grégorien (toordinal Python). */
    fun dailyIndex(): Int {
        val ordinal = LocalDate.now().toEpochDay() + 719163L
        return ((ordinal * 31 + 7) % secretCount()).toInt()
    }

    fun randomIndex(): Int = Random.nextInt(secretCount())

    fun secretWord(index: Int) = secrets[index].word

    fun guess(secretIndex: Int, word: String): GuessResult {
        val s = secrets[secretIndex]
        val n = norm(word)
        if (n.isEmpty()) return GuessResult(-100.0, -1, 0, recognized = false, exact = false)
        if (n == s.norm) return GuessResult(100.0, 0, 1000, recognized = true, exact = true)
        val hit = s.ranks[n]
        if (hit != null) {
            val rank = hit.first
            return GuessResult(tempForRank(rank), rank, if (rank <= 1000) 1000 - rank else 0,
                recognized = true, exact = false)
        }
        if (n in dico) {
            return GuessResult((-20 + hash(n + s.norm) % 50).toDouble(), -1, 0,
                recognized = true, exact = false)
        }
        return GuessResult((-90 + hash(n + s.norm) % 41).toDouble(), -1, 0,
            recognized = false, exact = false)
    }

    /** Révèle le voisin dont le rang est le plus proche de la cible (indices). */
    fun hintAtRank(secretIndex: Int, targetRank: Int): String {
        val s = secrets[secretIndex]
        return s.ranks.values.minByOrNull { kotlin.math.abs(it.first - targetRank) }!!.second
    }

    companion object {
        fun norm(w: String): String {
            val t = w.trim().lowercase()
            val nfd = Normalizer.normalize(t, Normalizer.Form.NFD)
            return nfd.filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
        }

        fun tempForRank(rank: Int): Double = when {
            rank <= 10 -> (99 - (rank - 1)).toDouble()
            rank <= 100 -> Math.round((89 - (rank - 11) * (19.0 / 89)) * 10) / 10.0
            rank <= 500 -> Math.round((69 - (rank - 101) * (24.0 / 399)) * 10) / 10.0
            rank <= 1000 -> Math.round((44 - (rank - 501) * (14.0 / 499)) * 10) / 10.0
            else -> 0.0
        }

        /** Mêmes 8 premiers hex du md5 que le backend. */
        fun hash(s: String): Long {
            val d = MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.UTF_8))
            var v = 0L
            for (i in 0 until 4) v = (v shl 8) or (d[i].toLong() and 0xFF)
            return v
        }
    }
}
