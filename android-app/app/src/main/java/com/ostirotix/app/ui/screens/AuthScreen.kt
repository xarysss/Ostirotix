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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ostirotix.app.data.auth.AuthMode
import com.ostirotix.app.ui.components.LeatherButton
import com.ostirotix.app.ui.components.ParchmentCard
import com.ostirotix.app.ui.theme.Garamond
import com.ostirotix.app.ui.theme.GoldOld
import com.ostirotix.app.ui.theme.GoldSoft
import com.ostirotix.app.ui.theme.InkBlue
import com.ostirotix.app.ui.theme.InkDark
import com.ostirotix.app.ui.theme.InkSoft
import com.ostirotix.app.ui.theme.LeatherDark
import com.ostirotix.app.ui.theme.PageIvory
import com.ostirotix.app.ui.theme.ParchmentShadow
import com.ostirotix.app.ui.theme.SealRed
import com.ostirotix.app.ui.theme.WoodDark
import com.ostirotix.app.ui.theme.WoodPanel
import com.ostirotix.app.vm.AuthViewModel

@Composable
fun AuthScreen(
    vm: AuthViewModel,
    initialMode: AuthMode,
    message: String?,
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val state by vm.state.collectAsState()
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var registerEmail by rememberSaveable { mutableStateOf("") }
    var registerPassword by rememberSaveable { mutableStateOf("") }
    var acceptsMarketing by rememberSaveable { mutableStateOf(false) }
    var acceptsTerms by rememberSaveable { mutableStateOf(false) }
    var localNote by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().background(WoodDark)
            .statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = GoldSoft)
            }
            Text(
                "COMPTE",
                fontFamily = Garamond,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = GoldSoft,
            )
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ParchmentCard(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Registre du joueur",
                        fontFamily = Garamond,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        message ?: "Connecte-toi pour sauvegarder ton identité et accéder aux fonctionnalités en ligne.",
                        color = InkSoft,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            AuthTabs(mode = mode, onMode = {
                mode = it
                vm.clearError()
                localNote = null
            })

            ParchmentCard(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (mode == AuthMode.LOGIN) {
                        AuthField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = "Adresse mail",
                            keyboardType = KeyboardType.Email,
                        )
                        AuthField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = "Mot de passe",
                            keyboardType = KeyboardType.Password,
                            password = true,
                        )
                        LeatherButton(
                            "Se connecter",
                            onClick = { vm.login(loginEmail, loginPassword, onAuthenticated) },
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Mot de passe oublié ?",
                            color = InkBlue,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    localNote = "Récupération de mot de passe prête à brancher au backend."
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    } else {
                        AuthField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Pseudo",
                            capitalization = KeyboardCapitalization.Words,
                        )
                        AuthField(
                            value = registerEmail,
                            onValueChange = { registerEmail = it },
                            label = "Adresse mail",
                            keyboardType = KeyboardType.Email,
                        )
                        AuthField(
                            value = registerPassword,
                            onValueChange = { registerPassword = it },
                            label = "Mot de passe",
                            keyboardType = KeyboardType.Password,
                            password = true,
                        )
                        ConsentRow(
                            checked = acceptsMarketing,
                            onChecked = { acceptsMarketing = it },
                            text = "J'accepte de recevoir des offres et actualités par email.",
                        )
                        ConsentRow(
                            checked = acceptsTerms,
                            onChecked = { acceptsTerms = it },
                            text = "J'accepte les conditions d'utilisation et la politique de confidentialité.",
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegalLink("Conditions") {
                                localNote = "Placeholder : ouvrir ici les conditions d'utilisation complètes."
                            }
                            LegalLink("Confidentialité") {
                                localNote = "Placeholder : ouvrir ici la politique de confidentialité complète."
                            }
                        }
                        LeatherButton(
                            "Créer mon compte",
                            onClick = {
                                vm.register(
                                    username,
                                    registerEmail,
                                    registerPassword,
                                    acceptsMarketing,
                                    acceptsTerms,
                                    onAuthenticated,
                                )
                            },
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    DividerLabel("ou")

                    GoogleButton(
                        onClick = { vm.googleLogin() },
                        enabled = !state.loading,
                    )

                    state.error?.let {
                        Text(it, color = SealRed, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    localNote?.let {
                        Text(it, color = InkSoft, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AuthTabs(mode: AuthMode, onMode: (AuthMode) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WoodPanel)
            .border(1.dp, GoldOld.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(AuthMode.LOGIN to "Connexion", AuthMode.REGISTER to "Créer un compte").forEach { (tab, label) ->
            val selected = mode == tab
            Box(
                Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(9.dp))
                    .background(
                        if (selected) Brush.verticalGradient(listOf(GoldSoft, GoldOld))
                        else Brush.verticalGradient(listOf(WoodPanel, WoodPanel)),
                    )
                    .clickable { onMode(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontFamily = Garamond,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) InkDark else GoldSoft,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    password: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(label, fontFamily = Garamond, fontStyle = FontStyle.Italic, color = InkSoft)
        },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        textStyle = TextStyle(fontFamily = Garamond, fontSize = 17.sp, color = InkDark),
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
}

@Composable
private fun ConsentRow(checked: Boolean, onChecked: (Boolean) -> Unit, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = SealRed,
                uncheckedColor = InkSoft,
                checkmarkColor = PageIvory,
            ),
        )
        Text(text, color = InkDark, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LegalLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = InkBlue,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun DividerLabel(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(ParchmentShadow.copy(alpha = 0.55f)))
        Text(text, color = InkSoft, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp))
        Box(Modifier.weight(1f).height(1.dp).background(ParchmentShadow.copy(alpha = 0.55f)))
    }
}

@Composable
private fun GoogleButton(onClick: () -> Unit, enabled: Boolean) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(PageIvory, Color(0xFFE4D4A8))))
            .border(1.dp, GoldOld.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(
                Modifier.clip(RoundedCornerShape(6.dp)).background(LeatherDark)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text("G", fontFamily = Garamond, color = GoldSoft, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.padding(horizontal = 5.dp))
            Text(
                "Continuer avec Google",
                fontFamily = Garamond,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
            )
            Icon(Icons.Filled.Check, null, tint = GoldOld, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
