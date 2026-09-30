package com.futureTech.weighwise.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(percent = 50),  // Fully rounded chips and buttons
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),          // Cards 24dp radius
    extraLarge = RoundedCornerShape(32.dp)
)
