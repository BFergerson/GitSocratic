package io.gitsocratic.command.impl

import io.gitsocratic.client.PhenomenaClient
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `add-local-repo` command.
 * Used to add local source code repository to the knowledge graph.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "add-local-repo",
        description = "Add local source code repository to the knowledge graph",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class AddLocalRepo implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The repository to add")
    private File repoLocation

    @CommandLine.Option(names = ["-p", "--parallel"], description = "Use parallel source code processing")
    private boolean parallelProcessing = true

    @Override
    Integer call() throws Exception {
        if (!repoLocation.exists()) {
            System.err.println "Invalid repository location: $repoLocation"
            return -1
        }

        //import source code into grakn with phenomena
        new PhenomenaClient(repoLocation.absolutePath).withCloseable {
            it.processSourceCodeRepository(parallelProcessing)
        }
        return 0
    }
}
