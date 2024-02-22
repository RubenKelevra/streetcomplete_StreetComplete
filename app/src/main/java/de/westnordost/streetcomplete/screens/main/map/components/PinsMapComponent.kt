package de.westnordost.streetcomplete.screens.main.map.components

import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.expressions.Expression
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController

/** Takes care of displaying pins on the map, e.g. quest pins or pins for recent edits */
class PinsMapComponent(private val ctrl: KtMapController) {

    /** Shows/hides the pins */
    var isVisible: Boolean
        // add / remove source
        get() = MainMapFragment.mapboxMap?.style?.sources?.any { it.id == "pins-source" } == true
        set(value) {
            if (isVisible == value) return
            if (value) {
                MainActivity.activity?.runOnUiThread { MainMapFragment.mapboxMap?.style?.addSource(MainMapFragment.pinsSource!!) }
            } else {
                MainActivity.activity?.runOnUiThread { MainMapFragment.mapboxMap?.style?.removeSource(MainMapFragment.pinsSource!!) }
            }
        }

    /** Show given pins. Previously shown pins are replaced with these.  */
    fun set(pins: Collection<Pin>) {
        // do sorting here, because we can set the symbolZOrder to SYMBOL_Z_ORDER_SOURCE, which
        // is the order in which the source has the features
        val mapLibreFeatures = pins.sortedBy { -it.importance }.map { pin ->
            val p = JsonObject()
            p.addProperty("icon-image", pin.iconName)
            p.addProperty("symbol-sort-key", -pin.importance.toFloat()) // still set sort key, because we may want to try it again
            pin.properties.forEach { p.addProperty(it.first, it.second) }
            Feature.fromGeometry(pin.position.toPoint(), p)
        }
        // todo: for testing the runOnUiThread is ok, but actually it should be handled differently...
        MainActivity.activity?.runOnUiThread { MainMapFragment.pinsSource?.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures)) }
    }

    /** Clear pins */
    fun clear() {
        val fc: FeatureCollection? = null // does it work?
        MainActivity.activity?.runOnUiThread { MainMapFragment.pinsSource?.setGeoJson(fc) }
    }

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val PINS_LAYER = "streetcomplete_pins"
    }
}

data class Pin(
    val position: LatLon,
    val iconName: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val importance: Int = 0
) {
    // todo: maplibre feature by lazy?
}
