package stats

import data.Generator
import data.findClusters
import java.util.*
import java.util.stream.IntStream

/**
 * Испытания на решётке [size] x [size]: в каждом решётка заполняется заново, кластеры размечаются
 * алгоритмом Хошена-Копельмана, и проверяется, есть ли среди них перколяционный.
 *
 * @property trials число испытаний K на каждую концентрацию
 */
class Experiment(val size: Int, val trials: Int) {

    private val random = SplittableRandom()

    /** Проводит [trials] испытаний с концентрацией [p], испытания идут параллельно. */
    fun run(p: Double): Point {
        // SplittableRandom не потокобезопасен, поэтому у каждого испытания свой генератор
        val generators = List(trials) { Generator(random.split()) }

        val results = IntStream.range(0, trials)
            .parallel()
            .mapToObj { trial ->
                val lattice = generators[trial].generate(size, p)
                lattice.concentration to lattice.findClusters().percolates
            }
            .toList()

        return Point(
            p = results.sumOf { it.first } / trials,
            probability = results.count { it.second }.toDouble() / trials,
        )
    }

}

/**
 * Строка таблицы для одной заданной концентрации.
 *
 * @property p полученная концентрация, средняя по испытаниям
 * @property probability P(p): доля испытаний, в которых возник перколяционный кластер
 */
data class Point(val p: Double, val probability: Double)
