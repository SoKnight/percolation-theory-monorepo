package data

/**
 * Кластеры занятых узлов решётки: узлы одного кластера связаны цепочкой соседей слева, справа, сверху или снизу.
 *
 * @property labels метки узлов, `labels[y][x]`: `0` у свободного узла, у занятого номер его кластера от `1` до [count]
 * @property sizes размеры кластеров, `sizes[k - 1]` — число узлов в кластере `k`
 */
class Clusters(val labels: Array<IntArray>, val sizes: IntArray) {

    /** Число кластеров. */
    val count: Int
        get() = sizes.size

    /** Распределение кластеров по размерам: размер → сколько кластеров такого размера, по возрастанию размера. */
    val distribution: Map<Int, Int>
        get() = sizes.asIterable()
            .groupingBy { it }
            .eachCount()
            .toSortedMap()

}

/**
 * Размечает кластеры алгоритмом Хошена-Копельмана в два прохода по решётке:
 * первый расставляет метки и склеивает их в [LabelTable], второй заменяет метки на настоящие и нумерует кластеры подряд.
 */
fun Lattice.findClusters(): Clusters {
    // новых меток не больше, чем занятых узлов без занятых соседей
    // слева и сверху, то есть не больше половины узлов
    val table = LabelTable(size * size / 2 + 1)
    val labels = assignLabels(table)
    val sizes = renumber(labels, table)
    return Clusters(labels, sizes)
}

/**
 * Первый проход: узлы обходятся по строкам сверху вниз, в строке слева направо, и у каждого занятого узла смотрятся
 * только уже пройденные соседи: левый и верхний.
 * - Оба свободны: узел начинает новый кластер с новой меткой.
 * - Занят один: узел присоединяется к его кластеру.
 * - Заняты оба: если метки у них в разных кластерах, это один и тот же кластер, который до сих пор считался двумя,
 *   и кластеры склеиваются.
 *
 * Метки в матрице остаются такими, какими были выданы, и после склеек могут ссылаться на другие.
 */
private fun Lattice.assignLabels(table: LabelTable): Array<IntArray> {
    val labels = Array(size) { IntArray(size) }

    for (y in 0..<size) {
        for (x in 0..<size) {
            if (this[x, y] == 0)
                continue

            val left = if (x > 0) labels[y][x - 1] else 0
            val top = if (y > 0) labels[y - 1][x] else 0

            labels[y][x] = when {
                left == 0 && top == 0 -> table.create()
                left == 0 || top == 0 -> table.join(maxOf(left, top))
                else -> table.merge(left, top)
            }
        }
    }

    return labels
}

/**
 * Второй проход: каждая метка в [labels] заменяется на настоящую, и кластеры нумеруются подряд с `1`
 * в порядке, в котором встречаются при обходе.
 *
 * @return размеры кластеров по новым номерам, `sizes[k - 1]` — размер кластера `k`
 */
private fun renumber(labels: Array<IntArray>, table: LabelTable): IntArray {
    val renumbered = IntArray(table.last + 1)
    val sizes = ArrayList<Int>()

    for (row in labels) {
        for (x in row.indices) {
            if (row[x] == 0)
                continue

            val root = table.root(row[x])

            if (renumbered[root] == 0) {
                sizes += table.size(root)
                renumbered[root] = sizes.size
            }

            row[x] = renumbered[root]
        }
    }

    return sizes.toIntArray()
}

/**
 * Таблица меток Хошена-Копельмана, массив `n`: `n[k] > 0` — метка `k` настоящая и в её кластере `n[k]` узлов,
 * `n[k] < 0` — метка `k` склеена с меткой `-n[k]`. Настоящая метка находится переходом по ссылкам, пока `n` не станет положительным.
 *
 * @param capacity сколько меток может понадобиться
 */
private class LabelTable(capacity: Int) {

    // n[0] не используется, метки начинаются с 1
    private val n = IntArray(capacity + 1)

    /** Последняя выданная метка. */
    var last = 0
        private set

    /** Новая метка для кластера из одного узла. */
    fun create(): Int {
        this.n[++last] = 1
        return last
    }

    /** Добавляет узел к кластеру метки [label] и возвращает настоящую метку этого кластера. */
    fun join(label: Int): Int {
        val root = root(label)
        this.n[root]++
        return root
    }

    /**
     * Добавляет узел, соседний с метками [a] и [b], и возвращает настоящую метку его кластера.
     * Если метки в разных кластерах, узел их связал: кластеры склеиваются, остаётся меньшая настоящая метка,
     * а большая теперь ссылается на неё.
     */
    fun merge(a: Int, b: Int): Int {
        val rootA = root(a)
        val rootB = root(b)

        val min = minOf(rootA, rootB)
        val max = maxOf(rootA, rootB)

        if (min != max) {
            this.n[min] += n[max]
            this.n[max] = -min
        }

        this.n[min]++
        return min
    }

    /** Настоящая метка кластера, в который входит [label]. */
    fun root(label: Int): Int {
        var k = label

        while (n[k] < 0)
            k = -n[k]

        return k
    }

    /** Число узлов в кластере с настоящей меткой [root]. */
    fun size(root: Int): Int =
        n[root]

}
