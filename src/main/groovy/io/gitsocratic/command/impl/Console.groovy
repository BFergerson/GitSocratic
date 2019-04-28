package io.gitsocratic.command.impl

import groovy.transform.ToString
import io.gitsocratic.command.console.ConsoleType
import io.gitsocratic.command.console.GraqlConsole
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `console` command.
 * Used to open interactive source code query console.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "console",
        description = "Open interactive source code query console",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Console implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The console to attach to")
    ConsoleType console

    @Override
    Integer call() throws Exception {
        if (console == ConsoleType.graql) {
            return new GraqlConsole().call()
        }
        return 0
    }
}
