package com.dsu.extended.util

import android.os.Build
import com.dsu.extended.model.DeviceInfo

/**
 * Comprehensive device analyzer for GSI compatibility checking
 */
object DeviceAnalyzer {

    /**
     * System properties to check for DSU/GSI compatibility
     */
    private val COMPATIBILITY_PROPS = mapOf(
        "ro.boot.dynamic_partitions" to "Dynamic Partitions",
        "ro.treble.enabled" to "Project Treble",
        "ro.vndk.version" to "VNDK Version",
        "ro.boot.vbmeta.device_state" to "Bootloader State",
        "ro.build.ab_update" to "A/B Partitions",
        "ro.virtual_ab.enabled" to "Virtual A/B",
        "ro.boot.slot_suffix" to "Current Slot",
        "ro.build.version.security_patch" to "Security Patch",
        "ro.hardware" to "Hardware",
        "ro.product.cpu.abi" to "CPU ABI",
        "ro.product.first_api_level" to "First API Level",
    )

    /**
     * Analyzes device compatibility for DSU/GSI installation
     */
    fun analyzeDevice(): DeviceAnalysisResult {
        val checks = mutableListOf<CompatibilityCheck>()
        val warnings = mutableListOf<String>()
        val deviceProps = mutableMapOf<String, String>()

        // Collect all device properties
        for ((prop, name) in COMPATIBILITY_PROPS) {
            val value = getSystemProp(prop)
            deviceProps[name] = value
        }

        // Check Android version
        val androidCheck = checkAndroidVersion()
        checks.add(androidCheck)

        // Check Dynamic Partitions
        val dynamicPartitionsCheck = checkDynamicPartitions(deviceProps["Dynamic Partitions"] ?: "")
        checks.add(dynamicPartitionsCheck)

        // Check Treble
        val trebleCheck = checkTreble(deviceProps["Project Treble"] ?: "")
        checks.add(trebleCheck)

        // Check VNDK
        val vndkCheck = checkVndk(deviceProps["VNDK Version"] ?: "")
        checks.add(vndkCheck)

        // Check Bootloader
        val bootloaderCheck = checkBootloader(deviceProps["Bootloader State"] ?: "")
        checks.add(bootloaderCheck)

        // Check CPU Architecture
        val cpuCheck = checkCpuArchitecture()
        checks.add(cpuCheck)

        // Check A/B partitions
        val abCheck = checkAbPartitions(deviceProps["A/B Partitions"] ?: "")
        checks.add(abCheck)

        // Check storage
        val storageCheck = checkStorage()
        checks.add(storageCheck)

        // Generate warnings based on checks
        checks.filter { it.status == CheckStatus.WARNING }.forEach { check ->
            warnings.add("${check.name}: ${check.details}")
        }

        val failedChecks = checks.count { it.status == CheckStatus.FAILED }
        val warningChecks = checks.count { it.status == CheckStatus.WARNING }

        val overallStatus = when {
            failedChecks > 0 -> OverallStatus.INCOMPATIBLE
            warningChecks > 1 -> OverallStatus.PARTIAL
            else -> OverallStatus.COMPATIBLE
        }

        return DeviceAnalysisResult(
            deviceInfo = DeviceInfo(),
            checks = checks,
            warnings = warnings,
            deviceProps = deviceProps,
            overallStatus = overallStatus,
            recommendedGsiArch = getRecommendedArch(),
            recommendedVndk = deviceProps["VNDK Version"] ?: "",
        )
    }

