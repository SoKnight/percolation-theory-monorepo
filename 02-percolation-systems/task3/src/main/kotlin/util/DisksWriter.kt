package util

import data.Boundary
import data.Disks
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createParentDirectories
import kotlin.math.ceil

// сторона квадрата на картинке в пикселях
private const val SQUARE_IMAGE_SIZE = 1000

private val SQUARE_COLOR = Color(0xF0EFEC)
private val DISK_COLOR = Color(0x2A78D6)
private val DISK_EDGE_COLOR = Color(0x184F95)

/**
 * Сохраняет круги в PNG: квадрат со стороной 1000 пикселей в рамке, круги залиты синим.
 * Вокруг квадрата поле шириной r, чтобы при [Boundary.FREE] было видно выступающие за край части.
 * При [Boundary.PERIODIC] круг у края дорисовывается и с противоположной стороны.
 */
fun Disks.writeAsPNG(p: Double, path: Path = defaultPath(p, "png")): Path {
    val scale = SQUARE_IMAGE_SIZE / size
    val margin = ceil(radius * scale).toInt() + 2
    val imageSize = SQUARE_IMAGE_SIZE + 2 * margin
    val square = Rectangle2D.Double(margin.toDouble(), margin.toDouble(), SQUARE_IMAGE_SIZE.toDouble(), SQUARE_IMAGE_SIZE.toDouble())
    val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB)

    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, imageSize, imageSize)
    graphics.color = SQUARE_COLOR
    graphics.fill(square)

    // на торе круг у края виден и с противоположной стороны: рисуются сдвиги на ±L, лишнее обрезается квадратом
    val shifts = if (boundary == Boundary.PERIODIC) listOf(-size, 0.0, size) else listOf(0.0)
    if (boundary == Boundary.PERIODIC)
        graphics.clip = square

    graphics.stroke = BasicStroke(1f)
    for (i in 0..<count)
        for (shiftY in shifts)
            for (shiftX in shifts) {
                val disk = Ellipse2D.Double(
                    margin + (xs[i] + shiftX - radius) * scale,
                    margin + (ys[i] + shiftY - radius) * scale,
                    2 * radius * scale,
                    2 * radius * scale,
                )
                graphics.color = DISK_COLOR
                graphics.fill(disk)
                graphics.color = DISK_EDGE_COLOR
                graphics.draw(disk)
            }

    graphics.clip = null
    graphics.color = Color.BLACK
    graphics.stroke = BasicStroke(2f)
    graphics.draw(square)
    graphics.dispose()

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

/** Сохраняет центры кругов в CSV со столбцами `x,y`. */
fun Disks.writeAsCSV(p: Double, path: Path = defaultPath(p, "csv")): Path {
    path.createParentDirectories().bufferedWriter().use { writer ->
        writer.appendLine("x,y")
        for (i in 0..<count)
            writer.appendLine("${xs[i].format()},${ys[i].format()}")
    }
    return path
}

/** Путь по умолчанию: `out/disks_L{L}_r{r}_p{p}_{граничные условия}.{extension}`. */
fun Disks.defaultPath(p: Double, extension: String): Path =
    Path("out", "disks_L${size.format()}_r${radius.format()}_p${p.format()}_${boundary.name.lowercase()}.$extension")
