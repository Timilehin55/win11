package com.example.win11

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Win11ViewModel : ViewModel() {

    // Time & Date State Flows
    private val _systemTime = MutableStateFlow("")
    val systemTime = _systemTime.asStateFlow()

    private val _systemDate = MutableStateFlow("")
    val systemDate = _systemDate.asStateFlow()

    // Workspace & UI States
    private val _startMenuOpen = MutableStateFlow(false)
    val startMenuOpen = _startMenuOpen.asStateFlow()

    private val _controlCenterOpen = MutableStateFlow(false)
    val controlCenterOpen = _controlCenterOpen.asStateFlow()

    private val _widgetsBoardOpen = MutableStateFlow(false)
    val widgetsBoardOpen = _widgetsBoardOpen.asStateFlow()

    private val _calendarOpen = MutableStateFlow(false)
    val calendarOpen = _calendarOpen.asStateFlow()

    private val _powerMenuOpen = MutableStateFlow(false)
    val powerMenuOpen = _powerMenuOpen.asStateFlow()

    private val _shutdownState = MutableStateFlow<String?>(null) // "shutting_down", "restarting"
    val shutdownState = _shutdownState.asStateFlow()

    // System Settings & Swipes
    private val _volumeSlider = MutableStateFlow(80)
    val volumeSlider = _volumeSlider.asStateFlow()

    private val _isSystemMuted = MutableStateFlow(false)
    val isSystemMuted = _isSystemMuted.asStateFlow()

    private val _brightnessSlider = MutableStateFlow(90)
    val brightnessSlider = _brightnessSlider.asStateFlow()

    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    val isBluetoothEnabled = _isBluetoothEnabled.asStateFlow()

    private val _isAirplaneModeEnabled = MutableStateFlow(false)
    val isAirplaneModeEnabled = _isAirplaneModeEnabled.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Start with dark modern win11 look
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _isTaskbarAutohide = MutableStateFlow(false)
    val isTaskbarAutohide = _isTaskbarAutohide.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Wallpaper Preset Configurations
    val wallpaperPresets = listOf(
        WallpaperPreset(
            name = "Classic Blue Bloom",
            previewColorHex = "#1E4EB8",
            isGradient = true,
            colors = listOf("#1E4EB8", "#82A1F1", "#0A2351") // Exact Immersive mesh gradient matching tailwind specs
        ),
        WallpaperPreset(
            name = "Aurora Violet",
            previewColorHex = "#8B5CF6",
            isGradient = true,
            colors = listOf("#2E1065", "#4C1D95", "#6D28D9", "#A78BFA") // Magical purples
        ),
        WallpaperPreset(
            name = "Cozy Sunset Warmth",
            previewColorHex = "#F59E0B",
            isGradient = true,
            colors = listOf("#7C2D12", "#9A3412", "#D97706", "#FBBF24") // Stunning warm gradient
        ),
        WallpaperPreset(
            name = "Cyberpunk Obsidian",
            previewColorHex = "#1D4ED8",
            isGradient = true,
            colors = listOf("#030712", "#111827", "#1F2937", "#06B6D4") // Slick neon teal and black
        )
    )

    private val _currentWallpaperIndex = MutableStateFlow(0)
    val currentWallpaperIndex = _currentWallpaperIndex.asStateFlow()

    // Virtual File System State
    private val _virtualFiles = MutableStateFlow<List<VirtualFile>>(emptyList())
    val virtualFiles = _virtualFiles.asStateFlow()

    // Multi-Window System State
    private val _activeWindows = MutableStateFlow<List<WindowState>>(emptyList())
    val activeWindows = _activeWindows.asStateFlow()

    private val _focusedWindowId = MutableStateFlow<Win11AppType?>(null)
    val focusedWindowId = _focusedWindowId.asStateFlow()

    // Command Prompt / Powershell State
    private val _terminalHistory = MutableStateFlow<List<String>>(emptyList())
    val terminalHistory = _terminalHistory.asStateFlow()

    private val _terminalCurrentDir = MutableStateFlow("C:\\Users\\User")
    val terminalCurrentDir = _terminalCurrentDir.asStateFlow()

    // System Monitor States
    private val _ramUsage = MutableStateFlow(42) // in percent
    val ramUsage = _ramUsage.asStateFlow()

    private val _cpuUsage = MutableStateFlow(18) // in percent
    val cpuUsage = _cpuUsage.asStateFlow()

    init {
        // Start live system clock
        viewModelScope.launch {
            while (true) {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val dateFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
                _systemTime.value = timeFormat.format(Date())
                _systemDate.value = dateFormat.format(Date())
                delay(1000)
            }
        }

        // Periodically refresh fake performance stats in Widgets
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _cpuUsage.value = (10..40).random()
                _ramUsage.value = (38..52).random()
            }
        }

        // Initialize Virtual File System
        initializeFiles()

        // Init CMD History with startup banners
        _terminalHistory.value = listOf(
            "Microsoft Windows [Version 10.0.22621.1702]",
            "(c) Microsoft Corporation. All rights reserved.",
            "",
            "Loading integrated PowerShell kernel...",
            "Welcome! Type 'help' to see available Windows Commands."
        )
    }

    private fun initializeFiles() {
        val initialList = mutableListOf<VirtualFile>()

        // Core system drives and structure
        initialList.add(VirtualFile("C:", "C:", isDirectory = true, iconType = "drive"))
        initialList.add(VirtualFile("Windows", "C:\\Windows", isDirectory = true, iconType = "system_folder"))
        initialList.add(VirtualFile("System32", "C:\\Windows\\System32", isDirectory = true, iconType = "system_folder"))
        initialList.add(VirtualFile("Users", "C:\\Users", isDirectory = true, iconType = "system_folder"))
        initialList.add(VirtualFile("User", "C:\\Users\\User", isDirectory = true, iconType = "system_folder"))
        
        // Custom user shell directories
        initialList.add(VirtualFile("Desktop", "C:\\Users\\User\\Desktop", isDirectory = true, iconType = "folder"))
        initialList.add(VirtualFile("Documents", "C:\\Users\\User\\Documents", isDirectory = true, iconType = "folder"))
        initialList.add(VirtualFile("Downloads", "C:\\Users\\User\\Downloads", isDirectory = true, iconType = "folder"))

        // Default Text Files
        initialList.add(
            VirtualFile(
                name = "Welcome to Windows 11.txt",
                path = "C:\\Users\\User\\Desktop\\Welcome to Windows 11.txt",
                isDirectory = false,
                content = """
                    ===========================================
                     WELCOME TO THE WINDOWS 11 SIMULATOR
                    ===========================================
                    Built entirely using Jetpack Compose & Kotlin!
                    
                    EXPERIENCE MODERN DESKTOP MULTITASKING:
                    - Draggable windows: Drag window title bars!
                    - Resizable panels: Pull bottom-right corners to resize.
                    - Full System Toggles: Center bottom tray controls volume,
                      brightness, dark theme, and WiFi.
                    - Start Menu: Center-positioned search & app drawers.
                    - Desktop Shortcuts: Double-click to invoke Edge, CMD, or 
                      Settings.
                    
                    BUILT-IN APPLICATIONS:
                    1. File Explorer: Create, open, and delete actual virtual files.
                    2. Notepad: Create rich text, open existing .txt files, and save.
                    3. Command Prompt: Interactive CLI with functional cmdlets.
                    4. Edge Browser: Realistic searching and website renders!
                    5. Solitaire: Interactive classic card layout.
                    6. Widgets Board: Live hardware counters and real-time news feedback.
                    
                    Enjoy experimenting!
                """.trimIndent(),
                iconType = "text"
            )
        )

        initialList.add(
            VirtualFile(
                name = "Specs.txt",
                path = "C:\\Users\\User\\Documents\\Specs.txt",
                isDirectory = false,
                content = """
                    SYSTEM SPECIFICATIONS:
                    -----------------------
                    OS Name: Microsoft Windows 11 Pro Android Edition
                    OS Version: 11.0.22621 (Simulated Sandbox)
                    OS Build: Co-Created by Google AI Studio
                    Processor: Qualcomm Snapdragon 8 Gen Elite Octa-Core @ 3.4GHz
                    Installed Memory: 16.0 GB (LPDDR5 HyperThreaded)
                    System Type: 64-bit Operating System, ARM-based Processor
                    User Directory: C:\Users\User\
                    Active UI Engine: Jetpack Compose v1.7 (Declarative Multi-Window)
                """.trimIndent(),
                iconType = "text"
            )
        )

        initialList.add(
            VirtualFile(
                name = "Edge_shortcuts.txt",
                path = "C:\\Users\\User\\Downloads\\Edge_shortcuts.txt",
                isDirectory = false,
                content = """
                    POPULAR SITES IN MS EDGE:
                    ------------------------
                    - google.com (Interactive Search Engine)
                    - wikipedia.org (Simulated informative articles)
                    - microsoft.com (The home of Windows 11 Fluent Dev)
                    - github.com (Repository references)
                    - local.win11.dev (Classic easter egg code console!)
                """.trimIndent(),
                iconType = "text"
            )
        )

        _virtualFiles.value = initialList
    }

    // Toggle Windows
    fun toggleStartMenu() {
        _startMenuOpen.value = !_startMenuOpen.value
        if (_startMenuOpen.value) {
            _controlCenterOpen.value = false
            _widgetsBoardOpen.value = false
            _calendarOpen.value = false
        }
    }

    fun toggleControlCenter() {
        _controlCenterOpen.value = !_controlCenterOpen.value
        if (_controlCenterOpen.value) {
            _startMenuOpen.value = false
            _widgetsBoardOpen.value = false
            _calendarOpen.value = false
        }
    }

    fun toggleWidgetsBoard() {
        _widgetsBoardOpen.value = !_widgetsBoardOpen.value
        if (_widgetsBoardOpen.value) {
            _startMenuOpen.value = false
            _controlCenterOpen.value = false
            _calendarOpen.value = false
        }
    }

    fun toggleCalendar() {
        _calendarOpen.value = !_calendarOpen.value
        if (_calendarOpen.value) {
            _startMenuOpen.value = false
            _controlCenterOpen.value = false
            _widgetsBoardOpen.value = false
        }
    }

    fun togglePowerMenu() {
        _powerMenuOpen.value = !_powerMenuOpen.value
    }

    fun setShutdownState(state: String?) {
        _shutdownState.value = state
        _powerMenuOpen.value = false
        _startMenuOpen.value = false
        if (state != null) {
            viewModelScope.launch {
                delay(3500) // Delay to let beautiful loading animation spin
                _shutdownState.value = null
                _activeWindows.value = emptyList() // Close all apps on reboot/boot
            }
        }
    }

    // Settings adjustments
    fun setVolume(volume: Int) {
        _volumeSlider.value = volume.coerceIn(0, 100)
    }

    fun toggleMute() {
        _isSystemMuted.value = !_isSystemMuted.value
    }

    fun setBrightness(brightness: Int) {
        _brightnessSlider.value = brightness.coerceIn(0, 100)
    }

    fun toggleWifi() {
        _isWifiEnabled.value = !_isWifiEnabled.value
    }

    fun toggleBluetooth() {
        _isBluetoothEnabled.value = !_isBluetoothEnabled.value
    }

    fun toggleAirplaneMode() {
        _isAirplaneModeEnabled.value = !_isAirplaneModeEnabled.value
        if (_isAirplaneModeEnabled.value) {
            _isWifiEnabled.value = false
            _isBluetoothEnabled.value = false
        }
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleTaskbarAutohide() {
        _isTaskbarAutohide.value = !_isTaskbarAutohide.value
    }

    fun setWallpaperIndex(index: Int) {
        if (index in wallpaperPresets.indices) {
            _currentWallpaperIndex.value = index
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // App Management Logic
    fun openApp(appType: Win11AppType, customData: String = "") {
        _startMenuOpen.value = false // close shell start menu on app launch
        
        val existing = _activeWindows.value.find { it.appType == appType }
        if (existing != null) {
            // Un-minimize and bring to top
            _activeWindows.value = _activeWindows.value.map {
                if (it.appType == appType) {
                    it.copy(isMinimized = false, customData = if (customData.isNotEmpty()) customData else it.customData)
                } else {
                    it
                }
            }
            focusWindow(appType)
        } else {
            // Create relative to center coordinate with offset based on count to prevent perfect overlaps
            val listCount = _activeWindows.value.size
            val defaultX = 60f + (listCount * 30).toFloat()
            val defaultY = 120f + (listCount * 30).toFloat()
            
            val title = when (appType) {
                Win11AppType.FILE_EXPLORER -> "File Explorer"
                Win11AppType.NOTEPAD -> "Notepad"
                Win11AppType.CALCULATOR -> "Calculator"
                Win11AppType.CMD_POWERSHELL -> "Command Prompt"
                Win11AppType.EDGE_BROWSER -> "Microsoft Edge"
                Win11AppType.SETTINGS -> "Settings"
                Win11AppType.WIDGETS_BOARD -> "Widgets"
                Win11AppType.SOLITAIRE -> "Classic Solitaire"
                Win11AppType.SYS_INFO -> "System Information"
            }

            // Window Dimensions based on category
            val (w, h) = when (appType) {
                Win11AppType.CALCULATOR -> Pair(290f, 410f)
                Win11AppType.CMD_POWERSHELL -> Pair(420f, 320f)
                Win11AppType.SOLITAIRE -> Pair(340f, 440f)
                else -> Pair(350f, 420f)
            }

            val newWindow = WindowState(
                appType = appType,
                title = title,
                x = defaultX,
                y = defaultY,
                width = w,
                height = h,
                customData = customData
            )

            _activeWindows.value = _activeWindows.value + newWindow
            focusWindow(appType)
        }
    }

    fun closeApp(appType: Win11AppType) {
        _activeWindows.value = _activeWindows.value.filter { it.appType != appType }
        if (_focusedWindowId.value == appType) {
            _focusedWindowId.value = _activeWindows.value.maxByOrNull { it.zIndex }?.appType
        }
    }

    fun minimizeApp(appType: Win11AppType) {
        _activeWindows.value = _activeWindows.value.map {
            if (it.appType == appType) it.copy(isMinimized = true) else it
        }
        if (_focusedWindowId.value == appType) {
            // refocus the next highest window
            val remainingVisible = _activeWindows.value.filter { !it.isMinimized && it.appType != appType }
            _focusedWindowId.value = remainingVisible.maxByOrNull { it.zIndex }?.appType
        }
    }

    fun toggleMaximize(appType: Win11AppType) {
        _activeWindows.value = _activeWindows.value.map {
            if (it.appType == appType) it.copy(isMaximized = !it.isMaximized) else it
        }
        focusWindow(appType)
    }

    fun focusWindow(appType: Win11AppType) {
        _focusedWindowId.value = appType
        val maxZ = (_activeWindows.value.maxOfOrNull { it.zIndex } ?: 0f) + 1f
        _activeWindows.value = _activeWindows.value.map {
            if (it.appType == appType) it.copy(zIndex = maxZ) else it
        }
    }

    fun dragWindow(appType: Win11AppType, dx: Float, dy: Float) {
        _activeWindows.value = _activeWindows.value.map {
            if (it.appType == appType && !it.isMaximized) {
                it.copy(x = it.x + dx, y = it.y + dy)
            } else {
                it
            }
        }
    }

    fun resizeWindow(appType: Win11AppType, dw: Float, dh: Float) {
        _activeWindows.value = _activeWindows.value.map {
            if (it.appType == appType && !it.isMaximized) {
                val newW = (it.width + dw).coerceAtLeast(200f)
                val newH = (it.height + dh).coerceAtLeast(200f)
                it.copy(width = newW, height = newH)
            } else {
                it
            }
        }
    }

    // Simulated File Manipulations
    fun addVirtualFile(name: String, path: String, content: String, isFolder: Boolean, customIconType: String = "") {
        val calculatedPath = if (path.endsWith("\\")) "$path$name" else "$path\\$name"
        
        // Prevent simple duplicates
        if (_virtualFiles.value.any { it.path.equals(calculatedPath, ignoreCase = true) }) return

        val iconType = if (customIconType.isNotEmpty()) {
            customIconType
        } else if (isFolder) {
            "folder"
        } else {
            "text"
        }

        val newFile = VirtualFile(
            name = name,
            path = calculatedPath,
            isDirectory = isFolder,
            content = content,
            iconType = iconType
        )
        
        _virtualFiles.value = _virtualFiles.value + newFile
    }

    fun deleteVirtualFile(path: String) {
        // Can't delete core system drives
        if (path == "C:" || path == "C:\\Windows" || path == "C:\\Users" || path == "C:\\Users\\User") return
        
        // Delete targeted file/folder and all children folders
        _virtualFiles.value = _virtualFiles.value.filter {
            !it.path.startsWith(path)
        }
    }

    fun updateVirtualFileContent(path: String, newContent: String) {
        _virtualFiles.value = _virtualFiles.value.map {
            if (it.path == path) it.copy(content = newContent) else it
        }
    }

    // Powershell CLI Execution kernel
    fun executeShellCommand(rawCommandText: String) {
        val trimmed = rawCommandText.trim()
        if (trimmed.isEmpty()) return

        val currentHistory = _terminalHistory.value.toMutableList()
        currentHistory.add("${_terminalCurrentDir.value}> $trimmed")

        val parts = trimmed.split(" ")
        val baseCommand = parts[0].lowercase()
        val args = parts.drop(1)

        when (baseCommand) {
            "help" -> {
                currentHistory.add("Available Command Shell Commandlets:")
                currentHistory.add("  help              - Display this helper catalog")
                currentHistory.add("  dir, ls           - List files and sub-folders in active scope")
                currentHistory.add("  cd [path]         - Shift directories (e.g., 'cd Desktop' or 'cd ..')")
                currentHistory.add("  mkdir [name]      - Form a new folder")
                currentHistory.add("  echo [words]      - Display simple parameter lines")
                currentHistory.add("  ver               - Output simulated OS Version specs")
                currentHistory.add("  cls, clear        - Scrub terminal scroll clean")
                currentHistory.add("  type [file]       - Read text out to display layout")
                currentHistory.add("  rm [file/folder]  - Delete virtual file directory item")
                currentHistory.add("  systeminfo        - Display comprehensive specs sheet")
                currentHistory.add("  matrix            - Stream diagnostic digital rain flow")
            }
            "cls", "clear" -> {
                _terminalHistory.value = emptyList()
                return
            }
            "ver" -> {
                currentHistory.add("Microsoft Windows [Version 10.0.22621.1702]")
                currentHistory.add("Fluent Material 3 Android Sandbox Edition (64-bit)")
            }
            "systeminfo" -> {
                currentHistory.add("Host Name:               WINDOWS-VIRTUAL-ANDROID")
                currentHistory.add("OS Name:                 Microsoft Windows 11 Sandbox")
                currentHistory.add("Product ID:              00330-80000-00000-AA618")
                currentHistory.add("System Manufacturer:     Google AI Studio Dev Container")
                currentHistory.add("BIOS Version:            INSYDE Corp. V99.01 (10/24/2026)")
                currentHistory.add("Virtual Memory Space:    16,384 MB Allocated")
                currentHistory.add("Host CPU Core Count:     8 Physical Cores (Snapdragon Elite)")
            }
            "dir", "ls" -> {
                val contents = _virtualFiles.value.filter {
                    val parent = it.path.substringBeforeLast("\\")
                    val isDirectChild = parent == _terminalCurrentDir.value && it.path != parent
                    // Edge case for C: files
                    val isCEdge = _terminalCurrentDir.value == "C:" && !it.path.contains("\\") && it.path != "C:"
                    isDirectChild || isCEdge
                }
                
                if (contents.isEmpty()) {
                    currentHistory.add(" Directory is fully empty.")
                } else {
                    currentHistory.add(" File Directory listing for: ${_terminalCurrentDir.value}")
                    currentHistory.add(" <DIR>    %-16s".format("."))
                    currentHistory.add(" <DIR>    %-16s".format(".."))
                    contents.forEach { file ->
                        if (file.isDirectory) {
                            currentHistory.add(" <DIR>    %-16s".format(file.name))
                        } else {
                            currentHistory.add("          %-16s  (%5d bytes)".format(file.name, file.content.length))
                        }
                    }
                }
            }
            "cd" -> {
                val target = args.joinToString(" ").trim()
                if (target == "..") {
                    val current = _terminalCurrentDir.value
                    if (current == "C:") {
                        currentHistory.add("Root path reached - cannot retreat further.")
                    } else {
                        val prev = current.substringBeforeLast("\\")
                        _terminalCurrentDir.value = if (prev.isEmpty()) "C:" else prev
                    }
                } else if (target.isEmpty()) {
                    currentHistory.add(_terminalCurrentDir.value)
                } else {
                    // Search in child directories
                    val lookAheadPath = if (_terminalCurrentDir.value == "C:") {
                        "C:\\$target"
                    } else {
                        "${_terminalCurrentDir.value}\\$target"
                    }
                    val found = _virtualFiles.value.find {
                        it.path.equals(lookAheadPath, ignoreCase = true) && it.isDirectory
                    }
                    if (found != null) {
                        _terminalCurrentDir.value = found.path
                    } else {
                        // Search by absolute path match just in case
                        val absoluteMatch = _virtualFiles.value.find {
                            it.path.equals(target, ignoreCase = true) && it.isDirectory
                        }
                        if (absoluteMatch != null) {
                            _terminalCurrentDir.value = absoluteMatch.path
                        } else {
                            currentHistory.add("Path error: Cannot locate folder directory '$target'")
                        }
                    }
                }
            }
            "mkdir" -> {
                val folderName = args.joinToString(" ").trim()
                if (folderName.isEmpty()) {
                    currentHistory.add("Parameter required: mkdir [folder_name]")
                } else {
                    addVirtualFile(folderName, _terminalCurrentDir.value, "", isFolder = true)
                    currentHistory.add("Folder Directory created: $folderName")
                }
            }
            "echo" -> {
                currentHistory.add(args.joinToString(" "))
            }
            "type" -> {
                val fileName = args.joinToString(" ").trim()
                if (fileName.isEmpty()) {
                    currentHistory.add("Parameter required: type [file_name.txt]")
                } else {
                    val filePath = if (_terminalCurrentDir.value == "C:") "C:\\$fileName" else "${_terminalCurrentDir.value}\\$fileName"
                    val fileObj = _virtualFiles.value.find { it.path.equals(filePath, ignoreCase = true) && !it.isDirectory }
                    if (fileObj != null) {
                        currentHistory.add(" --- Reading content of ${fileObj.name} --- ")
                        fileObj.content.lines().forEach { currentHistory.add(it) }
                    } else {
                        currentHistory.add("Error: Cannot find document file matching '$fileName' in current directory.")
                    }
                }
            }
            "rm" -> {
                val targetName = args.joinToString(" ").trim()
                if (targetName.isEmpty()) {
                    currentHistory.add("Parameter required: rm [file_or_folder]")
                } else {
                    val targetPath = if (_terminalCurrentDir.value == "C:") "C:\\$targetName" else "${_terminalCurrentDir.value}\\$targetName"
                    val exists = _virtualFiles.value.any { it.path.equals(targetPath, ignoreCase = true) }
                    if (exists) {
                        deleteVirtualFile(targetPath)
                        currentHistory.add("Deleted: $targetName")
                    } else {
                        currentHistory.add("Error: Could not locate '$targetName'")
                    }
                }
            }
            "matrix" -> {
                currentHistory.add("CRITICAL MATRIX OVERRIDE ACTIVATED...")
                for (i in 1..8) {
                    val line = (1..24).map { "0123456789ABCDEFqwertyuiopasdfghjklzxcvbnm".random() }.joinToString("  ")
                    currentHistory.add(line)
                }
                currentHistory.add("DIAGNOSTIC COMPLETED: OK.")
            }
            else -> {
                currentHistory.add("Command error: '$baseCommand' is not recognized as an internal or external command,")
                currentHistory.add("operable program or batch PowerShell script. Type 'help' for support.")
            }
        }

        _terminalHistory.value = currentHistory
    }
}
