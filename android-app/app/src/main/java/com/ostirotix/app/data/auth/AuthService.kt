package com.ostirotix.app.data.auth

import com.ostirotix.app.data.Prefs
import com.ostirotix.app.data.api.ApiClient
import com.ostirotix.app.data.model.UserAccount
import java.util.UUID

enum class AuthMode { LOGIN, REGISTER }

data class AuthResult(
    val success: Boolean,
    val account: UserAccount? = null,
    val message: String? = null,
)

class AuthService(
    private val prefs: Prefs,
    private val api: ApiClient,
) {
    var pendingTarget: String? = null
        private set
    var pendingMessage: String? = null
        private set
    var pendingMode: AuthMode = AuthMode.LOGIN
        private set

    fun currentUser(): UserAccount? = prefs.account?.takeIf { !it.isGuest }

    fun isAuthenticated(): Boolean = prefs.isAuthenticated

    fun requireAuth(target: String, message: String, mode: AuthMode = AuthMode.LOGIN) {
        pendingTarget = target
        pendingMessage = message
        pendingMode = mode
    }

    fun consumePendingTarget(): String? {
        val target = pendingTarget
        pendingTarget = null
        pendingMessage = null
        pendingMode = AuthMode.LOGIN
        return target
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        acceptsMarketing: Boolean,
        acceptsTerms: Boolean,
    ): AuthResult {
        val cleanName = username.trim()
        val cleanEmail = email.trim()
        val validation = validateRegister(cleanName, cleanEmail, password, acceptsTerms)
        if (validation != null) return AuthResult(false, message = validation)

        val serverAccount = runCatching { api.register(cleanName) }.getOrNull()
        val account = (serverAccount ?: UserAccount(
            id = UUID.randomUUID().toString(),
            username = cleanName,
            isGuest = false,
        )).copy(isGuest = false, email = cleanEmail, level = 1)

        // Placeholder propre : le backend actuel ne gère pas encore email, mot de passe,
        // consentements ni token. Ces champs sont prêts à être branchés à /auth/register.
        if (acceptsMarketing) {
            // A transmettre plus tard au backend CRM/newsletter si l'utilisateur confirme.
        }

        prefs.account = account
        prefs.rememberAuthProfile(account)
        return AuthResult(true, account)
    }

    fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        if (!looksLikeEmail(cleanEmail)) {
            return AuthResult(false, message = "Entre une adresse mail valide.")
        }
        if (password.length < 6) {
            return AuthResult(false, message = "Le mot de passe doit contenir au moins 6 caractères.")
        }

        val saved = prefs.savedAuthAccount(cleanEmail)
            ?: return AuthResult(
                false,
                message = "Connexion email/mot de passe prête à brancher au backend. Crée d'abord un compte sur cet appareil, ou ajoute l'endpoint /auth/login.",
            )

        prefs.account = saved
        return AuthResult(true, saved)
    }

    fun logout() {
        prefs.account = null
    }

    fun googleLogin(): AuthResult {
        // Placeholder volontaire : à relier plus tard à Google Sign-In, Firebase Auth,
        // puis à un échange de token côté backend. Ne crée aucun faux compte Google.
        return AuthResult(false, message = "Connexion Google prête à brancher à Google Sign-In.")
    }

    private fun validateRegister(
        username: String,
        email: String,
        password: String,
        acceptsTerms: Boolean,
    ): String? = when {
        username.length < 3 -> "Choisis un pseudo d'au moins 3 caractères."
        !looksLikeEmail(email) -> "Entre une adresse mail valide."
        password.length < 6 -> "Le mot de passe doit contenir au moins 6 caractères."
        !acceptsTerms -> "Tu dois accepter les conditions et la politique de confidentialité."
        else -> null
    }

    private fun looksLikeEmail(value: String): Boolean =
        value.contains("@") && value.substringAfter("@").contains(".") && !value.contains(" ")
}
