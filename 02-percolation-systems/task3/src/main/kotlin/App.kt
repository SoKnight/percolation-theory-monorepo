import cli.AppCommand
import com.github.ajalt.clikt.core.UsageError
import data.Boundary
import data.DiskGenerator
import data.Disks
import data.MAX_FAILURES
import util.format
import util.writeAsCSV
import util.writeAsPNG
import java.util.*

private const val MAX_PRINT_COUNT = 50

// проверка перебором всех пар идёт за O(N²), на больших N это долго
private const val MAX_CHECK_COUNT = 5000

/**
 * Размещает круги для каждого из граничных условий, печатает результат, проверяет его перебором
 * и сохраняет центры в CSV, а при `--png` и картинки.
 */
fun AppCommand.main() {
    // жёстким стенкам нужно L > 2r, а на торе при L < 4r круг может пересечься сам с собой через край
    if (size < 4 * radius)
        throw UsageError("Нужно L >= 4r, иначе периодические условия не работают")

    // все условия с одним seed, чтобы различия были только из-за границ
    val seed = seed ?: SplittableRandom().nextLong()

    echo("Квадрат ${size.format()} x ${size.format()}, радиус кругов ${radius.format()}, заданная концентрация ${p.format()}, seed $seed")

    for (boundary in Boundary.entries) {
        val startTime = System.nanoTime()
        val disks = DiskGenerator(SplittableRandom(seed)).generate(size, radius, p, boundary)
        val seconds = (System.nanoTime() - startTime) / 1e9

        echo()
        echo("Граничные условия: ${boundary.title}")
        report(disks)
        echo("Центры сохранены в: ${disks.writeAsCSV(p)}")

        if (png) {
            echo("Картинка сохранена в: ${disks.writeAsPNG(p)}")
        }

        echo("Время расчёта: ${seconds.format()} с")
    }
}

/** Печатает, сколько кругов встало, их центры при небольшом числе и результат проверки перебором. */
private fun AppCommand.report(disks: Disks) {
    echo("Нужно кругов: ${disks.target}, размещено: ${disks.count}, концентрация ${disks.concentration.format()}, бросков: ${disks.attempts}")

    if (disks.count < disks.target) {
        echo("Круг ${disks.count + 1} не удалось разместить за $MAX_FAILURES бросков подряд: места не осталось.")
        echo("Случайное последовательное размещение не заполняет плоскость плотнее примерно 0.547, у стенок ещё меньше.")
    }

    if (disks.count <= MAX_PRINT_COUNT) {
        echo("  i         x         y")

        for (i in 0..<disks.count) {
            echo("%3d %9s %9s".format(i + 1, disks.xs[i].format(), disks.ys[i].format()))
        }
    }

    if (disks.count <= MAX_CHECK_COUNT) {
        val overlap = disks.findOverlap()
        val outside = disks.findOutside()

        echo(when {
            overlap != null -> "Проверка: круги ${overlap.first + 1} и ${overlap.second + 1} пересекаются!"
            outside != null -> "Проверка: центр круга ${outside + 1} вне допустимой области!"
            else -> "Проверка перебором всех пар: пересечений нет, все центры в допустимой области"
        })
    }
}
