package com.example.ui.customer

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AuthUiState
import com.example.data.FirebaseAuthRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CustomerPhoneAuthDialog(
    authRepository: FirebaseAuthRepository = FirebaseAuthRepository.instance,
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by authRepository.authState.collectAsState()

    var phoneInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var timerSeconds by remember { mutableStateOf(60) }
    var isTimerActive by remember { mutableStateOf(false) }

    // Start timer when OTP is sent
    LaunchedEffect(authState) {
        if (authState is AuthUiState.OtpSent) {
            timerSeconds = 60
            isTimerActive = true
            while (timerSeconds > 0 && isTimerActive) {
                delay(1000)
                timerSeconds -= 1
            }
            isTimerActive = false
        } else if (authState is AuthUiState.Authenticated) {
            onLoginSuccess()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🇳🇵 Customer Login",
                            color = GoldenAmber,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "The Janakpur Waffle & Cafe",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Divider(color = CardBorder)

                when (val state = authState) {
                    is AuthUiState.Idle, is AuthUiState.SendingOtp, is AuthUiState.Error -> {
                        // Phone Number Input Stage
                        Text(
                            text = "Login with your Nepal Mobile Number to earn TJW Club Points and view past orders.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Name (Optional)", color = TextSecondary) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WaffleOrange) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = WaffleOrange,
                                unfocusedBorderColor = CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phoneInput = it
                                }
                            },
                            label = { Text("Nepal Mobile Number", color = TextSecondary) },
                            placeholder = { Text("98XXXXXXXX", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("🇳🇵 +977", color = GoldenAmber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = WaffleOrange,
                                unfocusedBorderColor = CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state is AuthUiState.Error) {
                            Text(
                                text = "⚠️ ${state.message}",
                                color = ErrorRed,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (activity != null) {
                                    authRepository.sendOtp(
                                        activity = activity,
                                        phoneNumber = phoneInput
                                    )
                                }
                            },
                            enabled = phoneInput.length >= 10 && state !is AuthUiState.SendingOtp,
                            colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (state is AuthUiState.SendingOtp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending SMS OTP...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Verification OTP", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is AuthUiState.OtpSent, is AuthUiState.VerifyingCode -> {
                        // OTP Verification Stage
                        val sentPhone = if (state is AuthUiState.OtpSent) state.phoneNumber else phoneInput
                        val verificationId = if (state is AuthUiState.OtpSent) state.verificationId else ""

                        Text(
                            text = "Enter the 6-digit OTP code sent via SMS to $sentPhone",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    otpInput = it
                                }
                            },
                            label = { Text("6-Digit OTP Code", color = TextSecondary) },
                            placeholder = { Text("123456", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldenAmber) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldenAmber,
                                unfocusedBorderColor = CardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                authRepository.verifyOtp(
                                    verificationId = verificationId,
                                    smsCode = otpInput,
                                    customerName = nameInput
                                )
                            },
                            enabled = otpInput.length == 6 && state !is AuthUiState.VerifyingCode,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (state is AuthUiState.VerifyingCode) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify & Login", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Resend OTP / Change Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { authRepository.resetState() }
                            ) {
                                Text("Edit Number", color = TextSecondary, fontSize = 12.sp)
                            }

                            if (isTimerActive) {
                                Text("Resend in ${timerSeconds}s", color = TextMuted, fontSize = 12.sp)
                            } else {
                                TextButton(
                                    onClick = {
                                        if (activity != null) {
                                            authRepository.resendOtp(
                                                activity = activity,
                                                phoneNumber = sentPhone
                                            )
                                        }
                                    }
                                ) {
                                    Text("Resend OTP", color = WaffleOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is AuthUiState.Authenticated -> {
                        // Already Authenticated
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        Text("Logged in as ${state.profile.name}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(state.phoneNumber, color = GoldenAmber, fontSize = 13.sp)
                        Button(
                            onClick = onLoginSuccess,
                            colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange)
                        ) {
                            Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
