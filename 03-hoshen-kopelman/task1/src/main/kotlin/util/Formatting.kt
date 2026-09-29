package util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

// Locale.ROOT, иначе в русской локали разделителем стала бы запятая
private val NUMBER_FORMAT = DecimalFormat("0.#####", DecimalFormatSymbols.getInstance(Locale.ROOT))

/** Число с точностью до 5 знаков после точки и без лишних нулей, например `0.5` или `0.30001`. */
fun Double.format(): String =
    NUMBER_FORMAT.format(this)
