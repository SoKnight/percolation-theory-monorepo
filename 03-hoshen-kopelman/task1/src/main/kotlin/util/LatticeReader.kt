package util

import data.Lattice
import java.nio.file.Path
import kotlin.io.path.readLines

/**
 * Читает решётку из текстового файла: по строке на ряд узлов, значения `0` и `1` через пробел.
 * Пустые строки пропускаются. Решётка должна быть квадратной.
 */
fun readLattice(path: Path): Lattice {
    val rows = path.readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.trim().split(Regex("\\s+")) }

    return Lattice(rows.size).apply {
        for ((y, row) in rows.withIndex()) {
            require(row.size == rows.size) {
                "Решётка должна быть квадратной: в строке ${y + 1} ${row.size} значений, а строк ${rows.size}"
            }

            for ((x, value) in row.withIndex()) {
                require(value == "0" || value == "1") { "В строке ${y + 1} значение «$value», а допустимы только 0 и 1" }
                this[x, y] = value.toInt()
            }
        }
    }
}
