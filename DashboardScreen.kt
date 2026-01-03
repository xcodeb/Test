package com.invictus.invictusmob.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.invictus.invictusmob.R
import com.invictus.invictusmob.core.Formatters
import com.invictus.invictusmob.data.api.ApiClient
import com.invictus.invictusmob.data.dto.UserCandidateRowDto
import com.invictus.invictusmob.ui.components.BadgePill
import com.invictus.invictusmob.ui.components.InfoCard
import com.invictus.invictusmob.ui.navigation.Routes
import com.invictus.invictusmob.ui.theme.*

@Composable
fun DashboardScreen(rootNav: NavController, padding: PaddingValues) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var totalUsers by remember { mutableIntStateOf(0) }
    var pendingRequests by remember { mutableIntStateOf(0) }
    var accountAmount by remember { mutableStateOf(0.0) }
    var latest by remember { mutableStateOf(listOf<UserCandidateRowDto>()) }

    LaunchedEffect(Unit) {
        loading = true; error = null
        try {
            val res = ApiClient.api.dashboard()
            if (res.ok && res.data != null) {
                totalUsers = res.data.totalusers
                pendingRequests = res.data.pendingrequests
                accountAmount = res.data.accountamount
                latest = res.data.latest
            } else error = res.error ?: "API error"
        } catch (e: Throwable) { error = e.message ?: "Network error" }
        finally { loading = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize
            .padding(padding)
            .padding(top = 20.dp)  // Malo prostora ispod AppTopBar
    ) {
        // Header background image (ispod AppTopBar)
        Image(
            painter = painterResource(id = R.drawable.h_back),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)  // Manje jer AppTopBar zauzima prostor
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )

        // Content (kartice preko slike)
        Column(
            modifier = Modifier
                .fillMaxSize
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp)  // Preklapanje sa slikom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).shadow(12.dp, RoundedCornerShape(22.dp))) {
                    InfoCard("Korisnici", "$totalUsers", bgIcon = Icons.Filled.Groups)
                }
                Box(modifier = Modifier.weight(1f).shadow(12.dp, RoundedCornerShape(22.dp))) {
                    InfoCard("Račun", Formatters.money(accountAmount), bgIcon = Icons.Filled.CreditCard)
                }
            }
            Spacer(Modifier.height(18.dp))

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)))
                return@Column
            }
            if (error != null) {
                Text("Greška: $error", color = Danger, style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Text("Poslednje uplate", style = MaterialTheme.typography.titleLarge.copy(
                color = Color.Black, fontWeight = FontWeight.Bold
            ))
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)  // Za BottomBar
            ) {
                items(latest) { row ->
                    val (bg, label) = when (row.paymentstatus) {
                        "paid" -> Success to "PAID"
                        "pending" -> Warning to "PENDING"
                        "failed" -> Danger to "FAILED"
                        "cancelled" -> Muted to "CANCELLED"
                        else -> Muted to (row.paymentstatus ?: "UNKNOWN")
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .clickable { rootNav.navigate(Routes.UserInfo(row.id)) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            // Firma + status
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    row.companyname,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                BadgePill(label, bg)
                            }
                            Spacer(Modifier.height(6.dp))
                            // Referent + iznos
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    row.referentimeprezime ?: "-",
                                    color = Muted,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                                )
                                Text(
                                    if (row.amount != null) Formatters.money(row.amount) + " KM" else "- KM",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                                    color = Color.Black
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            // Arrow
                            Box(
                                Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .align(Alignment.End),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.ArrowForwardIos, null, Modifier.size(16.dp), Color(0xFF334155))
                            }
                        }
                    }
                }
            }
        }
    }
}
