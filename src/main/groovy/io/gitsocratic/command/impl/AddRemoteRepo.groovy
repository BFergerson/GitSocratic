package io.gitsocratic.command.impl

import groovy.transform.ToString
import io.gitsocratic.client.PhenomenaClient
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
    String repoName

    @CommandLine.Option(names = ["-p", "--parallel"], description = "Use parallel source code processing")
    boolean parallelProcessing = true

    @Override
    Integer call() throws Exception {
        //clone repo
        new File("/tmp/gitsocratic/out/").deleteDir()
        cloneRepo(repoName, new File("/tmp/gitsocratic/out/"))

        //import source code into grakn with phenomena
        new PhenomenaClient("/tmp/gitsocratic/out/").withCloseable {
            it.processSourceCodeRepository(parallelProcessing)
        }
        return 0
    }

    static void cloneRepo(String githubRepository, File outputDirectory) {
        println "Cloning: $githubRepository"
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
        println "Cloned: $githubRepository"
    }
}
