package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CafeRepository
import com.example.data.FirestoreMenuRepository
import com.example.model.CustomerFeedback
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerFeedbackDialog(
    repository: CafeRepository,
    orderId: String? = null,
    orderNumber: String? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val customerProfile by repository.customerProfile.collectAsState()

    var overallRating by remember { mutableIntStateOf(5) }
    var foodRating by remember { mutableIntStateOf(5) }
    var serviceRating by remember { mutableIntStateOf(5) }
    var ambienceRating by remember { mutableIntStateOf(5) }

    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var comment by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf(customerProfile.name.ifBlank { "" }) }
    var customerPhone by remember { mutableStateOf(customerProfile.phone.ifBlank { "" }) }

    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val presetTags = listOf(
        "🧇 Crispy Waffles",
        "🥟 Delicious Momos",
        "🍫 Chocolate Overload",
        "🌱 100% Pure Veg",
        "⚡ Superfast Service",
        "🥤 Refreshing Coolers",
        "📦 Neat Packaging",
        "💰 Value for Money"
    )

    val ratingLabels = mapOf(
        1 to "😡 Needs Improvement",
        2 to "🙁 Fair",
        3 to "🙂 Good",
        4 to "😃 Very Good",
        5 to "🤩 TJW Amazing!"
    )

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⭐ Customer Feedback", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (orderNumber != null) "Order $orderNumber • The Janakpur Waffle & Cafe" else "The Janakpur Waffle & Cafe",
                            color = GoldenAmber,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Divider(color = CardBorder, modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                if (isSuccess) {
                    // Success View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F3D24))
                                    .border(2.dp, SuccessGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(44.dp))
                            }
                            Text(
                                text = "Dhanyabaad! 🙏",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your feedback has been recorded and saved to our Firestore database. It helps us serve Janakpurdham even better!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            PureVegBadge()
                        }
                    }
                } else {
                    // Form View
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Overall Star Rating
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Overall Dining Experience", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (star in 1..5) {
                                        IconButton(
                                            onClick = { overallRating = star },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (star <= overallRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "$star Stars",
                                                tint = if (star <= overallRating) GoldenAmber else TextMuted,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = ratingLabels[overallRating] ?: "Good",
                                    color = GoldenAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Specific Aspect Ratings
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Aspect Ratings", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            AspectRatingRow(
                                title = "🧇 Food Quality & Taste (100% Veg)",
                                rating = foodRating,
                                onRatingChange = { foodRating = it }
                            )

                            AspectRatingRow(
                                title = "⚡ Speed & Service",
                                rating = serviceRating,
                                onRatingChange = { serviceRating = it }
                            )

                            AspectRatingRow(
                                title = "☕ Ambience & Packaging",
                                rating = ambienceRating,
                                onRatingChange = { ambienceRating = it }
                            )
                        }

                        // Quick Experience Tags
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("What did you love most?", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                presetTags.forEach { tag ->
                                    val isSelected = selectedTags.contains(tag)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) Color(0xFF381E04) else DarkSurfaceVariant)
                                            .border(1.dp, if (isSelected) WaffleOrange else CardBorder, RoundedCornerShape(20.dp))
                                            .clickable {
                                                selectedTags = if (isSelected) {
                                                    selectedTags - tag
                                                } else {
                                                    selectedTags + tag
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = if (isSelected) GoldenAmber else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Customer Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Your Name", color = TextSecondary, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = WaffleOrange,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )

                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Phone (+977)", color = TextSecondary, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = WaffleOrange,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )
                        }

                        // Suggestions & Comments
                        OutlinedTextField(
                            value = suggestions,
                            onValueChange = { suggestions = it },
                            label = { Text("Suggestions or Comments (Optional)", color = TextSecondary, fontSize = 12.sp) },
                            placeholder = { Text("Tell us how we can make your TJW experience even better...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = WaffleOrange,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                val feedbackObj = CustomerFeedback(
                                    orderId = orderId,
                                    orderNumber = orderNumber,
                                    customerName = customerName.ifBlank { "Guest Customer" },
                                    customerPhone = customerPhone,
                                    overallRating = overallRating,
                                    foodRating = foodRating,
                                    serviceRating = serviceRating,
                                    ambienceRating = ambienceRating,
                                    tags = selectedTags.toList(),
                                    comment = suggestions,
                                    suggestions = suggestions,
                                    createdAt = System.currentTimeMillis()
                                )

                                FirestoreMenuRepository.instance.submitFeedback(feedbackObj)
                                repository.logAudit("Feedback", "Customer feedback submitted with rating: $overallRating stars")

                                isSubmitting = false
                                isSuccess = true
                                delay(1800)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaffleOrange,
                            contentColor = Color.Black
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving to Firestore...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectRatingRow(
    title: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (i <= rating) GoldenAmber else TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onRatingChange(i) }
                )
            }
        }
    }
}
