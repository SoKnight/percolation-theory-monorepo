package util

import data.Bonds
import data.Lattice
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories

// сторона картинки, до которой растягиваются маленькие решётки
private const val TARGET_IMAGE_SIZE = 1000

private val FREE_SITE_COLOR = Color(0xC8C8C8)

/**
 * Сохраняет решётку со связями в PNG: узлы — кружки с шагом около `1000 / L` пикселей, связи — отрезки между ними.
 * Занятый узел и связь чёрные, свободный узел светло-серый.
 */
fun Bonds.writeAsPNG(sites: Lattice, p: Double, trial: Int, path: Path = defaultPathPNG(p, trial)): Path {
    val step = maxOf(2, TARGET_IMAGE_SIZE / size)
    val imageSize = size * step
    val diameter = maxOf(1, step * 2 / 5)
    val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB)

    // центр узла с номером i по одной оси: узлы в серединах клеток step x step
    fun center(i: Int) = i * step + step / 2

    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, imageSize, imageSize)

    graphics.color = Color.BLACK
    graphics.stroke = BasicStroke(maxOf(1f, step / 8f))
    for (y in 0..<size)
        for (x in 0..<size) {
            if (x < size - 1 && horizontal[y][x] != 0)
                graphics.drawLine(center(x), center(y), center(x + 1), center(y))
            if (y < size - 1 && vertical[y][x] != 0)
                graphics.drawLine(center(x), center(y), center(x), center(y + 1))
        }

    for (y in 0..<size)
        for (x in 0..<size) {
            graphics.color = if (sites[x, y] != 0) Color.BLACK else FREE_SITE_COLOR
            graphics.fillOval(center(x) - diameter / 2, center(y) - diameter / 2, diameter, diameter)
        }

    graphics.dispose()
    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

/** Путь по умолчанию для картинки: `out/bonds_L{L}_p{p}_t{испытание}.png`. */
fun Bonds.defaultPathPNG(p: Double, trial: Int): Path =
    Path("out", "bonds_L${size}_p${p.format()}_t$trial.png")
