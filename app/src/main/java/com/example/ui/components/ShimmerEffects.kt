package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Shimmer brush modifier that creates a shimmering loading animation
 * across components while content is being fetched from Firestore or Room DB.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFF222224),
    highlightColor: Color = Color(0xFF38383C)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerAnimation"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Skeleton placeholder for Category Chips / Circles.
 */
@Composable
fun CategorySkeletonChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .shimmerEffect(CircleShape)
        )
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(14.dp)
                .shimmerEffect(RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Row of Shimmer Category Chips.
 */
@Composable
fun CategorySkeletonRow(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(itemCount) {
            CategorySkeletonChip()
        }
    }
}

/**
 * Skeleton placeholder for Menu Product Grid Card.
 */
@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Image Placeholder with Veg Badge placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .shimmerEffect(RoundedCornerShape(12.dp))
            ) {
                // Veg badge skeleton on top-left
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(width = 44.dp, height = 18.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), baseColor = Color(0xFF1B382B), highlightColor = Color(0xFF285440))
                )
            }

            // Product Title Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .shimmerEffect(RoundedCornerShape(4.dp))
            )

            // Short Description Skeleton (2 lines)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .shimmerEffect(RoundedCornerShape(3.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(10.dp)
                        .shimmerEffect(RoundedCornerShape(3.dp))
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Bottom Price & Add Button Row Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Skeleton
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(16.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp), baseColor = Color(0xFF382C10), highlightColor = Color(0xFF524018))
                )

                // Add button Skeleton
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 30.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp), baseColor = Color(0xFF382200), highlightColor = Color(0xFF5A3800))
                )
            }
        }
    }
}

/**
 * Skeleton for List Style Product Item
 */
@Composable
fun ProductListSkeletonItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(72.dp)
                .shimmerEffect(RoundedCornerShape(10.dp))
        )

        // Text details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .shimmerEffect(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(10.dp)
                    .shimmerEffect(RoundedCornerShape(3.dp))
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .shimmerEffect(RoundedCornerShape(3.dp))
            )
        }

        // Action button
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 32.dp)
                .shimmerEffect(RoundedCornerShape(8.dp))
        )
    }
}

/**
 * Full Menu Grid Skeleton loading view
 */
@Composable
fun MenuSkeletonLoadingGrid(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategorySkeletonRow()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(itemCount / 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductCardSkeleton(modifier = Modifier.weight(1f))
                    ProductCardSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
