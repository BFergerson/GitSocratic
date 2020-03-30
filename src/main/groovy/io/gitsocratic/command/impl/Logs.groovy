package io.gitsocratic.command.impl

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Frame
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.gitsocratic.GitSocraticService
import picocli.CommandLine

import java.util.concurrent.Callable

import static io.gitsocratic.SocraticCLI.dockerClient

/**
 * Represents the `logs` command.
 * Used to view logs of initialized services.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "logs",
        description = "View logs of initialized services",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Logs implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The service to view logs for")
    GitSocraticService service

    @CommandLine.Option(names = ["-t", "-f", "--tail"], description = "Tail logs")
    boolean tailLogs = false

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
            log.error "Couldn't find container for service: $service"
            return -1
        }
        return 0
    }

    static class LogCallback extends ResultCallback.Adapter<Frame> {

        @Override
        void onNext(Frame item) {
            log.info(item as String)
        }
    }
}
