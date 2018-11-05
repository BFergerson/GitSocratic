package io.gitsocratic.command.impl

import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.core.command.LogContainerResultCallback
import io.gitsocratic.GitSocraticService
import picocli.CommandLine

import java.util.concurrent.Callable

import static io.gitsocratic.GitSocraticCLI.dockerClient

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "logs",
        description = "View logs for initialized services",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Logs implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The service to view logs for")
    private GitSocraticService service

    @CommandLine.Option(names = ["-t", "-f", "--tail"], description = "Tail logs")
    private boolean tailLogs = false

    @Override
    Integer call() throws Exception {
        boolean foundContainer = false
        dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (service.command == it.command) {
                foundContainer = true
                dockerClient.logContainerCmd(it.getId())
                        .withStdErr(true)
                        .withStdOut(true)
                        .withFollowStream(true)
                        .withTailAll()
                        .exec(new LogCallback())

                //todo: smarter
                Thread.sleep(100)
                if (tailLogs) {
                    Thread.sleep(100000000)
                }
            }
        }

        if (!foundContainer) {
            System.err.println("Couldn't find container for service: $service")
            return -1
        }
        return 0
    }

    static class LogCallback extends LogContainerResultCallback {
        @Override
        void onNext(Frame item) {
            super.onNext(item)
            println item
        }
    }
}
