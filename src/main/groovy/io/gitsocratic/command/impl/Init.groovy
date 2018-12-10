package io.gitsocratic.command.impl

import com.codebrig.omnisrc.SourceLanguage
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.analysis.DependenceAnalysis
import com.codebrig.phenomena.code.analysis.MetricAnalysis
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.command.ExecStartResultCallback
import com.github.dockerjava.core.command.PullImageResultCallback
import com.github.rholder.retry.*
import io.gitsocratic.GitSocraticCLI
import io.gitsocratic.GitSocraticService
import io.gitsocratic.command.config.ConfigOption
import picocli.CommandLine

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Represents the `init` command.
 * Used to initialize services necessary to use GitSocratic.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "init",
        description = "Initialize services necessary to use GitSocratic",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Init implements Callable<Integer> {

    @CommandLine.Option(names = ["-g", "--grakn"], description = "Initialize Grakn")
    private boolean initGrakn = true

    @CommandLine.Option(names = ["-b", "--babelfish"], description = "Initialize Babelfish")
    private boolean initBabelfish = true

    @CommandLine.Option(names = ["-v", "--verbose"], description = "Verbose logging")
    private static boolean verbose = false

    private static int validateExternalGrakn() {
        println "Validating external Grakn installation"
        def host = ConfigOption.babelfish_host.value
        def port = ConfigOption.babelfish_port.value as int
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
        GitSocraticCLI.dockerClient.pullImageCmd("bfergerson/grakn-docker-toolbox:latest").exec(callback)
        callback.awaitCompletion()

        Container graknContainer
        GitSocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
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
                GitSocraticCLI.dockerClient.startContainerCmd(graknContainer.id).exec()
                println "Grakn container started"
            } else {
                println "Grakn already running"
            }
        } else {
            //create container
            List<Image> images = GitSocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains("bfergerson/grakn-docker-toolbox:latest") && initGrakn) {
                    def graknPort = ConfigOption.grakn_port.getValue() as int
                    ExposedPort graknTcpPort = ExposedPort.tcp(ConfigOption.grakn_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    portBindings.bind(graknTcpPort, Ports.Binding.bindPort(graknPort))

                    CreateContainerResponse container = GitSocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(graknTcpPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .exec()
                    GitSocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()

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
                if (Boolean.valueOf(ConfigOption.use_docker_grakn.value)) {
                    phenomena.graknHost = ConfigOption.docker_host.value
                } else {
                    phenomena.graknHost = ConfigOption.grakn_host.value
                }
                phenomena.graknPort = ConfigOption.grakn_port.value as int
                phenomena.graknKeyspace = ConfigOption.grakn_keyspace.value
                phenomena.connectToGrakn()
                println "Successfully connected to Grakn"

                println "Installing base structure"
                phenomena.setupOntology(SourceLanguage.OmniSRC.getBaseStructureSchemaDefinition())
                println "Base structure installed"

                if (Boolean.valueOf(ConfigOption.individual_semantic_roles.value)) {
                    println "Installing individual semantic roles"
                    phenomena.setupOntology(SourceLanguage.OmniSRC.getIndividualSemanticRolesSchemaDefinition())
                    println "Individual semantic roles installed"
                }
                if (Boolean.valueOf(ConfigOption.actual_semantic_roles.value)) {
                    println "Installing actual semantic roles"
                    phenomena.setupOntology(SourceLanguage.OmniSRC.getActualSemanticRolesSchemaDefinition())
                    println "Actual semantic roles installed"
                }

                installObserverSchemas(phenomena)
                phenomena.close()
                return true
            }
        }
        RetryerBuilder.<Boolean> newBuilder()
                .retryIfRuntimeException()
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
        if (Boolean.valueOf(ConfigOption.identifier_access.value)) {
            println "Installing identifier access schema"
            phenomena.setupOntology(DependenceAnalysis.Identifier_Access.schemaDefinition)
            println "Identifier access schema installed"
        }
        if (Boolean.valueOf(ConfigOption.method_call.value)) {
            println "Installing method call schema"
            phenomena.setupOntology(DependenceAnalysis.Method_Call.schemaDefinition)
            println "Method call schema installed"
        }

        //metric observers
        if (Boolean.valueOf(ConfigOption.cyclomatic_complexity.value)) {
            println "Installing cyclomatic complexity schema"
            phenomena.setupOntology(MetricAnalysis.Cyclomatic_Complexity.schemaDefinition)
            println "Cyclomatic complexity schema installed"
        }
    }

    private static int validateExternalBabelfish() {
        println "Validating external Babelfish installation"
        def host = ConfigOption.babelfish_host.value
        def port = ConfigOption.babelfish_port.value as int
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
        GitSocraticCLI.dockerClient.pullImageCmd("bblfsh/bblfshd:latest").exec(callback)
        callback.awaitCompletion()

        Container babelfishContainer
        GitSocraticCLI.dockerClient.listContainersCmd().withShowAll(true).exec().each {
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
                GitSocraticCLI.dockerClient.startContainerCmd(babelfishContainer.id).exec()
                println "Babelfish container started"
            } else {
                println "Babelfish already running"
            }
        } else {
            //create container
            List<Image> images = GitSocraticCLI.dockerClient.listImagesCmd().withShowAll(true).exec()
            images.each {
                if (it.repoTags?.contains("bblfsh/bblfshd:latest") && initBabelfish) {
                    def babelfishPort = ConfigOption.babelfish_port.getValue() as int
                    ExposedPort babelfishTcpPort = ExposedPort.tcp(ConfigOption.babelfish_port.defaultValue as int)
                    Ports portBindings = new Ports()
                    portBindings.bind(babelfishTcpPort, Ports.Binding.bindPort(babelfishPort))
                    CreateContainerResponse container = GitSocraticCLI.dockerClient.createContainerCmd(it.id)
                            .withPrivileged(true)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withExposedPorts(babelfishTcpPort)
                            .withPortBindings(portBindings)
                            .withPublishAllPorts(true)
                            .exec()
                    GitSocraticCLI.dockerClient.startContainerCmd(container.getId()).exec()

                    //auto-install recommended language drivers
                    ExecCreateCmdResponse execCreateCmdResponse =
                            GitSocraticCLI.dockerClient.execCreateCmd(container.id).withCmd("bblfshctl", "driver", "install", "--recommended")
                                    .withTty(true)
                                    .withPrivileged(true)
                                    .exec()
                    GitSocraticCLI.dockerClient.execStartCmd(execCreateCmdResponse.getId())
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
        def status = 0
        if (initBabelfish) {
            if (Boolean.valueOf(ConfigOption.use_docker_babelfish.getValue())) {
                status = initDockerBabelfish()
                if (status != 0) return status
            } else {
                status = validateExternalBabelfish()
                if (status != 0) return status
            }
            println()
        }
        if (initGrakn) {
            if (Boolean.valueOf(ConfigOption.use_docker_grakn.getValue())) {
                status = initDockerGrakn()
                if (status != 0) return status
            } else {
                status = validateExternalGrakn()
                if (status != 0) return status
            }
        }
        return status
    }

    static class PullImageProgress extends PullImageResultCallback {
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
}
