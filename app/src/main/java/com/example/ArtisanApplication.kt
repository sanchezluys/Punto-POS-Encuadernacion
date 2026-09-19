package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Aplicación principal que inicializa y configura Coil como biblioteca global de carga
 * de imágenes para controlar automáticamente la reducción de muestras (downsampling),
 * el almacenamiento en caché en memoria y disco, y la administración óptima de memoria.
 */
class ArtisanApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}
