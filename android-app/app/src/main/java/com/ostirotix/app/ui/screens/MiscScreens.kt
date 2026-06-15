package com.ostirotix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ostirotix.app.BuildConfig
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.auth.AuthMode
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.LeatherRed
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.vm.AccountViewModel
import com.ostirotix.app.vm.MultiViewModel

@Composable
private fun PageHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
        }
        Text(
            title, fontFamily = Garamond, fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontFamily = Garamond, fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold, color = InkDark)
}

// ---------------------------------------------------------------------------
// OPTIONS (version joueur : compte, langue, jeu, boutique, confidentialité)
// ---------------------------------------------------------------------------

private enum class LegalPage { PRIVACY, TERMS, LICENSES, DELETE }

@Composable
fun SettingsScreen(
    vm: MultiViewModel,
    onLibrary: () -> Unit,
    onAuth: (AuthMode) -> Unit,
    onBack: () -> Unit,
) {
    val prefs = ServiceLocator.prefs
    val mstate by vm.state.collectAsState()
    var haptics by remember { mutableStateOf(prefs.hapticsEnabled) }
    var lang by remember { mutableStateOf(prefs.language) }
    var url by rememberSaveable { mutableStateOf(prefs.serverUrl) }
    var legal by remember { mutableStateOf<LegalPage?>(null) }

    Column(Modifier.fillMaxSize().background(WoodDark).padding(horizontal = 20.dp)) {
        PageHeader("OPTIONS", onBack)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ---- Compte ----
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle("Compte")
                        val acc = mstate.account?.takeIf { !it.isGuest }
                        if (acc == null) {
                            Text("Tu joues actuellement en invité.",
                                color = InkDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Mot du jour et Recherche libre restent jouables. Un compte est requis pour les duels, le classement, le profil en ligne et les achats.",
                                color = InkSoft, fontSize = 12.sp, lineHeight = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LeatherButton("Créer un compte",
                                    onClick = { onAuth(AuthMode.REGISTER) },
                                    enabled = !mstate.busy,
                                    modifier = Modifier.weight(1f))
                                LeatherButton("Se connecter",
                                    onClick = { onAuth(AuthMode.LOGIN) },
                                    enabled = !mstate.busy,
                                    modifier = Modifier.weight(1f))
                            }
                            mstate.error?.let { Text(it, color = SealRed, fontSize = 12.sp) }
                        } else {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(acc.username, fontFamily = Garamond, fontSize = 19.sp,
                                        fontWeight = FontWeight.SemiBold, color = InkDark)
                                    Text(acc.email ?: "Compte enregistré", color = InkSoft, fontSize = 12.sp)
                                    Text("Niveau ${acc.level}", color = InkSoft, fontSize = 11.sp)
                                }
                                Text("Se déconnecter", color = SealRed, fontSize = 13.sp,
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                        .clickable { vm.logout() }.padding(8.dp))
                            }
                        }
                    }
                }
            }

            // ---- Langue ----
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle("Langue")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LangChip("Français", selected = lang == "fr") {
                                lang = "fr"; prefs.language = "fr"
                            }
                            LangChip("English — à venir", selected = false, enabled = false) {}
                        }
                    }
                }
            }

            // ---- Jeu ----
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("Jeu")
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Retour haptique", fontFamily = Garamond, fontSize = 16.sp, color = InkDark)
                                Text("Vibration sur les mots très chauds", color = InkSoft, fontSize = 12.sp)
                            }
                            Switch(
                                checked = haptics,
                                onCheckedChange = { haptics = it; prefs.hapticsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldOld,
                                    checkedTrackColor = LeatherRed,
                                    uncheckedThumbColor = InkSoft,
                                    uncheckedTrackColor = ParchmentShadow,
                                ),
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Tutoriel", fontFamily = Garamond, fontSize = 16.sp, color = InkDark)
                                Text("Revoir l'introduction au jeu", color = InkSoft, fontSize = 12.sp)
                            }
                            Text("Revoir", color = InkBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .clickable { prefs.tutorialSeen = false; onBack() }.padding(8.dp))
                        }
                    }
                }
            }

            // ---- Boutique ----
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle("Boutique")
                        Text("Améliorations de la bibliothèque et recharge d'encre.",
                            color = InkSoft, fontSize = 12.sp)
                        LeatherButton("Ouvrir la Bibliothèque", onClick = onLibrary,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // ---- Serveur (uniquement en build de développement) ----
            if (BuildConfig.DEBUG) {
                item {
                    ParchmentCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Serveur (développement)")
                            Text("Section invisible dans la version publiée.",
                                color = InkSoft, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                            TextField(
                                value = url,
                                onValueChange = { url = it },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = Garamond, fontSize = 14.sp, color = InkDark),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = InkBlue,
                                    unfocusedIndicatorColor = InkSoft.copy(alpha = 0.5f),
                                    cursorColor = InkBlue,
                                    focusedTextColor = InkDark,
                                    unfocusedTextColor = InkDark,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            LeatherButton("Enregistrer l'adresse",
                                onClick = { prefs.serverUrl = url },
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // ---- Confidentialité et mentions ----
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                        SectionTitle("Confidentialité et mentions")
                        Spacer(Modifier.height(4.dp))
                        LegalRow("Politique de confidentialité") { legal = LegalPage.PRIVACY }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow.copy(alpha = 0.5f)))
                        LegalRow("Conditions d'utilisation") { legal = LegalPage.TERMS }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow.copy(alpha = 0.5f)))
                        LegalRow("Licences open source") { legal = LegalPage.LICENSES }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow.copy(alpha = 0.5f)))
                        LegalRow("Supprimer mes données", color = SealRed) { legal = LegalPage.DELETE }
                    }
                }
            }

            item {
                Text(
                    "Ostirotix — version ${BuildConfig.VERSION_NAME}",
                    color = InkSoft, fontSize = 11.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                )
            }
        }
    }

    when (legal) {
        LegalPage.PRIVACY -> ParchmentDialog(
            title = "Politique de confidentialité",
            body = "Ostirotix respecte ta vie privée.\n\n" +
                "Mode solo : aucune donnée n'est collectée ni transmise. Ta progression " +
                "(pièces, encre, améliorations, statistiques, série) est stockée uniquement " +
                "sur ton appareil.\n\n" +
                "Mode duel : si tu crées un compte, seuls un pseudonyme et tes résultats de " +
                "parties (score, classement) sont conservés sur le serveur de jeu. Aucune " +
                "adresse e-mail, aucun mot de passe, aucune donnée personnelle n'est demandée.\n\n" +
                "Aucune publicité, aucun traceur, aucun partage avec des tiers.\n\n" +
                "Tu peux effacer toutes tes données locales à tout moment via " +
                "« Supprimer mes données ».",
            onClose = { legal = null },
        )
        LegalPage.TERMS -> ParchmentDialog(
            title = "Conditions d'utilisation",
            body = "En jouant à Ostirotix, tu acceptes ces conditions :\n\n" +
                "1. Le jeu est fourni tel quel, sans garantie.\n" +
                "2. Le mode duel exige un comportement respectueux : pseudonyme correct, pas de triche.\n" +
                "3. Les comptes abusifs peuvent être supprimés.\n" +
                "4. La progression locale n'est pas sauvegardée en ligne : elle peut être " +
                "perdue en cas de désinstallation.\n" +
                "5. Le jeu est destiné à un public de 13 ans et plus.",
            onClose = { legal = null },
        )
        LegalPage.LICENSES -> ParchmentDialog(
            title = "Licences open source",
            body = "Ostirotix utilise les composants suivants :\n\n" +
                "EB Garamond — police de caractères, SIL Open Font License 1.1\n\n" +
                "Jetpack Compose et bibliothèques AndroidX — Apache License 2.0\n\n" +
                "Kotlin et kotlinx.coroutines — Apache License 2.0\n\n" +
                "OkHttp (Square) — Apache License 2.0\n\n" +
                "Merci aux auteurs de ces projets.",
            onClose = { legal = null },
        )
        LegalPage.DELETE -> ParchmentDialog(
            title = "Supprimer mes données",
            body = "Cette action efface définitivement ta progression locale : pièces, pages " +
                "rares, encre, améliorations, statistiques et série quotidienne. " +
                "Elle te déconnecte aussi du mode duel.\n\nCette action est irréversible.",
            confirmLabel = "Tout effacer",
            onConfirm = {
                prefs.clearAll()
                vm.logout()
                haptics = prefs.hapticsEnabled
                lang = prefs.language
                url = prefs.serverUrl
            },
            onClose = { legal = null },
        )
        null -> {}
    }
}

