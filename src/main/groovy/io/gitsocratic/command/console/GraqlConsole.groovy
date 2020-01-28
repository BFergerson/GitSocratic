package io.gitsocratic.command.console

import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.core.command.ExecStartResultCallback
import groovy.util.logging.Slf4j
import io.gitsocratic.SocraticCLI
import io.gitsocratic.GitSocraticService

import java.util.concurrent.Callable

/**
 * Attaches and opens a Graql console on the Grakn service running in Docker.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
class GraqlConsole implements Callable<Integer> {

    private static ResourceBundle buildBundle = ResourceBundle.getBundle("gitsocratic_build")

    @Override
    Integer call() throws Exception {
        Container graknContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.grakn.command == it.command) {
                graknContainer = it
            }
        }
        if (graknContainer == null) {
            log.error "Could not find Grakn container to attach to"
            return -1
        }

        //attach to graql console
        ExecCreateCmdResponse execCreateCmdResponse =
                SocraticCLI.dockerClient.execCreateCmd(graknContainer.id).withAttachStdout(true)
                        .withAttachStdin(true)
                        .withTty(true)
                        .withCmd("/opt/grakn/grakn-core-" + buildBundle.getString("grakn_version") + "/./graql", "console")
                        .exec()
        SocraticCLI.dockerClient.execStartCmd(execCreateCmdResponse.getId())
                .withTty(true)
                .withStdIn(System.in)
                .exec(new ExecStartResultCallback(System.out, System.err))
                .awaitCompletion()
        return 0
    }
}