    private fun getSystemProp(prop: String): String {
        return try {
            Runtime.getRuntime().exec("getprop $prop")
                .inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun checkAndroidVersion(): CompatibilityCheck {
        val sdkInt = Build.VERSION.SDK_INT
        val version = Build.VERSION.RELEASE

        return when {
            sdkInt < 29 -> CompatibilityCheck(
                name = "Android Version",
                status = CheckStatus.FAILED,
                value = "$version (SDK $sdkInt)",
                details = "DSU requires Android 10 or higher",
                suggestion = "Your device cannot use DSU feature",
            )
            sdkInt == 29 -> CompatibilityCheck(
                name = "Android Version",
                status = CheckStatus.WARNING,
                value = "$version (SDK $sdkInt)",
                details = "Android 10 has limited DSU support",
                suggestion = "Some features may not work correctly",
            )
            else -> CompatibilityCheck(
                name = "Android Version",
                status = CheckStatus.PASSED,
                value = "$version (SDK $sdkInt)",
                details = "Android version is compatible",
            )
        }
    }

    private fun checkDynamicPartitions(value: String): CompatibilityCheck {
        val hasDynamic = value == "true"

        return if (hasDynamic) {
            CompatibilityCheck(
                name = "Dynamic Partitions",
                status = CheckStatus.PASSED,
                value = "Supported",
                details = "Device supports Dynamic Partitions",
            )
        } else {
            CompatibilityCheck(
                name = "Dynamic Partitions",
                status = CheckStatus.FAILED,
                value = "Not Supported",
                details = "DSU requires Dynamic Partitions",
                suggestion = "Your device cannot use DSU feature",
            )
        }
    }

    private fun checkTreble(value: String): CompatibilityCheck {
        val hasTreble = value == "true"

        return if (hasTreble) {
            CompatibilityCheck(
                name = "Project Treble",
                status = CheckStatus.PASSED,
                value = "Enabled",
                details = "Device is Treble-compliant",
            )
        } else {
            CompatibilityCheck(
                name = "Project Treble",
                status = CheckStatus.WARNING,
                value = "Not Detected",
                details = "Treble status unknown",
                suggestion = "Some GSIs may not work",
            )
        }
    }

    private fun checkVndk(value: String): CompatibilityCheck {
        return if (value.isNotEmpty()) {
            val vndkInt = value.toIntOrNull() ?: 0
            val status = when {
                vndkInt >= 33 -> CheckStatus.PASSED
                vndkInt >= 30 -> CheckStatus.PASSED
                vndkInt >= 28 -> CheckStatus.WARNING
                else -> CheckStatus.WARNING
            }

            CompatibilityCheck(
                name = "VNDK Version",
                status = status,
                value = value,
                details = "Use GSIs with this VNDK version or higher",
                suggestion = if (status == CheckStatus.WARNING) {
                    "Older VNDK may have compatibility issues"
                } else {
                    null
                },
            )
        } else {
            CompatibilityCheck(
                name = "VNDK Version",
                status = CheckStatus.WARNING,
                value = "Unknown",
                details = "Could not detect VNDK version",
            )
        }
    }

    private fun checkBootloader(value: String): CompatibilityCheck {
        val isUnlocked = value == "unlocked"

        return if (isUnlocked) {
            CompatibilityCheck(
                name = "Bootloader",
                status = CheckStatus.PASSED,
                value = "Unlocked",
                details = "Bootloader is unlocked",
            )
        } else {
            CompatibilityCheck(
                name = "Bootloader",
                status = CheckStatus.WARNING,
                value = value.ifEmpty { "Unknown" },
                details = "Bootloader may be locked",
                suggestion = "Unlock bootloader for best compatibility",
            )
        }
    }

    private fun checkCpuArchitecture(): CompatibilityCheck {
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        val allAbis = Build.SUPPORTED_ABIS.joinToString(", ")

        val is64Bit = primaryAbi.contains("64")

        return CompatibilityCheck(
            name = "CPU Architecture",
            status = if (is64Bit) CheckStatus.PASSED else CheckStatus.PASSED,
            value = primaryAbi,
            details = "Supported: $allAbis",
            suggestion = if (!is64Bit) "32-bit device - use arm GSIs" else null,
        )
    }

    private fun checkAbPartitions(value: String): CompatibilityCheck {
        val hasAb = value == "true"

        return CompatibilityCheck(
            name = "A/B Partitions",
            status = CheckStatus.PASSED,
            value = if (hasAb) "Enabled (Seamless Updates)" else "Legacy",
            details = if (hasAb) "Device uses A/B partition scheme" else "Device uses legacy partition scheme",
        )
    }

    private fun checkStorage(): CompatibilityCheck {
        val freeSpaceGb = StorageUtils.getFreeStorageGB()

        return when {
            freeSpaceGb < 5 -> CompatibilityCheck(
                name = "Storage",
                status = CheckStatus.FAILED,
                value = "${freeSpaceGb}GB free",
                details = "Insufficient storage for DSU",
                suggestion = "Free up at least 10GB for comfortable installation",
            )
            freeSpaceGb < 10 -> CompatibilityCheck(
                name = "Storage",
                status = CheckStatus.WARNING,
                value = "${freeSpaceGb}GB free",
                details = "Low storage may limit userdata size",
                suggestion = "Consider freeing more space",
            )
            else -> CompatibilityCheck(
                name = "Storage",
                status = CheckStatus.PASSED,
                value = "${freeSpaceGb}GB free",
                details = "Sufficient storage available",
            )
        }
    }

    private fun getRecommendedArch(): String {
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        return when {
            primaryAbi.contains("arm64") -> "arm64"
            primaryAbi.contains("x86_64") -> "x86_64"
            primaryAbi.contains("x86") -> "x86"
            primaryAbi.contains("armeabi") -> "arm"
            else -> "arm64"
        }
    }
}

/**
 * Result of device analysis
 */
data class DeviceAnalysisResult(
    val deviceInfo: DeviceInfo,
    val checks: List<CompatibilityCheck>,
    val warnings: List<String>,
    val deviceProps: Map<String, String>,
    val overallStatus: OverallStatus,
    val recommendedGsiArch: String,
    val recommendedVndk: String,
) {
    val passedChecks: Int get() = checks.count { it.status == CheckStatus.PASSED }
    val failedChecks: Int get() = checks.count { it.status == CheckStatus.FAILED }
    val warningChecks: Int get() = checks.count { it.status == CheckStatus.WARNING }
    val totalChecks: Int get() = checks.size

    fun toShareableText(): String = buildString {
        appendLine("═══════════════════════════════════════")
        appendLine("DSU EXTENDED - Device Analysis Report")
        appendLine("═══════════════════════════════════════")
        appendLine()
        appendLine("📱 Device: ${deviceInfo.manufacturer} ${deviceInfo.model}")
        appendLine("🤖 Android: ${deviceInfo.androidVersion} (SDK ${deviceInfo.sdkInt})")
        appendLine("🔧 CPU: ${deviceInfo.cpuAbi}")
        appendLine()
        appendLine("STATUS: ${overallStatus.emoji} ${overallStatus.label}")
        appendLine()
        appendLine("━━━ Compatibility Checks ━━━")
        checks.forEach { check ->
            appendLine("${check.status.emoji} ${check.name}: ${check.value}")
            if (check.suggestion != null) {
                appendLine("   └─ 💡 ${check.suggestion}")
            }
        }
        appendLine()
        appendLine("━━━ Recommendations ━━━")
        appendLine("• GSI Architecture: $recommendedGsiArch")
        appendLine("• VNDK Version: $recommendedVndk")
        appendLine()
        if (warnings.isNotEmpty()) {
            appendLine("━━━ Warnings ━━━")
            warnings.forEach { warning ->
                appendLine("⚠️ $warning")
            }
        }
    }
}

/**
 * Individual compatibility check result
 */
data class CompatibilityCheck(
    val name: String,
    val status: CheckStatus,
    val value: String,
    val details: String,
    val suggestion: String? = null,
)

enum class CheckStatus(val emoji: String) {
    PASSED("✅"),
    WARNING("⚠️"),
    FAILED("❌"),
}

enum class OverallStatus(val emoji: String, val label: String) {
    COMPATIBLE("✅", "COMPATIBLE"),
    PARTIAL("⚠️", "PARTIAL COMPATIBILITY"),
    INCOMPATIBLE("❌", "INCOMPATIBLE"),
}
