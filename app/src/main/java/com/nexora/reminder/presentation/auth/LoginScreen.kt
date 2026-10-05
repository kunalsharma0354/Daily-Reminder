package com.nexora.reminder.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexora.reminder.ui.theme.ReminderTokens

@Composable
fun LoginScreen(vm: LoginViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    fun submit() {
        vm.login(username, password)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(0.5f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        cursorColor = Color.White,
        focusedBorderColor = Color.White.copy(0.7f),
        unfocusedBorderColor = Color.White.copy(0.22f),
        focusedLabelColor = Color.White.copy(0.8f),
        unfocusedLabelColor = Color.White.copy(0.55f),
        focusedLeadingIconColor = Color.White.copy(0.85f),
        unfocusedLeadingIconColor = Color.White.copy(0.55f),
        focusedTrailingIconColor = Color.White.copy(0.85f),
        unfocusedTrailingIconColor = Color.White.copy(0.55f)
    )

    Box(
        Modifier.fillMaxSize().background(Color.Black)
    ) {
        // soft glass glow behind card
        Box(
            Modifier.size(300.dp).offset(x = (-90).dp, y = (-70).dp)
                .clip(CircleShape).background(Color.White.copy(0.07f)).blur(90.dp)
        )
        Box(
            Modifier.size(260.dp).align(Alignment.BottomEnd).offset(x = 80.dp, y = 90.dp)
                .clip(CircleShape).background(Color.White.copy(0.05f)).blur(90.dp)
        )

        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Daily Reminder",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.6f)
            )
            Spacer(Modifier.height(24.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = ReminderTokens.CardRadius,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(0.16f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; vm.clearError() },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = ReminderTokens.CardRadiusSmall,
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        enabled = !ui.loading
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; vm.clearError() },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = ReminderTokens.CardRadiusSmall,
                        colors = fieldColors,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        enabled = !ui.loading
                    )
                    AnimatedVisibility(ui.error != null) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                ui.error ?: "",
                                color = Color(0xFFFDA4A4),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { submit() },
                        enabled = !ui.loading && username.isNotBlank() && password.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = ReminderTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White.copy(0.25f),
                            disabledContentColor = Color.White.copy(0.6f)
                        )
                    ) {
                        if (ui.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Text("Sign in", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
