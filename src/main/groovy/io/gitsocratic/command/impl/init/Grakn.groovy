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
import groovy.transform.ToString
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
        return executeCommand(true).status
    }

    InitCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    InitCommandResult executeCommand(boolean outputLogging) throws Exception {
        def status
        if (Boolean.valueOf(use_docker_grakn.getValue())) {
            status = initDockerGrakn()
            if (status != 0) return new InitCommandResult(status)
        } else {
            status = validateExternalGrakn()
            if (status != 0) return new InitCommandResult(status)
        }
        return new InitCommandResult(status)
    }

    private static int validateExternalGrakn() {
        println "Validating external Grakn installation"
        def host = babelfish_host.value
        def port = babelfish_port.value as int
        println " Host: $host"
        println " Port: $port"

        try {
            setupGraknOntology()
            return 0
        } catch (all) {
            println "Failed to connect to Grakn"
            all.printStackTrace()
            return -1
        }
    }

    private int initDockerGrakn() {
        println "Initializing Grakn container"
        def callback = new PullImageProgress()
        SocraticCLI.dockerClient.pullImageCmd("graknlabs/grakn:$graknVersion").exec(callback)
        callback.awaitCompletion()

        Container graknContainer
        SocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
            if (GitSocraticService.grakn.command == it.command) {
                graknContainer = it
            }
        }

        if (graknContainer != null) {
            println "Found Grakn container"
            println " Id: " + graknContainer.id

            //start container (if necessary)
            if (graknContainer.state != "running") {
                println "Starting Grakn container"
                SocraticCLI.dockerClient.startContainerCmd(graknContainer.id).exec()
                println "Grakn container started"
            } else {
                println "Grakn already running"
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

                    println "Waiting for Grakn to start"
                    Thread.sleep(10 * 1000) //todo: smarter
                }
            }
        }
        setupGraknOntology()
        return 0
    }

    private static void setupGraknOntology() {
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
                println "Successfully connected to Grakn"

                println "Installing base structure"
                phenomena.setupOntology(SourceLanguage.Omnilingual.getBaseStructureSchemaDefinition())
                println "Base structure installed"

                if (Boolean.valueOf(semantic_roles.value)) {
                    println "Installing semantic roles"
                    phenomena.setupOntology(SourceLanguage.Omnilingual.getSemanticRolesSchemaDefinition())
                    new CodeSemanticObserver().getRules().each {
                        phenomena.setupOntology(it)
                    }
                    println "Semantic roles installed"
                }
                installObserverSchemas(phenomena)
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
                    println "Ontology setup failed. Retrying ontology setup in 15 seconds..."
                }
            }
        }).build().call(setupOntology)
    }

    private static void installObserverSchemas(Phenomena phenomena) {
        //dependence observers
        if (Boolean.valueOf(identifier_access.value)) {
            println "Installing identifier access schema"
            phenomena.setupOntology(DependenceAnalysis.Identifier_Access.schemaDefinition)
            println "Identifier access schema installed"
        }
        if (Boolean.valueOf(method_call.value)) {
            println "Installing method call schema"
            phenomena.setupOntology(DependenceAnalysis.Method_Call.schemaDefinition)
            println "Method call schema installed"
        }

        //metric observers
        if (Boolean.valueOf(cyclomatic_complexity.value)) {
            println "Installing cyclomatic complexity schema"
            phenomena.setupOntology(MetricAnalysis.Cyclomatic_Complexity.schemaDefinition)
            println "Cyclomatic complexity schema installed"
        }
    }

    static String getDefaultGraknVersion() {
        return "1.5.2"
    }
}
