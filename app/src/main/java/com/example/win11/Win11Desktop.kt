package com.example.win11

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================
// WINDOWS 11 CORE DESKTOP COMPONENT
// ============================================
@Composable
fun Win11DesktopHost(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isAutohide by viewModel.isTaskbarAutohide.collectAsState()
    
    val wallpapers = viewModel.wallpaperPresets
    val currentWpIdx by viewModel.currentWallpaperIndex.collectAsState()
    val activePreset = wallpapers[currentWpIdx]
    
    val activeWindows by viewModel.activeWindows.collectAsState()
    val density = LocalDensity.current.density

    // Setup linear gradient brush
    val wallpaperBrush = remember(currentWpIdx) {
        val colorObjects = activePreset.colors.map { Color(android.graphics.Color.parseColor(it)) }
        Brush.linearGradient(
            colors = colorObjects,
            start = Offset(0f, 0f),
            end = Offset(1000f, 1500f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaperBrush)
    ) {
        // Overlay a soft floating glowing light bulb bloom to recreate the Win11 wave wallpaper
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.5f, size.height * 0.4f)
            )
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.04f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.65f, size.height * 0.35f)
            )
        }

        // DESKTOP ICONS GRID (Left sided Column)
        DesktopIconsSidebar(viewModel)

        // DRAGGABLE WINDOWS AREA
        Box(modifier = Modifier.fillMaxSize()) {
            activeWindows.sortedBy { it.zIndex }.forEach { windowState ->
                key(windowState.appType) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        WindowContainer(
                            state = windowState,
                            viewModel = viewModel
                        ) { scope ->
                            // Custom gesture area wrapper for title bar dragging only!
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background Client
                                Box(modifier = Modifier.fillMaxSize()) {
                                    when (windowState.appType) {
                                        Win11AppType.FILE_EXPLORER -> FileExplorerApp(viewModel)
                                        Win11AppType.NOTEPAD -> NotepadApp(viewModel, windowState)
                                        Win11AppType.CALCULATOR -> CalculatorApp(viewModel)
                                        Win11AppType.CMD_POWERSHELL -> CmdPowershellApp(viewModel)
                                        Win11AppType.EDGE_BROWSER -> EdgeBrowserApp(viewModel)
                                        Win11AppType.SETTINGS -> SettingsApp(viewModel)
                                        Win11AppType.SOLITAIRE -> SolitaireMiniApp()
                                        else -> SettingsApp(viewModel)
                                    }
                                }

                                // Interactive invisible Drag Bar overlaid over Title Chrome coordinates
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .pointerInput(windowState.appType) {
                                            detectDragGestures(
                                                onDragStart = { viewModel.focusWindow(windowState.appType) },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    viewModel.dragWindow(
                                                        windowState.appType,
                                                        dragAmount.x / density,
                                                        dragAmount.y / density
                                                    )
                                                }
                                            )
                                        }
                                )

                                // Drag Resize Handle inside Window client at bottom right edge
                                if (!windowState.isMaximized) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp)
                                            .pointerInput(windowState.appType) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    viewModel.resizeWindow(
                                                        windowState.appType,
                                                        dragAmount.x / density,
                                                        dragAmount.y / density
                                                    )
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // OVERLAY COMPONENTS (Start Menu, Widgets board, Quick Tray configs)
        WidgetsBoardOverlay(viewModel)
        StartMenuPopover(viewModel)
        ControlCenterPopover(viewModel)
        CalendarPopover(viewModel)

        // MAIN SYSTEM TASKBAR (Stay at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(48.dp)
        ) {
            TaskbarMain(viewModel = viewModel)
        }

        // FULLSCREEN RESTART / UPDATE SIMULATION OVERLAYS
        SystemShuttingDownOverlay(viewModel = viewModel)
    }
}


