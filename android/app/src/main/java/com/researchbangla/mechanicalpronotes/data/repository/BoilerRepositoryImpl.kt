package com.researchbangla.mechanicalpronotes.data.repository

import com.researchbangla.mechanicalpronotes.data.local.database.dao.BoilerDao
import com.researchbangla.mechanicalpronotes.data.local.database.dao.EquipmentDao
import com.researchbangla.mechanicalpronotes.data.mapper.toDomain
import com.researchbangla.mechanicalpronotes.domain.model.Equipment
import com.researchbangla.mechanicalpronotes.domain.repository.BoilerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BoilerRepositoryImpl(
    private val boilerDao: BoilerDao,
    private val equipmentDao: EquipmentDao
) : BoilerRepository {

    override fun getBoilerEquipment(): Flow<List<Equipment>> {
        return boilerDao.getBoilerEquipment().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBoilerById(boilerId: String): Equipment? {
        val entity = equipmentDao.getEquipmentById(boilerId) ?: return null
        val subtypes = equipmentDao.getSubtypesForEquipment(boilerId).map { it.toDomain() }
        val components = equipmentDao.getComponentsForEquipment(boilerId).map { it.toDomain() }
        val parameters = equipmentDao.getParametersForEquipment(boilerId).map { it.toDomain() }
        val sections = equipmentDao.getKnowledgeSections(boilerId).map { it.toDomain() }
        val failures = equipmentDao.getFailureModes(boilerId).map { it.toDomain() }
        val procedures = equipmentDao.getMaintenanceProcedures(boilerId).map { it.toDomain() }

        return entity.toDomain(
            subtypes = subtypes,
            components = components,
            parameters = parameters,
            sections = sections,
            failures = failures,
            procedures = procedures
        )
    }

    override fun getGasBoilers(): Flow<List<Equipment>> {
        return boilerDao.getGasFiredBoilers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getJhutAndSolidFuelBoilers(): Flow<List<Equipment>> {
        return boilerDao.getJhutAndSolidFuelBoilers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
