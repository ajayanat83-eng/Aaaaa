package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthUiState
import com.example.data.CafeRepository
import com.example.data.FirebaseAuthRepository
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*

@Composable
fun CustomerProfileScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    authRepository: FirebaseAuthRepository = FirebaseAuthRepository.instance
) {
    val customerProfile by repository.customerProfile.collectAsState()
    val loyaltyTransactions by repository.loyaltyTransactions.collectAsState()
    val coupons by repository.coupons.collectAsState()
    val authState by authRepository.authState.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }

    if (showAuthDialog) {
        CustomerPhoneAuthDialog(
            authRepository = authRepository,
            onDismiss = { showAuthDialog = false },
            onLoginSuccess = { showAuthDialog = false }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text("My Profile & Rewards", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== 1. USER / AUTH STATUS CARD ====================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF381E04))
                                    .border(1.5.dp, WaffleOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 28.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(customerProfile.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(customerProfile.phone.ifBlank { "Guest (Not logged in)" }, color = TextSecondary, fontSize = 13.sp)
                                if (customerProfile.email.isNotBlank()) {
                                    Text(customerProfile.email, color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            PureVegBadge(showText = false)
                        }

                        // Auth Action Row
                        if (authState is AuthUiState.Authenticated) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Text("Verified with Firebase Phone Auth", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { authRepository.signOut() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Sign Out", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { showAuthDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🇳🇵 Login with Nepal Mobile OTP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ==================== 2. TJW LOYALTY CARD ====================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4A2800), Color(0xFF281800), Color(0xFF10281E))
                            )
                        )
                        .border(1.dp, GoldenAmber, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ TJW CLUB REWARDS", color = GoldenAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WaffleOrange)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("GOLD MEMBER", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("${customerProfile.loyaltyPoints}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                                Text("TJW Points Balance", color = GoldenAmber, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Worth NPR ${customerProfile.loyaltyPoints}", color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Earn 1 pt / NPR 100", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // ==================== 3. AVAILABLE COUPONS ====================
            item {
                Text("🏷️ Available Promo Offers", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            items(coupons) { coupon ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF381E04))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(coupon.code, color = GoldenAmber, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                            Text(coupon.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(coupon.description, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // ==================== 4. LOYALTY HISTORY ====================
            if (loyaltyTransactions.isNotEmpty()) {
                item {
                    Text("Points History", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                items(loyaltyTransactions) { tx ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tx.description, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = if (tx.pointsChange > 0) "+${tx.pointsChange}" else "${tx.pointsChange}",
                                color = if (tx.pointsChange > 0) SuccessGreen else ErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
