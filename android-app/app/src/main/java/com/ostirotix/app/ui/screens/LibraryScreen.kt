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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.data.Economy
import com.ostirotix.app.data.ResourcePack
import com.ostirotix.app.data.Upgrade
import com.ostirotix.app.data.UpgradeCost
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.components.ResourceBar
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.WoodPanel

enum class ShopTab(val route: String, val label: String) {
    LIBRARY("bibliotheque", "Bibliothèque"),
    TREASURY("tresorerie", "Trésorerie");

    companion object {
        fun fromRoute(route: String?) = entries.firstOrNull { it.route == route } ?: LIBRARY
    }
}

@Composable
fun LibraryScreen(
    initialTab: ShopTab = ShopTab.LIBRARY,
    onRequireAuth: () -> Unit,
    onBack: () -> Unit,
) {
    val prefs = ServiceLocator.prefs
    var refresh by remember { mutableIntStateOf(0) }
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var billingMessage by remember { mutableStateOf<String?>(null) }
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
                "BOUTIQUE", fontFamily = Garamond, fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = GoldSoft,
            )
        }
        Spacer(Modifier.height(4.dp))
        ResourceBar(coins, pages, ink)
        Spacer(Modifier.height(10.dp))
        ShopTabs(selectedTab = selectedTab, onSelect = {
            selectedTab = it
            billingMessage = null
        })
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).navigationBarsPadding(),
        ) {
            when (selectedTab) {
                ShopTab.LIBRARY -> {
                    item {
                        IntroCard(
                            title = "Bibliothèque",
                            body = "Développe tes rayonnages sur le long terme. Les premiers niveaux coûtent surtout des pièces; les paliers avancés réclament aussi pages rares et encre.",
                        )
                    }
                    items(Economy.upgrades, key = { it.id }) { upg ->
                        val level = remember(refresh) { prefs.upgradeLevel(upg.id) }
                        UpgradeCard(upg, level, coins, pages, ink) {
                            if (prefs.isAuthenticated) {
                                if (Economy.buyUpgrade(prefs, upg.id)) refresh++
                            } else {
                                onRequireAuth()
                            }
                        }
                    }
                }
                ShopTab.TREASURY -> {
                    item {
                        IntroCard(
                            title = "Comptoir du Scribe",
                            body = "Packs prêts pour Google Play Billing. Aucun paiement réel n'est déclenché tant que l'intégration officielle n'est pas branchée.",
                        )
                    }
                    billingMessage?.let { message ->
                        item {
                            ParchmentCard(Modifier.fillMaxWidth(), corner = 10.dp) {
                                Text(message, color = InkSoft, fontSize = 12.sp,
                                    modifier = Modifier.padding(14.dp), lineHeight = 16.sp)
                            }
                        }
                    }
                    items(Economy.resourcePacks, key = { it.productId }) { pack ->
                        ResourcePackCard(pack) {
                            if (prefs.isAuthenticated) {
                                Economy.requestResourcePackPurchase(pack)
                                billingMessage = "Achat non activé : ${pack.productId} doit être relié à Google Play Billing avant de créditer les ressources."
                            } else {
                                onRequireAuth()
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun ShopTabs(selectedTab: ShopTab, onSelect: (ShopTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WoodPanel)
            .border(1.dp, GoldOld.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ShopTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(9.dp))
                    .background(
                        if (selected) Brush.verticalGradient(listOf(GoldSoft, GoldOld))
                        else Brush.verticalGradient(listOf(WoodPanel, WoodPanel)),
                    )
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tab.label,
                    fontFamily = Garamond,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) InkDark else GoldSoft,
                )
            }
        }
    }
}

@Composable
private fun IntroCard(title: String, body: String) {
    ParchmentCard(Modifier.fillMaxWidth(), corner = 12.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GoldSoft, GoldOld, Color(0xFF6E3F12))))
                    .border(1.dp, PageIvory.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_book), null, tint = InkDark, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, fontFamily = Garamond, fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, color = InkDark)
        }
    }
}
@Composable
private fun UpgradeCard(upg: Upgrade, level: Int, coins: Int, pages: Int, ink: Int, onBuy: () -> Unit) {
    val cost = Economy.upgradeCost(upg, level)
    val maxed = level >= upg.maxLevel
    val canAfford = !maxed && coins >= cost.coins && pages >= cost.pages && ink >= cost.ink

    ParchmentCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LevelSeal(if (maxed) "MAX" else "$level/${upg.maxLevel}")
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(upg.name, fontFamily = Garamond, fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold, color = InkDark, maxLines = 1)
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp))
                        .background(ParchmentShadow),
                ) {
                    Box(
                        Modifier.fillMaxWidth(level.toFloat() / upg.maxLevel).height(6.dp)
                            .clip(RoundedCornerShape(4.dp)).background(GoldOld),
                    )
                }
                if (!maxed) CostLine(cost)
            }
            if (!maxed) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(SealRed, Color(0xFF5E1C10))))
                        .border(1.dp, GoldOld.copy(alpha = 0.65f), CircleShape)
                        .clickable(enabled = canAfford, onClick = onBuy),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PageIvory)
                }
            }
        }
    }
}
@Composable
private fun LevelSeal(text: String) {
    Box(
        Modifier.size(48.dp).clip(CircleShape)
            .background(Brush.radialGradient(listOf(SealRed, Color(0xFF5E1C10))))
            .border(1.dp, Color(0xFF4A150B), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = Garamond, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            color = PageIvory, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CostLine(cost: UpgradeCost) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        CostPip(R.drawable.ic_coin, cost.coins, GoldSoft, Modifier.weight(1f))
        if (cost.pages > 0) CostPip(R.drawable.ic_page, cost.pages, PageIvory, Modifier.weight(1f))
        if (cost.ink > 0) CostPip(R.drawable.ic_ink, cost.ink, InkBlue, Modifier.weight(1f))
    }
}

@Composable
private fun CostPip(iconRes: Int, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(WoodPanel.copy(alpha = 0.92f))
            .border(1.dp, GoldOld.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text("$value", color = PageIvory, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun ResourcePackCard(pack: ResourcePack, onBuy: () -> Unit) {
    ParchmentCard(Modifier.fillMaxWidth(), corner = 16.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(GoldSoft, GoldOld, Color(0xFF5F3511))))
                        .border(2.dp, PageIvory.copy(alpha = 0.32f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_book), null, tint = InkDark, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(pack.name, fontFamily = Garamond, fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold, color = InkDark, maxLines = 1)
                    Text(pack.priceLabel, fontFamily = Garamond, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = SealRed)
                }
                Box(
                    Modifier.size(50.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(SealRed, Color(0xFF5E1C10))))
                        .border(1.dp, GoldOld.copy(alpha = 0.7f), CircleShape)
                        .clickable(onClick = onBuy),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PageIvory)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PackResource(R.drawable.ic_coin, pack.coins, GoldSoft, Modifier.weight(1f))
                PackResource(R.drawable.ic_page, pack.pages, PageIvory, Modifier.weight(1f))
                PackResource(R.drawable.ic_ink, pack.ink, InkBlue, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PackResource(iconRes: Int, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(listOf(WoodPanel, Color(0xFF2B1A10))))
            .border(1.dp, GoldOld.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text("$value", color = PageIvory, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}