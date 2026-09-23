package util

import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomHLine
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomVLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.theme
import stats.Series
import stats.Statistics
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Сохраняет в [dir] графики по сериям [results] на решётке [size] x [size] и возвращает пути к файлам:
 * x² от p без поправки и с поправкой, долю прошедших критерий и гистограмму x² при `p = 0.5`.
 */
fun writePlots(results: List<Series>, size: Int, dir: Path = Path("out")): List<Path> {
    val trials = results.first().chiSquared.size
    val plots = mutableMapOf(
        "chi2_L$size.html" to chiSquaredPlot(results, "x² без поправки, L = $size, K = $trials") { it.raw },
        "chi2_corrected_L$size.html" to chiSquaredPlot(results, "x² / (1-p), L = $size, K = $trials") { it.corrected },
        "passed_L$size.html" to passedPlot(results, "Доля испытаний, прошедших критерий, L = $size, K = $trials"),
    )

    results.find { it.p == 0.5 }?.let {
        plots["chi2_histogram_L${size}_p0.5.html"] = histogram(it, "x² по $trials испытаниям, L = $size, p = 0.5")
    }

    return plots.map { (name, plot) -> Path(ggsave(plot, name, path = dir.toString())) }
}

private fun chiSquaredPlot(results: List<Series>, title: String, statistics: (Series) -> Statistics): Plot {
    val lines = listOf<Pair<String, (Series) -> Double>>(
        "одно испытание" to { statistics(it).first },
        "среднее по серии" to { statistics(it).mean },
        "суммарные счётчики" to { statistics(it).total },
        "критическое значение" to { it.uniformity.critical },
    )

    return linesPlot(results, lines) +
        labs(title = title, x = "p", y = "x²", color = "") +
        theme().legendPositionBottom()
}

private fun passedPlot(results: List<Series>, title: String): Plot {
    val lines = listOf<Pair<String, (Series) -> Double>>(
        "без поправки" to { it.raw.passed },
        "с поправкой" to { it.corrected.passed },
    )

    return linesPlot(results, lines) +
        geomHLine(yintercept = 0.95, linetype = "dashed", color = "gray") +
        // подпись в подзаголовке, а не у линии: у линии она наезжает на точки
        labs(title = title, subtitle = "пунктир: ожидается 0,95", x = "p", y = "доля прошедших", color = "") +
        theme().legendPositionBottom()
}

private fun histogram(series: Series, title: String): Plot {
    val uniformity = series.uniformity
    val lines = mapOf(
        "x" to listOf(uniformity.critical, uniformity.df.toDouble()),
        "line" to listOf("критическое значение", "df = ${uniformity.df}"),
    )

    return letsPlot(mapOf("chi2" to series.chiSquared.toList())) +
        geomHistogram(bins = 20, fill = "steelblue", color = "white") { x = "chi2" } +
        geomVLine(data = lines, size = 1.2) { xintercept = "x"; color = "line" } +
        labs(title = title, x = "x² без поправки", y = "число испытаний", color = "") +
        theme().legendPositionBottom() +
        ggsize(800, 500)
}

// линии в «длинном» формате: у каждой точки p, значение и название линии для легенды
private fun linesPlot(results: List<Series>, lines: List<Pair<String, (Series) -> Double>>): Plot {
    val data = mapOf(
        "p" to lines.flatMap { results.map { it.p } },
        "value" to lines.flatMap { (_, value) -> results.map(value) },
        "line" to lines.flatMap { (name, _) -> results.map { name } },
    )

    return letsPlot(data) { x = "p"; y = "value"; color = "line" } +
        geomLine() +
        geomPoint() +
        ggsize(800, 500)
}
