package io.gitsocratic.command.impl

import com.codebrig.omnisrc.SourceLanguage
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.analysis.DependenceAnalysis
import com.codebrig.phenomena.code.analysis.MetricAnalysis
import com.codebrig.phenomena.code.analysis.semantic.CodeSemanticObserver
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.ExecStartResultCallback
import com.github.dockerjava.core.command.PullImageResultCallback
import com.github.rholder.retry.*
import groovy.transform.ToString
import groovy.transform.builder.Builder
import io.gitsocratic.GitSocraticService
import io.gitsocratic.SocraticCLI
import io.gitsocratic.command.result.InitCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import static io.gitsocratic.command.config.ConfigOption.*

/**
 * Represents the `init` command.
 * Used to initialize services necessary to use GitSocratic.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Builder
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "init",
        description = "Initialize services necessary to use GitSocratic",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Init implements Callable<Integer> {

    @CommandLine.Option(names = ["-gv", "--grakn-version"], description = "Grakn version")
    String graknVersion = defaultGraknVersion

    @CommandLine.Option(names = ["-bv", "--babelfish-version"], description = "Babelfish version")
    String babelfishVersion = defaultBabelfishVersion

    @CommandLine.Option(names = ["-g", "--grakn"], description = "Initialize Grakn")
    boolean initGrakn = defaultInitGrakn

    @CommandLine.Option(names = ["-b", "--babelfish"], description = "Initialize Babelfish")
    boolean initBabelfish = defaultInitBabelfish

    @CommandLine.Option(names = ["-v", "--verbose"], description = "Verbose logging")
    boolean verbose = defaultVerbose

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
                if (it.repoTags?.contains("graknlabs/grakn:$graknVersion") && initGrakn) {
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
                phenomena.setupOntology(SourceLanguage.OmniSRC.getBaseStructureSchemaDefinition())
                println "Base structure installed"

                if (Boolean.valueOf(individual_semantic_roles.value)) {
                    println "Installing individual semantic roles"
                    phenomena.setupOntology(SourceLanguage.OmniSRC.getIndividualSemanticRolesSchemaDefinition())
                    new CodeSemanticObserver().getRules().each {
                        phenomena.setupOntology(it)
                    }
                    println "Individual semantic roles installed"
                }
                if (Boolean.valueOf(actual_semantic_roles.value)) {
                    println "Installing actual semantic roles"
                    phenomena.setupOntology(SourceLanguage.OmniSRC.getActualSemanticRolesSchemaDefinition())
                    new CodeSemanticObserver().getRules().each {
                        phenomena.setupOntology(it)
                    }
                    println "Actual semantic roles installed"
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
                if (it.repoTags?.contains("bblfsh/bblfshd:$babelfishVersion") && initBabelfish) {
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

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    InitCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    private InitCommandResult executeCommand(boolean outputLogging) throws Exception {
        def status = 0
        if (initBabelfish) {
            if (Boolean.valueOf(use_docker_babelfish.getValue())) {
                status = initDockerBabelfish()
                if (status != 0) return new InitCommandResult(status)
            } else {
                status = validateExternalBabelfish()
                if (status != 0) return new InitCommandResult(status)
            }
            println()
        }
        if (initGrakn) {
            if (Boolean.valueOf(use_docker_grakn.getValue())) {
                status = initDockerGrakn()
                if (status != 0) return new InitCommandResult(status)
            } else {
                status = validateExternalGrakn()
                if (status != 0) return new InitCommandResult(status)
            }
        }
        return new InitCommandResult(status)
    }

    class PullImageProgress extends PullImageResultCallback {
        private Set<String> seenStatuses = new HashSet<>()

        @Override
        void onNext(PullResponseItem item) {
            super.onNext(item)

            def status
            if (item.id == null) {
                status = item.status
            } else {
                status = "Id: " + item.id + " - Status: " + item.status
            }
            if (!seenStatuses.contains(status)) {
                println " " + status
                seenStatuses.add(status)
            }
            if (item.progress != null && verbose) println " Id: " + item.id + " - Progress: " + item.progress
        }
    }

    static String getDefaultGraknVersion() {
        return "1.5.1"
    }

    static String getDefaultBabelfishVersion() {
        return "v2.12.0-drivers"
    }

    static boolean getDefaultInitGrakn() {
        return true
    }

    static boolean getDefaultInitBabelfish() {
        return true
    }

    static boolean getDefaultVerbose() {
        return false
    }
}
