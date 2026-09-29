package util

import java.nio.file.Path
import kotlin.io.path.readLines

// заголовок блока с метками в файле задания 1
private const val LABELS_HEADER = "Метки кластеров:"

/**
 * Читает матрицу меток кластеров из файла задания 1: строки после заголовка [LABELS_HEADER] до первой пустой,
 * по строке на ряд узлов, метки через пробелы. `0` — свободный узел.
 */
fun readLabels(path: Path): Array<IntArray> {
    val lines = path.readLines()
    val start = lines.indexOf(LABELS_HEADER)
    require(start >= 0) { "нет строки «$LABELS_HEADER»" }

    val rows = lines.drop(start + 1)
        .takeWhile { it.isNotBlank() }
        .map { line -> line.trim().split(Regex("\\s+")) }

    require(rows.isNotEmpty()) { "после строки «$LABELS_HEADER» нет меток" }

    return Array(rows.size) { y ->
        require(rows[y].size == rows.size) {
            "матрица меток должна быть квадратной: в строке ${y + 1} ${rows[y].size} значений, а строк ${rows.size}"
        }

        IntArray(rows.size) { x ->
            rows[y][x].toIntOrNull()?.takeIf { it >= 0 }
                ?: throw IllegalArgumentException("в строке ${y + 1} метка «${rows[y][x]}» не целое неотрицательное число")
        }
    }
}
