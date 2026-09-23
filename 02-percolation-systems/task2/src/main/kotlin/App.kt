import cli.AppCommand
import data.Generator
import util.format
import util.writeAsPNG
import java.util.*

private const val MAX_PRINT_SIZE = 50

/**
 * Проводит заданное число испытаний задачи узлов и связей и печатает статистику концентраций по ним.
 */
fun AppCommand.main() {
    val generator = seed?.let { Generator(SplittableRandom(it)) } ?: Generator()

    val siteStats = DoubleSummaryStatistics()
    val bondStats = DoubleSummaryStatistics()
    val totalBondStats = DoubleSummaryStatistics()

    repeat(trials) { trial ->
        val sites = generator.generate(size, pSite)
        val bonds = generator.generateBonds(sites, pBond)

        siteStats.accept(sites.concentration)
        bondStats.accept(bonds.availableConcentration)
        totalBondStats.accept(bonds.concentration)

        val printed = size <= MAX_PRINT_SIZE && !quiet

        if (printed) {
            echo("Испытание ${trial + 1}: занято ${sites.occupied} узлов из ${size * size}, концентрация ${sites.concentration.format()}")
            echo("Рёбер с обоими занятыми концами: ${bonds.available} из ${bonds.total}")
            echo("Занято ${bonds.occupied} связей, концентрация ${bonds.availableConcentration.format()} от возможных, ${bonds.concentration.format()} от всех рёбер")
            echo(bonds.render(sites))
            echo()
            echo("Узлы:")
            echo(sites.render())
            echo()
            echo("Горизонтальные связи, (x, y) — (x+1, y):")
            echo(bonds.horizontal.render())
            echo()
            echo("Вертикальные связи, (x, y) — (x, y+1):")
            echo(bonds.vertical.render())
            echo()
        }

        // картинками сохраняются напечатанные испытания, чтобы сверять с ASCII, а если печати нет — только первое
        if (png && (printed || trial == 0)) {
            val path = bonds.writeAsPNG(sites, pSite, pBond, trial + 1)
            echo("Решётка сохранена в: $path")
        }
    }

    echo("Решётка $size x $size, заданные концентрации узлов ${pSite.format()} и связей ${pBond.format()}, испытаний: $trials")
    echo("Концентрация узлов: ${siteStats.render()}")
    echo("Концентрация связей от возможных рёбер: ${bondStats.render()}")
    echo("Концентрация связей от всех рёбер: ${totalBondStats.render()}")
}

/** Средняя, минимальная и максимальная концентрация в одну строку. */
private fun DoubleSummaryStatistics.render(): String =
    "средняя ${average.format()}, минимальная ${min.format()}, максимальная ${max.format()}"

/** Матрица из `0` и `1` через пробел, для вывода в консоль. */
private fun Array<IntArray>.render(): String =
    joinToString("\n") { it.joinToString(" ") }
