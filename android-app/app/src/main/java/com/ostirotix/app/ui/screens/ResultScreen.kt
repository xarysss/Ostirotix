package com.ostirotix.app.ui.screens

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.WaxSeal
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.vm.SoloMode
import com.ostirotix.app.vm.SoloViewModel

@Composable
fun ResultScreen(vm: SoloViewModel, onReplay: () -> Unit, onHome: () -> Unit) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    Column(
        Modifier.fillMaxSize().background(WoodDark).statusBarsPadding().padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Poussière dorée discrète
        val dust = rememberInfiniteTransition(label = "dust")
        val dustAlpha by dust.animateFloat(0.12f, 0.4f,
            infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "a")

        Text(
            if (state.won) "MOT DÉCHIFFRÉ" else "PAGE REFERMÉE",
            fontFamily = Garamond, fontSize = 30.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 3.sp, color = if (state.won) GoldSoft else InkSoft,
        )
        Spacer(Modifier.height(18.dp))

        // La page rare
        ParchmentCard(Modifier.fillMaxWidth(), corner = 16.dp) {
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Sceau brisé (deux moitiés décalées) si gagné, intact sinon
                if (state.won) {
                    Box(Modifier.size(64.dp)) {
                        Box(Modifier.size(64.dp).alpha(dustAlpha).clip(CircleShape)
                            .background(GoldOld.copy(alpha = 0.5f)))
                        Box(Modifier.size(54.dp).offset((-7).dp, 3.dp).align(Alignment.CenterStart)) {
                            WaxSeal(40.dp, "O", pulse = false)
                        }
                        Box(Modifier.size(54.dp).offset(7.dp, (-3).dp).align(Alignment.CenterEnd)) {
                            WaxSeal(40.dp, " ", pulse = false)
                        }
                    }
                } else {
                    WaxSeal(56.dp, "O", pulse = false)
                }
                Spacer(Modifier.height(12.dp))
                Text("Le mot secret était", color = InkSoft, fontSize = 13.sp,
                    fontFamily = Garamond, fontStyle = FontStyle.Italic)
                Text(
                    state.revealedWord ?: "?",
                    fontFamily = Garamond, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp, color = if (state.won) SealRed else InkDark,
                )
                Spacer(Modifier.height(8.dp))
                Text("${state.attempts} essais · ${state.hintsUsed} indice(s)",
                    color = InkDark, fontSize = 14.sp)
                state.best?.let {
                    if (!state.won) Text("Meilleure piste : ${it.word} (${it.result.temp.toInt()}°)",
                        color = InkSoft, fontSize = 12.sp)
                }

                // ---- Récompenses ----
                state.rewards?.let { rw ->
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(ParchmentShadow))
                    Spacer(Modifier.height(12.dp))
                    RewardRow(R.drawable.ic_coin, GoldOld, "Pièces", "+${rw.coins}")
                    if (rw.streakBonus > 0)
                        RewardRow(R.drawable.ic_bookmark, SealRed,
                            "Dont bonus de série (${state.streak} j)", "+${rw.streakBonus}")
                    if (rw.pages > 0)
                        RewardRow(R.drawable.ic_page, Color(0xFF9C7C2E), "Page rare", "+${rw.pages}")
                    RewardRow(R.drawable.ic_ink, Color(0xFF2C4A77), "Encre", "+${rw.ink}")
                }
                if (!state.won && state.lossConsolation > 0) {
                    Spacer(Modifier.height(10.dp))
                    RewardRow(R.drawable.ic_coin, GoldOld, "Le Scribe te console", "+${state.lossConsolation}")
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        if (state.won) {
            LeatherButton("Partager (sans révéler le mot)", onClick = {
                val mode = if (state.mode == SoloMode.DAILY) "le mot du jour" else "un mot secret"
                val txt = "Ostirotix — j'ai déchiffré $mode en ${state.attempts} essais." +
                    (if (state.streak > 1) " Série : ${state.streak} jours." else "")
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt)
                }
                ctx.startActivity(Intent.createChooser(send, "Partager"))
            }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
        }
        LeatherButton("Nouvelle recherche", onClick = onReplay, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(
            "Retour à la couverture", color = GoldSoft, fontSize = 14.sp, fontFamily = Garamond,
            modifier = Modifier.clickable(onClick = onHome).padding(10.dp),
        )
        Text("Reviens demain pour une nouvelle page.", color = InkSoft, fontSize = 12.sp,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun RewardRow(iconRes: Int, tint: Color, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = InkDark, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = InkDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
