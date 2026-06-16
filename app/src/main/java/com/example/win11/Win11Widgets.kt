package com.example.win11

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// ============================================
// WIDGETS BOARD POPUP PANEL (Left Hand Sidebar)
// ============================================
@Composable
fun BoxScope.WidgetsBoardOverlay(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isOpen by viewModel.widgetsBoardOpen.collectAsState()

    val cpuVal by viewModel.cpuUsage.collectAsState()
    val ramVal by viewModel.ramUsage.collectAsState()

    val boardBg = if (isDark) Color(0xE60F172A) else Color(0xE6FFFFFF)
    val textCol = if (isDark) Color.White else Color.Black
    val widgetCardBg = if (isDark) Color(0x33FFFFFF) else Color(0x0D000000)
    val boardShape = RoundedCornerShape(24.dp)

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .padding(bottom = 60.dp) // Floating Taskbar clearance
            .align(Alignment.BottomStart)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(boardBg, shape = boardShape)
                .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.45f), boardShape)
                .padding(16.dp)
        ) {
            // Widget Header Search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Widgets Board",
                    color = textCol,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = { viewModel.toggleWidgetsBoard() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Board", tint = textCol, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weather Widget
            Card(
                colors = CardDefaults.cardColors(containerColor = widgetCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                            )
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("London, UK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.Star, contentDescription = "Sun", tint = Color.Yellow, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("72°F", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Simulated Mild Sunshine • Rain 8%", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }

            // Hardware Performance Diagnostics Widget
            Card(
                colors = CardDefaults.cardColors(containerColor = widgetCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("System Hub Info", color = textCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text("• CPU Clock: $cpuVal% rate", color = Color.Gray, fontSize = 10.sp)
                    Text("• Memory Load: $ramVal% in-use", color = Color.Gray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sandbox status: Safe & Active", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Tech News widgets Column
            Text(
                "Simulated Headlines",
                color = textCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )

            listOf(
                "Kotlin Compose 1.7 Desktop releases Fluent extensions",
                "Google AI Studio optimizes complex local sandbox runtimes"
            ).forEach { headline ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = widgetCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(headline, color = textCol, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Windows News Sandbox • 1h ago", color = Color.Gray, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}


// ============================================
// QUICK ACTION CONTROL CENTER PANEL (Bottom Right popup)
// ============================================
@Composable
fun BoxScope.ControlCenterPopover(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isOpen by viewModel.controlCenterOpen.collectAsState()

    val isWifi by viewModel.isWifiEnabled.collectAsState()
    val isBth by viewModel.isBluetoothEnabled.collectAsState()
    val isAir by viewModel.isAirplaneModeEnabled.collectAsState()
    val volVal by viewModel.volumeSlider.collectAsState()
    val isMute by viewModel.isSystemMuted.collectAsState()
    val briVal by viewModel.brightnessSlider.collectAsState()

    val popoverBg = if (isDark) Color(0xE60F172A) else Color(0xE6FFFFFF)
    val textCol = if (isDark) Color.White else Color.Black
    val controlShape = RoundedCornerShape(24.dp)

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .padding(bottom = 74.dp, end = 12.dp) // Offset above floating taskbar
            .width(290.dp)
            .align(Alignment.BottomEnd)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.45f), controlShape),
            colors = CardDefaults.cardColors(containerColor = popoverBg),
            shape = controlShape,
            elevation = CardDefaults.cardElevation(22.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Toggles Grid Layout
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Wifi Toggle
                        QuickToggleItem(
                            label = "Wi-Fi",
                            isActive = isWifi,
                            isDark = isDark,
                            icon = Icons.Default.Share,
                            onClick = { viewModel.toggleWifi() },
                            modifier = Modifier.weight(1f)
                        )
                        // Bluetooth Toggle
                        QuickToggleItem(
                            label = "Bluetooth",
                            isActive = isBth,
                            isDark = isDark,
                            icon = Icons.Default.Send,
                            onClick = { viewModel.toggleBluetooth() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Airplane Toggle
                        QuickToggleItem(
                            label = "Airplane Mode",
                            isActive = isAir,
                            isDark = isDark,
                            icon = Icons.Default.LocationOn,
                            onClick = { viewModel.toggleAirplaneMode() },
                            modifier = Modifier.weight(1f)
                        )
                        // Dark Theme Toggle
                        QuickToggleItem(
                            label = "Dark Theme",
                            isActive = isDark,
                            isDark = isDark,
                            icon = Icons.Default.Settings,
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Volume slider section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleMute() }) {
                        Icon(
                            imageVector = if (isMute) Icons.Default.Warning else Icons.Default.PlayArrow,
                            contentDescription = "Mute",
                            tint = if (isMute) Color.Gray else Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Slider(
                        value = volVal.toFloat(),
                        onValueChange = { viewModel.setVolume(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF3B82F6),
                            activeTrackColor = Color(0xFF3B82F6)
                        )
                    )
                    Text("${if (isMute) 0 else volVal}%", color = textCol, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                }

                // Brightness slider section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Brightness",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp).padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = briVal.toFloat(),
                        onValueChange = { viewModel.setBrightness(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFBBF24),
                            activeTrackColor = Color(0xFFFBBF24)
                        )
                    )
                    Text("$briVal%", color = textCol, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Charging battery stats footer
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0x0C000000)).padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Battery", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Battery state: Charging (98%)", color = textCol, fontSize = 10.sp)
                    }
                    Text("C:\\", color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun QuickToggleItem(
    label: String,
    isActive: Boolean,
    isDark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val bg = if (isActive) Color(0xFF3B82F6) else (if (isDark) Color(0x22FFFFFF) else Color(0x1A000000))
    val tintColor = if (isActive) Color.White else (if (isDark) Color.White else Color.Black)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tintColor, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}


// ============================================
// DYNAMIC CALENDAR / ACTION DRAWER OVERLAY
// ============================================
@Composable
fun BoxScope.CalendarPopover(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isOpen by viewModel.calendarOpen.collectAsState()

    val calBg = if (isDark) Color(0xE60F172A) else Color(0xE6FFFFFF)
    val textCol = if (isDark) Color.White else Color.Black
    val calShape = RoundedCornerShape(24.dp)

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .padding(bottom = 74.dp, end = 12.dp) // Offset above floating taskbar
            .width(280.dp)
            .align(Alignment.BottomEnd)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.45f), calShape),
            colors = CardDefaults.cardColors(containerColor = calBg),
            shape = calShape,
            elevation = CardDefaults.cardElevation(22.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Calendar header with month info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cal = Calendar.getInstance()
                    val curMonthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: "Month"
                    val curYear = cal.get(Calendar.YEAR)
                    
                    Text("$curMonthName $curYear", color = textCol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = textCol, modifier = Modifier.size(16.dp))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))

                // Days names row
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Days Grid simulation
                val activeCalendar = Calendar.getInstance()
                val today = activeCalendar.get(Calendar.DAY_OF_MONTH)
                
                // Draw 5 rows of days
                var dayCount = 1
                for (row in 1..5) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        for (col in 1..7) {
                            val activeDay = dayCount
                            val isToday = activeDay == today
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(if (isToday) Color(0xFF3B82F6) else Color.Transparent)
                                    .clickable { /* Select simulated appointment calendar slot */ }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                if (activeDay <= 30) {
                                    Text(
                                        text = "$activeDay",
                                        color = if (isToday) Color.White else textCol,
                                        fontSize = 10.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            dayCount++
                        }
                    }
                }
            }
        }
    }
}


// ============================================
// SYSTEM SHUTDOWN / RESTART IMMERSIVE OVERLAYS
// ============================================
@Composable
fun SystemShuttingDownOverlay(viewModel: Win11ViewModel) {
    val shutdownMode by viewModel.shutdownState.collectAsState()

    if (shutdownMode == null) return

    val heading = if (shutdownMode == "shutting_down") "Shutting down" else "Restarting"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep space slate theme background
            .clickable { /* Block all user clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(44.dp),
                color = Color(0xFF3B82F6),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "$heading...",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Please keep your virtual hardware device open.",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
