package com.researchbangla.mechanicalpronotes

import com.researchbangla.mechanicalpronotes.data.local.database.dao.SearchDao
import com.researchbangla.mechanicalpronotes.data.local.database.entity.EquipmentEntity
import com.researchbangla.mechanicalpronotes.data.local.database.entity.EquipmentFtsEntity
import com.researchbangla.mechanicalpronotes.data.repository.SearchRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

/**
 * Regression tests for DEF-AUD-01 (Bengali Unicode NFC & combining mark preservation in FTS search).
 */
class SearchRepositoryTest {

    private class FakeSearchDao(
        var ftsResults: List<EquipmentEntity> = emptyList(),
        var likeResults: List<EquipmentEntity> = emptyList(),
        var throwOnFts: Boolean = false
    ) : SearchDao {
        var lastFtsQuery: String? = null
        var lastLikeQuery: String? = null

        override suspend fun searchFts(query: String, limit: Int): List<EquipmentEntity> {
            lastFtsQuery = query
            if (throwOnFts) throw IllegalStateException("Simulated FTS query error")
            return ftsResults
        }

        override suspend fun searchLike(query: String, limit: Int): List<EquipmentEntity> {
            lastLikeQuery = query
            return likeResults
        }

        override suspend fun insertFts(ftsItems: List<EquipmentFtsEntity>) = Unit
    }

    private fun sampleEntity(id: String, name: String, bengaliName: String) = EquipmentEntity(
        id = id,
        categoryId = "CAT-01",
        familyId = "FAM-G",
        name = name,
        bengaliName = bengaliName,
        contentDepthStatus = "FULL_BLUEPRINT",
        criticalityTier = "CRITICAL_UTILITY"
    )

    @Test
    fun buildSafeFtsQueryPreservesBengaliVowelSignsAndHalant() {
        val repo = SearchRepositoryImpl(FakeSearchDao())

        assertEquals("বয়লার*", repo.buildSafeFtsQuery("বয়লার"))
        assertEquals("বেলি* ওয়াশার*", repo.buildSafeFtsQuery("বেলি ওয়াশার"))
        assertEquals("হাইড্রো* এক্সট্র্যাক্টর*", repo.buildSafeFtsQuery("হাইড্রো এক্সট্র্যাক্টর"))
        assertEquals("রিভার্স* অসমোসিস*", repo.buildSafeFtsQuery("রিভার্স অসমোসিস!"))
    }

    @Test
    fun buildSafeFtsQueryNormalizesNfdToNfcAndQuotesEquipmentIds() {
        val repo = SearchRepositoryImpl(FakeSearchDao())
        val nfdBengali = Normalizer.normalize("বয়লার", Normalizer.Form.NFD)
        val nfcExpected = "${Normalizer.normalize("বয়লার", Normalizer.Form.NFC)}*"

        assertEquals(nfcExpected, repo.buildSafeFtsQuery(nfdBengali))
        assertEquals("\"EQ-A01\"", repo.buildSafeFtsQuery("EQ-A01"))
        assertNull(repo.buildSafeFtsQuery("   "))
        assertNull(repo.buildSafeFtsQuery("---***"))
    }

    @Test
    fun searchNormalizesQueryAndFallsBackToLikeOnEmptyFtsOrError() = runTest {
        val roPlant = sampleEntity("EQ-H03", "Industrial RO Plant", "ইন্ডাস্ট্রিয়াল আর.ও (RO) প্ল্যান্ট")
        val dao = FakeSearchDao(likeResults = listOf(roPlant), throwOnFts = true)
        val repo = SearchRepositoryImpl(dao)

        val nfdQuery = Normalizer.normalize("রিভার্স", Normalizer.Form.NFD)
        val results = repo.search(nfdQuery, limit = 10)

        assertEquals(1, results.size)
        assertEquals("EQ-H03", results.first().id)
        assertEquals("${Normalizer.normalize("রিভার্স", Normalizer.Form.NFC)}*", dao.lastFtsQuery)
        assertEquals(Normalizer.normalize("রিভার্স", Normalizer.Form.NFC), dao.lastLikeQuery)
        assertTrue(repo.search("   ").isEmpty())
    }
}
