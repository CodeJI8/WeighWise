package com.noboj.weighwise.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(percent = 50), // Fully rounded for chips/buttons
    medium = RoundedCornerShape(24.dp), // Cards
    large = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp) // Bottom sheets
)
