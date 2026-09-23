package util

import data.Bonds
import data.Lattice
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories

// сторона картинки, до которой растягиваются маленькие решётки
private const val TARGET_IMAGE_SIZE = 1000

/**
 * Сохраняет решётку со связями в PNG. Картинка — сетка (2L-1) x (2L-1) клеток: узел `(x, y)` в клетке `(2x, 2y)`,
 * связь между соседними узлами — в клетке между ними. Занятый узел и связь чёрные, остальное белое.
 * Маленькие решётки растягиваются, чтобы клетка была не меньше пикселя и картинка около 1000 пикселей.
 */
fun Bonds.writeAsPNG(sites: Lattice, p: Double, path: Path = defaultPathPNG(p)): Path {
    val cells = 2 * size - 1
    val scale = maxOf(1, TARGET_IMAGE_SIZE / cells)
    val image = BufferedImage(cells * scale, cells * scale, BufferedImage.TYPE_BYTE_BINARY)
    val raster = image.raster

    for (cy in 0..<cells)
        for (cx in 0..<cells) {
            val black = when {
                cx % 2 == 0 && cy % 2 == 0 -> sites[cx / 2, cy / 2] != 0
                cy % 2 == 0 -> horizontal[cy / 2][cx / 2] != 0
                cx % 2 == 0 -> vertical[cy / 2][cx / 2] != 0
                else -> false
            }

            // в палитре TYPE_BYTE_BINARY индекс 0 — чёрный, 1 — белый
            val sample = if (black) 0 else 1
            for (py in cy * scale..<(cy + 1) * scale)
                for (px in cx * scale..<(cx + 1) * scale)
                    raster.setSample(px, py, 0, sample)
        }

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

/** Путь по умолчанию для картинки: `out/bonds_L{L}_p{p}.png`. */
fun Bonds.defaultPathPNG(p: Double): Path =
    Path("out", "bonds_L${size}_p${p.format()}.png")
