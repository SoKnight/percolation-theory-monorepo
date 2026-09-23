package util

import data.Lattice
import stats.Grid
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.math.abs
import kotlin.math.sqrt

/** Путь по умолчанию для картинки: `out/lattice_L{L}_p{p}.png`. */
fun Lattice.defaultPathPNG(p: Double): Path =
    Path("out", "lattice_L${size}_p${p.format()}.png")

/**
 * Сохраняет решётку в PNG: один узел — один пиксель, занятый чёрный, свободный белый.
 */
fun Lattice.writeAsPNG(path: Path): Path {
    val image = BufferedImage(size, size, BufferedImage.TYPE_BYTE_BINARY)
    val raster = image.raster

    // в палитре TYPE_BYTE_BINARY индекс 0 — чёрный, 1 — белый
    for (y in 0..<size)
        for (x in 0..<size)
            raster.setSample(x, y, 0, if (this[x, y] != 0) 0 else 1)

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

/**
 * Сохраняет решётку с разбиением [grid] в PNG: занятые узлы чёрные, в центре интервала подписано
 * число занятых узлов [counts], свободные узлы подкрашены по отклонению `(O - E) / sqrt(E)`:
 * синим, если занятых меньше ожидаемого, и красным, если больше.
 */
fun Lattice.writeWithGridAsPNG(grid: Grid, counts: LongArray, path: Path): Path {
    val tints = IntArray(grid.m) { tint(deviation = (counts[it] - grid.expected(it)) / sqrt(grid.expected(it))) }
    val pixels = IntArray(size * size) { i ->
        val x = i % size
        val y = i / size
        if (this[x, y] != 0) 0 else tints[grid.indexOf(x, y)]
    }

    // при маленьких L один узел — несколько пикселей, иначе подписи не влезут в интервалы
    val scale = maxOf(1, 1000 / size)
    val base = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    base.setRGB(0, 0, size, size, pixels, 0, size)

    val image = BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_RGB)
    image.createGraphics().apply {
        drawImage(base, 0, 0, image.width, image.height, null)
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        this.color = Color(0, 160, 0)
        this.stroke = BasicStroke(maxOf(3f, scale / 2f))

        for (j in 1..<grid.k) {
            val c = grid.startOf(j) * scale
            drawLine(c, 0, c, image.height)
            drawLine(0, c, image.width, c)
        }

        this.font = Font(Font.SANS_SERIF, Font.BOLD, maxOf(8, image.width / grid.k / 5))
        with(fontMetrics) {
            for (index in 0..<grid.m) {
                val text = counts[index].toString()
                val centerX = (grid.startOf(index % grid.k) + grid.startOf(index % grid.k + 1)) * scale / 2
                val centerY = (grid.startOf(index / grid.k) + grid.startOf(index / grid.k + 1)) * scale / 2
                val width = stringWidth(text)

                color = Color(255, 255, 255, 220)
                fillRect(centerX - width / 2 - 3, centerY - height / 2, width + 6, height)

                color = Color.BLACK
                drawString(text, centerX - width / 2, centerY - height / 2 + ascent)
            }
        }
    }.dispose()

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}

// белый при нулевом отклонении, насыщенный синий или красный при |отклонении| >= 3
private fun tint(deviation: Double): Int {
    val t = minOf(abs(deviation) / 3, 1.0)
    val target = if (deviation < 0) Color(40, 90, 230) else Color(230, 40, 40)
    fun mix(channel: Int) = (255 + (channel - 255) * t).toInt()
    return Color(mix(target.red), mix(target.green), mix(target.blue)).rgb
}
