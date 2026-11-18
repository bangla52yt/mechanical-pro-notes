package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.domain.model.ContentDepth
import com.researchbangla.mechanicalpronotes.domain.model.CriticalityTier
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.presentation.equipment.buildConditionMonitoringGuidance
import com.researchbangla.mechanicalpronotes.presentation.equipment.getRelatedCalculationsForEquipment
import com.researchbangla.mechanicalpronotes.presentation.equipment.getSchematicForEquipment
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression & GAP-13 coverage tests for truthful equipment-to-schematic,
 * condition-monitoring guidance, and equipment-to-calculator mappings.
 */
class EquipmentMappingTest {

    private fun equipment(id: String, familyId: String, name: String) = Equipment(
        id = id,
        categoryId = "CAT-01",
        familyId = familyId,
        name = name,
        bengaliName = name,
        contentDepth = ContentDepth.FULL_BLUEPRINT,
        criticalityTier = CriticalityTier.SECONDARY_AUXILIARY
    )

    @Test
    fun industrialRoPlantHasDedicatedWaterTreatmentSchematicAndNoMismatchedCalculator() {
        val eqH03 = equipment("EQ-H03", "FAM-H", "Industrial Reverse Osmosis (RO) Plant")
        val schematic = getSchematicForEquipment(eqH03)

        assertNotNull("EQ-H03 must map to Family H water treatment schematic", schematic)
        assertEquals(R.drawable.diag_water_treatment_loop, schematic?.drawableRes)
        assertNotEquals(
            "EQ-H03 must never reuse steam condensate schematic",
            R.drawable.diag_steam_condensate_loop,
            schematic?.drawableRes
        )
        assertTrue(
            "EQ-H03 must not map to unrelated deaerator or softener calculators",
            getRelatedCalculationsForEquipment(eqH03).isEmpty()
        )
    }

    @Test
    fun familyHMapsSoftenerToCalc19AndPumpsToCalc07() {
        val softener = equipment("EQ-H01", "FAM-H", "Sodium Ion-Exchange Water Softener")
        val transferPump = equipment("EQ-H02", "FAM-H", "Raw & Soft Water Storage Tanks & Transfer Pumps")

        assertEquals(listOf("CALC-19"), getRelatedCalculationsForEquipment(softener).map { it.first })
        assertEquals(listOf("CALC-07"), getRelatedCalculationsForEquipment(transferPump).map { it.first })
    }

    @Test
    fun familyGMapsBoilersWithCalc01Calc02Calc08AndProperBoilerSchematics() {
        val gasBoiler = equipment("EQ-G01", "FAM-G", "Natural Gas Firetube Boiler")
        val jhutBoiler = equipment("EQ-G02", "FAM-G", "Solid-Fuel (Jhut) Boiler")
        val steamLoop = equipment("EQ-F01", "FAM-F", "Main Steam Distribution Header")

        assertEquals(R.drawable.diag_gas_boiler_3pass, getSchematicForEquipment(gasBoiler)?.drawableRes)
        assertEquals(R.drawable.diag_jhut_boiler_draft, getSchematicForEquipment(jhutBoiler)?.drawableRes)
        assertEquals(R.drawable.diag_steam_condensate_loop, getSchematicForEquipment(steamLoop)?.drawableRes)

        val gasCalcs = getRelatedCalculationsForEquipment(gasBoiler).map { it.first }
        assertTrue("Boiler family should link CALC-08 Deaerator Steam Balance", "CALC-08" in gasCalcs)
        assertTrue("Boiler family should link CALC-01 Direct Efficiency", "CALC-01" in gasCalcs)
        assertTrue("Gas boiler should link CALC-02 Indirect Efficiency", "CALC-02" in gasCalcs)

        val jhutCalcs = getRelatedCalculationsForEquipment(jhutBoiler).map { it.first }
        assertTrue("Jhut boiler should link CALC-02 Indirect Efficiency", "CALC-02" in jhutCalcs)
        assertTrue("Jhut boiler should link CALC-12 ID Fan Sizing", "CALC-12" in jhutCalcs)
    }

    @Test
    fun positiveDisplacementPumpsDoNotMislinkCentrifugalPumpCalculator() {
        val centrifugalPump = equipment("EQ-D01", "FAM-D", "End-Suction Water Pump")
        val aoddPump = equipment("EQ-D03", "FAM-D", "AODD Chemical Pump")
        val meteringPump = equipment("EQ-D04", "FAM-D", "Metering Dosing Pump")
        val dieselGearPump = equipment("EQ-D06", "FAM-D", "Diesel Gear Transfer Pump")

        assertEquals(listOf("CALC-07"), getRelatedCalculationsForEquipment(centrifugalPump).map { it.first })
        assertTrue(getRelatedCalculationsForEquipment(aoddPump).isEmpty())
        assertTrue(getRelatedCalculationsForEquipment(meteringPump).isEmpty())
        assertTrue(getRelatedCalculationsForEquipment(dieselGearPump).isEmpty())
    }

