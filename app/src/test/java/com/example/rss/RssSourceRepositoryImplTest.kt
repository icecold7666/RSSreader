package com.example.rss

import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.entity.RssSourceEntity
import com.example.rss.data.repository.RssSourceRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RssSourceRepositoryImplTest {

    @Test
    fun addSource_and_getByUrl_works() = runTest {
        val dao = FakeRssSourceDao()
        val repo = RssSourceRepositoryImpl(dao)

        val id = repo.addSource(
            com.example.rss.domain.model.RssSource(
                title = "Kotlin Blog",
                url = "https://blog.jetbrains.com/kotlin/feed/"
            )
        )

        val source = repo.getSourceByUrl("https://blog.jetbrains.com/kotlin/feed/")
        assertEquals(1L, id)
        assertNotNull(source)
        assertEquals("Kotlin Blog", source?.title)
    }

    @Test
    fun addAllSources_returnsIds() = runTest {
        val dao = FakeRssSourceDao()
        val repo = RssSourceRepositoryImpl(dao)

        val ids = repo.addAllSources(
            listOf(
                com.example.rss.domain.model.RssSource(title = "A", url = "https://a.com/rss"),
                com.example.rss.domain.model.RssSource(title = "B", url = "https://b.com/rss")
            )
        )

        assertEquals(listOf(1L, 2L), ids)
        assertEquals(2, repo.getAllSources().first().size)
    }

    @Test
    fun deactivate_affectsActiveCount_and_categoryFilter() = runTest {
        val dao = FakeRssSourceDao()
        val repo = RssSourceRepositoryImpl(dao)
        val id1 = repo.addSource(
            com.example.rss.domain.model.RssSource(title = "Tech 1", url = "https://t1.com/rss", category = "科技")
        )
        repo.addSource(
            com.example.rss.domain.model.RssSource(title = "News 1", url = "https://n1.com/rss", category = "新闻")
        )

        assertEquals(2, repo.getActiveSourceCount())
        assertEquals(1, repo.getSourcesByCategory("科技").first().size)

        repo.deactivateSource(id1)
        assertEquals(1, repo.getActiveSourceCount())
        assertEquals(0, repo.getSourcesByCategory("科技").first().size)
    }
}

private class FakeRssSourceDao : RssSourceDao {
    private val data = mutableListOf<RssSourceEntity>()
    private val flow = MutableStateFlow<List<RssSourceEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllSources(): Flow<List<RssSourceEntity>> = flow.map { list ->
        list.sortedBy { it.title }
    }

    override fun getActiveSources(): Flow<List<RssSourceEntity>> = flow.map { list ->
        list.filter { it.isActive }.sortedBy { it.title }
    }

    override suspend fun getSourceById(id: Long): RssSourceEntity? = data.firstOrNull { it.id == id }

    override suspend fun getSourceByUrl(url: String): RssSourceEntity? = data.firstOrNull { it.url == url }

    override suspend fun insertSource(source: RssSourceEntity): Long {
        val withId = if (source.id == 0L) source.copy(id = nextId++) else source
        data.removeAll { it.id == withId.id }
        data.add(withId)
        publish()
        return withId.id
    }

    override suspend fun insertAllSources(sources: List<RssSourceEntity>) {
        sources.forEach { insertSource(it) }
    }

    override suspend fun updateSource(source: RssSourceEntity) {
        data.replaceAll { if (it.id == source.id) source else it }
        publish()
    }

    override suspend fun updateAllSources(sources: List<RssSourceEntity>) {
        sources.forEach { updateSource(it) }
    }

    override suspend fun deleteSource(source: RssSourceEntity) {
        data.removeAll { it.id == source.id }
        publish()
    }

    override suspend fun deleteSourceById(id: Long) {
        data.removeAll { it.id == id }
        publish()
    }

    override suspend fun deactivateSource(id: Long) {
        data.replaceAll { if (it.id == id) it.copy(isActive = false) else it }
        publish()
    }

    override suspend fun updateLastUpdate(id: Long, timestamp: Long) {
        data.replaceAll { if (it.id == id) it.copy(lastUpdate = timestamp) else it }
        publish()
    }

    override suspend fun getActiveSourceCount(): Int = data.count { it.isActive }

    override fun getCategories(): Flow<List<String>> = flow.map { list ->
        list.filter { it.isActive }.map { it.category }.distinct()
    }

    override fun getSourcesByCategory(category: String): Flow<List<RssSourceEntity>> = flow.map { list ->
        list.filter { it.category == category && it.isActive }.sortedBy { it.title }
    }

    private fun publish() {
        flow.value = data.toList()
    }
}
