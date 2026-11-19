package com.example.debit.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun CustomColorPickerDialog(
    language: AppLanguage,
    initialColorHex: String?,
    onDismissRequest: () -> Unit,
    onConfirmColor: (hex: String) -> Unit
) {
    val isZh = language == AppLanguage.ZH

    // Convert initial color to HSV
    val initialColorInt = remember(initialColorHex) {
        try {
            val clean = initialColorHex?.trim() ?: "#1B4A31"
            val formatted = if (clean.startsWith("#")) clean else "#$clean"
            android.graphics.Color.parseColor(formatted)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#1B4A31")
        }
    }

    val initialHsv = remember(initialColorInt) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColorInt, hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2].coerceIn(0.15f, 1.0f)) }

    val currentColorInt = remember(hue, saturation, brightness) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }
    val currentColor = Color(currentColorInt)
    val hexCode = remember(currentColorInt) {
        String.format("#%06X", 0xFFFFFF and currentColorInt)
    }

    var hexText by remember(hexCode) { mutableStateOf(hexCode) }

    LaunchedEffect(hexCode) {
        hexText = hexCode
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    text = if (isZh) "主題調色盤" else "Theme Color Palette",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Circular HSV Color Wheel (色相環)
                HsvColorWheel(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f),
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    currentColor = currentColor,
                    onColorChanged = { newHue, newSat ->
                        hue = newHue
                        saturation = newSat
                    }
                )

                // 2. Brightness Slider (明度調整)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isZh) "色彩明暗度" else "Brightness",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${(brightness * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentColor,
                            activeTrackColor = currentColor,
                            inactiveTrackColor = currentColor.copy(alpha = 0.24f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Current Color Preview & Hex Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = currentColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(currentColor)
                                .border(2.5.dp, Color.White, CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isZh) "即時預覽效果" else "Live Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = hexCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // HEX input field
                        OutlinedTextField(
                            value = hexText,
                            onValueChange = { input ->
                                hexText = input
                                try {
                                    val formatted = if (input.startsWith("#")) input else "#$input"
                                    if (formatted.length == 7) {
                                        val parsed = android.graphics.Color.parseColor(formatted)
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(parsed, hsv)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        brightness = hsv[2].coerceIn(0.15f, 1.0f)
                                    }
                                } catch (_: Exception) {}
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier.width(110.dp),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val c = android.graphics.Color.parseColor("#1B4A31")
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(c, hsv)
                        hue = hsv[0]
                        saturation = hsv[1]
                        brightness = hsv[2].coerceIn(0.15f, 1.0f)
                        hexText = "#1B4A31"
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isZh) "重設墨綠" else "Default", fontSize = 13.sp)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(if (isZh) "取消" else "Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onConfirmColor(hexCode)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isZh) "確認套用" else "Apply Color",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun HsvColorWheel(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    brightness: Float,
    currentColor: Color,
    onColorChanged: (hue: Float, sat: Float) -> Unit
) {
    var wheelCenter by remember { mutableStateOf(Offset.Zero) }
    var wheelRadius by remember { mutableFloatStateOf(0f) }

    fun updateTouch(position: Offset) {
        if (wheelRadius <= 0f) return
        val dx = position.x - wheelCenter.x
        val dy = position.y - wheelCenter.y
        val dist = sqrt(dx * dx + dy * dy)
        val angleRad = atan2(dy.toDouble(), dx.toDouble())
        var deg = Math.toDegrees(angleRad).toFloat()
        if (deg < 0f) deg += 360f

        val newHue = (360f - deg) % 360f
        val sat = (dist / wheelRadius).coerceIn(0f, 1f)
        onColorChanged(newHue, sat)
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    updateTouch(offset)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateTouch(change.position)
                }
            }
    ) {
        val sizeMin = size.minDimension
        val radius = sizeMin / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        wheelCenter = center
        wheelRadius = radius

        // 1. Draw SweepGradient for full 360-degree Hue wheel matching reference palette
        // Right: Red (0°), Down-Right: Magenta (300°), Down-Left: Blue (240°), Left: Cyan (180°), Up-Left: Green (120°), Up-Right: Yellow (60°), Right: Red (0°)
        val sweepBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFF0000),
                Color(0xFFFF00FF),
                Color(0xFF0000FF),
                Color(0xFF00FFFF),
                Color(0xFF00FF00),
                Color(0xFFFFFF00),
                Color(0xFFFF0000)
            ),
            center = center
        )
        drawCircle(
            brush = sweepBrush,
            radius = radius,
            center = center
        )

        // 2. Draw RadialGradient for Saturation (Center is pure White, smoothly blending outwards to transparent white)
        val radialBrush = Brush.radialGradient(
            colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
            center = center,
            radius = radius
        )
        drawCircle(
            brush = radialBrush,
            radius = radius,
            center = center
        )

        // 3. Brightness shadow overlay: provides instant live preview of brightness directly on the wheel
        if (brightness < 1.0f) {
            drawCircle(
                color = Color.Black.copy(alpha = (1.0f - brightness).coerceIn(0f, 1f)),
                radius = radius,
                center = center
            )
        }

        // 4. Subtle wheel outer border ring
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )

        // 4. Selector thumb
        val currentAngle = (360f - hue) % 360f
        val currentRad = Math.toRadians(currentAngle.toDouble())
        val currentDist = saturation * radius
        val thumbX = center.x + (currentDist * cos(currentRad)).toFloat()
        val thumbY = center.y + (currentDist * sin(currentRad)).toFloat()
        val thumbCenter = Offset(thumbX, thumbY)

        // Shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = 15.dp.toPx(),
            center = Offset(thumbX, thumbY + 1.5.dp.toPx())
        )
        // White ring
        drawCircle(
            color = Color.White,
            radius = 13.dp.toPx(),
            center = thumbCenter
        )
        // Inner selected color
        drawCircle(
            color = currentColor,
            radius = 9.5.dp.toPx(),
            center = thumbCenter
        )
    }
}
