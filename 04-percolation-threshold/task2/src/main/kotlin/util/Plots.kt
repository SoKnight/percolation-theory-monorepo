package util

import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomErrorBar
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import stats.Line
import stats.Sigmoid
import java.nio.file.Path
import kotlin.io.path.Path

private const val CURVE_POINTS = 200

/**
 * Сохраняет в [dir] графики и возвращает пути к файлам: P(p) с сигмоидой для каждой таблицы
 * и, если есть [line], p_c от `L^(-1/ν)` с прямой до `x = 0`.
 *
 * @param x `L^(-1/ν)` каждой таблицы
 */
fun writePlots(tables: List<Table>, fits: List<Sigmoid>, x: DoubleArray, line: Line?, dir: Path = Path("out")): List<Path> {
    val plots = tables.zip(fits).associate { (table, fit) -> "probability_L${table.size}.html" to sigmoidPlot(table, fit) }.toMutableMap()

    if (line != null)
        plots["scaling.html"] = scalingPlot(tables, fits, x, line)

    return plots.map { (name, plot) -> Path(ggsave(plot, name, path = dir.toString())) }
}

private fun sigmoidPlot(table: Table, fit: Sigmoid): Plot {
    val points = mapOf(
        "p" to table.p.toList(),
        "P" to table.probability.toList(),
        "low" to table.probability.indices.map { table.probability[it] - table.error[it] },
        "high" to table.probability.indices.map { table.probability[it] + table.error[it] },
    )

    val from = table.p.first()
    val step = (table.p.last() - from) / CURVE_POINTS
    val curve = (0..CURVE_POINTS).map { from + it * step }

    return pointsWithLine(points, "p", "P", mapOf("p" to curve, "P" to curve.map { fit(it) })) +
        labs(title = "L = ${table.size}, p_c = ${fit.pc.value.format()} ± ${fit.pc.error.format()}", x = "p", y = "P(p)")
}

private fun scalingPlot(tables: List<Table>, fits: List<Sigmoid>, x: DoubleArray, line: Line): Plot {
    val points = mapOf(
        "x" to x.toList(),
        "pc" to fits.map { it.pc.value },
        "low" to fits.map { it.pc.value - it.pc.error },
        "high" to fits.map { it.pc.value + it.pc.error },
    )

    // прямая от x = 0, то есть от бесконечной системы, до самой маленькой решётки
    val ends = listOf(0.0, x.max())
    val title = "p_c(∞) = ${line.intercept.value.format()}" +
        if (line.intercept.error.isFinite()) " ± ${line.intercept.error.format()}" else ""

    return pointsWithLine(points, "x", "pc", mapOf("x" to ends, "pc" to ends.map { line(it) })) +
        labs(title = title, subtitle = "L = ${tables.joinToString { it.size.toString() }}, ν = 4/3", x = "L^(-1/ν)", y = "p_c")
}

// точки с планками погрешностей от low до high и красная пунктирная линия поверх
private fun pointsWithLine(points: Map<String, List<Double>>, x: String, y: String, line: Map<String, List<Double>>): Plot =
    letsPlot(points) +
        geomLine(data = line, color = "red", linetype = "dashed") { this.x = x; this.y = y } +
        geomErrorBar(width = 0.5) { this.x = x; ymin = "low"; ymax = "high" } +
        geomPoint(shape = 21, size = 3, fill = "white") { this.x = x; this.y = y } +
        ggsize(800, 500)
