package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import main

/**
 * Сторону квадрата `-L`, радиус `-r` и концентрацию `-p` можно передать при запуске,
 * а если какой-то из них не указан, команда спросит его у пользователя.
 * Например, `-L 20 -r 1 -p 0.4 --seed=1` или просто запуск без аргументов.
 *
 * Круги размещаются для всех трёх граничных условий по очереди. Для каждого выводится число размещённых кругов,
 * полученная концентрация и число бросков, при `N <= 50` — координаты центров. Центры всегда сохраняются в CSV
 * в `out/`, с `--png` ещё и картинки. Если не указан ни `--png`, ни `--no-png`, команда спросит.
 */
class AppCommand : CliktCommand() {

    val size by option("-L", "--size", help = "сторона квадрата")
        .double()
        .prompt("Сторона квадрата L")
        .check("должно быть больше 0") { it > 0 }

    val radius by option("-r", "--radius", help = "радиус кругов")
        .double()
        .prompt("Радиус кругов r")
        .check("должно быть больше 0") { it > 0 }

    val p by option("-p", "--concentration", help = "заданная концентрация: доля площади квадрата, занятая кругами")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Концентрация p")

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val png by option("--png", help = "сохранить картинки в out/")
        .flag("--no-png")
        .prompt("Сохранить картинки?", default = true)

    override fun help(context: Context): String =
        "Случайное размещение непересекающихся кругов радиуса r в квадрате L x L до концентрации p"

    override fun run() = main()

}
