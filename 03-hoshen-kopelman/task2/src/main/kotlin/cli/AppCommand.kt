package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.path
import main
import kotlin.io.path.Path

/**
 * Файл с метками `-i` можно передать при запуске, а если он не указан, команда спросит его у пользователя,
 * предложив файл, который сохраняет задание 1. Например, `-i ../task1/out/clusters.txt` или просто запуск без аргументов.
 *
 * Печатает `1` и номер и размер перколяционного кластера, если он есть, или `0`, если нет,
 * и сохраняет это в `out/percolation.txt`.
 */
class AppCommand : CliktCommand() {

    val input by option("-i", "--input", help = "файл с метками кластеров, который сохраняет задание 1")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .prompt("Файл с метками кластеров", default = Path("..", "task1", "out", "clusters.txt"))

    override fun help(context: Context): String =
        "Поиск перколяционного кластера, соединяющего верхний и нижний край решётки, по меткам кластеров"

    override fun run() = main()

}
