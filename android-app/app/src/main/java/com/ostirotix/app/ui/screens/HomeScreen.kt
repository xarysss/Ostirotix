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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.ui.components.BookCover
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.ResourceBar
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.WoodPanel

/** Élément de la barre de navigation basse. */
private data class NavItem(val label: String, val iconRes: Int, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    onDaily: () -> Unit,
    onTraining: () -> Unit,
    onMulti: () -> Unit,
    onLeaderboard: () -> Unit,
    onLibrary: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    val prefs = ServiceLocator.prefs
    Column(Modifier.fillMaxSize().background(WoodDark)) {
        Column(
            Modifier.weight(1f).statusBarsPadding().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(10.dp))
            ResourceBar(prefs.coins, prefs.pages, prefs.ink)
            Spacer(Modifier.weight(0.7f))

            BookCover(
                streak = prefs.streak,
                modifier = Modifier.fillMaxWidth(0.74f).height(300.dp),
            )
            Spacer(Modifier.weight(1f))

            // ---- Mot du jour : l'action évidente ----
            Box(Modifier.fillMaxWidth()) {
                // halo doré discret
                Box(
                    Modifier.matchParentSize().padding(0.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(GoldOld.copy(alpha = 0.16f)),
                )
                ParchmentCard(
                    Modifier.fillMaxWidth().padding(3.dp), corner = 16.dp,
                ) {
                    Row(
                        Modifier
                            .clickable(onClick = onDaily)
                            .border(1.dp, GoldOld.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(R.drawable.ic_bookmark), null,
                            tint = SealRed, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (prefs.dailyAlreadyWonToday()) "Mot du jour — déchiffré" else "Mot du jour",
                                fontFamily = Garamond, fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, color = InkDark,
                            )
                            Text("Relève le défi quotidien", color = InkSoft, fontSize = 13.sp)
                        }
                        // pastille de cire avec flèche
                        Box(
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(Brush.radialGradient(listOf(SealRed, Color(0xFF5E1C10))))
                                .border(1.dp, Color(0xFF4A150B), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("›", color = PageIvory, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ---- Modes secondaires ----
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryModeCard("Recherche libre", "Entraînement illimité",
                    R.drawable.ic_book, Modifier.weight(1f), onTraining)
                SecondaryModeCard("Duel lexical", "Affronte d'autres chercheurs",
                    R.drawable.ic_swords, Modifier.weight(1f), onMulti)
            }
            Spacer(Modifier.height(16.dp))
        }

        // ---- Navigation basse ----
        Row(
            Modifier.fillMaxWidth().background(WoodPanel)
                .border(0.5.dp, GoldOld.copy(alpha = 0.25f))
                .navigationBarsPadding().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf(
                NavItem("Classement", R.drawable.ic_trophy, onLeaderboard),
                NavItem("Bibliothèque", R.drawable.ic_book, onLibrary),
                NavItem("Profil", R.drawable.ic_quill, onProfile),
                NavItem("Options", R.drawable.ic_gear, onSettings),
            ).forEach { item ->
                Column(
                    Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = item.onClick)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(painterResource(item.iconRes), item.label,
                        tint = GoldSoft, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(item.label.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = GoldSoft)
                }
            }
        }
    }
}

@Composable
private fun SecondaryModeCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ParchmentCard(modifier, corner = 12.dp) {
        Column(
            Modifier.clickable(onClick = onClick).fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(painterResource(iconRes), null, tint = InkDark, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontFamily = Garamond, fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, color = InkDark)
            Text(subtitle, color = InkSoft, fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