@Composable
private fun LangChip(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (selected) LeatherRed else Color.Transparent)
            .border(1.dp,
                if (selected) GoldOld else InkSoft.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, fontFamily = Garamond, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = if (selected) PageIvory else if (enabled) InkDark else InkSoft)
    }
}

@Composable
private fun LegalRow(label: String, color: Color = InkDark, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = color, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("›", color = InkSoft, fontSize = 16.sp)
    }
}

@Composable
private fun ParchmentDialog(
    title: String,
    body: String,
    confirmLabel: String? = null,
    onConfirm: (() -> Unit)? = null,
    onClose: () -> Unit,
) {
    Dialog(onDismissRequest = onClose) {
        ParchmentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(title, fontFamily = Garamond, fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(Modifier.height(10.dp))
                Text(
                    body, color = InkDark, fontSize = 13.sp, lineHeight = 19.sp,
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                )
                Spacer(Modifier.height(16.dp))
                if (confirmLabel != null && onConfirm != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LeatherButton("Annuler", onClick = onClose, modifier = Modifier.weight(1f))
                        LeatherButton(confirmLabel,
                            onClick = { onConfirm(); onClose() },
                            modifier = Modifier.weight(1f))
                    }
                } else {
                    LeatherButton("Fermer", onClick = onClose, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CLASSEMENT
// ---------------------------------------------------------------------------

@Composable
fun LeaderboardScreen(vm: AccountViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.loadLeaderboard() }

    Column(
        Modifier.fillMaxSize().background(WoodDark).padding(horizontal = 20.dp),
    ) {
        PageHeader("CLASSEMENT", onBack)
        Spacer(Modifier.height(8.dp))
        ParchmentCard(Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldOld)
                }
                state.error != null -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Registre inaccessible", fontFamily = Garamond, fontSize = 20.sp,
                        color = InkDark)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Le classement mondial nécessite une connexion.",
                        color = InkSoft, fontSize = 13.sp, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    LeatherButton("Réessayer", onClick = { vm.loadLeaderboard() },
                        modifier = Modifier.fillMaxWidth(0.7f))
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    itemsIndexed(state.leaderboard) { idx, entry ->
                        LeaderRow(idx + 1, entry.username, entry.rating, entry.games, entry.wins)
                        if (idx < state.leaderboard.lastIndex)
                            Box(Modifier.fillMaxWidth().height(1.dp)
                                .background(ParchmentShadow.copy(alpha = 0.5f)))
                    }
                    if (state.leaderboard.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center) {
                                Text(
                                    "Registre vierge — soyez le premier archiviste.",
                                    fontFamily = Garamond, fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp, color = InkSoft, textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LeaderRow(rank: Int, name: String, rating: Int, games: Int, wins: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "#$rank", fontFamily = Garamond, fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) GoldOld else InkSoft,
            modifier = Modifier.width(36.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(name, fontFamily = Garamond, fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold, color = InkDark)
            Text("$games parties · $wins victoires", color = InkSoft, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$rating", fontFamily = Garamond, fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = InkDark)
            Text("ELO", color = InkSoft, fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// PROFIL
// ---------------------------------------------------------------------------

@Composable
fun ProfileScreen(vm: AccountViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val prefs = ServiceLocator.prefs
    LaunchedEffect(Unit) { vm.loadProfile() }

    Column(Modifier.fillMaxSize().background(WoodDark).padding(horizontal = 20.dp)) {
        PageHeader("PROFIL", onBack)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Archives locales", fontFamily = Garamond, fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        StatLine("Parties jouées", "${prefs.soloPlayed}")
                        StatLine("Parties gagnées", "${prefs.soloWon}")
                        if (prefs.bestAttempts > 0) StatLine("Meilleur score",
                            "${prefs.bestAttempts} essais")
                        StatLine("Série actuelle", "${prefs.streak} jour(s)")
                        StatLine("Meilleure série", "${prefs.bestStreak} jour(s)")
                    }
                }
            }
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Registre du duelliste", fontFamily = Garamond, fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        when {
                            state.loading -> Box(Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldOld)
                            }
                            prefs.account == null -> Text(
                                "Connecte-toi dans les Options pour voir tes statistiques multijoueur.",
                                color = InkSoft, fontSize = 13.sp, fontStyle = FontStyle.Italic,
                            )
                            state.error != null -> Text(
                                "Statistiques indisponibles hors connexion.",
                                color = InkSoft, fontSize = 12.sp,
                            )
                            state.profile != null -> {
                                val p = state.profile!!
                                StatLine("Chercheur", p.username)
                                StatLine("Classement ELO", "${p.rating}")
                                if (p.bestRating > 0) StatLine("Meilleur ELO", "${p.bestRating}")
                                StatLine("Parties", "${p.games}")
                                StatLine("Victoires", "${p.wins}")
                                if (p.games > 0) StatLine("Taux de victoire",
                                    "${(p.winrate * 100).toInt()}%")
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = InkSoft, fontSize = 14.sp)
        Text(value, fontFamily = Garamond, fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold, color = InkDark)
    }
}
