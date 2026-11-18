package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilter
import com.researchbangla.mechanicalpronotes.domain.equipment.EquipmentFilterCriteria
import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

/**
 * Stage 3 Area C — Equipment Filter Criteria & Multi-Select ContentDepth Tests.
 */
class EquipmentFilterTest {

    private val sampleEquipment = listOf(
        Equipment(
            id = "EQ-A01",
            categoryId = "CAT-01",
            familyId = "FAM-A",
            name = "Side-Loading Belly Washing Machine",
            bengaliName = "বেলি ওয়াশিং মেশিন",
            aliases = listOf("Belly Washer", "Garment Washer"),
            contentDepth = ContentDepth.FULL_BLUEPRINT,
            criticalityTier = CriticalityTier.PRIMARY_PRODUCTION,
            primaryDriveType = "V-belt & Inverter"
        ),
        Equipment(
            id = "EQ-A03",
            categoryId = "CAT-01",
            familyId = "FAM-A",
            name = "Sample Dyeing & Washing Machine",
            bengaliName = "স্যাম্পল ওয়াশিং মেশিন",
            contentDepth = ContentDepth.INVENTORY_ONLY,
            criticalityTier = CriticalityTier.SECONDARY_AUXILIARY
        ),
        Equipment(
            id = "EQ-B01",
            categoryId = "CAT-02",
            familyId = "FAM-B",
            name = "Centrifugal Hydro Extractor",
            bengaliName = "হাইড্রো এক্সট্র্যাক্টর",
            contentDepth = ContentDepth.FULL_BLUEPRINT,
            criticalityTier = CriticalityTier.SAFETY_CRITICAL
        ),
        Equipment(
            id = "EQ-G01",
            categoryId = "CAT-07",
            familyId = "FAM-G",
            name = "Gas-Fired 3-Pass Wet-Back Steam Boiler",
            bengaliName = "গ্যাস বয়লার",
            contentDepth = ContentDepth.PARTIAL_BLUEPRINT,
            criticalityTier = CriticalityTier.CRITICAL_UTILITY
        ),
        Equipment(
            id = "EQ-G02",
            categoryId = "CAT-07",
            familyId = "FAM-G",
            name = "Jhut / Solid-Fuel Hybrid Steam Boiler",
            bengaliName = "ঝুট বয়লার",
            contentDepth = ContentDepth.RESEARCH_PENDING,
            criticalityTier = CriticalityTier.CRITICAL_UTILITY
        )
    )

    @Test
    fun emptyCriteriaReturnsAllEquipmentInOriginalOrder() {
        val result = EquipmentFilter.apply(sampleEquipment, EquipmentFilterCriteria())
        assertEquals(sampleEquipment.map { it.id }, result.map { it.id })
    }

    @Test
    fun multiSelectContentDepthUsesOrUnionSemantics() {
        val result = EquipmentFilter.apply(
            sampleEquipment,
            EquipmentFilterCriteria(
                selectedDepths = setOf(ContentDepth.FULL_BLUEPRINT, ContentDepth.RESEARCH_PENDING)
            )
        )
        assertEquals(listOf("EQ-A01", "EQ-B01", "EQ-G02"), result.map { it.id })
    }

    @Test
    fun familyPlusDepthPlusQueryFilterCombinesCleanly() {
        val result = EquipmentFilter.apply(
            sampleEquipment,
            EquipmentFilterCriteria(
                familyId = "FAM-G",
                selectedDepths = setOf(ContentDepth.PARTIAL_BLUEPRINT, ContentDepth.RESEARCH_PENDING),
                query = "গ্যাস"
            )
        )
        assertEquals(listOf("EQ-G01"), result.map { it.id })
    }

    @Test
    fun nfdDecomposedBengaliQueryStillMatchesNfcEquipmentNames() {
        val nfdQuery = Normalizer.normalize("বয়লার", Normalizer.Form.NFD)
        val result = EquipmentFilter.apply(
            sampleEquipment,
            EquipmentFilterCriteria(query = nfdQuery)
        )
        assertEquals(listOf("EQ-G01", "EQ-G02"), result.map { it.id })
    }

    @Test
    fun countByDepthReflectsActiveFamilyAndQueryScopeIgnoringDepthSelection() {
        val counts = EquipmentFilter.countByDepth(
            items = sampleEquipment,
            familyId = "FAM-A",
            query = ""
        )
        assertEquals(1, counts[ContentDepth.FULL_BLUEPRINT])
        assertEquals(0, counts[ContentDepth.PARTIAL_BLUEPRINT])
        assertEquals(1, counts[ContentDepth.INVENTORY_ONLY])
        assertEquals(0, counts[ContentDepth.RESEARCH_PENDING])
    }

    @Test
    fun unmatchedQueryReturnsEmptyListCleanly() {
        val result = EquipmentFilter.apply(
            sampleEquipment,
            EquipmentFilterCriteria(query = "nonexistent-turbine-xyz")
        )
        assertTrue(result.isEmpty())
    }
}
