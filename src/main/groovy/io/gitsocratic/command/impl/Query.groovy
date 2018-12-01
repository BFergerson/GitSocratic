package io.gitsocratic.command.impl

import io.gitsocratic.command.impl.query.Graql
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "query",
        description = "Execute single source code query",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        subcommands = [Graql.class])
class Query implements Callable<Integer> {

    @Override
    Integer call() throws Exception {
        return 0
    }
}
