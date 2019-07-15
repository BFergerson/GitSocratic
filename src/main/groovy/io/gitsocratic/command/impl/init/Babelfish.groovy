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
import io.gitsocratic.command.result.InitDockerCommandResult
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

    private boolean useServicePorts = Init.defaultUseServicePorts

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

    Babelfish(String babelfishVersion, boolean verbose, boolean useServicePorts) {
        this.babelfishVersion = Objects.requireNonNull(babelfishVersion)
        this.verbose = verbose
        this.useServicePorts = useServicePorts
    }

    @Override
    Integer call() throws Exception {
        def input = new PipedInputStream()
        def output = new PipedOutputStream()
        input.connect(output)
        Thread.startDaemon {
            try {
                input.newReader().eachLine {
                    log.info it
                }
            } catch (IOException ex) {
                //ignore
            }
        }
        return execute(output).status
    }

    InitCommandResult execute() throws Exception {
        return execute(false)
    }

    InitCommandResult execute(boolean outputToStd) throws Exception {
        def input = new PipedInputStream()
        def output = new PipedOutputStream()
        input.connect(output)
        if (outputToStd) {
            Thread.startDaemon {
                try {
                    input.newReader().eachLine {
                        log.info it
                    }
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
        return execute(output)
    }

    InitCommandResult execute(PipedOutputStream output) throws Exception {
        def out = new GroovyPrintWriter(output, true)
        if (Boolean.valueOf(use_docker_babelfish.getValue())) {
            try {
                def portBindings = initDockerBabelfish(out)
                if (portBindings != null) {
                    return new InitDockerCommandResult("Babelfish", portBindings)
                }
            } catch (all) {
                out.println "Failed to initialize service"
                all.printStackTrace(out)
            }
            return new InitDockerCommandResult("Babelfish", -1)
        } else {
            try {
                def status = validateExternalBabelfish(out)
                return new InitCommandResult(status)
            } catch (all) {
                out.println "Failed to validate external service"
                all.printStackTrace(out)
                return new InitCommandResult(-1)
            }
        }
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

    private Map<String, String[]> initDockerBabelfish(PrintWriter out) {
        out.println "Initializing Babelfish container"

        def dockerRepository = "bblfsh/bblfshd:v$babelfishVersion-drivers"
        def callback = new PullImageProgress(out)
        SocraticCLI.dockerClient.pullImageCmd(dockerRepository).exec(callback)
        callback.awaitCompletion()

        Container babelfishContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.babelfish.command == it.command) {
                babelfishContainer = it
            }
        }

        def containerId = null
        if (babelfishContainer != null) {
            containerId = babelfishContainer.id
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
                if (it.repoTags?.contains(dockerRepository)) {
                    ExposedPort babelfishTcpPort = ExposedPort.tcp(babelfish_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    if (useServicePorts) {
                        portBindings.bind(babelfishTcpPort, Ports.Binding.bindPort(Integer.parseInt(
                                babelfish_port.defaultValue)))
                    } else {
                        portBindings.bind(babelfishTcpPort, Ports.Binding.empty())
                    }
                    CreateContainerResponse container = SocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withPrivileged(true)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(babelfishTcpPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .exec()
                    SocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()
                    containerId = container.id
                }
            }
        }

        def portBindings = new HashMap<String, String[]>()
        SocraticCLI.dockerClient.inspectContainerCmd(containerId).exec().networkSettings.ports.bindings.each {
            portBindings.put(it.key.toString(), it.value.collect { it.toString() } as String[])
        }
        return portBindings
    }

    static String getDefaultBabelfishVersion() {
        return "2.13.0"
    }
}
