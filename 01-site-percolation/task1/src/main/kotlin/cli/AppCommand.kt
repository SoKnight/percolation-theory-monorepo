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
 * Размер `-L`, концентрацию `-p` и число испытаний `-n` можно передать при запуске,
 * а если какой-то из них не указан, команда спросит его у пользователя.
 * Например, `-L 6 -p 0.5 -n 5 --seed=1` или просто запуск без аргументов.
 *
 * Выводит матрицу каждого испытания (только при `L <= 50` и без `--quiet`),
 * затем среднюю, минимальную и максимальную концентрацию по всем испытаниям.
 * С `--png` первая решётка сохраняется картинкой в `out/`.
 */
class AppCommand : CliktCommand() {

    val size by option("-L", "--size", help = "линейный размер решётки")
        .int()
        .restrictTo(min = 1)
        .prompt("Размер решётки L")

    val p by option("-p", "--concentration", help = "заданная концентрация, от 0 до 1")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Концентрация p")

    val trials by option("-n", "--trials", help = "число испытаний")
        .int()
        .restrictTo(min = 1)
        .prompt("Число испытаний", default = 1)

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val png by option("--png", help = "сохранить первую решётку картинкой в out/")
        .flag()

    val quiet by option("--quiet", help = "не печатать матрицы")
        .flag()

    override fun help(context: Context): String =
        "Случайное равномерное заполнение простой квадратной решётки L x L"

    override fun run() = main()

}
