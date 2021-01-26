package io.gitsocratic.command.impl

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.gitsocratic.client.PhenomenaClient
import io.gitsocratic.command.config.ImportMode
import io.gitsocratic.command.result.AddLocalRepoCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `add-local-repo` command.
 * Used to add local source code repository to the knowledge graph.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
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
    protected boolean parallelProcessing = defaultParallelProcessing

    protected ImportMode importMode = ImportMode.PROCESS

    @SuppressWarnings("unused")
    protected AddLocalRepo() {
        //used by Picocli
    }

    AddLocalRepo(File repoLocation, ImportMode importMode, boolean parallelProcessing) {
        this.repoLocation = Objects.requireNonNull(repoLocation)
        this.importMode = importMode
        this.parallelProcessing = parallelProcessing
    }

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    AddLocalRepoCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    private AddLocalRepoCommandResult executeCommand(boolean outputLogging) throws Exception {
        if (!repoLocation.exists()) {
            if (outputLogging) log.error "Invalid repository location: $repoLocation"
            return new AddLocalRepoCommandResult(-1)
        }

        if (importMode == ImportMode.PARSE) {
            //parse source code
            new PhenomenaClient(repoLocation.absolutePath).withCloseable {
                it.parseSourceCodeRepository(parallelProcessing)
            }
        } else {
            //parse & import source code into grakn
            new PhenomenaClient(repoLocation.absolutePath).withCloseable {
                it.processSourceCodeRepository(parallelProcessing)
            }
        }
        return new AddLocalRepoCommandResult(0)
    }

    File getRepoLocation() {
        return repoLocation
    }

    boolean getParallelProcessing() {
        return parallelProcessing
    }

    static boolean getDefaultParallelProcessing() {
        return true
    }
}
