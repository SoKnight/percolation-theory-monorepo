package stats

import data.Lattice
import kotlin.math.pow
import kotlin.math.sqrt

// квантиль уровня 0.95 стандартного нормального распределения, то есть для alpha = 0.05
private const val NORMAL_QUANTILE_95 = 1.6448536269514722

/**
 * Критерий Пирсона x² для проверки равномерности заполнения по интервалам [grid]
 * при уровне значимости [alpha].
 */
class Uniformity(val grid: Grid) {

    /** Уровень значимости, под него подобран [NORMAL_QUANTILE_95]. */
    val alpha: Double = 0.05

    /** Число степеней свободы `m - 1`. */
    val df: Int = grid.m - 1

    /**
     * Критическое значение x² для уровня значимости [alpha] по приближению Уилсона — Хилферти:
     * `(x² / df)^(1/3)` распределена почти нормально со средним `1 - 2 / (9 df)` и дисперсией `2 / (9 df)`.
     * Уже при df около 50 расходится с точным значением меньше чем на 0.01.
     */
    val critical: Double = (2.0 / (9.0 * df)).let { correction ->
        df * (1.0 - correction + NORMAL_QUANTILE_95 * sqrt(correction)).pow(3)
    }

    /** Число занятых узлов в каждом интервале. */
    fun count(lattice: Lattice): LongArray {
        val counts = LongArray(grid.m)

        for (y in 0..<lattice.size)
            for (x in 0..<lattice.size)
                if (lattice[x, y] != 0)
                    counts[grid.indexOf(x, y)]++

        return counts
    }

    /** x² для счётчиков, сложенных по [trials] испытаниям. */
    fun chiSquared(counts: LongArray, trials: Int = 1): Double =
        counts.indices.sumOf { i ->
            val expected = trials * grid.expected(i)
            (counts[i] - expected).pow(2) / expected
        }

    /** Проходит ли [chiSquared] критерий: гипотеза о равномерности не отвергается. */
    fun passes(chiSquared: Double): Boolean =
        chiSquared < critical

}
