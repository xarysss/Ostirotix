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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ui.components.GuessRow
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.PlayerHeatBar
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.vm.MultiStep
import com.ostirotix.app.vm.MultiViewModel

@Composable
fun MultiModeScreen(vm: MultiViewModel, onLobby: () -> Unit, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    var nameInput by rememberSaveable { mutableStateOf("") }
    var codeInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.step) {
        if (state.step == MultiStep.LOBBY) onLobby()
    }

    LazyColumn(
        Modifier.fillMaxSize().background(WoodDark)
            .statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
                }
                Text(
                    "DUEL LEXICAL", fontFamily = Garamond, fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
                )
            }
        }

        item {
            ParchmentCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.account == null) {
                        Text("Identité du chercheur", fontFamily = Garamond, fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        TextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = {
                                Text("Ton nom d'archiviste…", fontFamily = Garamond,
                                    fontStyle = FontStyle.Italic, color = InkSoft)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = Garamond, fontSize = 18.sp, color = InkDark),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
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
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LeatherButton(
                                "Créer un compte",
                                onClick = { vm.register(nameInput.trim()) },
                                enabled = nameInput.isNotBlank() && !state.busy,
                                modifier = Modifier.weight(1f),
                            )
                            LeatherButton(
                                "Invité",
                                onClick = { vm.guestLogin() },
                                enabled = !state.busy,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    state.account!!.username, fontFamily = Garamond,
                                    fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = InkDark,
                                )
                                if (state.account!!.isGuest)
                                    Text("Visiteur temporaire", color = InkSoft, fontSize = 12.sp)
                            }
                            Text(
                                "Déconnexion", color = InkSoft, fontSize = 13.sp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .clickable { vm.logout() }.padding(8.dp),
                            )
                        }
                    }
                    state.error?.let { err ->
                        Text(err, color = SealRed, fontSize = 13.sp)
                        TextButton(onClick = { vm.clearError() }) {
                            Text("Effacer", color = InkSoft, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (state.account != null) {
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Multijoueur", fontFamily = Garamond, fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        Text("Lance une table ouverte. Si aucun autre joueur n'arrive, un bot d'appoint complète le duel.",
                            color = InkSoft, fontSize = 13.sp, lineHeight = 17.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LeatherButton("Non classé",
                                onClick = { vm.createRoom(ranked = false, bot = true) },
                                enabled = !state.busy, modifier = Modifier.weight(1f))
                            LeatherButton("Classé",
                                onClick = { vm.createRoom(ranked = true, bot = true) },
                                enabled = !state.busy, modifier = Modifier.weight(1f))
                        }
                        Text("Le bot cède sa place dès qu'un adversaire humain rejoint l'antichambre.",
                            color = InkSoft, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Jouer avec des amis", fontFamily = Garamond, fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        Text("Créer une salle", fontFamily = Garamond, fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = InkSoft)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LeatherButton("Amicale", onClick = { vm.createRoom(false) },
                                enabled = !state.busy, modifier = Modifier.weight(1f))
                            LeatherButton("Classée", onClick = { vm.createRoom(true) },
                                enabled = !state.busy, modifier = Modifier.weight(1f))
                        }
                        Text("Partage le code de l'antichambre à tes amis pour jouer sans bot d'appoint.",
                            color = InkSoft, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                    }
                }
            }
            item {
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Rejoindre une salle", fontFamily = Garamond, fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold, color = InkDark)
                        TextField(
                            value = codeInput,
                            onValueChange = { codeInput = it.uppercase().take(6) },
                            placeholder = {
                                Text("Code de la salle…", fontFamily = Garamond,
                                    fontStyle = FontStyle.Italic, color = InkSoft)
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = Garamond, fontSize = 22.sp,
                                color = InkDark, letterSpacing = 4.sp),
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
                        LeatherButton(
                            "Rejoindre",
                            onClick = { vm.joinRoom(codeInput) },
                            enabled = codeInput.length >= 4 && !state.busy,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (state.busy) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldSoft, modifier = Modifier.size(28.dp))
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun LobbyScreen(vm: MultiViewModel, onGameStart: () -> Unit, onBack: () -> Unit) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.step) {
        if (state.step == MultiStep.PLAYING) onGameStart()
    }

    Column(
        Modifier.fillMaxSize().background(WoodDark)
            .statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { vm.leaveRoom(); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
            }
            Column {
                Text(
                    "ANTICHAMBRE", fontFamily = Garamond, fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Code : ", color = InkSoft, fontSize = 13.sp)
                    Text(state.roomCode, fontFamily = Garamond, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = GoldSoft, letterSpacing = 3.sp)
                    if (state.ranked) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp))
                                .background(SealRed.copy(alpha = 0.25f))
                                .border(1.dp, SealRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text("CLASSÉ", fontSize = 10.sp, letterSpacing = 1.sp, color = SealRed)
                        }
                    }
                }
            }
        }

        ParchmentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Chercheurs présents", fontFamily = Garamond, fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(Modifier.height(10.dp))
                if (state.players.isEmpty()) {
                    Text("En attente de chercheurs…", color = InkSoft,
                        fontStyle = FontStyle.Italic, fontSize = 14.sp)
                }
                state.players.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(GoldOld))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            p.username + if (state.account?.id == p.id) " (toi)" else "",
                            fontFamily = Garamond, fontSize = 18.sp, color = InkDark,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Text(
            "Partage le code avec tes adversaires.", color = InkSoft,
            fontSize = 13.sp, fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.weight(1f))

        if (state.isHost) {
            LeatherButton(
                if (state.players.size >= 2) "Lancer la partie"
                else "Attendre au moins 2 chercheurs",
                onClick = { vm.startGame() },
                enabled = state.players.size >= 2,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center) {
                Text(
                    "L'archiviste principal lance la partie…",
                    color = GoldSoft, fontFamily = Garamond, fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun MultiGameScreen(vm: MultiViewModel, onMatchEnd: () -> Unit, onQuit: () -> Unit) {
    val state by vm.state.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.step) {
        if (state.step == MultiStep.RESULT) onMatchEnd()
    }

    Box(Modifier.fillMaxSize().background(WoodDark)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .padding(horizontal = 14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "DUEL LEXICAL", fontFamily = Garamond, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
                    )
                    Text("${state.attempts} essais", color = InkSoft, fontSize = 11.sp)
                }
                state.secondsLeft?.let { sec ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(SealRed.copy(alpha = 0.25f))
                            .border(1.dp, SealRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("${sec}s", fontFamily = Garamond, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = SealRed)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    "Quitter", color = InkSoft, fontSize = 13.sp,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .clickable { vm.leaveRoom(); onQuit() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            ParchmentCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.players.forEach { p ->
                        PlayerHeatBar(p.username, p.maxTemp, p.found, state.account?.id == p.id)
                    }
                    if (state.players.isEmpty()) {
                        Text("En attente des joueurs…", color = InkSoft, fontSize = 13.sp)
                    }
                }
            }

            if (state.events.isNotEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    state.events.take(3).forEach { evt ->
                        Text(evt, color = GoldSoft.copy(alpha = 0.85f), fontSize = 12.sp,
                            fontStyle = FontStyle.Italic)
                    }
                }
            }

            ParchmentCard(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Mon registre", fontFamily = Garamond, fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, color = InkDark)
                    Spacer(Modifier.height(4.dp))
                    LazyColumn(Modifier.weight(1f)) {
                        items(state.myGuesses, key = { it.order }) { g ->
                            GuessRow(g, highlight = g.order == state.last?.order)
                        }
                        if (state.myGuesses.isEmpty()) {
                            item {
                                Text("Registre vierge", color = InkSoft,
                                    fontStyle = FontStyle.Italic, fontSize = 14.sp,
                                    modifier = Modifier.padding(16.dp))
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = {
                                Text("Écris ton mot…", fontFamily = Garamond,
                                    fontStyle = FontStyle.Italic, color = InkSoft)
                            },
                            textStyle = TextStyle(fontFamily = Garamond, fontSize = 19.sp, color = InkDark),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = InkBlue,
                                unfocusedIndicatorColor = InkSoft.copy(alpha = 0.6f),
                                cursorColor = InkBlue,
                                focusedTextColor = InkDark,
                                unfocusedTextColor = InkDark,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { vm.sendGuess(input); input = "" },
                            enabled = input.isNotBlank(),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_quill), "Envoyer",
                                tint = if (input.isNotBlank()) SealRed else InkSoft,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun RankedResultScreen(vm: MultiViewModel, onReplay: () -> Unit, onHome: () -> Unit) {
    val state by vm.state.collectAsState()

    Column(
        Modifier.fillMaxSize().background(WoodDark)
            .statusBarsPadding().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "RÉSULTATS DU DUEL", fontFamily = Garamond, fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.5.sp, color = GoldSoft,
        )
        state.secret?.let {
            Spacer(Modifier.height(4.dp))
            Text("Le mot secret était « $it »", fontFamily = Garamond, fontSize = 16.sp,
                fontStyle = FontStyle.Italic, color = InkSoft)
        }
        Spacer(Modifier.height(16.dp))

        ParchmentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("#", color = InkSoft, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                    Text("Chercheur", color = InkSoft, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("Max", color = InkSoft, fontSize = 12.sp,
                        modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                    Text("ELO", color = InkSoft, fontSize = 12.sp,
                        modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow))
                state.results.forEach { r ->
                    val isMe = r.username == state.account?.username
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            when (r.pos) { 1 -> "I"; 2 -> "II"; 3 -> "III"; else -> "${r.pos}" },
                            fontFamily = Garamond, fontSize = 15.sp,
                            color = if (r.pos == 1) GoldOld else InkSoft,
                            modifier = Modifier.width(28.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                r.username + if (isMe) " (toi)" else "",
                                fontFamily = Garamond, fontSize = 16.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                color = InkDark,
                            )
                            Text("${r.attempts} essais${if (r.found) " · trouvé" else ""}",
                                color = InkSoft, fontSize = 11.sp)
                        }
                        Text("${r.maxTemp.toInt()}°", color = InkSoft, fontSize = 13.sp,
                            modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                        val eloColor = when {
                            r.eloDelta > 0 -> Color(0xFF3A7A3A)
                            r.eloDelta < 0 -> SealRed
                            else -> InkSoft
                        }
                        Text(
                            (if (r.eloDelta >= 0) "+" else "") + r.eloDelta,
                            color = eloColor, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.width(44.dp), textAlign = TextAlign.End,
                        )
                    }
                }
                if (state.results.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Résultats en cours de calcul…", color = InkSoft,
                            fontStyle = FontStyle.Italic, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        LeatherButton("Rejouer", onClick = onReplay, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text(
            "Retour à la couverture", color = GoldSoft, fontSize = 14.sp, fontFamily = Garamond,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onHome).padding(10.dp),
        )
    }
}
