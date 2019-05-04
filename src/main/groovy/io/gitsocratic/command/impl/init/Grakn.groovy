package io.gitsocratic.command.impl.init

import com.codebrig.arthur.SourceLanguage
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.analysis.DependenceAnalysis
import com.codebrig.phenomena.code.analysis.MetricAnalysis
import com.codebrig.phenomena.code.analysis.semantic.CodeSemanticObserver
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.ExposedPort
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
import picocli.CommandLine

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static io.gitsocratic.command.config.ConfigOption.*

/**
 * Used to initialize the Grakn service.
 *
 * @version 0.2
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
        def status = -1
        def out = new GroovyPrintWriter(output, true)
        try {
            if (Boolean.valueOf(use_docker_grakn.getValue())) {
                status = initDockerGrakn(out)
                if (status != 0) return new InitCommandResult(status)
            } else {
                status = validateExternalGrakn(out)
                if (status != 0) return new InitCommandResult(status)
            }
        } catch (all) {
            out.println "Failed to initialize service"
            all.printStackTrace(out)
        }
        return new InitCommandResult(status)
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

    private int initDockerGrakn(PrintWriter out) {
        out.println "Initializing Grakn container"
        def callback = new PullImageProgress(out)
        SocraticCLI.dockerClient.pullImageCmd("graknlabs/grakn:$graknVersion").exec(callback)
        callback.awaitCompletion()

        Container graknContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.grakn.command == it.command) {
                graknContainer = it
            }
        }

        if (graknContainer != null) {
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
                if (it.repoTags?.contains("graknlabs/grakn:$graknVersion")) {
                    def graknPort = grakn_port.getValue() as int
                    ExposedPort graknTcpPort = ExposedPort.tcp(grakn_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    portBindings.bind(graknTcpPort, Ports.Binding.bindPort(graknPort))

                    CreateContainerResponse container = SocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(graknTcpPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .exec()
                    SocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()

                    out.println "Waiting for Grakn to start"
                    Thread.sleep(10 * 1000) //todo: smarter
                }
            }
        }
        setupGraknOntology()
        return 0
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
        return "1.5.2"
    }
}
