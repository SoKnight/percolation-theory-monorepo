package data

import kotlin.math.abs
import kotlin.math.min

/**
 * Граничные условия квадрата L x L для кругов радиуса r.
 *
 * @property title описание для вывода в консоль
 */
enum class Boundary(val title: String) {

    /** Жёсткие стенки: круг целиком внутри квадрата, центр в `[r, L-r]`. */
    HARD        ("жёсткие стенки, круг целиком внутри квадрата"),

    /** Периодические: квадрат склеен в тор, круг у края продолжается с противоположной стороны. */
    PERIODIC    ("периодические, квадрат склеен в тор"),

    /** Свободные: центр внутри квадрата, круг может выступать за край. */
    FREE        ("свободные, круг может выступать за край");

    /** Наименьшая и наибольшая координата центра круга радиуса [radius] по одной оси. */
    fun centers(size: Double, radius: Double): ClosedFloatingPointRange<Double> =
        if (this == HARD) (radius..size - radius) else (0.0..size)

    /**
     * Разность координат двух центров по одной оси. Для [PERIODIC] — по минимальному образу:
     * из двух путей по тору, прямо или через край, берётся короткий.
     */
    fun delta(from: Double, to: Double, size: Double): Double {
        val delta = abs(to - from)
        return if (this == PERIODIC) min(delta, size - delta) else delta
    }

}
