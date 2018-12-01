package io.gitsocratic.command.impl

import com.codebrig.omnisrc.SourceLanguage
import com.codebrig.omnisrc.observe.filter.FunctionFilter
import com.codebrig.omnisrc.observe.filter.MultiFilter
import com.codebrig.omnisrc.observe.filter.RoleFilter
import com.codebrig.phenomena.ParseException
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.CodeObserver
import com.codebrig.phenomena.code.analysis.metric.CyclomaticComplexity
import com.codebrig.phenomena.code.structure.CodeStructureObserver
import groovyx.gpars.GParsPool
import io.gitsocratic.command.config.ConfigOption
import org.eclipse.jgit.api.Git
import picocli.CommandLine

import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "add-remote-repo",
        description = "Add remote source code repository to the knowledge graph",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class AddRemoteRepo implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The repository to add")
    private String repoName

    @CommandLine.Option(names = ["-p", "--parallel"], description = "Use parallel source code processing")
    private boolean parallelProcessing = false

    @Override
    Integer call() throws Exception {
        long startTime = System.currentTimeMillis()

        //clone repo
        new File("/tmp/gitsocratic/out/").deleteDir()
        cloneRepo(repoName, new File("/tmp/gitsocratic/out/"))

        //import source code into grakn
        def phenomena = setupPhenomena()
        def processedCount = new AtomicInteger(0)
        def failCount = new AtomicInteger(0)
        GParsPool.withPool {
            if (parallelProcessing) {
                phenomena.sourceFilesInScanPath.eachParallel { File file ->
                    handleSourceCodeFile(phenomena, file, processedCount, failCount)
                }
            } else {
                phenomena.sourceFilesInScanPath.each { File file ->
                    handleSourceCodeFile(phenomena, file, processedCount, failCount)
                }
            }
        }
        println "Processed files: $processedCount"
        println "Failed files: $failCount"
        println "Processing time: " + humanReadableFormat(Duration.ofMillis(System.currentTimeMillis() - startTime))
        phenomena.close()
        return 0
    }

    private static Phenomena setupPhenomena() {
        def phenomena = new Phenomena()
        phenomena.graknHost = ConfigOption.grakn_host.value
        phenomena.graknPort = ConfigOption.grakn_port.value as int
        phenomena.graknKeyspace = ConfigOption.grakn_keyspace.value
        phenomena.babelfishHost = ConfigOption.babelfish_host.value
        phenomena.babelfishPort = ConfigOption.babelfish_port.value as int
        phenomena.scanPath = new ArrayList<>()
        phenomena.scanPath.add("/tmp/gitsocratic/out/")

        //setup observers
        def codeObservers = new ArrayList<CodeObserver>()
        def necessaryStructureFilter = new MultiFilter(MultiFilter.MatchStyle.ANY)
//
//        //dependence observers
//        if (Boolean.valueOf(ConfigOption.identifier_access.value)) {
//            println "Installing identifier access schema"
//            phenomena.setupOntology(IdentifierAccessObserver.fullSchema)
//            println "Identifier access schema installed"
//        }
//        if (Boolean.valueOf(ConfigOption.method_call.value)) {
//            println "Installing identifier access schema"
//            phenomena.setupOntology(MethodCallObserver.fullSchema)
//            println "Identifier access schema installed"
//        }

        //metric observers
        if (Boolean.valueOf(ConfigOption.cyclomatic_complexity.value)) {
            def observer = new CyclomaticComplexity()
            necessaryStructureFilter.accept(observer.getFilter())
            codeObservers.add(observer)
            println "Observing cyclomatic complexity"
        }

        //structure observer
        if (ConfigOption.source_schema.value.contains("necessary")) {
            if (ConfigOption.source_schema.value.contains("files")) {
                necessaryStructureFilter.accept(new RoleFilter("FILE"))
            }
            if (ConfigOption.source_schema.value.contains("functions")) {
                necessaryStructureFilter.accept(new FunctionFilter())
            }

            codeObservers.add(new CodeStructureObserver(necessaryStructureFilter))
        } else {
            def hasFilter = false
            def filter = new MultiFilter(MultiFilter.MatchStyle.ANY)
            if (ConfigOption.source_schema.value.contains("files")) {
                filter.accept(new RoleFilter("FILE"))
                hasFilter = true
            }
            if (ConfigOption.source_schema.value.contains("functions")) {
                filter.accept(new FunctionFilter())
                hasFilter = true
            }

            if (hasFilter) {
                codeObservers.add(new CodeStructureObserver(filter))
            } else {
                codeObservers.add(new CodeStructureObserver())
            }
        }
        phenomena.init(codeObservers)
        return phenomena
    }

    static void handleSourceCodeFile(Phenomena phenomena, File file,
                                     AtomicInteger processedCount, AtomicInteger failCount) {
        try {
            def processedFile = phenomena.processSourceFile(file, SourceLanguage.getSourceLanguage(file))
            def sourceFile = processedFile.sourceFile
            if (processedFile.parseResponse.status().isOk()) {
                println "Processed $sourceFile - Root node id: " + processedFile.rootNodeId
                processedCount.getAndIncrement()
            } else {
                failCount.getAndIncrement()
                System.err.println("Failed to parse file: $sourceFile - Reason: " + processedFile.parseResponse.errors().toString())
            }
        } catch (ParseException e) {
            failCount.getAndIncrement()
            System.err.println("Failed to parse file: " + e.sourceFile + " - Reason: " + e.parseResponse.errors().toString())
        } catch (all) {
            all.printStackTrace()
            failCount.getAndIncrement()
        }
    }

    static void cloneRepo(String githubRepository, File outputDirectory) {
        println "Cloning: $githubRepository"
        if (githubRepository.startsWith("http")) {
            Git.cloneRepository()
                    .setURI(githubRepository)
                    .setDirectory(outputDirectory)
                    .setCloneSubmodules(true)
                    .setTimeout(TimeUnit.MINUTES.toSeconds(5) as int)
                    .call()
        } else {
            Git.cloneRepository()
                    .setURI("https://github.com/" + githubRepository + ".git")
                    .setDirectory(outputDirectory)
                    .setCloneSubmodules(true)
                    .setTimeout(TimeUnit.MINUTES.toSeconds(5) as int)
                    .call()
        }
        println "Cloned: $githubRepository"
    }

    static String humanReadableFormat(Duration duration) {
        return duration.toString().substring(2)
                .replaceAll('(\\d[HMS])(?!$)', '$1 ')
                .toLowerCase()
    }
}
