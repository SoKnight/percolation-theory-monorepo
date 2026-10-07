package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.path
import main
import kotlin.io.path.Path

/**
 * Папку с таблицами `-i` можно передать при запуске, а если она не указана, команда спросит её у пользователя,
 * предложив папку, куда сохраняет задание 1. Например, `-i ../task1/out --plots` или просто запуск без аргументов.
 *
 * Берёт из папки все таблицы `probability_L*.csv`, для каждой находит порог перколяции p_c(L),
 * а по всем вместе p_c(∞), и сохраняет результаты в `out/`. С `--plots` в `out/` сохраняются ещё графики.
 * Если не указан ни `--plots`, ни `--no-plots`, команда спросит.
 */
class AppCommand : CliktCommand() {

    val input by option("-i", "--input", help = "папка с таблицами probability_L*.csv, которые сохраняет задание 1")
        .path(mustExist = true, canBeFile = false, mustBeReadable = true)
        .prompt("Папка с таблицами", default = Path("..", "task1", "out"))

    val plots by option("--plots", help = "сохранить графики в out/")
        .flag("--no-plots")
        .prompt("Сохранить графики?", default = true)

    override fun help(context: Context): String =
        "Порог перколяции по таблицам P(p): аппроксимация сигмоидой и переход к бесконечной системе"

    override fun run() = main()

}
