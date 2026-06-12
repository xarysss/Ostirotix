package com.ostirotix.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R
import com.ostirotix.app.data.model.Guess
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.LeatherDark
import com.ostirotix.app.ui.theme.LeatherRed
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.Parchment
import com.ostirotix.app.ui.theme.ParchmentDark
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.TempCold
import com.ostirotix.app.ui.theme.WoodPanel
import com.ostirotix.app.ui.theme.tempColor

// ---------- Ressources ----------

enum class ResourceKind { COINS, PAGES, INK }

/** Ancienne capsule compacte utilisée dans les écrans de jeu. */
@Composable
fun ResourceCapsule(iconRes: Int, value: Int, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(WoodPanel)
            .border(1.dp, GoldOld.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text("$value", color = Parchment, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Petite fiche de ressource : lisible, compacte, avec sceau d'achat optionnel. */
@Composable
fun ResourceCapsule(
    iconRes: Int,
    label: String,
    helper: String,
    value: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(WoodPanel)
            .border(1.dp, GoldOld.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = GoldSoft, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$value", color = Parchment, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), maxLines = 1)
            if (onAdd != null) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(GoldSoft, GoldOld)))
                        .border(1.dp, Color(0xFF7D5D13), CircleShape)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = InkDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(helper, color = Parchment.copy(alpha = 0.68f), fontSize = 8.sp,
            lineHeight = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Barre des trois ressources, compacte, en haut d'écran. */
@Composable
fun ResourceBar(
    coins: Int,
    pages: Int,
    ink: Int,
    modifier: Modifier = Modifier,
    onAddResource: ((ResourceKind) -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        ResourceCapsule(R.drawable.ic_coin, "Pièces", "Améliorations", coins, GoldSoft,
            modifier = Modifier.weight(1f), onAdd = onAddResource?.let { { it(ResourceKind.COINS) } })
        ResourceCapsule(R.drawable.ic_page, "Pages", "Niveaux rares", pages, PageIvory,
            modifier = Modifier.weight(1f), onAdd = onAddResource?.let { { it(ResourceKind.PAGES) } })
        ResourceCapsule(R.drawable.ic_ink, "Encre", "Indices", ink, Color(0xFF7FA3CC),
            modifier = Modifier.weight(1f), onAdd = onAddResource?.let { { it(ResourceKind.INK) } })
    }
}

// ---------- Surfaces ----------

/** Carte parchemin : papier clair, encre, coin légèrement vieilli. */
@Composable
fun ParchmentCard(
    modifier: Modifier = Modifier,
    corner: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(listOf(Parchment, ParchmentDark)))
            .border(1.dp, ParchmentShadow, RoundedCornerShape(corner)),
    ) { content() }
}

/** Bouton principal en cuir, bordure dorée, qui s'enfonce au toucher. */
@Composable
fun LeatherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90), label = "press")
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(
                if (enabled) listOf(LeatherRed, LeatherDark) else listOf(InkSoft, InkSoft)))
            .border(1.dp, GoldOld.copy(alpha = if (enabled) 0.8f else 0.3f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = Garamond, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp, color = PageIvory)
    }
}

// ---------- Le livre ----------

/** Sceau de cire, pulsation légère. */
@Composable
fun WaxSeal(size: Dp, letter: String = "O", color: Color = SealRed, pulse: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "seal")
    val s by transition.animateFloat(
        initialValue = 1f, targetValue = if (pulse) 1.045f else 1f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "pulse")
    Box(
        modifier = Modifier
            .size(size)
            .scale(s)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color, Color(0xFF5E1C10))))
            .border(2.dp, Color(0xFF4A150B), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(size * 0.72f).clip(CircleShape)
                .border(1.dp, PageIvory.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(letter, fontFamily = Garamond, fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.SemiBold, color = PageIvory)
        }
    }
}

