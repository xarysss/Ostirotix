package com.ostirotix.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.Economy
import com.ostirotix.app.data.Upgrade
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.ResourceBar
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.WoodPanel

@Composable
fun LibraryScreen(onBack: () -> Unit) {
    val prefs = ServiceLocator.prefs
    var refresh by remember { mutableIntStateOf(0) }
    val coins = remember(refresh) { prefs.coins }
    val pages = remember(refresh) { prefs.pages }
    val ink = remember(refresh) { prefs.ink }

    Column(
        Modifier.fillMaxSize().background(WoodDark)
            .statusBarsPadding().padding(horizontal = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
            }
            Text(
                "BIBLIOTHÈQUE", fontFamily = Garamond, fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
            )
        }
        Spacer(Modifier.height(4.dp))
        ResourceBar(coins, pages, ink)
        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Economy.upgrades, key = { it.id }) { upg ->
                val level = remember(refresh) { prefs.upgradeLevel(upg.id) }
                UpgradeCard(upg, level, coins) {
                    if (Economy.buyUpgrade(prefs, upg.id)) refresh++
                }
            }

            item {
                val currentInk = remember(refresh) { prefs.ink }
                ParchmentCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_ink), null,
                                tint = InkBlue, modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Encrier", fontFamily = Garamond, fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold, color = InkDark)
                                Text("Recharger pour obtenir des indices.",
                                    color = InkSoft, fontSize = 12.sp)
                            }
                            Text("$currentInk", fontFamily = Garamond, fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = InkBlue)
                        }
                        LeatherButton(
                            "${Economy.INK_REFILL_COST} pièces → +${Economy.INK_REFILL_AMOUNT} encre",
                            onClick = { if (Economy.buyInk(prefs)) refresh++ },
                            enabled = coins >= Economy.INK_REFILL_COST,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun UpgradeCard(upg: Upgrade, level: Int, coins: Int, onBuy: () -> Unit) {
    val cost = Economy.upgradeCost(upg, level)
    val maxed = level >= upg.maxLevel
    val canAfford = !maxed && coins >= cost

    ParchmentCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(upg.name, fontFamily = Garamond, fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold, color = InkDark)
                    Text(upg.desc, color = InkSoft, fontSize = 12.sp)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp))
                        .background(if (maxed) GoldOld.copy(alpha = 0.2f) else WoodPanel)
                        .border(
                            1.dp,
                            if (maxed) GoldOld.copy(alpha = 0.6f) else GoldOld.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (maxed) "MAX" else "Niv.$level",
                        fontFamily = Garamond, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (maxed) GoldOld else GoldSoft,
                    )
                }
            }

            if (level > 0) {
                Text(upg.effectText(level), color = InkBlue, fontSize = 13.sp,
                    fontStyle = FontStyle.Italic)
            }

            if (!maxed) {
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(ParchmentShadow),
                ) {
                    Box(
                        Modifier.fillMaxWidth(level.toFloat() / upg.maxLevel).height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(GoldOld),
                    )
                }
                LeatherButton(
                    "Améliorer · $cost pièces",
                    onClick = onBuy,
                    enabled = canAfford,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!canAfford && coins < cost) {
                    Text("Il te manque ${cost - coins} pièces.",
                        color = InkSoft, fontSize = 11.sp, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}