    @Test
    fun all16FamiliesMapToDedicatedOfflineSchematicsWithConceptualDisclaimer() {
        val sampleCases = listOf(
            Triple("EQ-A01", "FAM-A", R.drawable.diag_belly_washer),
            Triple("EQ-B01", "FAM-B", R.drawable.diag_hydro_extractor),
            Triple("EQ-B03", "FAM-B", R.drawable.diag_tumbler_dryer),
            Triple("EQ-C01", "FAM-C", R.drawable.diag_finishing_oven_booth),
            Triple("EQ-D02", "FAM-D", R.drawable.diag_centrifugal_pump_npsh),
            Triple("EQ-D03", "FAM-D", R.drawable.diag_pd_metering_pump),
            Triple("EQ-E01", "FAM-E", R.drawable.diag_power_transmission),
            Triple("EQ-F02", "FAM-F", R.drawable.diag_steam_condensate_loop),
            Triple("EQ-G01", "FAM-G", R.drawable.diag_gas_boiler_3pass),
            Triple("EQ-G02", "FAM-G", R.drawable.diag_jhut_boiler_draft),
            Triple("EQ-H01", "FAM-H", R.drawable.diag_water_treatment_loop),
            Triple("EQ-I02", "FAM-I", R.drawable.diag_chemical_dosing_safety),
            Triple("EQ-J01", "FAM-J", R.drawable.diag_compressed_air_system),
            Triple("EQ-K01", "FAM-K", R.drawable.diag_cooling_chiller_loop),
            Triple("EQ-L01", "FAM-L", R.drawable.diag_etp_clarification),
            Triple("EQ-M01", "FAM-M", R.drawable.diag_lifting_conveyor),
            Triple("EQ-N01", "FAM-N", R.drawable.diag_workshop_machines),
            Triple("EQ-O01", "FAM-O", R.drawable.diag_safety_exhaust_utility),
            Triple("EQ-P01", "FAM-P", R.drawable.diag_safety_exhaust_utility)
        )

        val observedDrawables = mutableSetOf<Int>()
        sampleCases.forEach { (id, familyId, expectedDrawable) ->
            val meta = getSchematicForEquipment(equipment(id, familyId, id))
            assertNotNull("Expected schematic for $id", meta)
            assertEquals("Unexpected schematic for $id", expectedDrawable, meta?.drawableRes)
            assertTrue("Missing legend for $id", meta?.legend?.contains("[1]") == true)
            assertTrue("Missing conceptual disclaimer for $id", meta?.subtitle?.contains("Conceptual", ignoreCase = true) == true)
            observedDrawables.add(expectedDrawable)
        }
        assertEquals("All 18 offline schematics should be referenced", 18, observedDrawables.size)
    }

    @Test
    fun conditionMonitoringGuidanceDistinguishesStaticPdBeltsAndRotatingMachinery() {
        val (_, staticBody) = buildConditionMonitoringGuidance(equipment("EQ-I02", "FAM-I", "Safety Shower"))
        assertTrue(staticBody.contains("Excluded from ISO 20816-3"))

        val (_, headerValveBody) = buildConditionMonitoringGuidance(equipment("EQ-F01", "FAM-F", "Angle-Seat Valve"))
        assertTrue(headerValveBody.contains("Excluded from ISO 20816-3"))

        val (_, compressorBody) = buildConditionMonitoringGuidance(equipment("EQ-J01", "FAM-J", "Screw Compressor"))
        assertTrue(compressorBody.contains("excluded from standard ISO 20816-3"))

        val (_, pumpBody) = buildConditionMonitoringGuidance(equipment("EQ-D01", "FAM-D", "End-Suction Water Pump"))
        assertTrue(pumpBody.contains("ISO 10816-7"))

        val (_, dieselFirePumpBody) = buildConditionMonitoringGuidance(equipment("EQ-O01", "FAM-O", "Diesel Fire Pump"))
        assertTrue(dieselFirePumpBody.contains("ISO 10816-6"))
        assertTrue(dieselFirePumpBody.contains("ISO 10816-7"))

        val (_, conveyorBody) = buildConditionMonitoringGuidance(equipment("EQ-M03", "FAM-M", "Cleated Belt Conveyor"))
        assertTrue(conveyorBody.contains("<120 r/min"))

        val (_, beltBody) = buildConditionMonitoringGuidance(equipment("EQ-E04", "FAM-E", "V-Belt Drive"))
        assertTrue(beltBody.contains("coplanarity"))

        val (_, smallMachineBody) = buildConditionMonitoringGuidance(equipment("EQ-A01", "FAM-A", "Belly Washer"))
        assertTrue(smallMachineBody.contains("15 kW"))

        val (_, rotatingBody) = buildConditionMonitoringGuidance(equipment("EQ-P01", "FAM-P", "Exhaust Blower"))
        assertTrue(rotatingBody.contains("ISO 20816-3:2026"))
        assertTrue(rotatingBody.contains("120–30,000 r/min"))
    }
}
