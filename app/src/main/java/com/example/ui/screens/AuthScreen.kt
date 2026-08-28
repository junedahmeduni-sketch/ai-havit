package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.ShapeGeometricCard
import com.example.ui.theme.ShapeGeometricSubtle
import com.example.ui.viewmodel.MainViewModel

enum class AuthMode {
    LOGIN, SIGN_UP, FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("alex@habittrack.ai") }
    var password by remember { mutableStateOf("password123") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isBangla = settings.language == AppLanguage.BENGALI

    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Icon
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBangla) "HabitTrack AI" else "HabitTrack AI",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> if (isBangla) "তোমার একাউন্টে লগইন করো" else "Sign in to your Life & Habit Coach"
                    AuthMode.SIGN_UP -> if (isBangla) "নতুন একাউন্ট তৈরি করো" else "Create your personalized account"
                    AuthMode.FORGOT_PASSWORD -> if (isBangla) "পাসওয়ার্ড রিসেট করো" else "Reset your password"
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                shape = ShapeGeometricCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (authError != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = ShapeGeometricSubtle,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                                )
                            }
                        }
                    }

                    if (mode == AuthMode.SIGN_UP) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; viewModel.clearAuthError() },
                            label = { Text(if (isBangla) "তোমার নাম" else "Full Name") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearAuthError() },
                        label = { Text(if (isBangla) "ইমেইল অ্যাড্রেস" else "Email Address") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearAuthError() },
                        label = {
                            Text(
                                if (mode == AuthMode.FORGOT_PASSWORD) {
                                    if (isBangla) "নতুন পাসওয়ার্ড" else "New Password"
                                } else {
                                    if (isBangla) "পাসওয়ার্ড" else "Password"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (mode == AuthMode.SIGN_UP) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; viewModel.clearAuthError() },
                            label = { Text(if (isBangla) "কনফার্ম পাসওয়ার্ড" else "Confirm Password") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            when (mode) {
                                AuthMode.LOGIN -> viewModel.login(email, password, onAuthSuccess)
                                AuthMode.SIGN_UP -> viewModel.signUp(name, email, password, confirmPassword, onAuthSuccess)
                                AuthMode.FORGOT_PASSWORD -> viewModel.forgotPassword(email, password) {
                                    mode = AuthMode.LOGIN
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_action_btn"),
                        shape = ShapeGeometricSubtle
                    ) {
                        Text(
                            text = when (mode) {
                                AuthMode.LOGIN -> if (isBangla) "লগইন করুন" else "Log In"
                                AuthMode.SIGN_UP -> if (isBangla) "সাইন আপ করুন" else "Sign Up"
                                AuthMode.FORGOT_PASSWORD -> if (isBangla) "পাসওয়ার্ড আপডেট করুন" else "Reset Password"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (mode == AuthMode.LOGIN) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { mode = AuthMode.FORGOT_PASSWORD; viewModel.clearAuthError() }) {
                                Text(
                                    text = if (isBangla) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = { mode = AuthMode.SIGN_UP; viewModel.clearAuthError() }) {
                                Text(
                                    text = if (isBangla) "নতুন একাউন্ট? সাইন আপ" else "Sign Up",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { mode = AuthMode.LOGIN; viewModel.clearAuthError() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (isBangla) "লগইন স্ক্রিনে ফিরে যান" else "Back to Login",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Demo Login Button
            OutlinedButton(
                onClick = {
                    viewModel.login("alex@habittrack.ai", "password123", onAuthSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_quick_login_btn"),
                shape = ShapeGeometricSubtle
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "ডেমো একাউন্টে ১-ক্লিকে প্রবেশ ⚡" else "Quick Demo Login (Alex Johnson) ⚡",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
