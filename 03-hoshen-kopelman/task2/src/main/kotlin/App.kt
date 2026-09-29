import cli.AppCommand
import com.github.ajalt.clikt.core.UsageError
import util.readLabels
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createParentDirectories

/**
 * Читает метки кластеров, ищет перколяционные кластеры и сохраняет ответ в `out/percolation.txt`.
 */
fun AppCommand.main() {
    val labels = try {
        readLabels(input)
    } catch (ex: IllegalArgumentException) {
        throw UsageError("Не удалось прочитать метки из $input: ${ex.message}")
    }

    val percolating = findPercolating(labels)

    echo("Решётка ${labels.size} x ${labels.size}, кластеров: ${labels.maxOf { it.max() }}")
    echo("Перколяционный кластер: ${if (percolating.isEmpty()) 0 else 1}")

    for ((label, size) in percolating)
        echo("Номер: $label, размер: $size")

    echo("Результат сохранён в: ${writeResult(percolating)}")
}

/**
 * Перколяционные кластеры — те, что касаются и верхнего, и нижнего края решётки.
 * Обычно такой кластер один, но на маленьких решётках их может быть несколько, например два столбца через один.
 *
 * @return номер кластера → его размер, по возрастанию номера
 */
private fun findPercolating(labels: Array<IntArray>): Map<Int, Int> {
    val percolating = (labels.first().toSet() intersect labels.last().toSet()) - 0
    val sizes = sortedMapOf<Int, Int>()

    for (row in labels)
        for (label in row)
            if (label in percolating)
                sizes.merge(label, 1, Int::plus)

    return sizes
}

/** Сохраняет ответ: `1` и номер и размер каждого перколяционного кластера или `0`, если их нет. */
private fun writeResult(percolating: Map<Int, Int>, path: Path = Path("out", "percolation.txt")): Path {
    path.createParentDirectories().bufferedWriter().use { writer ->
        writer.appendLine("Перколяционный кластер: ${if (percolating.isEmpty()) 0 else 1}")

        for ((label, size) in percolating) {
            writer.appendLine("Номер: $label, размер: $size")
        }
    }

    return path
}
