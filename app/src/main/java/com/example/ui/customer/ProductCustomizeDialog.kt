package com.example.ui.customer

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Product
import com.example.model.ProductAddon
import com.example.model.ProductVariant
import com.example.ui.components.NprPriceText
import com.example.ui.components.ProductBadgeTag
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*

@Composable
fun ProductCustomizeDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (variant: ProductVariant?, addons: List<ProductAddon>, notes: String) -> Unit
) {
    var selectedVariant by remember { mutableStateOf(product.variants.firstOrNull()) }
    var selectedAddons by remember { mutableStateOf<List<ProductAddon>>(emptyList()) }
    var itemNotes by remember { mutableStateOf("") }

    val basePrice = selectedVariant?.price ?: product.price
    val addonsPrice = selectedAddons.sumOf { it.price }
    val totalPrice = basePrice + addonsPrice

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PureVegBadge()
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Emoji visual & Product Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.imageEmoji, fontSize = 28.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.badge != null) {
                            Spacer(modifier = Modifier.height(3.dp))
                            ProductBadgeTag(badgeText = product.badge)
                        }
                    }
                }

                Text(
                    text = product.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Divider(color = CardBorder, thickness = 0.8.dp)

                // VARIANTS SECTION (if any)
                if (product.variants.isNotEmpty()) {
                    Text(
                        text = "Select Size / Option:",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.variants.forEach { variant ->
                            val isSelected = selectedVariant?.id == variant.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF3D2205) else DarkSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) WaffleOrange else CardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedVariant = variant }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedVariant = variant },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = WaffleOrange,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                    Text(
                                        text = variant.name,
                                        color = if (isSelected) GoldenAmber else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                NprPriceText(amount = variant.price, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // ADDONS SECTION (if any)
                if (product.addons.isNotEmpty()) {
                    Text(
                        text = "Customize with Add-ons:",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.addons.forEach { addon ->
                            val isChecked = selectedAddons.any { it.id == addon.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isChecked) Color(0xFF2A200A) else DarkSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isChecked) GoldenAmber else CardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedAddons = if (isChecked) {
                                            selectedAddons.filterNot { it.id == addon.id }
                                        } else {
                                            selectedAddons + addon
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedAddons = if (checked) {
                                                selectedAddons + addon
                                            } else {
                                                selectedAddons.filterNot { it.id == addon.id }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GoldenAmber,
                                            checkmarkColor = Color.Black,
                                            uncheckedColor = TextMuted
                                        )
                                    )
                                    Text(
                                        text = addon.name,
                                        color = if (isChecked) GoldenAmber else TextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "+NPR ${addon.price.toInt()}",
                                    color = GoldenAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // SPECIAL INSTRUCTIONS
                OutlinedTextField(
                    value = itemNotes,
                    onValueChange = { itemNotes = it },
                    label = { Text("Special Instructions (Optional)", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("e.g. Extra crispy, less sweet, no onions", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = WaffleOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Add Button
                Button(
                    onClick = {
                        onAddToCart(selectedVariant, selectedAddons, itemNotes)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaffleOrange,
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add to Cart",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NPR ${totalPrice.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
