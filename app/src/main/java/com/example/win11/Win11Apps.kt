package com.example.win11

import java.util.Locale
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Common Windows 11 App Card wrapper container that models Window chromes: Minimize, Maximize, Close
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowContainer(
    state: WindowState,
    viewModel: Win11ViewModel,
    content: @Composable (BoxWithConstraintsScope) -> Unit
) {
    if (state.isMinimized) return

    val isDark by viewModel.isDarkTheme.collectAsState()
    val isFocused = viewModel.focusedWindowId.collectAsState().value == state.appType

    // Adjust container style depending on theme and active state
    val backgroundColor = if (isDark) Color(0xEC1E1E1E) else Color(0xECFFFFFF)
    val borderColor = if (isFocused) {
        if (isDark) Color(0xFF4B5563) else Color(0xFF3B82F6)
    } else {
        if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB)
    }

    val shadowElevation = if (isFocused) 18.dp else 4.dp
    val windowShape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .offset(
                x = if (state.isMaximized) 0.dp else state.x.dp,
                y = if (state.isMaximized) 0.dp else state.y.dp
            )
            .size(
                width = if (state.isMaximized) {
                    val context = androidx.compose.ui.platform.LocalConfiguration.current
                    context.screenWidthDp.dp
                } else state.width.dp,
                height = if (state.isMaximized) {
                    val context = androidx.compose.ui.platform.LocalConfiguration.current
                    // Reserve some bottom space for taskbar
                    (context.screenHeightDp - 48).dp
                } else state.height.dp
            )
            .shadow(shadowElevation, shape = windowShape)
            .clip(windowShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, windowShape)
            .combinedClickable(
                onClick = { viewModel.focusWindow(state.appType) }
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Task Window Chrome Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(if (isDark) Color(0xFF181818) else Color(0xFFF3F4F6))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Title/Icon segment with simple drag capability simulation
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = getAppIcon(state.appType)
                    Icon(
                        imageVector = icon,
                        contentDescription = state.title,
                        tint = if (state.appType == Win11AppType.CMD_POWERSHELL) Color(0xFF10B981) else Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.title,
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Window Controls Custom Taps (Min, Max, Close)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize
                    IconButton(
                        onClick = { viewModel.minimizeApp(state.appType) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp, 1.dp)
                                .background(if (isDark) Color.White else Color.Black)
                        )
                    }
                    // Maximize
                    IconButton(
                        onClick = { viewModel.toggleMaximize(state.appType) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .border(1.dp, if (isDark) Color.White else Color.Black)
                        )
                    }
                    // Close Action
                    IconButton(
                        onClick = { viewModel.closeApp(state.appType) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (isDark) Color.White else Color.Black
                        ),
                        modifier = Modifier
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Client Content Workspace Area
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                content(this)
            }
        }

        // Resize node corner handler at bottom right
        if (!state.isMaximized) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(15.dp)
                    .clip(RoundedCornerShape(topStart = 15.dp))
                    .clickable { /* Drag to Resize logic bound on parent gestures in Desktop drawer */ }
            )
        }
    }
}

// Helper to resolve Icons
fun getAppIcon(type: Win11AppType): ImageVector {
    return when (type) {
        Win11AppType.FILE_EXPLORER -> Icons.Default.List
        Win11AppType.NOTEPAD -> Icons.Default.Edit
        Win11AppType.CALCULATOR -> Icons.Default.Menu
        Win11AppType.CMD_POWERSHELL -> Icons.Default.PlayArrow
        Win11AppType.EDGE_BROWSER -> Icons.Default.LocationOn
        Win11AppType.SETTINGS -> Icons.Default.Settings
        Win11AppType.WIDGETS_BOARD -> Icons.Default.Home
        Win11AppType.SOLITAIRE -> Icons.Default.Star
        Win11AppType.SYS_INFO -> Icons.Default.Info
    }
}

