package com.clipvault.clipvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipvault.clipvault.ui.theme.*

// === MODERN BUTTON (GRADIENT + SHADOW) ===
@Composable
fun ClipVaultButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    gradient: Boolean = true // Toggle gradient on/off
) {
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(RoyalBlue, BrightBlue, ElectricCyan)
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = BrightBlue.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (gradient) Color.Transparent else BrightBlue,
            contentColor = White
        ),
        enabled = !isLoading,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (gradient) Modifier.background(buttonGradient)
                    else Modifier
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// === OUTLINED BUTTON (MODERN BORDER) ===
@Composable
fun ClipVaultOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrightBlue
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, BrightBlue)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

// === MODERN INPUT FIELD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipVaultInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = BrightBlue.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrightBlue,
            focusedLabelColor = BrightBlue,
            cursorColor = BrightBlue,
            unfocusedBorderColor = MediumGray,
            disabledBorderColor = LightGray,
            errorBorderColor = ErrorRed,
            focusedContainerColor = White,
            unfocusedContainerColor = OffWhite
        )
    )
}

// === MODERN CARD ===
@Composable
fun ClipVaultCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DeepPurple.copy(alpha = 0.2f)
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// === GRADIENT BACKGROUND (UNTUK SPLASH/ONBOARDING) ===
@Composable
fun ClipVaultGradientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepPurple,
                        DarkNavy,
                        RoyalBlue.copy(alpha = 0.8f)
                    )
                )
            ),
        content = content
    )
}

// === FLOATING ACTION BUTTON (MODERN) ===
@Composable
fun ClipVaultFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit = {
        Text("+", fontSize = 28.sp, color = White, fontWeight = FontWeight.Bold)
    }
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = BrightBlue.copy(alpha = 0.4f)
            ),
        containerColor = BrightBlue,
        contentColor = White,
        shape = RoundedCornerShape(32.dp)
    ) {
        icon()
    }
}