import cli.AppCommand
import stats.Experiment
import stats.Series
import util.*
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeLines

private val CONCENTRATIONS = (1..9).map { it / 10.0 }

private val HEADERS = listOf("p", "k", "m", "df", "x²крит", "x²(1)", "<x²>", "доля", "x²сумм", "x²(1)", "<x²>", "доля", "x²сумм")
private val WIDTHS = listOf(4, 3, 4, 4, 8, 8, 8, 5, 8, 8, 8, 5, 8)

private val CSV_HEADERS = listOf(
    "p", "k", "m", "df", "critical",
    "chi2_first", "chi2_mean", "passed", "chi2_total",
    "chi2_first_corrected", "chi2_mean_corrected", "passed_corrected", "chi2_total_corrected",
    "mean_concentration",
)

/**
 * Проводит серии испытаний для всех концентраций, печатает таблицу, сохраняет CSV и, если нужно, картинки решёток и графики.
 */
fun AppCommand.main() {
    // решётки нужны только для картинок, а при больших L каждая занимает сотни МБ памяти
    val experiment = Experiment(size, trials, parallel, seed, keepSample = lattices)

    val start = System.nanoTime()
    val results = CONCENTRATIONS.map(experiment::run)
    val elapsed = (System.nanoTime() - start) / 1e9

    val mode = if (parallel) "параллельно" else "в одном потоке"
    echo("Решётка $size x $size, испытаний на концентрацию: $trials, alpha = ${results[0].uniformity.alpha.format()}, $mode")
    echo()
    echo(" ".repeat(WIDTHS.take(5).sum() + 5) + "без поправки".padEnd(WIDTHS.slice(5..8).sum() + 4) + "с поправкой x²/(1-p)")
    echo(HEADERS.zip(WIDTHS) { header, width -> header.padStart(width) }.joinToString(" "))
    for (series in results)
        echo(series.values().zip(WIDTHS) { value, width -> value.cell().padStart(width) }.joinToString(" "))

    val csv = Path("out", "uniformity_L$size.csv").createParentDirectories()
    csv.writeLines(listOf(CSV_HEADERS.joinToString(",")) + results.map { series ->
        (series.values() + series.concentration).joinToString(",") { if (it is Double) it.format() else it.toString() }
    })
    echo()
    echo("Таблица сохранена в: $csv")

    if (lattices) {
        for (series in results) {
            val lattice = series.sample!!
            lattice.writeAsPNG(lattice.defaultPathPNG(series.p))
            lattice.writeWithGridAsPNG(
                grid = series.uniformity.grid,
                counts = series.uniformity.count(lattice),
                path = Path("out", "grid_L${size}_p${series.p.format()}.png"),
            )
        }

        echo("Решётки первых испытаний сохранены в: out/lattice_L${size}_p*.png, out/grid_L${size}_p*.png")
    }

    if (plots) {
        for (path in writePlots(results, size))
            echo("График сохранён в: $path")
    }

    echo("Время расчёта: ${"%.2f".format(Locale.ROOT, elapsed)} с")
}

private fun Series.values(): List<Number> = listOf(
    p, uniformity.grid.k, uniformity.grid.m, uniformity.df, uniformity.critical,
    raw.first, raw.mean, raw.passed, raw.total,
    corrected.first, corrected.mean, corrected.passed, corrected.total,
)

private fun Number.cell(): String =
    if (this is Double) "%.2f".format(Locale.ROOT, this) else toString()