// ============================================
// 1. FILE EXPLORER APP
// ============================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExplorerApp(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val files by viewModel.virtualFiles.collectAsState()
    
    var currentDirPath by remember { mutableStateOf("C:\\Users\\User") }
    var explorerQuery by remember { mutableStateOf("") }
    
    // Dialog inputs
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }

    val activePathFiles = files.filter {
        val parent = it.path.substringBeforeLast("\\")
        val isDirectChild = parent == currentDirPath && it.path != parent
        // C: edge case
        val isCDriveRootMatch = currentDirPath == "C:" && !it.path.contains("\\") && it.path != "C:"
        isDirectChild || isCDriveRootMatch
    }

    val textCol = if (isDark) Color.White else Color.Black
    val bgCol = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF9FAFB)

    Row(modifier = Modifier.fillMaxSize().background(bgCol)) {
        // Left Sidebar Navigation Quick Access
        Column(
            modifier = Modifier
                .width(105.dp)
                .fillMaxHeight()
                .background(if (isDark) Color(0xFF151515) else Color(0xFFECEFF1))
                .padding(vertical = 8.dp)
        ) {
            Text(
                "Quick Access",
                color = if (isDark) Color.Gray else Color.DarkGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            val sidebarDestinations = listOf(
                Pair("This PC", "C:"),
                Pair("Desktop", "C:\\Users\\User\\Desktop"),
                Pair("Documents", "C:\\Users\\User\\Documents"),
                Pair("Downloads", "C:\\Users\\User\\Downloads")
            )

            sidebarDestinations.forEach { (label, targetPath) ->
                val isSelected = currentDirPath == targetPath
                val itemBg = if (isSelected) {
                    if (isDark) Color(0xFF2E2E2E) else Color(0xFFCFD8DC)
                } else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(itemBg)
                        .clickable { currentDirPath = targetPath }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (label) {
                            "This PC" -> Icons.Default.Lock
                            "Desktop" -> Icons.Default.Home
                            "Documents" -> Icons.Default.Email
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = label,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = textCol,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Divider
        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFD1D5DB)))

        // Main File grid
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Explorer Address bar & Toolbar Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF1C131E).copy(alpha = 0.2f) else Color(0xFFF3F4F6))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = {
                        if (currentDirPath != "C:") {
                            val parent = currentDirPath.substringBeforeLast("\\")
                            currentDirPath = if (parent.isEmpty()) "C:" else parent
                        }
                    },
                    modifier = Modifier.size(24.dp),
                    enabled = currentDirPath != "C:"
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Back Folder",
                        modifier = Modifier.size(16.dp),
                        tint = if (currentDirPath == "C:") Color.Gray else Color(0xFF3B82F6)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Path Breadcrumb box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDark) Color(0xFF282828) else Color.White)
                        .border(1.dp, if (isDark) Color(0xFF3F3F3F) else Color(0xFFD1D5DB), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        currentDirPath,
                        color = textCol,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Toolbar utility options
                Row {
                    IconButton(
                        onClick = { showNewFolderDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "New Folder", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = "New Text File", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Divider
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDark) Color(0xFF2E2E2E) else Color(0xFFD1D5DB)))

            // Files Listing Area
            if (activePathFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty Folder",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This directory is empty.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activePathFiles) { file ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (file.isDirectory) {
                                            currentDirPath = file.path
                                        } else {
                                            // Handle file read and push to Notepad editor App
                                            viewModel.openApp(Win11AppType.NOTEPAD, customData = file.path)
                                        }
                                    }
                                )
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (file.isDirectory) {
                                    if (file.iconType == "drive") Icons.Default.Lock else Icons.Default.List
                                } else {
                                    Icons.Default.Edit
                                },
                                contentDescription = file.name,
                                tint = if (file.isDirectory) Color(0xFFFBBF24) else Color(0xFF60A5FA),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = file.name,
                                color = textCol,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.heightIn(max = 28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal creation dialog overlays
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Directory", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter name for a new directory folder:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        singleLine = true,
                        placeholder = { Text("New Folder", fontSize = 11.sp) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val name = folderNameInput.trim().ifEmpty { "New Folder" }
                    viewModel.addVirtualFile(name, currentDirPath, "", isFolder = true)
                    folderNameInput = ""
                    showNewFolderDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New Text Document", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter name for the new document file:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        singleLine = true,
                        placeholder = { Text("Document.txt", fontSize = 11.sp) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    var name = fileNameInput.trim().ifEmpty { "NewNote.txt" }
                    if (!name.endsWith(".txt")) name = "$name.txt"
                    viewModel.addVirtualFile(name, currentDirPath, "Empty note doc.", isFolder = false)
                    fileNameInput = ""
                    showNewFileDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }
}


// ============================================
// 2. NOTEPAD WRITER APP
// ============================================
@Composable
fun NotepadApp(viewModel: Win11ViewModel, windowState: WindowState) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val files by viewModel.virtualFiles.collectAsState()

    var activeFilePath by remember { mutableStateOf("") }
    var activeFileName by remember { mutableStateOf("Untitled.txt") }
    var documentText by remember { mutableStateOf("") }

    // Synchronize to see if a file path was passed when window opened
    LaunchedEffect(windowState.customData) {
        if (windowState.customData.isNotEmpty()) {
            val fileObj = files.find { it.path == windowState.customData }
            if (fileObj != null) {
                activeFilePath = fileObj.path
                activeFileName = fileObj.name
                documentText = fileObj.content
            }
        }
    }

    var showSavePrompt by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf("Note.txt") }

    val bgCol = if (isDark) Color(0xFF272727) else Color.White
    val textCol = if (isDark) Color.White else Color.Black

    Column(modifier = Modifier.fillMaxSize().background(bgCol)) {
        // Notepad Menu Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFECEFF1))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            TextButton(
                onClick = {
                    activeFilePath = ""
                    activeFileName = "Untitled.txt"
                    documentText = ""
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("New", color = textCol, fontSize = 11.sp)
            }

            TextButton(
                onClick = { showSavePrompt = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Save", color = textCol, fontSize = 11.sp)
            }

            TextButton(
                onClick = {
                    if (activeFilePath.isNotEmpty()) {
                        viewModel.updateVirtualFileContent(activeFilePath, documentText)
                    } else {
                        showSavePrompt = true
                    }
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Apply Changes", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Active workspace status header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF151515) else Color(0xFFE0E0E0))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("File: $activeFileName", color = Color.Gray, fontSize = 9.sp)
            Text(if (activeFilePath.isEmpty()) "*Unsaved Sandbox" else "Disk synced", color = Color.Gray, fontSize = 9.sp)
        }

        // Writing Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp)
        ) {
            BasicTextField(
                value = documentText,
                onValueChange = { documentText = it },
                textStyle = TextStyle(
                    color = textCol,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showSavePrompt) {
        AlertDialog(
            onDismissRequest = { showSavePrompt = false },
            title = { Text("Save Document to Desktop", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This node will write directly to C:\\Users\\User\\Desktop", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveNameInput,
                        onValueChange = { saveNameInput = it },
                        singleLine = true,
                        placeholder = { Text("Doc.txt", fontSize = 11.sp) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    var saveName = saveNameInput.trim().ifEmpty { "MyNewNote.txt" }
                    if (!saveName.endsWith(".txt")) saveName = "$saveName.txt"
                    val desktopPath = "C:\\Users\\User\\Desktop"
                    
                    viewModel.addVirtualFile(saveName, desktopPath, documentText, isFolder = false)
                    activeFilePath = "$desktopPath\\$saveName"
                    activeFileName = saveName
                    showSavePrompt = false
                }) {
                    Text("Save to Disk")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePrompt = false }) { Text("Cancel") }
            }
        )
    }
}


// ============================================
// 3. THE CALCULATOR APP
// ============================================
@Composable
fun CalculatorApp(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }

    val bgCol = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val textCol = if (isDark) Color.White else Color.Black
    val displayBgCol = if (isDark) Color(0xFF121212) else Color.White

    val buttons = listOf(
        listOf("CE", "C", "Del", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("+/-", "0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCol)
            .padding(8.dp)
    ) {
        // Output screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(displayBgCol)
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression.ifEmpty { " " },
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resultText,
                color = textCol,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Keypad grid
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { char ->
                        val isOperator = char in listOf("/", "*", "-", "+", "=")
                        val btnColor = if (char == "=") {
                            Color(0xFF3B82F6) // Accent color
                        } else if (isOperator) {
                            if (isDark) Color(0xFF2A2A2A) else Color(0xFFE5E7EB)
                        } else {
                            if (isDark) Color(0xFF242424) else Color(0xFFF9FAFB)
                        }

                        val fontCol = if (char == "=") Color.White else textCol

                        Button(
                            onClick = {
                                when (char) {
                                    "C" -> {
                                        expression = ""
                                        resultText = "0"
                                    }
                                    "CE" -> {
                                        resultText = "0"
                                    }
                                    "Del" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                        }
                                    }
                                    "=" -> {
                                        try {
                                             if (expression.isNotEmpty()) {
                                                 val solved = evalCustomMathExpression(expression)
                                                 resultText = solved
                                                 expression = solved
                                             }
                                        } catch (e: Exception) {
                                            resultText = "Error"
                                        }
                                    }
                                    "+/-" -> {
                                        if (resultText.startsWith("-")) {
                                            resultText = resultText.substring(1)
                                        } else if (resultText != "0") {
                                            resultText = "-$resultText"
                                        }
                                        expression = resultText
                                    }
                                    else -> {
                                        if (expression.length < 18) {
                                            expression += char
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = btnColor,
                                contentColor = fontCol
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .padding(0.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(char, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Simple arithmetic parser for the calculator simulator
fun evalCustomMathExpression(expr: String): String {
    // Basic solver matching sequences of numbers separated by +, -, *, /
    val additionAndSubstr = expr.split(Regex("(?=[+\\-])|(?<=[+\\-])"))
    var currentTermSolved = 0.0
    var solvedSum = 0.0
    var lastSign = 1 // 1 for +, -1 for -
    
    // Evaluate divisions and multiplications first
    var idx = 0
    while (idx < additionAndSubstr.size) {
        val segment = additionAndSubstr[idx]
        if (segment == "+") {
            lastSign = 1
            idx++
            continue
        } else if (segment == "-") {
            lastSign = -1
            idx++
            continue
        }
        
        // This segment is a potential Multiplication/Division term
        val multDivTerms = segment.split(Regex("(?=[*/])|(?<=[*/])"))
        var factor = multDivTerms[0].toDoubleOrNull() ?: 0.0
        var factorIdx = 1
        while (factorIdx < multDivTerms.size) {
            val op = multDivTerms[factorIdx]
            val nextVal = multDivTerms.getOrNull(factorIdx + 1)?.toDoubleOrNull() ?: 1.0
            if (op == "*") {
                factor *= nextVal
            } else if (op == "/") {
                if (nextVal != 0.0) factor /= nextVal
            }
            factorIdx += 2
        }
        
        solvedSum += (lastSign * factor)
        idx++
    }
    
    // Nice output format
    return if (solvedSum % 1.0 == 0.0) {
        solvedSum.toInt().toString()
    } else {
        String.format(Locale.US, "%.4f", solvedSum).trimEnd('0').trimEnd('.')
    }
}


// ============================================
// 4. THE COMMAND PROMPT / POWERSHELL APP
// ============================================
@Composable
fun CmdPowershellApp(viewModel: Win11ViewModel) {
    val history by viewModel.terminalHistory.collectAsState()
    val rawDirPath by viewModel.terminalCurrentDir.collectAsState()

    var consoleInput by remember { mutableStateOf("") }
    val consoleFocus = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C)) // Dark terminal background
            .padding(6.dp)
    ) {
        // Output Lazy Scroll space
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(history) { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("Error:") || line.startsWith("Command error:")) {
                        Color(0xFFEF4444)
                    } else if (line.contains(">")) {
                        Color(0xFF38BDF8) // highlight prompt paths
                    } else {
                        Color(0xFFE2E8F0)
                    },
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp
                )
            }
        }

        // Active prompt line
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rawDirPath> ",
                color = Color(0xFF34D399), // Neon mint green prompt
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Input TextField
            BasicTextField(
                value = consoleInput,
                onValueChange = { consoleInput = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.executeShellCommand(consoleInput)
                        consoleInput = ""
                    }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// ============================================
// 5. MICROSOFT EDGE BROWSER APP
// ============================================
@Composable
fun EdgeBrowserApp(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()

    var browserUrl by remember { mutableStateOf("google.com") }
    var searchField by remember { mutableStateOf("google.com") }
    
    // Custom simulated Google Search query state
    var googleQuery by remember { mutableStateOf("") }
    var showGoogleResultsFor by remember { mutableStateOf("") }

    val bgCol = if (isDark) Color(0xFF212529) else Color.White
    val textCol = if (isDark) Color.White else Color.Black

    Column(modifier = Modifier.fillMaxSize().background(bgCol)) {
        // Toolbar with URL address controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF1B1F23) else Color(0xFFECEFF1))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = Color(0xFF3B82F6),
                modifier = Modifier
                    .clickable { 
                        googleQuery = ""
                        showGoogleResultsFor = ""
                    }
                    .size(18.dp)
                    .padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // URL Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDark) Color(0xFF2A2E35) else Color.White)
                    .border(1.dp, if (isDark) Color(0xFF3E434F) else Color(0xFFCFD8DC), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                BasicTextField(
                    value = searchField,
                    onValueChange = { searchField = it },
                    singleLine = true,
                    textStyle = TextStyle(color = textCol, fontSize = 11.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { 
                            browserUrl = searchField.trim().lowercase()
                            showGoogleResultsFor = ""
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Horizontal Loading Line simulation
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF3B82F6)))

        // Simulated Web content body based on current Address bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            when {
                browserUrl.contains("google.com") -> {
                    SimulatedGooglePage(
                        isDark = isDark,
                        query = googleQuery,
                        onQueryChange = { googleQuery = it },
                        resultsFor = showGoogleResultsFor,
                        onPerformSearch = { showGoogleResultsFor = it }
                    )
                }
                browserUrl.contains("wikipedia.org") -> {
                    SimulatedWikipediaPage(isDark = isDark)
                }
                browserUrl.contains("microsoft.com") -> {
                    SimulatedMicrosoftPage(isDark = isDark, viewModel = viewModel)
                }
                else -> {
                    // Fallback search suggestions
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "404", tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("This site can't be reached", color = textCol, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Search terms on simulated Edge are google.com, wikipedia.org, and microsoft.com", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { 
                            browserUrl = "google.com"
                            searchField = "google.com"
                        }) {
                            Text("Go to Google Engine")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedGooglePage(
    isDark: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    resultsFor: String,
    onPerformSearch: (String) -> Unit
) {
    if (resultsFor.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simulated Logo
            Text(
                "G o o g l e",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF3B82F6) // classic blue
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                placeholder = { Text("Search virtual web or ask...", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onPerformSearch(query) }),
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { onPerformSearch(query) }) {
                Text("Google Search", fontSize = 11.sp)
            }
        }
    } else {
        // Results rendering
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Search results for '$resultsFor':", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            val mockResults = when {
                resultsFor.lowercase().contains("windows") -> listOf(
                     Pair("Windows 11 OS Specs - Microsoft Official", "Get to know the latest build versions, performance limits, and Fluent design styles of simulated Windows 11 context."),
                     Pair("How to Code simulated environment in Android", "Using Jetpack Compose we can draw custom canvases and layered floating dialogs to model desktop multitasking effortlessly.")
                )
                else -> listOf(
                    Pair("Android Compose Visual Simulator", "The definitive wiki page details for crafting pixel art operating system mockups using nested layout models in Kotlin."),
                    Pair("Did you know?", "This simulation workspace represents a fully operational Windows 11 shell complete with internal file system, notepad edits, and quick action bars.")
                )
            }

            items(mockResults) { (title, snippet) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF2C2F36) else Color(0xFFF3F4F6)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(title, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(snippet, color = if (isDark) Color.White else Color.Black, fontSize = 10.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { onPerformSearch("") }) {
                    Text("Clear search")
                }
            }
        }
    }
}

@Composable
fun SimulatedWikipediaPage(isDark: Boolean) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Windows 11", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else Color.Black)
            Text("From Wikipedia, the free virtual encyclopedia", color = Color.Gray, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Windows 11 is the major release of the Windows NT operating system developed by Microsoft. Announced on June 24, 2021, and released to the public on October 5, 2021, it is the successor to Windows 10, released six years earlier.",
                fontSize = 11.sp,
                color = if (isDark) Color.White else Color.Black,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "FEATURES & USER INTERFACE:",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF3B82F6)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Windows 11 features a major redesign of the user interface, incorporating the Fluent Design System. This includes rounded window corners, modern transparency effects, centered taskbar icons (mimicking simulated macOS traits), and an updated Start Menu with pinned apps.",
                fontSize = 11.sp,
                color = if (isDark) Color.White else Color.Black,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun SimulatedMicrosoftPage(isDark: Boolean, viewModel: Win11ViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Microsoft Web Store", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
        Text("Hot-install visual wallpaper themes immediately!", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(10.dp))

        viewModel.wallpaperPresets.forEachIndexed { idx, preset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDark) Color(0xFF2C2F36) else Color(0xFFECEFF1))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(android.graphics.Color.parseColor(preset.previewColorHex)))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(preset.name, color = if (isDark) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.setWallpaperIndex(idx) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Apply Preset", fontSize = 10.sp)
                }
            }
        }
    }
}


// ============================================
// 6. CLASSIC SOLITAIRE MINI GAME
// ============================================
@Composable
fun SolitaireMiniApp() {
    // A simplified layout displaying randomized solitaire stack and allowing cards to shuffle
    var score by remember { mutableStateOf(120) }
    var moves by remember { mutableStateOf(8) }
    
    // Simulate active columns
    var deckRefreshes by remember { mutableStateOf(0) }
    
    val columnCards = remember(deckRefreshes) {
        listOf(
            listOf("K♠", "Q♦", "J♣"),
            listOf("10♥", "9♠"),
            listOf("A♠", "2♠", "3♠"),
            listOf("7♦", "6♣", "5♥")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF065F46)) // Classic dark green felt solitaire background
            .padding(8.dp)
    ) {
        // Headers stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x27000000))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Score: $score", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Moves Count: $moves", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Draw Stack deck
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Unopened Stack Cards
            Box(
                modifier = Modifier
                    .size(52.dp, 72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E3A8A)) // Blue back color
                    .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                    .clickable {
                        moves++
                        score += 15
                        deckRefreshes++
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Deck", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Done stack mockers
                listOf("♦", "♠", "♥", "♣").forEach { suit ->
                    Box(
                        modifier = Modifier
                            .size(36.dp, 52.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(suit, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Layout stacks grid (Cards table)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            columnCards.forEach { stack ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    stack.forEachIndexed { index, card ->
                        val isRed = card.contains("♥") || card.contains("♦")
                        
                        Box(
                            modifier = Modifier
                                .offset(y = (-index * 26).dp) // nested stack look overlaying cards
                                .size(44.dp, 60.dp)
                                .shadow(2.dp, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .clickable {
                                    moves++
                                    score -= 5
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = card,
                                color = if (isRed) Color.Red else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ============================================
// 7. COMPLEX SETTINGS APP
// ============================================
@Composable
fun SettingsApp(viewModel: Win11ViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isAutohide by viewModel.isTaskbarAutohide.collectAsState()
    val currentWallpaperIdx by viewModel.currentWallpaperIndex.collectAsState()

    var activeSettingTab by remember { mutableStateOf("system") } // "system", "personalization", "about"

    val bgCol = if (isDark) Color(0xFF1F1F1F) else Color(0xFFF3F4F6)
    val textCol = if (isDark) Color.White else Color.Black
    val rightPanelBg = if (isDark) Color(0xFF272727) else Color.White

    Row(modifier = Modifier.fillMaxSize().background(bgCol)) {
        // Navigation side rail
        Column(
            modifier = Modifier
                .width(105.dp)
                .fillMaxHeight()
                .background(if (isDark) Color(0xFF151515) else Color(0xFFECEFF1))
                .padding(vertical = 8.dp)
        ) {
            listOf(
                Pair("System Specs", "system"),
                Pair("Personalization", "personalization"),
                Pair("About Windows", "about")
            ).forEach { (label, route) ->
                val isSelected = activeSettingTab == route
                val tabBg = if (isSelected) {
                    if (isDark) Color(0xFF2E2E2E) else Color(0xFFCFD8DC)
                } else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tabBg)
                        .clickable { activeSettingTab = route }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = when (route) {
                            "system" -> Icons.Default.Info
                            "personalization" -> Icons.Default.Settings
                            else -> Icons.Default.Home
                        },
                        contentDescription = label,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label, color = textCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Settings Workspace Panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(rightPanelBg)
                .padding(12.dp)
        ) {
            when (activeSettingTab) {
                "system" -> {
                    Text("System Hardware Monitor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textCol)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Simulated processor stats:", color = Color.Gray, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("CPU Core Utilization", color = textCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val cpuVal by viewModel.cpuUsage.collectAsState()
                            LinearProgressIndicator(
                                progress = { cpuVal / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = Color(0xFF3B82F6),
                                trackColor = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$cpuVal% active clock rate", color = Color.Gray, fontSize = 9.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("RAM Memory Load", color = textCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val ramVal by viewModel.ramUsage.collectAsState()
                            LinearProgressIndicator(
                                progress = { ramVal / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = Color(0xFF10B981),
                                trackColor = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$ramVal% memory in use (of 16GB total)", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
                "personalization" -> {
                    Text("Desktop Personalization", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textCol)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark Mode Theme Color", color = textCol, fontSize = 10.sp)
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkTheme() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-hide Desktop Taskbar", color = textCol, fontSize = 10.sp)
                        Switch(
                            checked = isAutohide,
                            onCheckedChange = { viewModel.toggleTaskbarAutohide() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Choose Desktop Background:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textCol)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(viewModel.wallpaperPresets.size) { idx ->
                            val preset = viewModel.wallpaperPresets[idx]
                            val isSelected = currentWallpaperIdx == idx
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(android.graphics.Color.parseColor(preset.previewColorHex)))
                                    .border(
                                        2.dp,
                                        if (isSelected) Color(0xFF3B82F6) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { viewModel.setWallpaperIndex(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(preset.name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                "about" -> {
                    Text("About Windows 11 Sandbox", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textCol)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This interactive Windows 11 Workspace simulation represents a high-fidelity Fluent Design overlay built natively on top of the Jetpack Compose Android layout system.",
                        color = textCol,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Developer: Google AI Studio Sandbox", fontSize = 10.sp, color = Color.Gray)
                    Text("• Runtime VM: Kotlin Coroutines System", fontSize = 10.sp, color = Color.Gray)
                    Text("• Build Code: 22621.1702_Release", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
