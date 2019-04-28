package io.gitsocratic.command.impl

import groovy.transform.ToString
import io.gitsocratic.SocraticCLI
import io.gitsocratic.command.impl.query.Graql
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `query` command.
 * Used to execute a single source code query.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "query",
        description = "Execute a single source code query",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        subcommands = [Graql.class])
class Query implements Callable<Integer> {

    @CommandLine.ParentCommand
    private SocraticCLI cli

    @Override
    Integer call() throws Exception {
        if (cli.fullCommand.length == 1) {
            CommandLine.usage(this, System.out)
        }
        return 0
    }
}
