package com.example.ui.screens

import com.example.data.location.DeviceCoordinates
import org.mapsforge.core.model.Tile
import org.mapsforge.core.util.MercatorProjection
import org.mapsforge.map.reader.MapFile
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal data class OfflineMapPlace(
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double
)

/** Searches named POIs embedded in the bundled Mapsforge map. No network or extra POI download is used. */
internal fun searchOfflineMapPlaces(
    mapFile: File,
    center: DeviceCoordinates,
    query: String,
    radiusMeters: Double = 5_000.0
): List<OfflineMapPlace> {
    if (!mapFile.isFile || query.isBlank()) return emptyList()

    val zoom = 13.toByte()
    val centerX = MercatorProjection.longitudeToTileX(center.longitude, zoom)
    val centerY = MercatorProjection.latitudeToTileY(center.latitude, zoom)
    val tileMeters = 40_075_016.686 * cos(Math.toRadians(center.latitude)) / 2.0.pow(zoom.toDouble())
    val tileRadius = (ceil(radiusMeters / tileMeters).toInt() + 1).coerceIn(1, 5)
    val maxTile = Tile.getMaxTileNumber(zoom)
    val searchTerms = query.split(',', ';')
        .map { normalize(it) }
        .filter { it.isNotBlank() }
        .flatMap { term -> if (term.endsWith('s') && term.length > 4) listOf(term, term.dropLast(1)) else listOf(term) }
        .distinct()

    val results = LinkedHashMap<String, OfflineMapPlace>()
    val reader = runCatching { MapFile(mapFile) }.getOrNull() ?: return emptyList()
    try {
        for (y in (centerY - tileRadius).coerceAtLeast(0)..(centerY + tileRadius).coerceAtMost(maxTile)) {
            for (x in (centerX - tileRadius).coerceAtLeast(0)..(centerX + tileRadius).coerceAtMost(maxTile)) {
                val readResult = runCatching { reader.readNamedItems(Tile(x, y, zoom, 256)) }.getOrNull()
                readResult?.pois.orEmpty().forEach { poi ->
                    val tags = poi.tags
                    val searchableText = normalize(
                        buildString {
                            tags.forEach { tag ->
                                append(tag.key).append(' ').append(tag.value).append(' ')
                            }
                        }
                    )
                    if (searchTerms.none(searchableText::contains)) return@forEach

                    val latitude = poi.position.latitude
                    val longitude = poi.position.longitude
                    val distance = distanceMeters(center.latitude, center.longitude, latitude, longitude)
                    if (distance > radiusMeters) return@forEach

                    val name = tags.firstOrNull { it.key == "name" }?.value
                        ?: tags.firstOrNull { it.key.startsWith("name:") }?.value
                        ?: tags.firstOrNull { it.key == "ref" }?.value
                    val category = poiCategory(tags.map { it.key to it.value }.toMap())
                    val title = name?.takeIf(String::isNotBlank) ?: category
                    val place = OfflineMapPlace(title, category, latitude, longitude, distance)
                    val key = "${normalize(title)}:${"%.5f".format(java.util.Locale.US, latitude)}:${"%.5f".format(java.util.Locale.US, longitude)}"
                    val previous = results[key]
                    if (previous == null || place.distanceMeters < previous.distanceMeters) results[key] = place
                }
            }
        }
    } finally {
        runCatching { reader.close() }
    }

    return results.values.sortedBy(OfflineMapPlace::distanceMeters).take(40)
}

private fun poiCategory(tags: Map<String, String>): String = when {
    tags["amenity"] == "hospital" || tags["healthcare"] == "hospital" -> "Hospital"
    tags["amenity"] == "police" -> "Police"
    tags["amenity"] == "fire_station" -> "Fire station"
    tags["amenity"] == "pharmacy" || tags["shop"] == "chemist" -> "Pharmacy"
    tags["amenity"] == "clinic" || tags["healthcare"] == "clinic" -> "Clinic"
    tags["emergency"] == "ambulance_station" -> "Ambulance station"
    else -> tags["amenity"]?.replace('_', ' ')?.replaceFirstChar(Char::uppercase)
        ?: tags["shop"]?.replace('_', ' ')?.replaceFirstChar(Char::uppercase)
        ?: "Place"
}

private fun normalize(value: String): String = value
    .lowercase(java.util.Locale.ROOT)
    .replace('_', ' ')
    .replace(Regex("[^\\p{L}\\p{N} ]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    return earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
}
