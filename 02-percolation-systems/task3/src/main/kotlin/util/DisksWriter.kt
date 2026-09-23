package util

import data.Boundary
import data.Disks
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.Area
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
 * При [Boundary.PERIODIC] поле шириной 2r: круги дорисовываются со сдвигом на ±L, внутри квадрата непрозрачными,
 * а за его границей полупрозрачными, чтобы было видно, как квадрат продолжается на торе.
 */
fun Disks.writeAsPNG(p: Double, path: Path = defaultPath(p, "png")): Path {
    val scale = SQUARE_IMAGE_SIZE / size
    val margin = ceil((if (boundary == Boundary.PERIODIC) 2 else 1) * radius * scale).toInt() + 2
    val imageSize = SQUARE_IMAGE_SIZE + 2 * margin
    val square = Rectangle2D.Double(margin.toDouble(), margin.toDouble(), SQUARE_IMAGE_SIZE.toDouble(), SQUARE_IMAGE_SIZE.toDouble())
    val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB)

    image.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        color = Color.WHITE
        fillRect(0, 0, imageSize, imageSize)
        color = SQUARE_COLOR
        fill(square)

        // на торе круг у края виден и с противоположной стороны: рисуются сдвиги на ±L
        val shifts = if (boundary == Boundary.PERIODIC) listOf(-size, 0.0, size) else listOf(0.0)

        fun drawDisks() {
            for (i in 0..<count) {
                for (shiftY in shifts) {
                    for (shiftX in shifts) {
                        val disk = Ellipse2D.Double(
                            margin + (xs[i] + shiftX - radius) * scale,
                            margin + (ys[i] + shiftY - radius) * scale,
                            2 * radius * scale,
                            2 * radius * scale,
                        )

                        color = DISK_COLOR
                        fill(disk)
                        color = DISK_EDGE_COLOR
                        draw(disk)
                    }
                }
            }
        }

        stroke = BasicStroke(1f)

        if (boundary == Boundary.PERIODIC) {
            // сначала всё полупрозрачным, потом внутри квадрата поверх непрозрачным
            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            clip = Area(Rectangle2D.Double(0.0, 0.0, imageSize.toDouble(), imageSize.toDouble())).apply {
                subtract(Area(square))
            }

            drawDisks()
            composite = AlphaComposite.SrcOver
            clip = square
        }

        drawDisks()

        clip = null
        color = Color.BLACK
        stroke = BasicStroke(2f)
        draw(square)
    }.dispose()

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
