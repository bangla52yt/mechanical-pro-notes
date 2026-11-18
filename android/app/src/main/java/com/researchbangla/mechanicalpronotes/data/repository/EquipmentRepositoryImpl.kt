package com.researchbangla.mechanicalpronotes.data.repository

import com.researchbangla.mechanicalpronotes.data.local.database.dao.EquipmentDao
import com.researchbangla.mechanicalpronotes.data.mapper.toDomain
import com.researchbangla.mechanicalpronotes.domain.model.*
import com.researchbangla.mechanicalpronotes.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EquipmentRepositoryImpl(
    private val equipmentDao: EquipmentDao
) : EquipmentRepository {

    override fun getAllSystemFamilies(): Flow<List<SystemFamily>> {
        return equipmentDao.getAllSystemFamilies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllEquipment(): Flow<List<Equipment>> {
        return equipmentDao.getAllEquipment().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEquipmentByFamily(familyId: String): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentByFamily(familyId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEquipmentById(equipmentId: String): Equipment? {
        val entity = equipmentDao.getEquipmentById(equipmentId) ?: return null
        val subtypes = equipmentDao.getSubtypesForEquipment(equipmentId).map { it.toDomain() }
        val components = equipmentDao.getComponentsForEquipment(equipmentId).map { it.toDomain() }
        val parameters = equipmentDao.getParametersForEquipment(equipmentId).map { it.toDomain() }
        val sections = equipmentDao.getKnowledgeSections(equipmentId).map { it.toDomain() }
        val failures = equipmentDao.getFailureModes(equipmentId).map { it.toDomain() }
        val procedures = equipmentDao.getMaintenanceProcedures(equipmentId).map { it.toDomain() }

        return entity.toDomain(
            subtypes = subtypes,
            components = components,
            parameters = parameters,
            sections = sections,
            failures = failures,
            procedures = procedures
        )
    }

    override fun getEquipmentByContentDepth(depth: ContentDepth): Flow<List<Equipment>> {
        return equipmentDao.getEquipmentByContentDepth(depth.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getKnowledgeSections(equipmentId: String): List<KnowledgeSection> {
        return equipmentDao.getKnowledgeSections(equipmentId).map { it.toDomain() }
    }

    override suspend fun getFailureModes(equipmentId: String): List<FailureMode> {
        return equipmentDao.getFailureModes(equipmentId).map { it.toDomain() }
    }

    override suspend fun getMaintenanceProcedures(equipmentId: String): List<MaintenanceProcedure> {
        return equipmentDao.getMaintenanceProcedures(equipmentId).map { it.toDomain() }
    }
}
