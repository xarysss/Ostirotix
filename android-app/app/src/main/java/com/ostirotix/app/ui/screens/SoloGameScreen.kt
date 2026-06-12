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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.ui.components.GuessRow
import com.ostirotix.app.ui.components.NewBestBadge
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.ResourceCapsule
import com.ostirotix.app.ui.components.SealGauge
import com.ostirotix.app.ui.components.TutorialOverlay
import com.ostirotix.app.ui.components.objectiveFor
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.feedbackMessage
import com.ostirotix.app.vm.SoloMode
import com.ostirotix.app.vm.SoloViewModel

@Composable
fun SoloGameScreen(
    vm: SoloViewModel,
    onFinished: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
) {
    val state by vm.state.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    var sortByTemp by rememberSaveable { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(!ServiceLocator.prefs.tutorialSeen) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state.last) {
        val t = state.last?.result?.temp ?: return@LaunchedEffect
        if (ServiceLocator.prefs.hapticsEnabled) {
            if (t >= 90) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            else if (t >= 70) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    LaunchedEffect(state.finished) { if (state.finished) onFinished() }

    Box(Modifier.fillMaxSize().background(WoodDark)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .padding(horizontal = 14.dp),
        ) {
            // ---- En-tête compact ----
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onHome) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Accueil", tint = GoldSoft)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (state.mode == SoloMode.DAILY) "MOT DU JOUR" else "RECHERCHE LIBRE",
                        fontFamily = Garamond, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.5.sp, color = GoldSoft,
                    )
                    Text("${state.attempts} essais", color = InkSoft, fontSize = 11.sp)
                }
                ResourceCapsule(R.drawable.ic_ink, state.ink, Color(0xFF7FA3CC))
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldSoft)
                }
                return@Column
            }

            // ---- La page du registre ----
            ParchmentCard(Modifier.fillMaxWidth().weight(1f), corner = 16.dp) {
                Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SealGauge(state.last?.result?.temp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.last?.let { feedbackMessage(it.result.temp, it.result.recognized, it.result.exact) }
                            ?: "Trempe ta plume et écris un premier mot.",
                        fontFamily = Garamond, fontSize = 17.sp, fontStyle = FontStyle.Italic,
                        color = InkDark, modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(objectiveFor(state.best?.result?.temp), color = InkSoft, fontSize = 12.sp)
                        NewBestBadge(state.last?.isNewBest == true && state.last?.result?.exact == false)
                    }
                    state.message?.let {
                        Text(it, color = SealRed, fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    state.hintWord?.let {
                        Text("Indice : cherche autour de « $it »", color = InkBlue, fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow))
                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            state.best?.let { "Meilleur : ${it.word} (${it.result.temp.toInt()}°)" } ?: "Registre vierge",
                            fontFamily = Garamond, fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp, color = InkDark, modifier = Modifier.weight(1f),
                        )
                        Text(
                            if (sortByTemp) "Tri : chaleur" else "Tri : récent",
                            color = InkBlue, fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .clickable { sortByTemp = !sortByTemp }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }

                    val list = if (sortByTemp) state.guesses.sortedByDescending { it.result.temp } else state.guesses
                    LazyColumn(Modifier.weight(1f)) {
                        items(list, key = { it.order }) { g ->
                            GuessRow(g, highlight = g.order == state.last?.order)
                        }
                    }

                    // ---- Actions ----
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = state.hintsUsed < vm.maxHints) { vm.useHint() }
                                .border(1.dp, InkBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_ink), null, tint = InkBlue,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Indice ${vm.maxHints - state.hintsUsed} · ${state.hintCost} encre",
                                color = InkBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Révéler", color = InkSoft, fontSize = 13.sp,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .clickable { vm.giveUp() }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }

                    // ---- Ligne d'écriture à l'encre ----
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = input,
                            onValueChange = { input = it; vm.clearMessage() },
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
                            onClick = { vm.submit(input); input = "" },
                            enabled = input.isNotBlank(),
                        ) {
                            Icon(painterResource(R.drawable.ic_quill), "Inscrire",
                                tint = if (input.isNotBlank()) SealRed else InkSoft,
                                modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (showTutorial) {
            TutorialOverlay {
                ServiceLocator.prefs.tutorialSeen = true
                showTutorial = false
            }
        }
    }
}
