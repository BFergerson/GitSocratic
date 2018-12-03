package io.gitsocratic

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.netty.NettyDockerCmdExecFactory
import io.gitsocratic.command.config.ConfigOption
import io.gitsocratic.command.impl.*
import org.apache.commons.lang.SystemUtils
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Main entry point of the GitSocratic CLI implementation.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "gitsocratic",
        description = "Source code query command line interface",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        versionProvider = GitSocraticVersion.class,
        subcommands = [AddLocalRepo.class, AddRemoteRepo.class, Config.class, Console.class,
                Init.class, Logs.class, Query.class, Question.class])
class GitSocraticCLI implements Callable<Integer> {

    @CommandLine.Option(names = ["-c", "--config"], description = 'Config file to use (default: ${DEFAULT-VALUE})')
    private static File configFile = new File(System.getProperty("java.io.tmpdir"), "gitsocratic.config")
    private static DockerClient dockerClient

    static void main(String[] args) {
        def cmd = new CommandLine(new GitSocraticCLI())
        List<Object> result = cmd.parseWithHandler(new CommandLine.RunAll(), args)
        dockerClient?.close()
        if (result != null) {
            if (result.size() == 1) {
                cmd.usage(System.out)
            } else {
                def status = result.find { it != 0 } as Integer
                if (status != null) {
                    System.exit(status)
                }
            }
        }
    }

    static File getConfigFile() {
        return configFile
    }

    static DockerClient getDockerClient() {
        if (dockerClient == null) {
            NettyDockerCmdExecFactory factory = new NettyDockerCmdExecFactory()
            def config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            if (SystemUtils.IS_OS_WINDOWS) {
                def dockerHost = ConfigOption.docker_host.value
                def dockerPort = ConfigOption.docker_port.value as int
                config.withDockerHost("tcp://$dockerHost:$dockerPort")
            } else {
                config.withDockerHost("unix:///var/run/docker.sock")
            }
            dockerClient = DockerClientBuilder.getInstance(config.build())
                    .withDockerCmdExecFactory(factory)
                    .build()
        }
        return dockerClient
    }

    @Override
    Integer call() throws Exception {
        return 0
    }
}
