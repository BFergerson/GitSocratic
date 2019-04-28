package io.gitsocratic.command.impl.init

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.core.command.ExecStartResultCallback
import groovy.transform.ToString
import io.gitsocratic.GitSocraticService
import io.gitsocratic.SocraticCLI
import io.gitsocratic.command.impl.Init
import io.gitsocratic.command.impl.init.docker.PullImageProgress
import io.gitsocratic.command.result.InitCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable

import static io.gitsocratic.command.config.ConfigOption.*

/**
 * Used to initialize the Babelfish service.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "babelfish",
        description = "Initialize Babelfish service",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Babelfish implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", arity = "0", description = "Version to initialize")
    private String babelfishVersion = defaultBabelfishVersion

    @CommandLine.Option(names = ["-v", "--verbose"], description = "Verbose logging")
    boolean verbose = Init.defaultVerbose

    @SuppressWarnings("unused")
    protected Babelfish() {
        //used by Picocli
    }

    Babelfish(String babelfishVersion) {
        this.babelfishVersion = Objects.requireNonNull(babelfishVersion)
    }

    Babelfish(String babelfishVersion, boolean verbose) {
        this.babelfishVersion = Objects.requireNonNull(babelfishVersion)
        this.verbose = verbose
    }

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    InitCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    InitCommandResult executeCommand(boolean outputLogging) throws Exception {
        def status
        if (Boolean.valueOf(use_docker_babelfish.getValue())) {
            status = initDockerBabelfish()
            if (status != 0) return new InitCommandResult(status)
        } else {
            status = validateExternalBabelfish()
            if (status != 0) return new InitCommandResult(status)
        }
        return new InitCommandResult(status)
    }

    private static int validateExternalBabelfish() {
        println "Validating external Babelfish installation"
        def host = babelfish_host.value
        def port = babelfish_port.value as int
        println " Host: $host"
        println " Port: $port"

        println "Connecting to Babelfish"
        Socket s1 = new Socket()
        try {
            s1.setSoTimeout(200)
            s1.connect(new InetSocketAddress(host, port), 200)
            println "Successfully connected to Babelfish"
            //todo: real connection test
            return 0
        } catch (all) {
            println "Failed to connect to Babelfish"
            all.printStackTrace()
            return -1
        } finally {
            s1.close()
        }
    }

    private int initDockerBabelfish() {
        println "Initializing Babelfish container"
        def callback = new PullImageProgress()
        SocraticCLI.dockerClient.pullImageCmd("bblfsh/bblfshd:$babelfishVersion").exec(callback)
        callback.awaitCompletion()

        Container babelfishContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.babelfish.command == it.command) {
                babelfishContainer = it
            }
        }

        if (babelfishContainer != null) {
            println "Found Babelfish container"
            println " Id: " + babelfishContainer.id

            //start container (if necessary)
            if (babelfishContainer.state != "running") {
                println "Starting Babelfish container"
                SocraticCLI.dockerClient.startContainerCmd(babelfishContainer.id).exec()
                println "Babelfish container started"
            } else {
                println "Babelfish already running"
            }
        } else {
            //create container
            List<Image> images = SocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains("bblfsh/bblfshd:$babelfishVersion")) {
                    def babelfishPort = babelfish_port.getValue() as int
                    ExposedPort babelfishTcpPort = ExposedPort.tcp(babelfish_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    portBindings.bind(babelfishTcpPort, Ports.Binding.bindPort(babelfishPort))
                    CreateContainerResponse container = SocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withPrivileged(true)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(babelfishTcpPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .exec()
                    SocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()

                    //auto-install recommended language drivers
                    ExecCreateCmdResponse execCreateCmdResponse =
                            SocraticCLI.dockerClient.execCreateCmd(container.id)
                                    .withCmd("bblfshctl", "driver", "install", "--recommended")
                                    .withTty(true)
                                    .withPrivileged(true)
                                    .exec()
                    SocraticCLI.dockerClient.execStartCmd(execCreateCmdResponse.getId())
                            .withTty(true)
                            .exec(new ExecStartResultCallback(System.out, System.err))
                            .awaitCompletion()
                    //todo: real wait on driver installation
                }
            }
        }
        //todo: real connection test
        return 0
    }

    static String getDefaultBabelfishVersion() {
        return "v2.12.1-drivers"
    }
}