/** Lettres dorées qui flottent doucement autour du livre. */
@Composable
fun FloatingLetters(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "letters")
    val drift by transition.animateFloat(0f, 6f,
        infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "drift")
    val glow by transition.animateFloat(0.18f, 0.45f,
        infiniteRepeatable(tween(2100), RepeatMode.Reverse), label = "glow")
    Box(modifier.fillMaxSize()) {
        val letters = listOf(
            Triple("A", Alignment.CenterStart, 0.dp),
            Triple("R", Alignment.BottomStart, 12.dp),
            Triple("B", Alignment.TopEnd, 6.dp),
            Triple("M", Alignment.CenterEnd, 16.dp),
        )
        letters.forEachIndexed { i, (l, align, extra) ->
            Text(
                l, fontFamily = Garamond, fontSize = (18 + i * 3).sp, color = GoldSoft,
                modifier = Modifier.align(align)
                    .offset(x = if (align == Alignment.CenterStart || align == Alignment.BottomStart) 6.dp else (-6).dp,
                        y = extra + (if (i % 2 == 0) drift.dp else (-drift).dp))
                    .alpha(glow),
            )
        }
    }
}

/** Couverture du dictionnaire ancien avec sceau et marque-page de série. */
@Composable
fun BookCover(streak: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier.then(if (onClick != null) Modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick) else Modifier),
    ) {
        // Ombre portée
        Box(
            Modifier.matchParentSize().offset(5.dp, 7.dp)
                .clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.4f)),
        )
        // Dos en cuir
        Box(
            Modifier.matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF3D1F12), LeatherDark))),
        )
        // Couverture parchemin (la tranche cuir reste visible à gauche)
        BoxWithConstraints(
            Modifier.matchParentSize()
                .padding(start = 22.dp, top = 7.dp, end = 7.dp, bottom = 9.dp)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, topStart = 3.dp, bottomStart = 3.dp))
                .background(Brush.verticalGradient(listOf(PageIvory, Parchment, ParchmentDark)))
                .border(1.dp, ParchmentShadow,
                    RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, topStart = 3.dp, bottomStart = 3.dp)),
        ) {
            val titleSize = when {
                maxWidth < 235.dp -> 24.sp
                maxWidth < 275.dp -> 27.sp
                else -> 30.sp
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(painterResource(R.drawable.ic_quill), null, tint = InkSoft, modifier = Modifier.size(26.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    "OSTIROTIX",
                    fontFamily = Garamond,
                    fontSize = titleSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = InkDark,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(34.dp).height(1.dp).background(GoldOld))
                    Box(Modifier.padding(horizontal = 6.dp).size(5.dp).scale(1f)
                        .background(GoldOld, CircleShape))
                    Box(Modifier.width(34.dp).height(1.dp).background(GoldOld))
                }
                Spacer(Modifier.height(12.dp))
                Text("Déchiffre le mot secret.", fontFamily = Garamond, fontSize = 15.sp,
                    fontStyle = FontStyle.Italic, color = InkSoft, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                WaxSeal(64.dp, pulse = onClick != null)
                Spacer(Modifier.height(6.dp))
            }
        }
        FloatingLetters(Modifier.matchParentSize().padding(2.dp))
        // Marque-page de série quotidienne, attaché au bord du grimoire.
        if (streak > 0) {
            Column(
                Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 34.dp)
                    .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 10.dp, topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(Brush.verticalGradient(listOf(PageIvory, ParchmentDark)))
                    .border(1.dp, ParchmentShadow, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 8.dp, bottomEnd = 8.dp))
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(painterResource(R.drawable.ic_bookmark), null, tint = SealRed, modifier = Modifier.size(14.dp))
                Text("SÉRIE", fontSize = 8.sp, letterSpacing = 1.2.sp, color = InkSoft)
                Text("$streak", fontFamily = Garamond, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, color = InkDark)
                Text(if (streak > 1) "JOURS" else "JOUR", fontSize = 8.sp, letterSpacing = 1.2.sp, color = InkSoft)
            }
        }
    }
}

// ---------- Jeu ----------