// ============================================
// DESKTOP INTERACTIVE ICONS GRID
// ============================================
@Composable
fun DesktopIconsSidebar(viewModel: Win11ViewModel) {
    val shortcuts = listOf(
        DesktopIcon("This PC", Win11AppType.SYS_INFO, "pc"),
        DesktopIcon("Recycle Bin", Win11AppType.FILE_EXPLORER, "trash"),
        DesktopIcon("Edge Browser", Win11AppType.EDGE_BROWSER, "edge"),
        DesktopIcon("File Explorer", Win11AppType.FILE_EXPLORER, "folder"),
        DesktopIcon("PowerShell", Win11AppType.CMD_POWERSHELL, "cmd"),
        DesktopIcon("Text Notepad", Win11AppType.NOTEPAD, "text"),
        DesktopIcon("Calculator", Win11AppType.CALCULATOR, "calc"),
        DesktopIcon("Classic Solitaire", Win11AppType.SOLITAIRE, "solitaire")
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(85.dp)
            .padding(top = 16.dp, start = 8.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        shortcuts.forEach { shortcut ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        if (shortcut.title == "Recycle Bin") {
                            // Target C:\ Recycle Bin folder directory path
                            viewModel.openApp(Win11AppType.FILE_EXPLORER, "C:\\Recycle Bin")
                        } else {
                            viewModel.openApp(shortcut.appType)
                        }
                    }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vector icon resolution
                val (vector, color) = getDesktopIconTraits(shortcut.iconType)
                Icon(
                    imageVector = vector,
                    contentDescription = shortcut.title,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = shortcut.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 11.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 75.dp)
                )
            }
        }
    }
}

fun getDesktopIconTraits(type: String): Pair<ImageVector, Color> {
    return when (type) {
        "pc" -> Pair(Icons.Default.Lock, Color(0xFF60A5FA))
        "trash" -> Pair(Icons.Default.Delete, Color(0xFF94A3B8))
        "edge" -> Pair(Icons.Default.LocationOn, Color(0xFF3B82F6))
        "cmd" -> Pair(Icons.Default.PlayArrow, Color(0xFF10B981))
        "text" -> Pair(Icons.Default.Edit, Color(0xFF38BDF8))
        "calc" -> Pair(Icons.Default.Menu, Color(0xFF818CF8))
        "solitaire" -> Pair(Icons.Default.Star, Color(0xFFFBBF24))
        else -> Pair(Icons.Default.List, Color(0xFFFBBF24))
    }
}


// ============================================
// SOLID FLUENT NAVIGATION TASKBAR (Center-docked)
// ============================================
@Composable
fun TaskbarMain(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val activeWindows by viewModel.activeWindows.collectAsState()
    val focusedAppId by viewModel.focusedWindowId.collectAsState()

    val sysTime by viewModel.systemTime.collectAsState()
    val sysDate by viewModel.systemDate.collectAsState()

    val barBg = if (isDark) Color(0x990F172A) else Color(0x99FFFFFF) // Sleek immersive transparent look
    val labelTextCol = if (isDark) Color.White else Color.Black
    val taskbarShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .shadow(12.dp, shape = taskbarShape)
            .background(barBg, shape = taskbarShape)
            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.45f), taskbarShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Live Widgets Launch Panel button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { viewModel.toggleWidgetsBoard() }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = "Weather Widget", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text("London", color = labelTextCol, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                Text("72° Sunny", color = Color.Gray, fontSize = 7.sp)
            }
        }

        // CENTER: Taskbar launcher items
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            // Taskbar Start Icon button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewModel.toggleStartMenu() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Windows 11 iconic 4 grid squares vector icon
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Start Button",
                    tint = Color(0xFF0EA5E9),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Quick Pinned icons with open dot indicators
            val taskBarApps = listOf(
                Pair(Win11AppType.FILE_EXPLORER, "folder"),
                Pair(Win11AppType.EDGE_BROWSER, "edge"),
                Pair(Win11AppType.NOTEPAD, "text"),
                Pair(Win11AppType.CALCULATOR, "calc"),
                Pair(Win11AppType.CMD_POWERSHELL, "cmd"),
                Pair(Win11AppType.SETTINGS, "settings")
            )

            taskBarApps.forEach { (appType, iconType) ->
                val isOpen = activeWindows.any { it.appType == appType }
                val isActive = focusedAppId == appType

                Column(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { viewModel.openApp(appType) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val (vector, color) = getDesktopIconTraits(iconType)
                    Icon(
                        imageVector = vector,
                        contentDescription = appType.name,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))

                    // Open dot indicator line
                    if (isOpen) {
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 12.dp else 4.dp, 2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isActive) Color(0xFF3B82F6) else Color.Gray)
                        )
                    }
                }
            }
        }

        // RIGHT: System tray notifications and Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            // Volume Signal Tray quick-clicks
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewModel.toggleControlCenter() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "WiFi Status", tint = labelTextCol, modifier = Modifier.size(13.dp))
                Icon(Icons.Default.PlayArrow, contentDescription = "Audio Level", tint = labelTextCol, modifier = Modifier.size(13.dp))
                Icon(Icons.Default.Info, contentDescription = "Battery charge", tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
            }

            // Simple vertical divider
            Box(modifier = Modifier.size(1.dp, 16.dp).background(Color.Gray.copy(alpha = 0.5f)))

            // Real Time Clock layout
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewModel.toggleCalendar() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = sysTime.ifEmpty { "12:00 PM" },
                    color = labelTextCol,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sysDate.ifEmpty { "6/16/2026" },
                    color = Color.Gray,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


