
import cli.AppCommand
import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.terminal.prompt
import data.Generator
import data.Lattice
import data.findClusters
import util.format
import util.readLattice
import util.writeAsPNG
import util.writeAsText
import java.util.SplittableRandom
import kotlin.collections.ArrayDeque

private const val MAX_PRINT_SIZE = 50

/**
 * Получает решётку из файла или генератора, размечает кластеры, проверяет разметку обходом в ширину
 * и сохраняет результат в текстовый файл, а при `--png` и картинку.
 */
fun AppCommand.main() {
    val lattice = input?.let { path ->
        if (size != null || p != null)
            throw UsageError("Решётка берётся либо из файла -i, либо генерируется по -L и -p, но не всё сразу")

        try {
            readLattice(path)
        } catch (ex: IllegalArgumentException) {
            throw UsageError("Не удалось прочитать решётку из $path: ${ex.message}")
        }
    } ?: generate()

    val startTime = System.nanoTime()
    val clusters = lattice.findClusters()
    val seconds = (System.nanoTime() - startTime) / 1e9

    echo("Решётка ${lattice.size} x ${lattice.size}, занято узлов: ${lattice.occupied}, концентрация ${lattice.concentration.format()}")

    if (lattice.size <= MAX_PRINT_SIZE && !quiet) {
        val width = clusters.count.toString().length

        echo()
        echo(lattice.render())
        echo()

        echo("Метки кластеров:")
        echo(clusters.labels.joinToString("\n") { row ->
            row.joinToString(" ") { it.toString().padStart(width) }
        })
    }

    echo()

    echo("Кластеров: ${clusters.count}")
    echo("Распределение по размерам (размер: число кластеров):")
    for ((size, number) in clusters.distribution)
        echo("%6d: %d".format(size, number))

    echo()

    if (clusters.labels.contentDeepEquals(lattice.floodFill())) {
        echo("Проверка обходом в ширину: разметка совпадает")
    } else {
        echo("Проверка обходом в ширину: разметка НЕ совпадает!")
    }

    echo("Результат сохранён в: ${clusters.writeAsText(lattice)}")

    if (png)
        echo("Картинка сохранена в: ${clusters.writeAsPNG()}")

    echo("Время разметки: ${seconds.format()} с")
}

/** Генерирует решётку как в работе 1, спрашивая у пользователя размер и концентрацию, если их не передали. */
private fun AppCommand.generate(): Lattice {
    val size = size ?: ask("Размер решётки L") { it.toIntOrNull()?.takeIf { it >= 1 } }
    val p = p ?: ask("Концентрация p") { it.toDoubleOrNull()?.takeIf { it in 0.0..1.0 } }
    val generator = seed?.let { Generator(SplittableRandom(it)) } ?: Generator()
    return generator.generate(size, p)
}

/**
 * Спрашивает значение, пока ответ не пройдёт [convert].
 * Clikt спрашивает опции сразу при разборе аргументов, а размер и концентрация нужны, только если файла нет, поэтому здесь вручную.
 */
private fun <T : Any> AppCommand.ask(question: String, convert: (String) -> T?): T {
    while (true) {
        val answer = terminal.prompt(question) ?: throw Abort()
        convert(answer.trim())?.let { return it }
        echo("Неверное значение: $answer", err = true)
    }
}

/**
 * Та же разметка другим способом, для проверки: от каждого ещё не размеченного занятого узла обходом в ширину
 * помечается весь его кластер. Узлы обходятся в том же порядке, что и у Хошена-Копельмана, поэтому номера кластеров совпадут.
 */
private fun Lattice.floodFill(): Array<IntArray> {
    val labels = Array(size) { IntArray(size) }
    val queue = ArrayDeque<Int>()
    var count = 0

    for (startY in 0..<size) {
        for (startX in 0..<size) {
            if (this[startX, startY] == 0 || labels[startY][startX] != 0)
                continue

            labels[startY][startX] = ++count
            queue += startY * size + startX

            while (queue.isNotEmpty()) {
                val cell = queue.removeFirst()
                val x = cell % size
                val y = cell / size

                for ((nx, ny) in listOf(x - 1 to y, x + 1 to y, x to y - 1, x to y + 1)) {
                    if (nx in 0..<size && ny in 0..<size && this[nx, ny] != 0 && labels[ny][nx] == 0) {
                        labels[ny][nx] = count
                        queue += ny * size + nx
                    }
                }
            }
        }
    }

    return labels
}
