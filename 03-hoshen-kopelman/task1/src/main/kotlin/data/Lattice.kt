package data

/**
 * Простая квадратная решётка L x L для задачи узлов.
 *
 * Узел `(x, y)` хранится в `cells[y][x]`, где `0` — свободен, а `1` — занят.
 *
 * @property cells строки решётки, все длины L
 */
@JvmInline
value class Lattice private constructor(val cells: Array<IntArray>) {

    /** Пустая решётка [size] x [size], все узлы свободны. */
    constructor(size: Int) : this(Array(size) { IntArray(size) })

    /** Текущая концентрация: доля занятых узлов, от `0` до `1`. */
    val concentration: Double
        get() = (occupied.toDouble() / size) / size

    /** Число занятых узлов. Value class ничего больше не хранит, поэтому каждый раз это проход по всей решётке. */
    val occupied: Int
        // != 0, а не сумма: по требованию массив должен состоять из целых чисел
        get() = cells.sumOf { row -> row.count { it != 0 } }

    /** Линейный размер решётки L. */
    val size: Int
        get() = cells.size

    /** Значение узла `(x, y)`. */
    operator fun get(x: Int, y: Int): Int =
        cells[y][x]

    /** Записывает [value] в узел `(x, y)`. */
    operator fun set(x: Int, y: Int, value: Int) {
        cells[y][x] = value
    }

    /** Решётка в виде строк из `0` и `1` через пробел, для вывода в консоль. */
    fun render(): String =
        cells.joinToString("\n") { it.joinToString(" ") }

}
