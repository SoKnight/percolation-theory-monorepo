package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.restrictTo
import main

/**
 * Решётка либо читается из файла `-i`, либо генерируется по размеру `-L` и концентрации `-p`, как в работе 1.
 * Если файл не указан, а `-L` или `-p` нет, команда спросит их у пользователя.
 * Например, `-i example.txt`, `-L 6 -p 0.6 --seed=1` или просто запуск без аргументов.
 *
 * Печатает решётку и матрицу меток (только при `L <= 50` и без `--quiet`), распределение кластеров по размерам
 * и сохраняет всё это в `out/clusters.txt`. С `--png` кластеры сохраняются цветной картинкой в `out/clusters.png`.
 * Если не указан ни `--png`, ни `--no-png`, команда спросит.
 */
class AppCommand : CliktCommand() {

    val input by option("-i", "--input", help = "файл с решёткой из 0 и 1, по строке на ряд")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)

    val size by option("-L", "--size", help = "линейный размер решётки, если она генерируется")
        .int()
        .restrictTo(min = 1)

    val p by option("-p", "--concentration", help = "заданная концентрация от 0 до 1, если решётка генерируется")
        .double()
        .restrictTo(0.0..1.0)

    val seed by option("--seed", help = "seed генератора для воспроизводимых результатов")
        .long()

    val png by option("--png", help = "сохранить кластеры картинкой в out/")
        .flag("--no-png")
        .prompt("Сохранить картинку?", default = true)

    val quiet by option("--quiet", help = "не печатать матрицы")
        .flag()

    override fun help(context: Context): String =
        "Разметка кластеров задачи узлов на простой квадратной решётке алгоритмом Хошена-Копельмана"

    override fun run() = main()

}
