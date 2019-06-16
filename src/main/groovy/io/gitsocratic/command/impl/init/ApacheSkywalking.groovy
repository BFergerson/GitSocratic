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
 * Used to initialize the Apache Skywalking service.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "apache_skywalking",
        description = "Initialize Apache Skywalking service",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class ApacheSkywalking implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", arity = "0", description = "Version to initialize")
    private String skywalkingVersion = defaultApacheSkywalkingVersion

    @CommandLine.Option(names = ["-v", "--verbose"], description = "Verbose logging")
    boolean verbose = Init.defaultVerbose

    boolean useServicePorts = Init.defaultUseServicePorts

    @SuppressWarnings("unused")
    protected ApacheSkywalking() {
        //used by Picocli
    }

    ApacheSkywalking(String skywalkingVersion) {
        this.skywalkingVersion = Objects.requireNonNull(skywalkingVersion)
    }

    ApacheSkywalking(String skywalkingVersion, boolean verbose) {
        this.skywalkingVersion = Objects.requireNonNull(skywalkingVersion)
        this.verbose = verbose
    }

    ApacheSkywalking(String skywalkingVersion, boolean verbose, boolean useServicePorts) {
        this.skywalkingVersion = Objects.requireNonNull(skywalkingVersion)
        this.verbose = verbose
        this.useServicePorts = useServicePorts
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
        if (Boolean.valueOf(use_docker_apache_skywalking.getValue())) {
            try {
                def portBindings = initDockerApacheSkywalking(out)
                if (portBindings != null) {
                    return new InitDockerCommandResult(portBindings)
                }
            } catch (all) {
                out.println "Failed to initialize service"
                all.printStackTrace(out)
            }
            return new InitDockerCommandResult(-1)
        } else {
            try {
                def status = validateExternalApacheSkywalking(out)
                return new InitCommandResult(status)
            } catch (all) {
                out.println "Failed to validate external service"
                all.printStackTrace(out)
                return new InitCommandResult(-1)
            }
        }
    }

    private static int validateExternalApacheSkywalking(PrintWriter out) {
        out.println "Validating external Apache Skywalking installation"
        def host = apache_skywalking_host.value
        def port = apache_skywalking_rest_port.value as int
        out.println " Host: $host"
        out.println " Port: $port"

        out.println "Connecting to Apache Skywalking"
        Socket s1 = new Socket()
        try {
            s1.setSoTimeout(200)
            s1.connect(new InetSocketAddress(host, port), 200)
            out.println "Successfully connected to Apache Skywalking"
            //todo: real connection test
            return 0
        } catch (all) {
            out.println "Failed to connect to Apache Skywalking"
            all.printStackTrace(out)
            return -1
        } finally {
            s1.close()
        }
    }

    private Map<String, String[]> initDockerApacheSkywalking(PrintWriter out) {
        out.println "Initializing Apache Skywalking container"

        def dockerRepository = "apache/skywalking-oap-server:$skywalkingVersion"
        def callback = new PullImageProgress(out)
        SocraticCLI.dockerClient.pullImageCmd(dockerRepository).exec(callback)
        callback.awaitCompletion()

        Container skywalkingContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.apache_skywalking.command == it.command) {
                skywalkingContainer = it
            }
        }

        def containerId = null
        if (skywalkingContainer != null) {
            containerId = skywalkingContainer.id
            out.println "Found Apache Skywalking container"
            out.println " Id: " + skywalkingContainer.id

            //start container (if necessary)
            if (skywalkingContainer.state != "running") {
                out.println "Starting Apache Skywalking container"
                SocraticCLI.dockerClient.startContainerCmd(skywalkingContainer.id).exec()
                out.println "Apache Skywalking container started"
            } else {
                out.println "Apache Skywalking already running"
            }
        } else {
            //create container
            List<Image> images = SocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains(dockerRepository)) {
                    ExposedPort grpcPort = ExposedPort.tcp(apache_skywalking_grpc_port.defaultValue as int)
                    ExposedPort restPort = ExposedPort.tcp(apache_skywalking_rest_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    if (useServicePorts) {
                        portBindings.bind(grpcPort, Ports.Binding.bindPort(Integer.parseInt(
                                apache_skywalking_grpc_port.defaultValue)))
                        portBindings.bind(restPort, Ports.Binding.bindPort(Integer.parseInt(
                                apache_skywalking_rest_port.defaultValue)))
                    } else {
                        portBindings.bind(grpcPort, Ports.Binding.empty())
                        portBindings.bind(restPort, Ports.Binding.empty())
                    }
                    CreateContainerResponse container = SocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(grpcPort, restPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .withNetworkMode("host")
                            .exec()
                    SocraticCLI.dockerClient.startContainerCmd(container.id).exec()
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

    static String getDefaultApacheSkywalkingVersion() {
        return "6.0.0-GA"
    }
}
