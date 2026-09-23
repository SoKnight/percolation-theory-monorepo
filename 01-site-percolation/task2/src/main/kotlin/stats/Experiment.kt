package stats

import data.Generator
import data.Lattice
import java.util.*
import java.util.stream.IntStream
import kotlin.math.roundToInt

/**
 * Серии испытаний для оценки равномерности заполнения решётки [size] x [size] критерием Пирсона.
 *
 * @property trials число испытаний K в каждой серии
 * @property parallel проводить испытания серии параллельно
 * @property keepSample сохранять в [Series.sample] решётку первого испытания
 */
class Experiment(
    val size: Int,
    val trials: Int,
    val parallel: Boolean = true,
    seed: Long? = null,
    val keepSample: Boolean = false,
) {

    private val random = seed?.let(::SplittableRandom) ?: SplittableRandom()

    /** Проводит [trials] испытаний с концентрацией [p]. */
    fun run(p: Double): Series {
        // k по номинальному n, а не по фактическому: разбиение одно на всю серию, и счётчики можно складывать
        val uniformity = Uniformity(Grid(size, (p * size * size).roundToInt()))

        // SplittableRandom не потокобезопасен, поэтому генераторы раздаются заранее и последовательно
        // так при одном seed каждое испытание получает те же числа при любом порядке работы потоков
        val generators = List(trials) { Generator(random.split()) }

        val results = IntStream.range(0, trials)
            .let { if (parallel) it.parallel() else it }
            .mapToObj { trial ->
                val lattice = generators[trial].generate(size, p)
                // решётку оставляем только от первого испытания, иначе в памяти окажутся все K штук
                uniformity.count(lattice) to lattice.takeIf { keepSample && trial == 0 }
            }
            .toList()

        // счётчики именно складываются, а не усредняются: у среднего разброс в K раз меньше,
        // а ожидания те же, и критерий проходил бы всегда. У суммы ожидание и дисперсия растут в k раз вместе
        val total = LongArray(uniformity.grid.m)
        for ((counts, _) in results)
            for (i in counts.indices)
                total[i] += counts[i]

        return Series(
            p = p,
            uniformity = uniformity,
            chiSquared = DoubleArray(trials) { uniformity.chiSquared(results[it].first) },
            total = total,
            sample = results[0].second,
        )
    }

}

/**
 * Результат серии испытаний с концентрацией [p].
 *
 * @property chiSquared x² каждого испытания
 * @property total счётчики по интервалам, сложенные по всем испытаниям
 * @property sample решётка первого испытания, если она сохранялась ([Experiment.keepSample])
 */
class Series(
    val p: Double,
    val uniformity: Uniformity,
    val chiSquared: DoubleArray,
    val total: LongArray,
    val sample: Lattice?,
) {

    /** Средняя полученная концентрация по всем испытаниям. */
    val concentration: Double
        get() = total.sum().toDouble() / chiSquared.size / uniformity.grid.size / uniformity.grid.size

    /** Статистика без поправки. */
    val raw: Statistics = statistics(1.0)

    /**
     * Статистика с поправкой x² / (1-p).
     *
     * Узел занимается не более одного раза, и общее число занятых фиксировано. Поэтому число занятых в интервале
     * распределено с дисперсией Ei * (1-p), а не Ei, как предполагает критерий, и x² систематически занижена в (1-p) раз.
     */
    val corrected: Statistics = statistics(1 - p)

    private fun statistics(scale: Double) = Statistics(
        first = chiSquared[0] / scale,
        mean = chiSquared.average() / scale,
        passed = chiSquared.count { uniformity.passes(it / scale) }.toDouble() / chiSquared.size,
        total = uniformity.chiSquared(total, chiSquared.size) / scale,
    )

}

/**
 * Четыре величины x² одной серии.
 *
 * @property first x² первого испытания
 * @property mean среднее x² по испытаниям
 * @property passed доля испытаний, прошедших критерий
 * @property total x² по счётчикам, сложенным по всем испытаниям
 */
data class Statistics(val first: Double, val mean: Double, val passed: Double, val total: Double)
