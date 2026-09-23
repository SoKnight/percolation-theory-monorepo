package util

import data.Lattice
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories

/**
 * Сохраняет решётку в PNG: один узел — один пиксель, занятый чёрный, свободный белый.
 */
fun Lattice.writeAsPNG(p: Double, path: Path = defaultPathPNG(p)): Path {
    val image = BufferedImage(size, size, BufferedImage.TYPE_BYTE_BINARY)
    val raster = image.raster

    // в палитре TYPE_BYTE_BINARY индекс 0 — чёрный, 1 — белый
    for (y in 0..<size)
        for (x in 0..<size)
            raster.setSample(x, y, 0, if (this[x, y] != 0) 0 else 1)

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

/** Путь по умолчанию для картинки: `out/lattice_L{L}_p{p}.png`. */
fun Lattice.defaultPathPNG(p: Double): Path =
    Path("out", "lattice_L${size}_p${p.format()}.png")
