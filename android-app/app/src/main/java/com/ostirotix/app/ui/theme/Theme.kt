package com.ostirotix.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ostirotix.app.R

// ---- Palette « vieux dictionnaire » ----
val WoodDark = Color(0xFF17100A)        // fond bois sombre presque noir
val WoodPanel = Color(0xFF241710)       // panneaux bois
val WoodLight = Color(0xFF3A2718)       // bois éclairé
val Parchment = Color(0xFFEFE3C4)       // papier parchemin clair
val ParchmentDark = Color(0xFFE2D0A8)   // parchemin vieilli
val ParchmentShadow = Color(0xFFC9B488) // ombre de page
val InkDark = Color(0xFF2C1F12)         // encre brune très foncée (texte principal)
val InkSoft = Color(0xFF6E5C44)         // texte secondaire
val GoldOld = Color(0xFFC9A227)         // or vieilli
val GoldSoft = Color(0xFFD9BC6A)        // dorure douce
val LeatherRed = Color(0xFF6E2A1A)      // cuir brun-rouge (boutons)
val LeatherDark = Color(0xFF4A1D12)
val SealRed = Color(0xFF8C2B1A)         // cire à cacheter
val PageIvory = Color(0xFFF4EAD0)       // pages rares
val InkBlue = Color(0xFF1F3A5F)         // encre bleu nuit

// Températures
val TempCold = Color(0xFF2C4A77)        // bleu encre
val TempWarm = Color(0xFFB8912B)        // doré
val TempHot = Color(0xFFC06B2A)         // orange doux
val TempBurning = Color(0xFF8E1F12)     // rouge profond

/** Couleur d'encre selon la température (sur fond parchemin). */
fun tempColor(temp: Double): Color = when {
    temp >= 90 -> TempBurning
    temp >= 70 -> TempHot
    temp >= 30 -> TempWarm
    else -> TempCold
}

fun feedbackMessage(temp: Double, recognized: Boolean, exact: Boolean): String = when {
    exact -> "Le sceau se brise. Mot déchiffré."
    !recognized -> "Ce mot n'est pas dans le registre."
    temp >= 95 -> "Le papier roussit sous ta plume."
    temp >= 90 -> "Brûlant. Tu y es presque."
    temp >= 70 -> "Très chaud. Le sens se précise."
    temp >= 45 -> "Chaud. Tu tiens une piste."
    temp >= 30 -> "Tiède. Continue de chercher."
    temp >= 0 -> "Froid. La piste s'éloigne."
    else -> "Glacial. Change de registre."
}

val Garamond = FontFamily(Font(R.font.eb_garamond))

private val Scheme = darkColorScheme(
    primary = GoldOld,
    onPrimary = InkDark,
    secondary = LeatherRed,
    onSecondary = PageIvory,
    tertiary = InkBlue,
    background = WoodDark,
    onBackground = Parchment,
    surface = WoodPanel,
    onSurface = Parchment,
    surfaceVariant = WoodLight,
    onSurfaceVariant = GoldSoft,
    error = SealRed,
)

private val OstirotixType = Typography(
    // Titres : serif ancienne, espacement façon gravure
    displayLarge = TextStyle(fontFamily = Garamond, fontSize = 44.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp),
    headlineMedium = TextStyle(fontFamily = Garamond, fontSize = 26.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp),
    titleLarge = TextStyle(fontFamily = Garamond, fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = Garamond, fontSize = 18.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp),
    // Corps : sans-serif moderne lisible
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
)

@Composable
fun OstirotixTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = OstirotixType, content = content)
}
