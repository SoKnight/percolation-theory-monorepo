package data

/**
 * Связи простой квадратной решётки L x L между соседними узлами, `0` — связи нет, `1` — есть.
 *
 * - `horizontal[y][x]`, L строк по L-1: связь между узлами `(x, y)` и `(x+1, y)`;
 * - `vertical[y][x]`, L-1 строк по L: связь между узлами `(x, y)` и `(x, y+1)`.
 *
 * Для розыгрыша все рёбра пронумерованы подряд: сначала горизонтальные построчно, затем вертикальные.
 */
class Bonds(val size: Int) {

    val horizontal: Array<IntArray> = Array(size) { IntArray(size - 1) }
    val vertical: Array<IntArray> = Array(size - 1) { IntArray(size) }

    /** Число горизонтальных рёбер, с него начинаются номера вертикальных. */
    private val horizontalCount: Int = size * (size - 1)

    /** Всего рёбер в решётке: `2L(L-1)`. */
    val total: Int = 2 * horizontalCount

    /** Число занятых связей. */
    val occupied: Int
        get() {
            val horizontalSum = horizontal.sumOf { row -> row.count { it != 0 } }
            val verticalSum = vertical.sumOf { row -> row.count { it != 0 } }
            return horizontalSum + verticalSum
        }

    /** Доля занятых связей от всех рёбер. */
    val concentration: Double
        get() = occupied.toDouble() / total

    /** Значение ребра с номером [edge]. */
    operator fun get(edge: Int): Int =
        if (edge < horizontalCount) {
            horizontal[edge / (size - 1)][edge % (size - 1)]
        } else {
            (edge - horizontalCount).let { vertical[it / size][it % size] }
        }

    /** Записывает [value] в ребро с номером [edge]. */
    operator fun set(edge: Int, value: Int) {
        if (edge < horizontalCount) {
            horizontal[edge / (size - 1)][edge % (size - 1)] = value
        } else {
            (edge - horizontalCount).let { vertical[it / size][it % size] = value }
        }
    }

    /** Номер ребра, соединяющего узел `(x, y)` с соседом справа (`right`) или снизу. */
    fun edgeOf(x: Int, y: Int, right: Boolean): Int =
        if (right) (y * (size - 1) + x) else (horizontalCount + y * size + x)

    /**
     * Решётка вместе со связями для вывода в консоль: занятый узел `o`, свободный `.`,
     * связь `-` или `|`, пустое ребро — пробел.
     */
    fun render(sites: Lattice): String = buildString {
        for (y in 0..<size) {
            for (x in 0..<size) {
                append(if (sites[x, y] != 0) 'o' else '.')
                if (x < size - 1) append(if (horizontal[y][x] != 0) '-' else ' ')
            }

            if (y < size - 1) {
                append('\n')
                append(vertical[y].joinToString(" ") { if (it != 0) "|" else " " }.trimEnd())
                append('\n')
            }
        }
    }

}
