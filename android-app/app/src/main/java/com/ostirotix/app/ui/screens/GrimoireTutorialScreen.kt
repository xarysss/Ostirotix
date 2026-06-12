package com.ostirotix.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.ServiceLocator
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.Parchment
import com.ostirotix.app.ui.theme.ParchmentDark
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.WoodPanel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class GrimoirePage(
    val title: String,
    val body: String,
    val note: String,
    val iconRes: Int,
)

private val tutorialPages = listOf(
    GrimoirePage(
        title = "Propose un mot",
        body = "Écris un mot qui pourrait se rapprocher du secret. N'importe quelle piste peut ouvrir le chemin.",
        note = "Chaque essai entre dans ton registre.",
        iconRes = R.drawable.ic_quill,
    ),
    GrimoirePage(
        title = "Lis la chaleur",
        body = "Après chaque mot, le grimoire indique une température. Plus elle monte, plus ton idée se rapproche du sens caché.",
        note = "Froid, tiède, chaud, brûlant : suis la trace.",
        iconRes = R.drawable.ic_ink,
    ),
    GrimoirePage(
        title = "Avance par le sens",
        body = "Compare tes meilleurs mots. Une bonne piste révèle une famille d'idées et te rapproche du mot exact.",
        note = "Les indices coûtent de l'encre, utilise-les avec soin.",
        iconRes = R.drawable.ic_book,
    ),
    GrimoirePage(
        title = "Trouve vite",
        body = "Le but est de déchiffrer le mot secret avec le moins d'essais possible. Les victoires donnent des ressources.",
        note = "Plus ta recherche est précise, plus le registre te récompense.",
        iconRes = R.drawable.ic_trophy,
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GrimoireTutorialScreen(onClose: () -> Unit, onStartDaily: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == tutorialPages.lastIndex

    Box(Modifier.fillMaxSize().background(WoodDark)) {
        GrimoireDust(Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "GRIMOIRE",
                        fontFamily = Garamond,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = GoldSoft,
                    )
                    Text("Feuillette pour apprendre le jeu", color = InkSoft, fontSize = 12.sp)
                }
                Text(
                    "Fermer",
                    color = GoldSoft,
                    fontSize = 12.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(
                        1.dp,
                        GoldOld.copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp),
                    ).clickable(onClick = onClose)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                pageSpacing = 14.dp,
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                GrimoirePageView(
                    page = tutorialPages[page],
                    index = page,
                    count = tutorialPages.size,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        rotationY = pageOffset * -24f
                        cameraDistance = 14f * density
                        translationX = pageOffset * 22.dp.toPx()
                        alpha = 0.72f + (1f - pageOffset.absoluteValue.coerceAtMost(1f)) * 0.28f
                    },
                    turnAmount = pageOffset.absoluteValue.coerceIn(0f, 1f),
                )
            }

            Spacer(Modifier.height(14.dp))
            PageDots(current = pagerState.currentPage, total = tutorialPages.size)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (pagerState.currentPage > 0) {
                    LeatherButton(
                        "Précédent",
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier.weight(1f),
                    )
                }
                LeatherButton(
                    if (isLast) "Commencer le mot du jour" else "Tourner la page",
                    onClick = {
                        if (isLast) {
                            ServiceLocator.prefs.tutorialSeen = true
                            onStartDaily()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.weight(if (pagerState.currentPage > 0) 1.35f else 1f),
                )
            }
        }
    }
}

@Composable
private fun GrimoirePageView(
    page: GrimoirePage,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    turnAmount: Float,
) {
    Box(modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.matchParentSize().padding(start = 8.dp, top = 12.dp, end = 2.dp, bottom = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.35f)),
        )
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3A2115), WoodPanel, Color(0xFF3A2115))))
                .border(1.dp, GoldOld.copy(alpha = 0.32f), RoundedCornerShape(22.dp))
                .padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        ) {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(ParchmentDark, PageIvory, Parchment)))
                    .border(1.dp, ParchmentShadow, RoundedCornerShape(16.dp)),
            ) {
                Box(
                    Modifier.matchParentSize()
                        .background(Brush.horizontalGradient(listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.04f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.08f * turnAmount),
                        ))),
                )
                Box(
                    Modifier.align(Alignment.Center).height(1.dp).fillMaxWidth(0.92f)
                        .background(GoldOld.copy(alpha = 0.16f)),
                )
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(painterResource(page.iconRes), null, tint = SealRed, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        page.title,
                        fontFamily = Garamond,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        page.body,
                        color = InkDark,
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        page.note,
                        fontFamily = Garamond,
                        fontStyle = FontStyle.Italic,
                        color = InkSoft,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        "${index + 1} / $count",
                        color = InkSoft,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                    )
                }
                Box(
                    Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(0.16f)
                        .background(Brush.horizontalGradient(listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.10f + turnAmount * 0.12f),
                        ))),
                )
            }
        }
    }
}

@Composable
private fun PageDots(current: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(total) { i ->
            Box(
                Modifier.padding(horizontal = 4.dp).size(if (i == current) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (i == current) GoldOld else GoldOld.copy(alpha = 0.28f)),
            )
        }
    }
}

@Composable
private fun GrimoireDust(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "grimoire_dust")
    val drift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "dust_drift",
    )
    Box(modifier) {
        val dots = listOf(
            Alignment.TopStart to Pair(70.dp, 180.dp),
            Alignment.TopEnd to Pair((-66).dp, 250.dp),
            Alignment.CenterStart to Pair(30.dp, (-80).dp),
            Alignment.CenterEnd to Pair((-40).dp, 30.dp),
        )
        dots.forEachIndexed { index, (align, offset) ->
            Box(
                Modifier.align(align)
                    .offset(x = offset.first, y = offset.second + ((index + 1) * 3 * drift.value).dp)
                    .size((3 + index % 2).dp)
                    .clip(CircleShape)
                    .background(GoldSoft.copy(alpha = 0.18f)),
            )
        }
    }
}
