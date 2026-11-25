package com.example.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ApexAmber
import com.example.ui.theme.ApexAmberContainer
import com.example.ui.theme.ApexBorder
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexErrorContainer
import com.example.ui.theme.ApexSuccess
import com.example.ui.theme.ApexSuccessContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceElevated
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    containerColor: Color = ApexSurface,
    borderColor: Color = ApexBorder,
    cornerRadius: Dp = 14.dp,
    contentPadding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun ApexPillBadge(
    text: String,
    accentColor: Color = ApexCyan,
    containerColor: Color = ApexCyanContainer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ApexActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ApexCyan
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(10.dp),
        color = ApexSurfaceElevated,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = ApexTextPrimary,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ApexSectionTitle(
    title: String,
    subtitle: String? = null,
    badge: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ApexTextPrimary
            )
            if (badge != null) {
                ApexPillBadge(text = badge, accentColor = ApexAmber, containerColor = ApexAmberContainer)
            }
        }
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ApexTextSecondary
            )
        }
    }
}

@Composable
fun MonoFormulaBox(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = ApexCyan
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ApexSurfaceElevated)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = ApexTextPrimary
        )
    }
}

@Composable
fun StatusBanner(
    text: String,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when {
        isError -> ApexErrorContainer to ApexError
        isSuccess -> ApexSuccessContainer to ApexSuccess
        else -> ApexAmberContainer to ApexAmber
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(color = fg, fontWeight = FontWeight.Medium)
        )
    }
}
