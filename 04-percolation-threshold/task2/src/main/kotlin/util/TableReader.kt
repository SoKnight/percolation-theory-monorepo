package util

import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readLines

/**
 * Таблица из задания 1 для решётки [size] x [size].
 *
 * @property p полученные концентрации, по возрастанию
 * @property probability P(p) для каждой концентрации
 * @property error стандартная ошибка P(p)
 */
class Table(val size: Int, val p: DoubleArray, val probability: DoubleArray, val error: DoubleArray)

/** Читает таблицу `probability_L{L}.csv`: заголовок `p,P,dP` и по строке на концентрацию. Размер берётся из имени файла. */
fun readTable(path: Path): Table {
    val size = path.name.removePrefix("probability_L").removeSuffix(".csv").toIntOrNull()
    requireNotNull(size) { "размер L не найден в имени файла ${path.name}" }

    val rows = path.readLines()
        .drop(1)
        .filter { it.isNotBlank() }
        .mapIndexed { index, line ->
            val values = line.split(",").map { it.trim().toDoubleOrNull() }
            require(values.size == 3 && null !in values) { "в строке ${index + 2} должно быть три числа: «$line»" }
            values.requireNoNulls()
        }
        .sortedBy { it[0] }

    return Table(
        size = size,
        p = DoubleArray(rows.size) { rows[it][0] },
        probability = DoubleArray(rows.size) { rows[it][1] },
        error = DoubleArray(rows.size) { rows[it][2] },
    )
}
