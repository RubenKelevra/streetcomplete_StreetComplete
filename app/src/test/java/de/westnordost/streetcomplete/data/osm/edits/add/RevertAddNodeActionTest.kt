package de.westnordost.streetcomplete.data.osm.edits.add

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RevertAddNodeActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test
    fun `revert add node`() {
        val node = node(123, LatLon(12.0, 34.0), mapOf("amenity" to "atm"), 1)
        val data = RevertAddNodeAction.createUpdates(node, node, repos, provider)

        assertTrue(data.creations.isEmpty())
        assertTrue(data.modifications.isEmpty())

        val deletedNode = data.deletions.single() as Node
        assertEquals(node, deletedNode)
    }

    @Test(expected = ConflictException::class)
    fun `conflict revert add node when already deleted`() {
        RevertAddNodeAction.createUpdates(node(), null, repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `moved element creates conflict`() {
        val node = node()
        RevertAddNodeAction.createUpdates(
            node, node.copy(position = p(1.0, 1.0)), repos, provider
        )
    }

    @Test(expected = ConflictException::class)
    fun `tags changed on element creates conflict`() {
        val node = node()
        RevertAddNodeAction.createUpdates(
            node, node.copy(tags = mapOf("something" to "else")), repos, provider
        )
    }
}
