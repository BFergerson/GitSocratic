package io.gitsocratic.command.impl

import com.codebrig.omnisrc.SourceLanguage
import com.codebrig.phenomena.ParsedSourceFile
import com.codebrig.phenomena.Phenomena
import gopkg.in.bblfsh.sdk.v1.uast.generated.Node
import groovyx.gpars.GParsPool
import io.gitsocratic.command.config.ConfigOption
import org.bblfsh.client.BblfshClient
import picocli.CommandLine
import scala.collection.JavaConverters

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "add-local-repo",
        description = "Add local source code repository to the knowledge graph",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class AddLocalRepo implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The repository to add")
    private File repoLocation

    @Override
    Integer call() throws Exception {
        if (!repoLocation.exists()) {
            System.err.println "Invalid repository location: $repoLocation"
            return -1
        }

        //scan repo for source code
        def phenomena = new Phenomena()
        phenomena.graknHost = ConfigOption.grakn_host.value
        phenomena.graknPort = ConfigOption.grakn_port.value as int
        phenomena.graknKeyspace = ConfigOption.grakn_keyspace.value
        phenomena.babelfishHost = ConfigOption.babelfish_host.value
        phenomena.babelfishPort = ConfigOption.babelfish_port.value as int
        phenomena.scanPath = new ArrayList<>()
        phenomena.scanPath.add(repoLocation.absolutePath)
        phenomena.init()

        //import into grakn
        def successCount = 0
        def failCount = 0
        def parsedFiles = new ArrayList<ParsedSourceFile>()
        GParsPool.withPool {
            phenomena.sourceFilesInScanPath.eachParallel {
                try {
                    def resp = phenomena.parseSourceFile(it, SourceLanguage.getSourceLangauge(it))
                    def parsedSourceFile = new ParsedSourceFile()
                    parsedSourceFile.sourceFile = it
                    parsedSourceFile.parseResponse = resp
                    parsedFiles.add(parsedSourceFile)
                } catch (Exception e) {
                    println "Failed on file: " + it
                    e.printStackTrace()
                }
            }

            def doneList = new ArrayList<ArrayList<Node>>()
            parsedFiles.each {
                def uastList = new ArrayList<Node>()
                asJavaIterator(BblfshClient.iterator(it.parseResponse.uast, BblfshClient.PostOrder())).each {
                    uastList.add(it)
                }
                doneList.add(uastList)
            }
            doneList.eachWithIndexParallel { uast, i ->
                def file = parsedFiles.get(i).sourceFile
                try {
                    def rootId = phenomena.processUAST(uast, SourceLanguage.getSourceLangauge(file))
                    println "Saved UAST of  $file - Root id: " + rootId
                    parsedFiles.get(i).rootNodeId = rootId
                    successCount++
                } catch (Exception e) {
                    failCount++
                    println "Failed on file: " + file
                    e.printStackTrace()
                }
            }
        }

        println "Success files: $successCount"
        println "Failed files: $failCount"
        return 0
    }

    private static <T> Iterator<T> asJavaIterator(scala.collection.Iterator<T> scalaIterator) {
        return JavaConverters.asJavaIteratorConverter(scalaIterator).asJava()
    }
}
