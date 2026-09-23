package data

import java.util.*
import java.util.random.RandomGenerator
import kotlin.math.roundToInt

/**
 * Случайно и равномерно заполняет решётку до заданной концентрации.
 *
 * По умолчанию используется [SplittableRandom], а не привычный `java.util.Random`:
 * - он даёт более «честную» случайность: у `Random` соседние числа слегка связаны,
 *   а мы как раз берём их парами как координаты `(x, y)`;
 * - он быстрее: `Random` тратит время на защиту от одновременного доступа из разных потоков,
 *   а у нас поток один;
 * - через `split()` из него можно получить независимые генераторы для параллельных испытаний;
 * - ему можно задать seed, и тогда при повторном запуске решётки получатся те же самые
 *   (`ThreadLocalRandom` так не умеет).
 */
class Generator(private val random: RandomGenerator = SplittableRandom()) {

    /**
     * Создаёт решётку [size] x [size] и занимает в ней `round(p * L^2)` случайных свободных узлов.
     */
    fun generate(size: Int, p: Double): Lattice {
        require(p in 0.0..1.0) { "Концентрация должна быть в [0, 1]: $p" }

        val lattice = Lattice(size)
        val target = (p * size * size).roundToInt()
        var occupied = 0

        while (occupied < target) {
            val x = random.nextInt(size)
            val y = random.nextInt(size)

            if (lattice[x, y] == 0) {
                lattice[x, y] = 1
                occupied++
            }
        }

        return lattice
    }

    /**
     * Занимает `round(p * B)` случайных связей решётки [lattice], где B — число рёбер,
     * у которых оба конца заняты. Так же, как [generate]: бросается случайное ребро из этих B,
     * свободное занимается, занятое пропускается.
     */
    fun generateBonds(lattice: Lattice, p: Double): Bonds {
        require(p in 0.0..1.0) { "Концентрация должна быть в [0, 1]: $p" }

        val size = lattice.size
        val bonds = Bonds(size)

        // номера рёбер, у которых оба конца заняты, лежат в первых count ячейках
        val candidates = IntArray(bonds.total)
        var count = 0

        for (y in 0..<size) {
            for (x in 0..<size) {
                if (lattice[x, y] != 0) {
                    if (x < size - 1 && lattice[x + 1, y] != 0)
                        candidates[count++] = bonds.edgeOf(x, y, right = true)

                    if (y < size - 1 && lattice[x, y + 1] != 0) {
                        candidates[count++] = bonds.edgeOf(x, y, right = false)
                    }
                }
            }
        }

        val target = (p * count).roundToInt()
        var occupied = 0

        while (occupied < target) {
            val edge = candidates[random.nextInt(count)]

            if (bonds[edge] == 0) {
                bonds[edge] = 1
                occupied++
            }
        }

        return bonds
    }

}
