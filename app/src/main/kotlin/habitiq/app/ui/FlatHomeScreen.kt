package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.flats.FlatHomeViewModel
import habitiq.app.flats.Member
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.ui.theme.HabitiqBrand

// Ink at reduced alpha rather than InkMute for small secondary text: InkMute on the
// dark Canvas measures ~2.4:1, below the 4.5:1 contrast floor.
private val SecondaryInk = HabitiqBrand.Ink.copy(alpha = 0.72f)
private val LabelInk = HabitiqBrand.Ink.copy(alpha = 0.55f)

@Composable
fun FlatHomeScreen(viewModel: FlatHomeViewModel, currentUid: String) {
    val flat by viewModel.flat.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    val currentFlat = flat
    if (currentFlat == null) {
        FlatHomeLoading()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        FlatHeader(name = currentFlat.name, memberCount = currentFlat.memberCount)

        if (currentFlat.adminUid == currentUid) {
            Spacer(Modifier.height(24.dp))
            InviteCodeCard(
                code = currentFlat.id,
                onShare = { launchShareInviteCode(context, currentFlat.name, currentFlat.id) }
            )
        }

        Spacer(Modifier.height(28.dp))
        SectionLabel("ROOMMATES")
        Spacer(Modifier.height(12.dp))
        members.forEach { member ->
            MemberRow(member = member, isCurrentUser = member.uid == currentUid)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FlatHomeLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = HabitiqBrand.Primary, strokeWidth = 3.dp)
        Spacer(Modifier.height(16.dp))
        Text("Loading your flat…", color = SecondaryInk, fontSize = 15.sp)
    }
}

@Composable
private fun FlatHeader(name: String, memberCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(HabitiqBrand.Primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = HabitiqBrand.PrimarySoft,
                modifier = Modifier.size(26.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(name, color = HabitiqBrand.Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                if (memberCount == 1) "1 member" else "$memberCount members",
                color = SecondaryInk,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun InviteCodeCard(code: String, onShare: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HabitiqBrand.InputBackground, RoundedCornerShape(16.dp))
            .border(1.dp, HabitiqBrand.InputBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text("INVITE CODE", color = LabelInk, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Text(
            code,
            color = HabitiqBrand.PrimarySoft,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "Share this with a roommate so they can join your flat.",
            color = SecondaryInk,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitiqBrand.Primary,
                contentColor = HabitiqBrand.OnPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Share invite code",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = LabelInk, fontSize = 11.sp, letterSpacing = 1.5.sp)
}

@Composable
private fun MemberRow(member: Member, isCurrentUser: Boolean) {
    val isAdmin = member.role == "admin"
    val displayName = member.nickname.ifBlank { "Roommate" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HabitiqBrand.InputBackground, RoundedCornerShape(12.dp))
            .border(1.dp, HabitiqBrand.InputBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isAdmin) HabitiqBrand.Primary else HabitiqBrand.InputBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                displayName.first().uppercase(),
                color = if (isAdmin) HabitiqBrand.OnPrimary else HabitiqBrand.Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                displayName,
                color = HabitiqBrand.Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isCurrentUser) {
                Text("You", color = SecondaryInk, fontSize = 12.sp)
            }
        }
        RoleBadge(isAdmin = isAdmin)
    }
}

@Composable
private fun RoleBadge(isAdmin: Boolean) {
    Row(
        modifier = Modifier
            .background(
                if (isAdmin) HabitiqBrand.Primary.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(999.dp)
            )
            .border(
                1.dp,
                if (isAdmin) HabitiqBrand.Primary.copy(alpha = 0.5f) else HabitiqBrand.InputBorder,
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAdmin) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = HabitiqBrand.PrimarySoft,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            if (isAdmin) "Admin" else "Member",
            color = if (isAdmin) HabitiqBrand.PrimarySoft else SecondaryInk,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
