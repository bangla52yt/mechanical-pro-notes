package com.researchbangla.mechanicalpronotes.domain.model

/**
 * Strict 4-tier content depth classification as mandated by DEC-009, DEC-010,
 * and Master Prompts 02-A / 02-B / 04.
 *
 * INVIOLABLE RULE:
 * An equipment record must NEVER be automatically promoted to a higher status.
 * INVENTORY_ONLY records must never be presented to the user as complete technical manuals.
 */
enum class ContentDepth(val displayName: String, val badgeColorHex: String) {
    /**
     * Complete 21-section engineering teardown including working principles,
     * component materials, wear parts, torque specs, LOTO procedures,
     * failure vibration signatures, and worked design calculations.
     */
    FULL_BLUEPRINT("Full Blueprint", "#2E7D32"),

    /**
     * Deep subsystem documentation covering operational specs, primary drives,
     * maintenance intervals, and basic failure modes, but lacking complete 21-section coverage.
     */
    PARTIAL_BLUEPRINT("Partial Blueprint", "#0288D1"),

    /**
     * Verified physical presence in textile/garment washing plants with 24 baseline
     * technical parameters (power, capacity, drives, lubrication), but awaiting deep teardown.
     */
    INVENTORY_ONLY("Inventory Only", "#757575"),

    /**
     * Plant-dependent or specialized machinery whose physical installation,
     * model number, or grate configuration is awaiting plant-floor owner confirmation.
     */
    RESEARCH_PENDING("Research Pending", "#ED6C02");

    companion object {
        fun fromString(value: String): ContentDepth {
            return when (value.trim().uppercase()) {
                "FULL_BLUEPRINT", "FULL_CONTENT_BLUEPRINT" -> FULL_BLUEPRINT
                "PARTIAL_BLUEPRINT", "PARTIAL_CONTENT_BLUEPRINT" -> PARTIAL_BLUEPRINT
                "RESEARCH_PENDING" -> RESEARCH_PENDING
                else -> entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                    ?: INVENTORY_ONLY
            }
        }
    }
}
