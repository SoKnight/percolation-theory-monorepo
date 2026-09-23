import cli.AppCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal

/**
 * Точка входа: передаёт аргументы командной строки в [AppCommand].
 *
 * Разбор аргументов, промпты, `--help` и сообщения об ошибках берёт на себя фреймворк Clikt.
 * Если аргументы неверные, программа печатает ошибку и завершается с кодом `1`.
 */
fun main(args: Array<String>) =
    AppCommand()
        .context { terminal = createTerminal() }
        .main(args)

/**
 * - `interactive = true`: под `gradle run` и в консоли IDEA ввод идёт не из настоящего терминала,
 *   и без этого Clikt не задаёт вопрос, а сразу падает с ошибкой `missing option`;
 * - `AnsiLevel.NONE`: цвета выключены, чтобы при выводе в файл туда не попадали ANSI-коды.
 */
private fun createTerminal() = Terminal(
    ansiLevel = AnsiLevel.NONE,
    interactive = true,
)
