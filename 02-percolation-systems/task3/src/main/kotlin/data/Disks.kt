package data

import kotlin.math.PI

/**
 * Непересекающиеся круги радиуса [radius] в квадрате [size] x [size], центр круга `i` — `(xs[i], ys[i])`.
 *
 * @property target сколько кругов нужно было разместить
 * @property attempts сколько всего было бросков, удачных и неудачных
 */
class Disks(
    val size: Double,
    val radius: Double,
    val boundary: Boundary,
    val target: Int,
    val xs: DoubleArray,
    val ys: DoubleArray,
    val attempts: Long,
) {

    /** Число размещённых кругов. */
    val count: Int
        get() = xs.size

    /** Доля площади квадрата, занятая кругами: `N * pi * r² / L²`. */
    val concentration: Double
        get() = count * PI * radius * radius / (size * size)

    /**
     * Проверка перебором всех пар за O(N²), независимо от сетки ячеек генератора:
     * первая пара пересекающихся кругов или `null`, если таких нет.
     */
    fun findOverlap(): Pair<Int, Int>? {
        val minDistance = 2 * radius

        for (i in 0..<count) {
            for (j in i + 1..<count) {
                val dx = boundary.delta(xs[i], xs[j], size)
                val dy = boundary.delta(ys[i], ys[j], size)

                if (dx * dx + dy * dy < minDistance * minDistance) {
                    return i to j
                }
            }
        }

        return null
    }

    /** Номер первого круга, чей центр вне допустимой области, или `null`. */
    fun findOutside(): Int? {
        val centers = boundary.centers(size, radius)
        return (0..<count).firstOrNull { xs[it] !in centers || ys[it] !in centers }
    }

}
