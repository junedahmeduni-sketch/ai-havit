package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Geometric Balance: Sharp-edged containers and architectural grid shapes
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(6.dp)
)

// Sharp corner shape tokens for consistent container geometry
val ShapeSharp = RoundedCornerShape(0.dp)
val ShapeGeometricSubtle = RoundedCornerShape(2.dp)
val ShapeGeometricCard = RoundedCornerShape(4.dp)
val ShapeGeometricPill = RoundedCornerShape(2.dp)
