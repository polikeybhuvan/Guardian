package com.example.ui.screens

import android.content.Context
import android.view.View
import android.graphics.Bitmap as AndroidBitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.viewinterop.AndroidView
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.graphics.Style
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.layer.Layer
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.`internal`.MapsforgeThemes
import java.io.File
import java.io.FileOutputStream

internal const val INDIA_MAP_SIZE_BYTES = 1_627_329_123L
internal const val INDIA_MAP_ASSET_NAME = "india.map"

internal fun indiaMapFile(context: Context): File =
    File(context.filesDir, "india.map")

internal fun indiaMapPartFile(context: Context): File =
    File(context.filesDir, "india.map.part")

/** Installs the bundled map into app-private storage so Mapsforge can read it as a File. */
internal fun installBundledIndiaMap(context: Context, onProgress: (Long) -> Unit = {}): File {
    val target = indiaMapFile(context)
    if (target.isFile && target.length() == INDIA_MAP_SIZE_BYTES) return target
    val part = indiaMapPartFile(context)
    part.parentFile?.mkdirs()
    part.delete()
    try {
        context.assets.open(INDIA_MAP_ASSET_NAME).use { input ->
            FileOutputStream(part).use { output ->
                val buffer = ByteArray(1024 * 1024)
                var copied = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    copied += count
                    if (copied % (8L * 1024 * 1024) < buffer.size) onProgress(copied)
                }
                output.fd.sync()
                if (copied != INDIA_MAP_SIZE_BYTES) {
                    error("Bundled map size mismatch: $copied bytes")
                }
            }
        }
        target.delete()
        check(part.renameTo(target)) { "Could not move the bundled map into place" }
        return target
    } catch (error: Throwable) {
        part.delete()
        throw error
    }
}

@Composable
internal fun OfflineIndiaMapSurface(
    mapFile: File,
    latitude: Double?,
    longitude: Double?,
    accuracyM: Float?,
    recenterRequest: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCenteredOnGps by remember(mapFile) { androidx.compose.runtime.mutableStateOf(latitude != null && longitude != null) }
    var lastRecenterRequest by remember(mapFile) { androidx.compose.runtime.mutableIntStateOf(recenterRequest) }
    val mapView = remember(context, mapFile) {
        MapView(context).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            val centerLat = latitude ?: 22.0
            val centerLon = longitude ?: 79.0
            setCenter(LatLong(centerLat, centerLon))
            setZoomLevel(if (latitude != null && longitude != null) 13.toByte() else 5.toByte())
            val cacheDir = File(context.getExternalFilesDir(null), "map-cache")
            cacheDir.mkdirs()
            val cache = AndroidUtil.createTileCache(
                context, cacheDir, "india", model.displayModel.tileSize,
                context.resources.displayMetrics.density, 1.0, false
            )
            val renderer = TileRendererLayer(
                cache, MapFile(mapFile), model.mapViewPosition, false, true, false,
                AndroidGraphicFactory.INSTANCE
            )
            renderer.setXmlRenderTheme(MapsforgeThemes.DEFAULT)
            layerManager.layers.add(renderer)
        }
    }
    DisposableEffect(mapView) { onDispose { mapView.destroyAll() } }
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { mapView },
            update = {
                if (latitude != null && longitude != null && (!hasCenteredOnGps || recenterRequest != lastRecenterRequest)) {
                    it.setCenter(LatLong(latitude, longitude))
                    it.setZoomLevel(15.toByte())
                    hasCenteredOnGps = true
                    lastRecenterRequest = recenterRequest
                }
                val previous = (it.tag as? List<*>)?.filterIsInstance<Layer>().orEmpty()
                previous.forEach(it.layerManager.layers::remove)
                val markerLayers = buildList {
                    if (latitude != null && longitude != null) {
                        add(accuracyCircle(latitude, longitude, accuracyM))
                        add(marker(context, latitude, longitude, 0xFF1976D2.toInt()))
                    }
                }
                it.layerManager.layers.addAll(markerLayers)
                it.tag = markerLayers
            }
        )
        Text(
            text = "© OpenStreetMap contributors · Mapsforge",
            color = Color.White,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart)
                .padding(8.dp).background(Color(0x990F1720)).padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun accuracyCircle(latitude: Double, longitude: Double, accuracyM: Float?): org.mapsforge.map.layer.overlay.Circle {
    val fill = AndroidGraphicFactory.INSTANCE.createPaint().apply {
        setColor(0x331976D2)
        setStyle(Style.FILL)
    }
    val outline = AndroidGraphicFactory.INSTANCE.createPaint().apply {
        setColor(0xAA1976D2.toInt())
        setStrokeWidth(2f)
        setStyle(Style.STROKE)
    }
    return org.mapsforge.map.layer.overlay.Circle(
        LatLong(latitude, longitude),
        (accuracyM ?: 50f).coerceIn(5f, 10_000f),
        fill,
        outline
    )
}

private fun marker(context: Context, latitude: Double, longitude: Double, color: Int): Marker {
    val bitmap = AndroidBitmap.createBitmap(40, 40, AndroidBitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { this.color = color }
    canvas.drawCircle(20f, 20f, 18f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(20f, 20f, 7f, paint)
    val drawable = BitmapDrawable(context.resources, bitmap)
    val mapsforgeBitmap = AndroidGraphicFactory.convertToBitmap(drawable)
    return Marker(LatLong(latitude, longitude), mapsforgeBitmap, -20, -20)
}


