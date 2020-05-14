package io.gitsocratic.command.impl.init

import com.codebrig.arthur.SourceLanguage
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.analysis.DependenceAnalysis
import com.codebrig.phenomena.code.analysis.MetricAnalysis
import com.codebrig.phenomena.code.analysis.semantic.CodeSemanticObserver
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Ports
import com.github.rholder.retry.*
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
import java.util.concurrent.TimeUnit

import static io.gitsocratic.command.config.ConfigOption.*

/**
 * Used to initialize the Grakn service.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "grakn",
        description = "Initialize Grakn service",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Grakn implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", arity = "0", description = "Version to initialize")
    private String graknVersion = defaultGraknVersion

    @CommandLine.Option(names = ["-v", "--verbose"], description = "Verbose logging")
    boolean verbose = Init.defaultVerbose

    private boolean useServicePorts = Init.defaultUseServicePorts

    @SuppressWarnings("unused")
    protected Grakn() {
        //used by Picocli
    }

    Grakn(String graknVersion) {
        this.graknVersion = Objects.requireNonNull(graknVersion)
    }

    Grakn(String graknVersion, boolean verbose) {
        this.graknVersion = Objects.requireNonNull(graknVersion)
        this.verbose = verbose
    }

    Grakn(String graknVersion, boolean verbose, boolean useServicePorts) {
        this.graknVersion = Objects.requireNonNull(graknVersion)
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
        if (Boolean.valueOf(use_docker_grakn.getValue())) {
            try {
                def portBindings = initDockerGrakn(out)
                if (portBindings != null) {
                    return new InitDockerCommandResult("Grakn", portBindings)
                }
            } catch (all) {
                out.println "Failed to initialize service"
                all.printStackTrace(out)
            }
            return new InitDockerCommandResult("Grakn", -1)
        } else {
            try {
                def status = validateExternalGrakn(out)
                return new InitCommandResult(status)
            } catch (all) {
                out.println "Failed to validate external service"
                all.printStackTrace(out)
                return new InitCommandResult(-1)
            }
        }
    }

    private static int validateExternalGrakn(PrintWriter out) {
        out.println "Validating external Grakn installation"
        def host = babelfish_host.value
        def port = babelfish_port.value as int
        out.println " Host: $host"
        out.println " Port: $port"

        try {
            setupGraknOntology()
            return 0
        } catch (all) {
            out.println "Failed to connect to Grakn"
            all.printStackTrace(out)
            return -1
        }
    }

    private Map<String, String[]> initDockerGrakn(PrintWriter out) {
        out.println "Initializing Grakn container"

        def dockerRepository = "graknlabs/grakn:$graknVersion"
        def callback = new PullImageProgress(out)
        SocraticCLI.dockerClient.pullImageCmd(dockerRepository).exec(callback)
        callback.awaitCompletion()

        Container graknContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.grakn.command == it.command) {
                graknContainer = it
            }
        }

        def containerId = null
        if (graknContainer != null) {
            containerId = graknContainer.id
            out.println "Found Grakn container"
            out.println " Id: " + graknContainer.id

            //start container (if necessary)
            if (graknContainer.state != "running") {
                out.println "Starting Grakn container"
                SocraticCLI.dockerClient.startContainerCmd(graknContainer.id).exec()
                out.println "Grakn container started"
            } else {
                out.println "Grakn already running"
            }
        } else {
            //create container
            List<Image> images = SocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains(dockerRepository)) {
                    ExposedPort graknTcpPort = ExposedPort.tcp(grakn_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    if (useServicePorts) {
                        portBindings.bind(graknTcpPort, Ports.Binding.bindPort(Integer.parseInt(
                                grakn_port.defaultValue)))
                    } else {
                        portBindings.bind(graknTcpPort, Ports.Binding.empty())
                    }
                    def createContainerCommand = SocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(graknTcpPort)
                            .withHostConfig(HostConfig.newHostConfig()
                                    .withPortBindings(portBindings)
                                    .withPublishAllPorts(true)
                            )
                    if (docker_grakn_hostname.getValue() != null) {
                        createContainerCommand = createContainerCommand
                                .withHostName(docker_grakn_hostname.getValue())
                    }

                    def container = createContainerCommand.exec()
                    SocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()
                    out.println "Waiting for Grakn to start"
                    Thread.sleep(10 * 1000) //todo: smarter
                    containerId = container.id
                }
            }
        }
        setupGraknOntology()

        def portBindings = new HashMap<String, String[]>()
        SocraticCLI.dockerClient.inspectContainerCmd(containerId).exec().networkSettings.ports.bindings.each {
            portBindings.put(it.key.toString(), it.value.collect { it.toString() } as String[])
        }
        return portBindings
    }

    private static void setupGraknOntology(PrintWriter out) {
        Callable<Boolean> setupOntology = new Callable<Boolean>() {
            Boolean call() throws Exception {
                def phenomena = new Phenomena()
                if (Boolean.valueOf(use_docker_grakn.value)) {
                    phenomena.graknHost = docker_host.value
                } else {
                    phenomena.graknHost = grakn_host.value
                }
                phenomena.graknPort = grakn_port.value as int
                phenomena.graknKeyspace = grakn_keyspace.value
                phenomena.connectToGrakn()
                out.println "Successfully connected to Grakn"

                out.println "Installing base structure"
                phenomena.setupOntology(SourceLanguage.Omnilingual.getBaseStructureSchemaDefinition())
                out.println "Base structure installed"

                if (Boolean.valueOf(semantic_roles.value)) {
                    out.println "Installing semantic roles"
                    phenomena.setupOntology(SourceLanguage.Omnilingual.getSemanticRolesSchemaDefinition())
                    new CodeSemanticObserver().getRules().each {
                        phenomena.setupOntology(it)
                    }
                    out.println "Semantic roles installed"
                }
                installObserverSchemas(out, phenomena)
                phenomena.close()
                return true
            }
        }
        RetryerBuilder.<Boolean> newBuilder()
                .retryIfExceptionOfType(ConnectException.class)
                .withWaitStrategy(WaitStrategies.fixedWait(15, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .withRetryListener(new RetryListener() {
            @Override
            void onRetry(Attempt attempt) {
                if (attempt.hasException()) {
                    out.println "Ontology setup failed. Retrying ontology setup in 15 seconds..."
                }
            }
        }).build().call(setupOntology)
    }

    private static void installObserverSchemas(PrintWriter out, Phenomena phenomena) {
        //dependence observers
        if (Boolean.valueOf(identifier_access.value)) {
            out.println "Installing identifier access schema"
            phenomena.setupOntology(DependenceAnalysis.Identifier_Access.schemaDefinition)
            out.println "Identifier access schema installed"
        }
        if (Boolean.valueOf(method_call.value)) {
            out.println "Installing method call schema"
            phenomena.setupOntology(DependenceAnalysis.Method_Call.schemaDefinition)
            out.println "Method call schema installed"
        }

        //metric observers
        if (Boolean.valueOf(cyclomatic_complexity.value)) {
            out.println "Installing cyclomatic complexity schema"
            phenomena.setupOntology(MetricAnalysis.Cyclomatic_Complexity.schemaDefinition)
            out.println "Cyclomatic complexity schema installed"
        }
    }

    static String getDefaultGraknVersion() {
        return "1.7.1"
    }
}
