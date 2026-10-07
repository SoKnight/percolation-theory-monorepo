package stats

import data.Generator
import data.findClusters
import java.util.*
import java.util.stream.IntStream
import kotlin.math.sqrt

/**
 * Испытания на решётке [size] x [size]: в каждом решётка заполняется заново, кластеры размечаются
 * алгоритмом Хошена-Копельмана, и проверяется, есть ли среди них перколяционный.
 *
 * @property trials число испытаний K на каждую концентрацию
 * @property parallel проводить испытания параллельно
 */
class Experiment(
    val size: Int,
    val trials: Int,
    val parallel: Boolean = true,
    seed: Long? = null,
) {

    private val random = seed?.let(::SplittableRandom) ?: SplittableRandom()

    /** Проводит [trials] испытаний с концентрацией [p]. */
    fun run(p: Double): Point {
        // SplittableRandom не потокобезопасен, поэтому генераторы раздаются заранее и последовательно
        // так при одном seed каждое испытание получает те же числа при любом порядке работы потоков
        val generators = List(trials) { Generator(random.split()) }

        val results = IntStream.range(0, trials)
            .let { if (parallel) it.parallel() else it }
            .mapToObj { trial ->
                val lattice = generators[trial].generate(size, p)
                lattice.concentration to lattice.findClusters().percolates
            }
            .toList()

        return Point(
            p = results.sumOf { it.first } / trials,
            probability = results.count { it.second }.toDouble() / trials,
            trials = trials,
        )
    }

}

/**
 * Результат испытаний с одной заданной концентрацией.
 *
 * @property p полученная концентрация, средняя по испытаниям
 * @property probability P(p): доля испытаний, в которых возник перколяционный кластер
 * @property trials число испытаний K
 */
class Point(val p: Double, val probability: Double, val trials: Int) {

    /** Стандартная ошибка P(p): у доли из K независимых испытаний она `sqrt(P(1-P) / K)`. */
    val error: Double
        get() = sqrt(probability * (1 - probability) / trials)

}
