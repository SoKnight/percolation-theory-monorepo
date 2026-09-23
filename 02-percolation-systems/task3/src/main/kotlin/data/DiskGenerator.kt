package data

import java.util.*
import java.util.random.RandomGenerator
import kotlin.math.PI
import kotlin.math.roundToInt

/** Сколько неудачных бросков подряд допускается, прежде чем признать, что места для круга не осталось. */
const val MAX_FAILURES = 1_000_000

/**
 * Размещает непересекающиеся круги случайной последовательной адсорбцией (RSA): бросается случайный центр,
 * круг остаётся, если не пересекается ни с одним уже стоящим, иначе бросок повторяется.
 * Поставленные круги больше не двигаются.
 *
 * Генератор по умолчанию — [SplittableRandom], как в задачах на решётке.
 */
class DiskGenerator(private val random: RandomGenerator = SplittableRandom()) {

    /**
     * Размещает `round(p * L² / (pi * r²))` кругов, чтобы они заняли долю площади [p].
     *
     * Если после [MAX_FAILURES] неудачных бросков подряд очередной круг так и не встал,
     * размещение прекращается и возвращается то, что успело встать: RSA не заполняет плоскость
     * плотнее примерно 0.547.
     *
     * Чтобы не сравнивать каждый бросок со всеми кругами, квадрат делится на ячейки со стороной не меньше `2r`.
     * Пересечься новый круг может только с кругами своей ячейки и восьми соседних.
     */
    fun generate(size: Double, radius: Double, p: Double, boundary: Boundary): Disks {
        require(p in 0.0..1.0) { "Концентрация должна быть в [0, 1]: $p" }

        val target = (p * size * size / (PI * radius * radius)).roundToInt()
        val centers = boundary.centers(size, radius)

        val cells = maxOf(1, (size / (2 * radius)).toInt())
        val cellSize = size / cells
        val grid = Array(cells * cells) { mutableListOf<Int>() }

        fun cellOf(coordinate: Double): Int =
            minOf(cells - 1, (coordinate / cellSize).toInt())

        val xs = DoubleArray(target)
        val ys = DoubleArray(target)

        var count = 0
        var attempts = 0L
        var failures = 0

        // пересекается ли круг с центром (x, y) с уже стоящими
        fun overlaps(x: Double, y: Double): Boolean {
            val cx = cellOf(x)
            val cy = cellOf(y)

            for (ny in cy - 1..cy + 1)
                for (nx in cx - 1..cx + 1) {
                    val (gx, gy) = when {
                        boundary == Boundary.PERIODIC -> Math.floorMod(nx, cells) to Math.floorMod(ny, cells)
                        nx !in 0..<cells || ny !in 0..<cells -> continue
                        else -> nx to ny
                    }

                    for (i in grid[gy * cells + gx]) {
                        val dx = boundary.delta(xs[i], x, size)
                        val dy = boundary.delta(ys[i], y, size)

                        // квадрат расстояния до каждого круга и (2r)², без корня быстрее
                        if (dx * dx + dy * dy < 4 * radius * radius) {
                            return true
                        }
                    }
                }

            return false
        }

        while (count < target && failures < MAX_FAILURES) {
            val x = random.nextDouble(centers.start, centers.endInclusive)
            val y = random.nextDouble(centers.start, centers.endInclusive)
            attempts++

            if (overlaps(x, y)) {
                failures++
            } else {
                xs[count] = x
                ys[count] = y
                grid[cellOf(y) * cells + cellOf(x)] += count
                count++
                failures = 0
            }
        }

        return Disks(size, radius, boundary, target, xs.copyOf(count), ys.copyOf(count), attempts)
    }

}
