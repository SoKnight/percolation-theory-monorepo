package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import main

/**
 * Команда второго задания: оценивает равномерность заполнения решётки критерием Пирсона
 * сериями испытаний для концентраций от `0.1` до `0.9`.
 *
 * Размер `-L` и число испытаний `-n` можно передать при запуске, а если какой-то из них не указан,
 * команда спросит его у пользователя. Например, `--L=1000 --trials=100 --seed=42 --no-lattices --plots`.
 *
 * Печатает таблицу по всем концентрациям и сохраняет её в `out/uniformity_L{L}.csv`.
 * С `--lattices` в `out/` сохраняются ещё картинки решёток, с `--plots` графики.
 * Если не указан ни флаг, ни его вариант с `--no-`, команда тоже спросит.
 */
class AppCommand : CliktCommand() {

    // при меньших L и p = 0.1 выходит n < 96, то есть один интервал и df = 0
    val size by option("-L", "--L", "--size", help = "линейный размер решётки, не меньше 31")
        .int()
        .restrictTo(min = 31)
        .prompt("Размер решётки L")

    val trials by option("-n", "--trials", help = "число испытаний на каждую концентрацию")
        .int()
        .restrictTo(min = 1)
        .prompt("Число испытаний", default = 100)

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val parallel by option("--parallel", help = "проводить испытания параллельно")
        .boolean()
        .optionalValue(true)
        .default(true)

    val lattices by option("--lattices", help = "сохранить картинки решёток в out/")
        .flag("--no-lattices")
        .prompt("Сохранить картинки решёток?", default = false)

    val plots by option("--plots", help = "сохранить графики в out/")
        .flag("--no-plots")
        .prompt("Сохранить графики?", default = false)

    override fun help(context: Context): String =
        "Оценка равномерности заполнения решётки L x L критерием Пирсона"

    override fun run() = main()

}
