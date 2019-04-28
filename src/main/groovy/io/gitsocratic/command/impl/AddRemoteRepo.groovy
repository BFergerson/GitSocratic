package io.gitsocratic.command.impl

import groovy.transform.ToString
import io.gitsocratic.client.PhenomenaClient
import io.gitsocratic.command.result.AddRemoteRepoCommandResult
import org.eclipse.jgit.api.Git
import picocli.CommandLine

import java.util.concurrent.TimeUnit

/**
 * Represents the `add-remote-repo` command.
 * Used to add remote source code repository to the knowledge graph.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "add-remote-repo",
        description = "Add remote source code repository to the knowledge graph",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class AddRemoteRepo extends AddLocalRepo {

    @CommandLine.Parameters(index = "0", description = "The repository to add")
    private String repoName

    @CommandLine.Option(names = ["-p", "--parallel"], description = "Use parallel source code processing")
    private boolean parallelProcessing = defaultParallelProcessing

    @SuppressWarnings("unused")
    protected AddRemoteRepo() {
        //used by Picocli
    }

    AddRemoteRepo(String repoName, boolean parallelProcessing) {
        this.repoName = Objects.requireNonNull(repoName)
        this.parallelProcessing = parallelProcessing
    }

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    AddRemoteRepoCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    private AddRemoteRepoCommandResult executeCommand(boolean outputLogging) throws Exception {
        //clone repo
        new File("/tmp/gitsocratic/out/").deleteDir()
        cloneRepo(repoName, new File("/tmp/gitsocratic/out/"), outputLogging)

        //import source code into grakn with phenomena
        new PhenomenaClient("/tmp/gitsocratic/out/").withCloseable {
            it.processSourceCodeRepository(parallelProcessing)
        }
        return new AddRemoteRepoCommandResult(0)
    }

    String getRepoName() {
        return repoName
    }

    boolean getParallelProcessing() {
        return parallelProcessing
    }

    private static void cloneRepo(String githubRepository, File outputDirectory, boolean outputLogging) {
        if (outputLogging) println "Cloning: $githubRepository"
        if (githubRepository.startsWith("http")) {
            Git.cloneRepository()
                    .setURI(githubRepository)
                    .setDirectory(outputDirectory)
                    .setCloneSubmodules(true)
                    .setTimeout(TimeUnit.MINUTES.toSeconds(5) as int)
                    .call()
        } else {
            Git.cloneRepository()
                    .setURI("https://github.com/" + githubRepository + ".git")
                    .setDirectory(outputDirectory)
                    .setCloneSubmodules(true)
                    .setTimeout(TimeUnit.MINUTES.toSeconds(5) as int)
                    .call()
        }
        if (outputLogging) println "Cloned: $githubRepository"
    }

    static boolean getDefaultParallelProcessing() {
        return true
    }
}
