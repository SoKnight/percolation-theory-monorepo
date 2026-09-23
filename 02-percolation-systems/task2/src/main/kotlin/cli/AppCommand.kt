package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import main

/**
 * Размер `-L`, концентрации узлов `-s` и связей `-b` и число испытаний `-n` можно передать при запуске,
 * а если какой-то из них не указан, команда спросит его у пользователя.
 * Например, `-L 6 -s 0.7 -b 0.5 -n 5 --seed=1` или просто запуск без аргументов.
 *
 * Выводит решётку со связями, матрицы узлов и связей каждого испытания (только при `L <= 50` и без `--quiet`),
 * затем среднюю, минимальную и максимальную концентрацию узлов и связей по всем испытаниям.
 * С `--png` напечатанные испытания сохраняются картинками в `out/`, а если матрицы не печатаются — только первое.
 * Если не указан ни `--png`, ни `--no-png`, команда спросит.
 */
class AppCommand : CliktCommand() {

    val size by option("-L", "--size", help = "линейный размер решётки, не меньше 2")
        .int()
        .restrictTo(min = 2)
        .prompt("Размер решётки L")

    val pSite by option("-s", "--site", help = "заданная концентрация узлов, от 0 до 1")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Концентрация узлов p_site")

    val pBond by option("-b", "--bond", help = "заданная концентрация связей среди рёбер с обоими занятыми концами, от 0 до 1")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Концентрация связей p_bond")

    val trials by option("-n", "--trials", help = "число испытаний")
        .int()
        .restrictTo(min = 1)
        .prompt("Число испытаний", default = 1)

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val png by option("--png", help = "сохранить картинки испытаний в out/")
        .flag("--no-png")
        .prompt("Сохранить картинки?", default = true)

    val quiet by option("--quiet", help = "не печатать матрицы")
        .flag()

    override fun help(context: Context): String =
        "Задача узлов и связей: случайное равномерное заполнение узлов и связей решётки L x L"

    override fun run() = main()

}
