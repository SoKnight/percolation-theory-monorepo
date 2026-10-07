package cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import main

/**
 * Размер `-L`, концентрации от `-p` до `--p-max` с шагом `--step` и число испытаний `-k` можно передать при запуске,
 * а если что-то не указано, команда спросит у пользователя. Например, `-L 100 -p 0.5 --p-max 0.7 --step 0.005 -k 100`.
 *
 * Печатает таблицу p и P(p) и сохраняет её в `out/probability_L{L}.csv`.
 */
class AppCommand : CliktCommand() {

    val size by option("-L", "--size", help = "линейный размер решётки")
        .int()
        .restrictTo(min = 2)
        .prompt("Размер решётки L")

    val pMin by option("-p", "--p-min", help = "минимальная концентрация")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Минимальная концентрация p", default = 0.5)

    val pMax by option("--p-max", help = "максимальная концентрация")
        .double()
        .restrictTo(0.0..1.0)
        .prompt("Максимальная концентрация", default = 0.7)
        .validate { if (it < pMin) throw BadParameterValue("должна быть не меньше минимальной $pMin") }

    val step by option("--step", help = "шаг концентрации")
        .double()
        .restrictTo(min = 1e-6)
        .prompt("Шаг концентрации", default = 0.005)

    val trials by option("-k", "--trials", help = "число испытаний на каждую концентрацию, не меньше 100")
        .int()
        .restrictTo(min = 100)
        .prompt("Число испытаний", default = 100)

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val parallel by option("--parallel", help = "проводить испытания параллельно")
        .boolean()
        .optionalValue(true)
        .default(true)

    override fun help(context: Context): String =
        "Вероятность возникновения перколяционного кластера в задаче узлов на квадратной решётке L x L"

    override fun run() = main()

}
