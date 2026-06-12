package com.ostirotix.app

import android.content.Context
import com.ostirotix.app.data.Prefs
import com.ostirotix.app.data.SemanticEngine
import com.ostirotix.app.data.api.ApiClient

/** Injection simple pour le MVP (remplaçable par Hilt plus tard). */
object ServiceLocator {
    lateinit var engine: SemanticEngine
        private set
    lateinit var prefs: Prefs
        private set
    lateinit var api: ApiClient
        private set

    fun init(context: Context) {
        if (::engine.isInitialized) return
        val app = context.applicationContext
        prefs = Prefs(app)
        engine = SemanticEngine(app)
        api = ApiClient(prefs)
    }
}
