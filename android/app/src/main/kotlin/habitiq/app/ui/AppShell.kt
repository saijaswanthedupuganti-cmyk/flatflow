package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DISCOVER("Discover", Icons.Filled.Search),
    MANAGE("Manage", Icons.Filled.GridView),
    PROFILE("Profile", Icons.Filled.Person)
}

@Composable
fun AppShell(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FigmaColors.Background)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
        HabitiqBottomBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onPlusClick = onPlusClick
        )
    }
}

@Composable
private fun HabitiqBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onPlusClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FigmaColors.Background,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    tab = AppTab.HOME,
                    selected = selectedTab == AppTab.HOME,
                    onClick = { onTabSelected(AppTab.HOME) }
                )
                BottomNavItem(
                    tab = AppTab.DISCOVER,
                    selected = selectedTab == AppTab.DISCOVER,
                    onClick = { onTabSelected(AppTab.DISCOVER) }
                )
                Spacer(Modifier.width(56.dp))
                BottomNavItem(
                    tab = AppTab.MANAGE,
                    selected = selectedTab == AppTab.MANAGE,
                    onClick = { onTabSelected(AppTab.MANAGE) }
                )
                BottomNavItem(
                    tab = AppTab.PROFILE,
                    selected = selectedTab == AppTab.PROFILE,
                    onClick = { onTabSelected(AppTab.PROFILE) }
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(56.dp)
                .shadow(12.dp, CircleShape, spotColor = FigmaColors.Primary.copy(alpha = 0.4f))
                .background(FigmaColors.Primary, CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .clickable(onClick = onPlusClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) FigmaColors.Primary else FigmaColors.InkMuted
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tab.label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
