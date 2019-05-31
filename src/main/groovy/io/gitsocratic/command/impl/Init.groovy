package io.gitsocratic.command.impl

import groovy.transform.ToString
import io.gitsocratic.SocraticCLI
import io.gitsocratic.command.impl.init.ApacheSkywalking
import io.gitsocratic.command.impl.init.Babelfish
import io.gitsocratic.command.impl.init.Grakn
import io.gitsocratic.command.impl.init.SourcePlusPlus
import io.gitsocratic.command.result.InitCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `init` command.
 * Used to initialize services necessary to use GitSocratic.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "init",
        description = "Initialize services necessary to use GitSocratic",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        subcommands = [ApacheSkywalking.class, Babelfish.class, Grakn.class, SourcePlusPlus.class])
class Init implements Callable<Integer> {

    @CommandLine.ParentCommand
    private SocraticCLI cli

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    InitCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    private InitCommandResult executeCommand(boolean outputLogging) throws Exception {
        if (cli == null || cli.fullCommand.length == 1) {
            //install base services: babelfish & grakn
            def status = new Babelfish().call()
            if (status == 0) {
                status = new Grakn().call()
            }
            return new InitCommandResult(status)
        }
        return new InitCommandResult(0)
    }

    static boolean getDefaultVerbose() {
        return false
    }

    static boolean getDefaultUseServicePorts() {
        return true
    }
}
