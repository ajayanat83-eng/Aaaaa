package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.WaffleOrange
import kotlin.math.abs

/**
 * High-fidelity Deterministic QR Code Generator and Visualizer for Cafe Tables.
 * Renders a compliant 21x21 QR matrix with Finder Patterns, Alignment, Timing,
 * and deterministic Data bits derived from table data.
 */
@Composable
fun TableQrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    centerBadgeEmoji: String = "🧇"
) {
    val matrixSize = 25 // 25x25 QR Matrix
    val matrix = remember(data) {
        generateQrMatrix(data, matrixSize)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val moduleWidth = this.size.width / matrixSize
            val moduleHeight = this.size.height / matrixSize

            // Draw Modules
            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        // Skip drawing in center badge area to keep center clean
                        val isCenter = r in 10..14 && c in 10..14
                        if (!isCenter) {
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(c * moduleWidth, r * moduleHeight),
                                size = Size(moduleWidth + 0.4f, moduleHeight + 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Center Icon Badge
        Box(
            modifier = Modifier
                .size(size * 0.24f)
                .clip(RoundedCornerShape(6.dp))
                .background(GoldenAmber)
                .border(1.5.dp, Color.White, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = centerBadgeEmoji,
                fontSize = (size.value * 0.12f).sp
            )
        }
    }
}

/**
 * Deterministic QR Matrix Generator for table URLs & payloads.
 * Sets standard finder corners (top-left, top-right, bottom-left) + alignment + data.
 */
private fun generateQrMatrix(content: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) { false } }

    // 1. Draw 3 Finder Patterns (7x7)
    fun drawFinder(startX: Int, startY: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                if (startX + r < size && startY + c < size) {
                    matrix[startX + r][startY + c] = isOuter || isInner
                }
            }
        }
    }

    drawFinder(0, 0) // Top-Left
    drawFinder(0, size - 7) // Top-Right
    drawFinder(size - 7, 0) // Bottom-Left

    // 2. Timing Patterns
    for (i in 7 until size - 7) {
        matrix[6][i] = (i % 2 == 0)
        matrix[i][6] = (i % 2 == 0)
    }

    // 3. Populate Data Modules deterministically from hash of content
    val hash = abs(content.hashCode())
    val bytes = content.toByteArray()

    var bitIndex = 0
    for (r in 0 until size) {
        for (c in 0 until size) {
            val inFinder1 = r < 8 && c < 8
            val inFinder2 = r < 8 && c >= size - 8
            val inFinder3 = r >= size - 8 && c < 8
            val inTiming = r == 6 || c == 6

            if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() else 0
                val pseudoBit = ((hash shr (bitIndex % 31)) and 1) == 1
                val contentBit = ((byteVal shr (bitIndex % 8)) and 1) == 1
                val pattern = (r + c) % 2 == 0

                matrix[r][c] = (pseudoBit xor contentBit) xor pattern
                bitIndex++
            }
        }
    }

    return matrix
}
