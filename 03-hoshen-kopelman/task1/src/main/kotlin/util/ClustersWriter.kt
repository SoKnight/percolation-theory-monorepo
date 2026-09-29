package util

import data.Clusters
import data.Lattice
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createParentDirectories

private const val TARGET_IMAGE_SIZE = 1000

/** Заголовок блока с метками: по нему задание 2 находит матрицу в файле. */
const val LABELS_HEADER = "Метки кластеров:"

/**
 * Сохраняет результат в текстовый файл: исходную решётку, матрицу меток и распределение кластеров по размерам.
 * Числа в матрице меток выровнены по ширине самой длинной метки.
 */
fun Clusters.writeAsText(lattice: Lattice, path: Path = Path("out", "clusters.txt")): Path {
    val width = count.toString().length

    path.createParentDirectories().bufferedWriter().use { writer ->
        writer.appendLine("Решётка ${lattice.size} x ${lattice.size}, занято узлов: ${lattice.occupied}, кластеров: $count")
        writer.appendLine()
        writer.appendLine("Исходная решётка:")
        writer.appendLine(lattice.render())
        writer.appendLine()
        writer.appendLine(LABELS_HEADER)

        for (row in labels)
            writer.appendLine(row.joinToString(" ") { it.toString().padStart(width) })

        writer.appendLine()
        writer.appendLine("Распределение кластеров по размерам:")
        writer.appendLine("размер  число кластеров")

        for ((size, number) in distribution) {
            writer.appendLine("%6d  %15d".format(size, number))
        }
    }

    return path
}

/**
 * Сохраняет кластеры в PNG: узел — квадрат со стороной около `1000 / L` пикселей, свободный белый,
 * а каждый кластер своим случайным цветом, одинаковым от запуска к запуску.
 */
fun Clusters.writeAsPNG(path: Path = Path("out", "clusters.png")): Path {
    val size = labels.size
    val scale = maxOf(1, TARGET_IMAGE_SIZE / size)
    val image = BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_RGB)

    // яркость ограничена сверху, чтобы кластер не сливался с белым фоном
    val colors = IntArray(count + 1) { label ->
        if (label == 0) 0xFFFFFF else Color.HSBtoRGB(label * 0.618034f, 0.75f, 0.85f)
    }

    for (y in 0..<size * scale)
        for (x in 0..<size * scale)
            image.setRGB(x, y, colors[labels[y / scale][x / scale]])

    ImageIO.write(image, "png", path.createParentDirectories().toFile())
    return path
}