// ============================================
// CENTER ARYLIC START MENU POPUP
// ============================================
@Composable
fun BoxScope.StartMenuPopover(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isOpen by viewModel.startMenuOpen.collectAsState()
    val isPowerMenuOpen by viewModel.powerMenuOpen.collectAsState()

    val popoverBg = if (isDark) Color(0xE60F172A) else Color(0xE6FFFFFF) // Slate mesh compatible translucent background
    val cardBg = if (isDark) Color(0x33FFFFFF) else Color(0x0D000000) // Glass card backgrounds
    val textCol = if (isDark) Color.White else Color.Black
    val startMenuShape = RoundedCornerShape(24.dp)

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 74.dp, start = 12.dp, end = 12.dp) // Align beautifully above floating taskbar
            .align(Alignment.BottomCenter)
            .widthIn(max = 350.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.45f), startMenuShape),
            colors = CardDefaults.cardColors(containerColor = popoverBg),
            shape = startMenuShape,
            elevation = CardDefaults.cardElevation(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Input bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search apps, settings...", fontSize = 11.sp, color = if (isDark) Color.LightGray else Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(22.dp), // Modern pill shape
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pinned Apps text header
                Text(
                    text = "Pinned Applications",
                    color = textCol,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Apps Launcher Grid
                val startupApps = listOf(
                    Pair("Edge", Win11AppType.EDGE_BROWSER),
                    Pair("Explorer", Win11AppType.FILE_EXPLORER),
                    Pair("Notepad", Win11AppType.NOTEPAD),
                    Pair("Calculator", Win11AppType.CALCULATOR),
                    Pair("CMD", Win11AppType.CMD_POWERSHELL),
                    Pair("Solitaire", Win11AppType.SOLITAIRE),
                    Pair("Settings", Win11AppType.SETTINGS),
                    Pair("Specs", Win11AppType.SYS_INFO)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(startupApps) { (name, type) ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.openApp(type) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val icon = getAppIcon(type)
                            val color = when (type) {
                                Win11AppType.FILE_EXPLORER -> Color(0xFFFBBF24)
                                Win11AppType.CMD_POWERSHELL -> Color(0xFF10B981)
                                Win11AppType.SOLITAIRE -> Color(0xFFEF4444)
                                Win11AppType.CALCULATOR -> Color(0xFF818CF8)
                                else -> Color(0xFF3B82F6)
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                name,
                                color = textCol,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Recommended / Recent files section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recommended Docs", color = textCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("C:\\Files", color = Color.Gray, fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Suggested files cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Pair("Welcome to Win11", "C:\\Users\\User\\Desktop\\Welcome to Windows 11.txt"),
                        Pair("Specs Report", "C:\\Users\\User\\Documents\\Specs.txt")
                    ).forEach { (label, fullPath) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.openApp(Win11AppType.NOTEPAD, customData = fullPath) }
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Txt Doc", tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    label,
                                    color = textCol,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Start Menu Profile Info and Power Toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0a000000))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("U", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registered User", color = textCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Power Option triggers
                    Box {
                        IconButton(onClick = { viewModel.togglePowerMenu() }) {
                            Icon(Icons.Default.Lock, contentDescription = "Power Mode", tint = textCol, modifier = Modifier.size(16.dp))
                        }

                        // Dropdown absolute overlay for Restart / Shutdown options
                        DropdownMenu(
                            expanded = isPowerMenuOpen,
                            onDismissRequest = { viewModel.togglePowerMenu() },
                            modifier = Modifier.background(popoverBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Restart Simulator", color = textCol, fontSize = 11.sp) },
                                onClick = { viewModel.setShutdownState("restarting") }
                            )
                            DropdownMenuItem(
                                text = { Text("Shut Down", color = textCol, fontSize = 11.sp) },
                                onClick = { viewModel.setShutdownState("shutting_down") }
                            )
                        }
                    }
                }
            }
        }
    }
}