/** Sceau-thermomètre : la cire chauffe avec la température. */
@Composable
fun SealGauge(temp: Double?, size: Dp = 116.dp) {
    val color = if (temp == null) TempCold.copy(alpha = 0.6f) else tempColor(temp)
    val hot = (temp ?: -100.0) >= 70
    val transition = rememberInfiniteTransition(label = "heat")
    val pulse by transition.animateFloat(1f, if (hot) 1.06f else 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
    Box(
        Modifier.size(size).scale(pulse).clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.95f), color, Color(0xFF2A130B))))
            .border(2.dp, GoldOld.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(size * 0.78f).clip(CircleShape)
                .border(1.dp, PageIvory.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (temp == null) "—" else "${temp.toInt()}°",
                fontFamily = Garamond, fontSize = (size.value * 0.3f).sp,
                fontWeight = FontWeight.Bold, color = PageIvory,
            )
        }
    }
}

/** Ligne du registre : un essai et sa température, à l'encre. */
@Composable
fun GuessRow(g: Guess, highlight: Boolean = false) {
    val r = g.result
    val color = if (r.recognized) tempColor(r.temp) else InkSoft
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlight) ParchmentShadow.copy(alpha = 0.35f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${g.order}.", color = InkSoft, fontSize = 12.sp, modifier = Modifier.width(30.dp))
        Text(g.word, fontFamily = Garamond, fontSize = 17.sp, color = InkDark,
            fontWeight = if (g.isNewBest) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f))
        if (r.rank in 1..1000) {
            Text("n°${r.rank} · ${r.progression}‰", color = InkSoft, fontSize = 11.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(if (r.recognized) "${r.temp.toInt()}°" else "—",
            color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// ---------- Tutoriel & divers ----------

@Composable
fun TutorialOverlay(onDone: () -> Unit) {
    var stepIdx by remember { mutableIntStateOf(0) }
    val steps = listOf(
        "Écris un mot, n'importe lequel.",
        "Plus c'est chaud, plus tu es proche du mot secret.",
        "Ce n'est pas une question de lettres, mais de sens.",
    )
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f))
            .clickable { if (stepIdx < steps.size - 1) stepIdx++ else onDone() },
        contentAlignment = Alignment.Center,
    ) {
        ParchmentCard(Modifier.padding(36.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painterResource(R.drawable.ic_quill), null, tint = InkSoft, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(10.dp))
                Text(steps[stepIdx], fontFamily = Garamond, fontSize = 19.sp, color = InkDark,
                    textAlign = TextAlign.Center, lineHeight = 26.sp)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { i ->
                        Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(if (i == stepIdx) GoldOld else InkSoft.copy(alpha = 0.35f)))
                    }
                }
                Spacer(Modifier.height(14.dp))
                LeatherButton(
                    if (stepIdx < steps.size - 1) "Suivant" else "Commencer",
                    onClick = { if (stepIdx < steps.size - 1) stepIdx++ else onDone() },
                )
            }
        }
    }
}

@Composable
fun NewBestBadge(visible: Boolean) {
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier.clip(RoundedCornerShape(6.dp))
                .background(GoldOld.copy(alpha = 0.22f))
                .border(1.dp, GoldOld.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text("Meilleure piste", color = Color(0xFF7A6210), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

fun objectiveFor(bestTemp: Double?): String = when {
    bestTemp == null -> "Objectif : atteins 70°"
    bestTemp >= 90 -> "Objectif : déchiffre le mot"
    bestTemp >= 70 -> "Objectif : atteins 90°"
    else -> "Objectif : atteins 70°"
}

/** Barre de chaleur d'un joueur (duel lexical, sur parchemin). */
@Composable
fun PlayerHeatBar(name: String, maxTemp: Double, found: Boolean, isMe: Boolean) {
    val frac by animateFloatAsState(((maxTemp.coerceIn(-100.0, 100.0) + 100) / 200).toFloat(),
        tween(500), label = "heat")
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (found) {
                Icon(painterResource(R.drawable.ic_trophy), null, tint = GoldOld, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(name + if (isMe) " (toi)" else "", color = InkDark, fontSize = 13.sp,
                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f))
            Text("${maxTemp.toInt()}°", color = SealRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp))
            .background(ParchmentShadow.copy(alpha = 0.6f))) {
            Box(Modifier.fillMaxWidth(frac).height(5.dp).clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(listOf(TempCold, GoldOld, SealRed))))
        }
    }
}
