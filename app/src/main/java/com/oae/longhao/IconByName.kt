package com.oae.longhao

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun IconByName(name: String) {
    val icon: ImageVector? = remember(name) {
        try {
            val cl = Class.forName("androidx.compose.material.icons.outlined.${name}Kt")
            val method = cl.declaredMethods.first()
            method.invoke(null, Icons.Outlined) as ImageVector
        } catch (_: Throwable) {
            null
        }
    }
    if (icon != null) {
        Icon(icon, "$name icon")
    }
}