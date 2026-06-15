package com.ostirotix.app

import android.content.Context
import com.ostirotix.app.data.Prefs
import com.ostirotix.app.data.SemanticEngine
import com.ostirotix.app.data.api.ApiClient
import com.ostirotix.app.data.auth.AuthService

/** Injection simple pour le MVP (remplaçable par Hilt plus tard). */
object ServiceLocator {
    lateinit var engine: SemanticEngine
        private set
    lateinit var prefs: Prefs
        private set
    lateinit var api: ApiClient
        private set
    lateinit var auth: AuthService
        private set

    fun init(context: Context) {
        if (::engine.isInitialized) return
        val app = context.applicationContext
        prefs = Prefs(app)
        engine = SemanticEngine(app)
        api = ApiClient(prefs)
        auth = AuthService(prefs, api)
    }
}
