package io.gitsocratic.client

import com.codebrig.arthur.SourceLanguage
import com.codebrig.arthur.observe.structure.filter.FunctionFilter
import com.codebrig.arthur.observe.structure.filter.MultiFilter
import com.codebrig.arthur.observe.structure.filter.RoleFilter
import com.codebrig.phenomena.ParseException
import com.codebrig.phenomena.Phenomena
import com.codebrig.phenomena.code.CodeObserver
import com.codebrig.phenomena.code.analysis.DependenceAnalysis
import com.codebrig.phenomena.code.analysis.MetricAnalysis
import com.codebrig.phenomena.code.analysis.semantic.CodeSemanticObserver
import com.codebrig.phenomena.code.structure.CodeStructureObserver
import groovyx.gpars.GParsPool

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

import static io.gitsocratic.command.config.ConfigOption.*

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class PhenomenaClient implements Closeable {

    private final String repoLocation
    private final Phenomena phenomena

    PhenomenaClient(String repoLocation) throws ConnectException {
        this.repoLocation = repoLocation
        this.phenomena = setupPhenomena()
    }

    private Phenomena setupPhenomena() throws ConnectException {
        def phenomena = new Phenomena()
        if (Boolean.valueOf(use_docker_grakn.value)) {
            phenomena.graknHost = docker_host.value
        } else {
            phenomena.graknHost = grakn_host.value
        }
        phenomena.graknPort = grakn_port.value as int
        phenomena.graknKeyspace = grakn_keyspace.value

        if (Boolean.valueOf(use_docker_babelfish.value)) {
            phenomena.babelfishHost = docker_host.value
        } else {
            phenomena.babelfishHost = babelfish_host.value
        }
        phenomena.babelfishPort = babelfish_port.value as int
        phenomena.scanPath = new ArrayList<>()
        phenomena.scanPath.add(repoLocation)

        //setup observers
        def codeObservers = new ArrayList<CodeObserver>()

        //dependence observers
        def dependenceAnalyses = new ArrayList<DependenceAnalysis>()
        if (Boolean.valueOf(identifier_access.value)) {
            dependenceAnalyses.add(DependenceAnalysis.Identifier_Access)
            println "Observing identifier access"
        }
        if (Boolean.valueOf(method_call.value)) {
            dependenceAnalyses.add(DependenceAnalysis.Method_Call)
            println "Observing method calls"
        }
        codeObservers.addAll(DependenceAnalysis.getCodeObserversByAnalysis(phenomena, dependenceAnalyses))

        //metric observers
        def metricAnalyses = new ArrayList<MetricAnalysis>()
        if (Boolean.valueOf(cyclomatic_complexity.value)) {
            metricAnalyses.add(MetricAnalysis.Cyclomatic_Complexity)
            println "Observing cyclomatic complexity"
        }
        codeObservers.addAll(MetricAnalysis.getCodeObserversByAnalysis(phenomena, metricAnalyses))

        //structure observer
        if (source_schema.value.contains("necessary")) {
            def necessaryStructureFilter = new MultiFilter(MultiFilter.MatchStyle.ANY)
            if (source_schema.value.contains("files")) {
                necessaryStructureFilter.accept(new RoleFilter("FILE", "MODULE"))
            }
            if (source_schema.value.contains("functions")) {
                necessaryStructureFilter.accept(new FunctionFilter())
            }
            codeObservers.each {
                necessaryStructureFilter.accept(it.filter)
            }
            codeObservers.add(new CodeStructureObserver(necessaryStructureFilter))

            if (Boolean.valueOf(semantic_roles.value)) {
                codeObservers.add(new CodeSemanticObserver())
            }
        } else {
            def hasFilter = false
            def filter = new MultiFilter(MultiFilter.MatchStyle.ANY)
            if (source_schema.value.contains("files")) {
                filter.accept(new RoleFilter("FILE", "MODULE"))
                hasFilter = true
            }
            if (source_schema.value.contains("functions")) {
                filter.accept(new FunctionFilter())
                hasFilter = true
            }

            if (hasFilter) {
                codeObservers.add(new CodeStructureObserver(filter))
            } else {
                codeObservers.add(new CodeStructureObserver())
            }
            if (Boolean.valueOf(semantic_roles.value)) {
                codeObservers.add(new CodeSemanticObserver())
            }
        }
        phenomena.init(codeObservers)
        return phenomena
    }

    void processSourceCodeRepository(boolean parallelProcessing) {
        long startTime = System.currentTimeMillis()
        def processedCount = new AtomicInteger(0)
        def failCount = new AtomicInteger(0)
        if (parallelProcessing) {
            GParsPool.withPool {
                phenomena.sourceFilesInScanPath.eachParallel { File file ->
                    processSourceCodeFile(file, processedCount, failCount)
                }
            }
        } else {
            phenomena.sourceFilesInScanPath.each { File file ->
                processSourceCodeFile(file, processedCount, failCount)
            }
        }
        println "Processed files: $processedCount"
        println "Failed files: $failCount"
        println "Processing time: " + humanReadableFormat(Duration.ofMillis(System.currentTimeMillis() - startTime))
    }

    private void processSourceCodeFile(File file, AtomicInteger processedCount, AtomicInteger failCount) {
        try {
            def processedFile = phenomena.processSourceFile(file, SourceLanguage.getSourceLanguage(file))
            def sourceFile = processedFile.sourceFile
            if (processedFile.parseResponse.status().isOk()) {
                println "Processed $sourceFile - Root node id: " + processedFile.rootNodeId
                processedCount.getAndIncrement()
            } else {
                failCount.getAndIncrement()
                System.err.println("Failed to parse file: $sourceFile - Reason: "
                        + processedFile.parseResponse.errors().toString())
            }
        } catch (ParseException e) {
            failCount.getAndIncrement()
            System.err.println("Failed to parse file: " + e.sourceFile + " - Reason: "
                    + e.parseResponse.errors().toString())
        } catch (all) {
            System.err.println("Failed to parse file: " + file + " - Reason: " + all.message)
            all.printStackTrace()
            failCount.getAndIncrement()
        }
    }

    @Override
    void close() {
        phenomena?.close()
    }

    private static String humanReadableFormat(Duration duration) {
        return duration.toString().substring(2)
                .replaceAll('(\\d[HMS])(?!$)', '$1 ')
                .toLowerCase()
    }
}
