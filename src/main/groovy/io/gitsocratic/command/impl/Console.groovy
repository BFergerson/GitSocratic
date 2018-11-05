package io.gitsocratic.command.impl

import io.gitsocratic.command.console.ConsoleType
import io.gitsocratic.command.console.GraqlConsole
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "console",
        description = "Open interactive source code query console",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Console implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The console to attach to")
    private ConsoleType console

    @Override
    Integer call() throws Exception {
        if (console == ConsoleType.graql) {
            return new GraqlConsole().call()
        }
        return 0
    }
}
