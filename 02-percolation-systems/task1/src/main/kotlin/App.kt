import cli.AppCommand
import data.Generator
import data.Lattice
import util.format
import util.writeAsPNG
import java.util.*

private const val MAX_PRINT_SIZE = 50

/**
 * Проводит заданное число испытаний на полностью занятой решётке и печатает статистику концентрации связей по ним.
 */
fun AppCommand.main() {
    val generator = seed?.let { Generator(SplittableRandom(it)) } ?: Generator()
    val stats = DoubleSummaryStatistics()

    // в задаче связей все узлы заняты
    val sites = Lattice(size).apply { fill() }

    repeat(trials) { trial ->
        val bonds = generator.generateBonds(sites, p)
        stats.accept(bonds.concentration)

        if (size <= MAX_PRINT_SIZE && !quiet) {
            echo("Испытание ${trial + 1}: занято ${bonds.occupied} связей из ${bonds.total}, концентрация ${bonds.concentration.format()}")
            echo(bonds.render(sites))
            echo()
            echo("Горизонтальные связи, (x, y) — (x+1, y):")
            echo(bonds.horizontal.render())
            echo()
            echo("Вертикальные связи, (x, y) — (x, y+1):")
            echo(bonds.vertical.render())
            echo()
        }

        if (png && trial == 0) {
            val path = bonds.writeAsPNG(sites, p = p)
            echo("Решётка сохранена в: $path")
        }
    }

    echo("Решётка $size x $size, заданная концентрация связей ${p.format()}, испытаний: $trials")
    echo("Средняя концентрация: ${stats.average.format()}")
    echo("Минимальная: ${stats.min.format()}")
    echo("Максимальная: ${stats.max.format()}")
}

/** Матрица из `0` и `1` через пробел, для вывода в консоль. */
private fun Array<IntArray>.render(): String =
    joinToString("\n") { it.joinToString(" ") }
