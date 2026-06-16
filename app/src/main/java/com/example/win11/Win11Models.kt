package com.example.win11

import androidx.compose.runtime.Composable
import java.io.Serializable

// Virtual File representation
data class VirtualFile(
    val name: String,
    val path: String, // Full path e.g. "C:\Users\User\Documents"
    val isDirectory: Boolean,
    val content: String = "", // content for non-directory text files
    val iconType: String = "file" // "folder", "text", "drive", "pc", "trash", "system"
) : Serializable

// Windows 11 App Types
enum class Win11AppType {
    FILE_EXPLORER,
    NOTEPAD,
    CALCULATOR,
    CMD_POWERSHELL,
    EDGE_BROWSER,
    SETTINGS,
    WIDGETS_BOARD,
    SOLITAIRE,
    SYS_INFO
}

// Window state metadata for multitasking and window placement
data class WindowState(
    val appType: Win11AppType,
    val title: String,
    val x: Float = 50f,
    val y: Float = 100f,
    val width: Float = 360f,
    val height: Float = 480f,
    val isMaximized: Boolean = false,
    val isMinimized: Boolean = false,
    val zIndex: Float = 1f,
    val customData: String = "" // Optional metadata to pass (e.g., loaded file path for Notepad)
)

// Desktop icon representation
data class DesktopIcon(
    val title: String,
    val appType: Win11AppType,
    val iconType: String,
    val customPath: String = "" // Optional target path to open in app (e.g. C:\Users\User\Documents in File Explorer)
)

// Command structure for terminal shell
data class ShellCommand(
    val command: String,
    val description: String,
    val execute: (args: List<String>, currentDir: String, files: List<VirtualFile>) -> ShellResult
)

data class ShellResult(
    val output: List<String>,
    val nextDirectory: String? = null,
    val updatedFiles: List<VirtualFile>? = null
)

// Wallpaper style metadata
data class WallpaperPreset(
    val name: String,
    val previewColorHex: String,
    val isGradient: Boolean = true,
    val colors: List<String> // hex strings for dynamic Canvas generation
)

// Taskbar item model
data class TaskbarItem(
    val appType: Win11AppType?,
    val title: String,
    val iconType: String,
    val isPinned: Boolean = true
)
