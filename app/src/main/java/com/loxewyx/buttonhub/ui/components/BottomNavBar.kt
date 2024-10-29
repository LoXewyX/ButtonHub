package com.loxewyx.buttonhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.loxewyx.buttonhub.R

@Composable
fun BottomNavBar(navController: NavController) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem(
                R.drawable.ic_dashboard,
                stringResource(R.string.navbar_songs),
                "songs",
                navController,
                Modifier.weight(1f)
            )
            BottomNavItem(
                R.drawable.ic_profile,
                stringResource(R.string.navbar_favorites),
                "favorites",
                navController,
                Modifier.weight(1f)
            )
            BottomNavItem(
                R.drawable.ic_settings,
                stringResource(R.string.navbar_settings),
                "settings",
                navController,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: Int,
    label: String,
    route: String,
    navController: NavController,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            .padding(8.dp)
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.background
        )
        Text(
            text = label,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.background
        )
    }
}
