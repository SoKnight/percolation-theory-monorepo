import cli.AppCommand
import stats.Experiment
import util.format
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeLines

/**
 * Для каждой концентрации от `pMin` до `pMax` проводит серию испытаний, печатает таблицу p и P(p)
 * и сохраняет её в CSV, который можно открыть в Origin.
 */
fun AppCommand.main() {
    // концентрации по номеру шага, а не прибавлением: иначе ошибки округления копятся, и последняя может выпасть
    val count = ((pMax - pMin) / step + 1e-9).toInt()
    val concentrations = (0..count).map { pMin + it * step }
    val experiment = Experiment(size, trials)

    val start = System.nanoTime()
    echo("Решётка $size x $size, испытаний на концентрацию: $trials")
    echo()
    echo("%-9s %s".format("p", "P(p)"))

    val points = concentrations.map { p ->
        experiment.run(p).also {
            echo("%-9s %s".format(it.p.format(), it.probability.format()))
        }
    }

    val elapsed = (System.nanoTime() - start) / 1e9

    val csv = Path("out", "probability_L$size.csv").createParentDirectories()
    csv.writeLines(listOf("p,P") + points.map { "${it.p.format()},${it.probability.format()}" })

    echo()
    echo("Таблица сохранена в: $csv")
    echo("Время расчёта: ${"%.2f".format(Locale.ROOT, elapsed)} с")
}
