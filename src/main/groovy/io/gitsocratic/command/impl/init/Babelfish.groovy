package io.gitsocratic.command.impl.init

import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Ports
import groovy.io.GroovyPrintWriter
import groovy.transform.ToString
import groovy.util.logging.Slf4j
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
@Slf4j
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
        def input = new PipedInputStream()
        def output = new PipedOutputStream()
        input.connect(output)
        Thread.startDaemon {
            input.newReader().eachLine {
                log.info it
            }
        }
        return execute(output).status
    }

    InitCommandResult execute() throws Exception {
        def input = new PipedInputStream()
        def output = new PipedOutputStream()
        input.connect(output)
        return execute(output)
    }

    InitCommandResult execute(PipedOutputStream output) throws Exception {
        def out = new GroovyPrintWriter(output, true)
        def status
        if (Boolean.valueOf(use_docker_babelfish.getValue())) {
            status = initDockerBabelfish(out)
            if (status != 0) return new InitCommandResult(status)
        } else {
            status = validateExternalBabelfish(out)
            if (status != 0) return new InitCommandResult(status)
        }
        return new InitCommandResult(status)
    }

    private static int validateExternalBabelfish(PrintWriter out) {
        out.println "Validating external Babelfish installation"
        def host = babelfish_host.value
        def port = babelfish_port.value as int
        out.println " Host: $host"
        out.println " Port: $port"

        out.println "Connecting to Babelfish"
        Socket s1 = new Socket()
        try {
            s1.setSoTimeout(200)
            s1.connect(new InetSocketAddress(host, port), 200)
            out.println "Successfully connected to Babelfish"
            //todo: real connection test
            return 0
        } catch (all) {
            out.println "Failed to connect to Babelfish"
            all.printStackTrace(out)
            return -1
        } finally {
            s1.close()
        }
    }

    private int initDockerBabelfish(PrintWriter out) {
        out.println "Initializing Babelfish container"
        def callback = new PullImageProgress(out)
        SocraticCLI.dockerClient.pullImageCmd("bblfsh/bblfshd:v$babelfishVersion-drivers").exec(callback)
        callback.awaitCompletion()

        Container babelfishContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.babelfish.command == it.command) {
                babelfishContainer = it
            }
        }

        if (babelfishContainer != null) {
            out.println "Found Babelfish container"
            out.println " Id: " + babelfishContainer.id

            //start container (if necessary)
            if (babelfishContainer.state != "running") {
                out.println "Starting Babelfish container"
                SocraticCLI.dockerClient.startContainerCmd(babelfishContainer.id).exec()
                out.println "Babelfish container started"
            } else {
                out.println "Babelfish already running"
            }
        } else {
            //create container
            List<Image> images = SocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains("bblfsh/bblfshd:v$babelfishVersion-drivers")) {
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
                }
            }
        }
        //todo: real connection test
        return 0
    }

    static String getDefaultBabelfishVersion() {
        return "2.13.0"
    }
}
