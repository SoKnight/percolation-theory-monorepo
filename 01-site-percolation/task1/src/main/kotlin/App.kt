import cli.AppCommand
import data.Generator
import util.format
import util.writeAsPNG
import java.util.*

private const val MAX_PRINT_SIZE = 50

/**
 * Проводит заданное число испытаний и печатает статистику концентрации по ним.
 */
fun AppCommand.main() {
    val generator = seed?.let { Generator(SplittableRandom(it)) } ?: Generator()
    val stats = DoubleSummaryStatistics()

    repeat(trials) { trial ->
        val lattice = generator.generate(size, p)
        stats.accept(lattice.concentration)

        if (size <= MAX_PRINT_SIZE && !quiet) {
            echo("Испытание ${trial + 1}: занято ${lattice.occupied} из ${size * size}, концентрация ${lattice.concentration.format()}")
            echo(lattice.render())
            echo()
        }

        if (png && trial == 0) {
            val path = lattice.writeAsPNG(p = p)
            echo("Решётка сохранена в: $path")
        }
    }

    echo("Решётка ${size}×${size}, заданная концентрация ${p.format()}, испытаний: $trials")
    echo("Средняя концентрация: ${stats.average.format()}")
    echo("Минимальная: ${stats.min.format()}")
    echo("Максимальная: ${stats.max.format()}")
}
