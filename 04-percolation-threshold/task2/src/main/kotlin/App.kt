import cli.AppCommand
import com.github.ajalt.clikt.core.UsageError
import stats.Estimate
import stats.NU
import stats.fitLine
import stats.fitSigmoid
import util.format
import util.readTable
import util.writePlots
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeLines
import kotlin.math.pow

/**
 * Аппроксимирует каждую таблицу P(p) сигмоидой и находит p_c(L), затем по скейлинговому соотношению
 * `p_c(L) = p_c(∞) + c L^(-1/ν)` находит p_c(∞) как свободный член прямой p_c от `L^(-1/ν)`.
 */
fun AppCommand.main() {
    val tables = input.listDirectoryEntries("probability_L*.csv")
        .map { path ->
            try {
                readTable(path)
            } catch (ex: IllegalArgumentException) {
                throw UsageError("Не удалось прочитать таблицу $path: ${ex.message}")
            }
        }
        .sortedBy { it.size }

    if (tables.isEmpty())
        throw UsageError("В $input нет таблиц probability_L*.csv, сначала запустите задание 1")

    val fits = tables.map { table ->
        try {
            fitSigmoid(table.p, table.probability)
        } catch (ex: IllegalArgumentException) {
            throw UsageError("L = ${table.size}: ${ex.message}")
        }
    }

    echo("Аппроксимация P(p) = 1 / (1 + exp(-(p - p_c) a)):")
    echo("%5s  %-20s  %s".format("L", "p_c", "a"))
    for ((table, fit) in tables.zip(fits))
        echo("%5d  %-20s  %s".format(table.size, fit.pc.format(), fit.a.format()))

    // скейлинг: x = L^(-1/ν), и при L → ∞ точка уходит в x = 0, где прямая и даёт p_c(∞)
    val x = tables.map { it.size.toDouble().pow(-1 / NU) }.toDoubleArray()
    val pc = fits.map { it.pc.value }.toDoubleArray()
    val line = if (tables.size >= 2) fitLine(x, pc) else null

    echo()
    if (line != null) {
        echo("Скейлинг p_c(L) = p_c(∞) + c L^(-1/ν), ν = 4/3:")
        echo("p_c(∞) = ${line.intercept.format()}, c = ${line.slope.format()}")
    } else {
        echo("Для p_c(∞) нужны таблицы хотя бы для двух размеров L")
    }

    val thresholds = Path("out", "thresholds.csv").createParentDirectories()
    thresholds.writeLines(listOf("L,L^(-1/nu),p_c,dp_c,a,da") + tables.indices.map {
        val fit = fits[it]
        listOf(tables[it].size, x[it].format(), fit.pc.value.format(), fit.pc.error.format(), fit.a.value.format(), fit.a.error.format())
            .joinToString(",")
    })

    echo()
    echo("Пороги сохранены в: $thresholds")

    if (plots) {
        for (path in writePlots(tables, fits, x, line))
            echo("График сохранён в: $path")
    }
}

/** Значение с ошибкой, например `0.59274 ± 0.00012`, или без неё, если ошибку не посчитать. */
private fun Estimate.format(): String =
    if (error.isFinite()) "${value.format()} ± ${error.format()}" else value.format()
