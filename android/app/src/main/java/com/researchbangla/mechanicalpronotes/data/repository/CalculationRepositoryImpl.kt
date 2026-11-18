package com.researchbangla.mechanicalpronotes.data.repository

import com.researchbangla.mechanicalpronotes.data.local.database.dao.CalculationDao
import com.researchbangla.mechanicalpronotes.data.mapper.toDomain
import com.researchbangla.mechanicalpronotes.domain.calculation.BoilerThermalCalculations
import com.researchbangla.mechanicalpronotes.domain.calculation.CalculationSpecifications
import com.researchbangla.mechanicalpronotes.domain.calculation.WashPlantMaintenanceCalculations
import com.researchbangla.mechanicalpronotes.domain.model.CalculationDefinition
import com.researchbangla.mechanicalpronotes.domain.model.CalculationExecutionResult
import com.researchbangla.mechanicalpronotes.domain.model.CalculationStatus
import com.researchbangla.mechanicalpronotes.domain.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CalculationRepositoryImpl(
    private val calculationDao: CalculationDao
) : CalculationRepository {

    override fun getAllCalculations(): Flow<List<CalculationDefinition>> {
        return calculationDao.getAllCalculations().map { entities ->
            if (entities.isNotEmpty() && entities.size >= 20) {
                entities.map { it.toDomain() }
            } else {
                CalculationSpecifications.ALL_CALCULATIONS
            }
        }
    }

    override suspend fun getCalculationById(id: String): CalculationDefinition? {
        val normalizedId = id.trim()
        val entity = calculationDao.getCalculationById(normalizedId)
            ?: calculationDao.getCalculationById(normalizedId.uppercase())
        if (entity != null) {
            val variables = calculationDao.getVariablesForCalculation(entity.id)
                .map { it.toDomain() }
                .ifEmpty {
                    CalculationSpecifications.getCalculationById(entity.id)?.variables.orEmpty()
                }
            return entity.toDomain(variables)
        }
        return CalculationSpecifications.getCalculationById(normalizedId)
    }

    override suspend fun executeCalculation(
        id: String,
        inputs: Map<String, Double>
    ): CalculationExecutionResult {
        return when (id.uppercase().trim()) {
            // Category A: Boiler Thermal & Utility Calculations (CALC-01 to CALC-14)
            "CALC-01" -> BoilerThermalCalculations.executeCalc01(inputs)
            "CALC-02" -> BoilerThermalCalculations.executeCalc02(inputs)
            "CALC-03" -> BoilerThermalCalculations.executeCalc03(inputs)
            "CALC-04" -> BoilerThermalCalculations.executeCalc04(inputs)
            "CALC-05" -> BoilerThermalCalculations.executeCalc05(inputs)
            "CALC-06" -> BoilerThermalCalculations.executeCalc06(inputs)
            "CALC-07" -> BoilerThermalCalculations.executeCalc07(inputs)
            "CALC-08" -> BoilerThermalCalculations.executeCalc08(inputs)
            "CALC-09" -> BoilerThermalCalculations.executeCalc09(inputs)
            "CALC-10" -> BoilerThermalCalculations.executeCalc10(inputs)
            "CALC-11" -> BoilerThermalCalculations.executeCalc11(inputs)
            "CALC-12" -> BoilerThermalCalculations.executeCalc12(inputs)
            "CALC-13" -> BoilerThermalCalculations.executeCalc13(inputs)
            "CALC-14" -> BoilerThermalCalculations.executeCalc14(inputs)

            // Category B: Wash Plant Maintenance & Reliability Tools (CALC-15 to CALC-20)
            "CALC-15" -> WashPlantMaintenanceCalculations.executeCalc15(inputs)
            "CALC-16" -> WashPlantMaintenanceCalculations.executeCalc16(inputs)
            "CALC-17" -> WashPlantMaintenanceCalculations.executeCalc17(inputs)
            "CALC-18" -> WashPlantMaintenanceCalculations.executeCalc18(inputs)
            "CALC-19" -> WashPlantMaintenanceCalculations.executeCalc19(inputs)
            "CALC-20" -> WashPlantMaintenanceCalculations.executeCalc20(inputs)

            else -> {
                CalculationExecutionResult(
                    calculationId = id,
                    primaryOutputName = "UNVERIFIED_OR_PENDING",
                    primaryOutputValue = 0.0,
                    primaryOutputUnit = "N/A",
                    notesAndRecommendations = "Calculation definition '$id' not recognized in verified engineering registry.",
                    status = CalculationStatus.NOT_IMPLEMENTED
                )
            }
        }
    }
}
