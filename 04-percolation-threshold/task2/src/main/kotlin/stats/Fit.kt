package stats

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/** Критический показатель длины корреляции ν для двумерной перколяции. */
const val NU = 4.0 / 3

/** Подобранный параметр [value] и его стандартная ошибка [error]. */
data class Estimate(val value: Double, val error: Double)

/** Сигмоида из задания `P(p) = 1 / (1 + exp(-(p - pc) a))`, [pc] — порог перколяции, где `P = 0.5`. */
class Sigmoid(val pc: Estimate, val a: Estimate) {

    operator fun invoke(p: Double): Double =
        sigmoid(p, pc.value, a.value)

}

/** Прямая `y = intercept + slope x`. */
class Line(val intercept: Estimate, val slope: Estimate) {

    operator fun invoke(x: Double): Double =
        intercept.value + slope.value * x

}

/**
 * Подбирает сигмоиду по точкам `(x, y)` методом наименьших квадратов, алгоритмом Левенберга-Марквардта.
 * Точки должны идти по возрастанию x. Ошибки параметров считаются как в Origin:
 * из обратной матрицы `JᵀJ`, умноженной на остаточную дисперсию `SSR / (n - 2)`.
 */
fun fitSigmoid(x: DoubleArray, y: DoubleArray): Sigmoid {
    require(x.size > 2) { "нужно хотя бы 3 точки, а их ${x.size}" }
    require(y.any { it < 0.5 } && y.any { it >= 0.5 }) { "P(p) не переходит через 0.5, нужен шире диапазон концентраций" }

    // начальное приближение: порог в первой точке с P >= 0.5, а переход от 0.1 до 0.9 занимает
    // четверть диапазона. Переход сигмоиды от 0.1 до 0.9 длиной ln(81) / a ≈ 4.4 / a
    var pc = x[y.indexOfFirst { it >= 0.5 }]
    var a = 4.4 / ((x.last() - x.first()) / 4)
    var ssr = residuals(x, y, pc, a)
    var lambda = 1e-3

    // λ растёт, только когда шаг не уменьшает SSR, и до 1e12 он дорастает, лишь если в минимуме уже нечего улучшать
    var iterations = 0

    while (lambda <= 1e12 && iterations++ < 1000) {
        val (s00, s01, s11, g0, g1) = normalEquations(x, y, pc, a)

        // (JᵀJ + λ diag(JᵀJ)) δ = Jᵀr: при большом λ шаг идёт по градиенту, при малом как у Гаусса-Ньютона
        val m00 = s00 * (1 + lambda)
        val m11 = s11 * (1 + lambda)
        val det = m00 * m11 - s01 * s01
        val dpc = (g0 * m11 - g1 * s01) / det
        val da = (g1 * m00 - g0 * s01) / det

        val candidate = residuals(x, y, pc + dpc, a + da)

        if (candidate <= ssr) {
            pc += dpc
            a += da
            lambda /= 10

            val converged = ssr - candidate <= 1e-15 * ssr && abs(dpc) < 1e-12
            ssr = candidate

            if (converged)
                break
        } else {
            lambda *= 10
        }
    }

    return result(x, y, pc, a, ssr)
}

/**
 * Подбирает прямую по точкам `(x, y)` методом наименьших квадратов.
 * Ошибки параметров считаются только при трёх точках и больше, при двух прямая проходит через них точно и ошибки NaN.
 */
fun fitLine(x: DoubleArray, y: DoubleArray): Line {
    require(x.size >= 2) { "нужно хотя бы 2 точки, а их ${x.size}" }

    val n = x.size
    val meanX = x.average()
    val meanY = y.average()
    val sxx = x.sumOf { (it - meanX).pow(2) }
    val sxy = x.indices.sumOf { (x[it] - meanX) * (y[it] - meanY) }

    val slope = sxy / sxx
    val intercept = meanY - slope * meanX
    val variance = if (n > 2) x.indices.sumOf { (y[it] - intercept - slope * x[it]).pow(2) } / (n - 2) else Double.NaN

    return Line(
        intercept = Estimate(intercept, sqrt(variance * (1.0 / n + meanX * meanX / sxx))),
        slope = Estimate(slope, sqrt(variance / sxx)),
    )
}

private fun sigmoid(p: Double, pc: Double, a: Double): Double =
    1 / (1 + exp(-(p - pc) * a))

private fun residuals(x: DoubleArray, y: DoubleArray, pc: Double, a: Double): Double =
    x.indices.sumOf { (y[it] - sigmoid(x[it], pc, a)).pow(2) }

/**
 * Элементы `JᵀJ` (s00, s01, s11) и `Jᵀr` (g0, g1), где J — производные сигмоиды по pc и a, а r — остатки.
 * У сигмоиды `f' = f (1 - f)`, поэтому `∂f/∂pc = -a f (1 - f)` и `∂f/∂a = (p - pc) f (1 - f)`.
 */
private fun normalEquations(x: DoubleArray, y: DoubleArray, pc: Double, a: Double): DoubleArray {
    val sums = DoubleArray(5)

    for (i in x.indices) {
        val f = sigmoid(x[i], pc, a)
        val j0 = -a * f * (1 - f)
        val j1 = (x[i] - pc) * f * (1 - f)
        val r = y[i] - f

        sums[0] += j0 * j0
        sums[1] += j0 * j1
        sums[2] += j1 * j1
        sums[3] += j0 * r
        sums[4] += j1 * r
    }

    return sums
}

private fun result(x: DoubleArray, y: DoubleArray, pc: Double, a: Double, ssr: Double): Sigmoid {
    val (s00, s01, s11) = normalEquations(x, y, pc, a)
    val det = s00 * s11 - s01 * s01
    val variance = ssr / (x.size - 2)

    return Sigmoid(
        pc = Estimate(pc, sqrt(s11 / det * variance)),
        a = Estimate(a, sqrt(s00 / det * variance)),
    )
}
