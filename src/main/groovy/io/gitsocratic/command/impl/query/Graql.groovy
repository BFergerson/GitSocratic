package io.gitsocratic.command.impl.query

import grakn.core.concept.answer.Numeric
import groovy.transform.ToString
import io.gitsocratic.client.GraknClient
import picocli.CommandLine

import java.time.Duration
import java.util.concurrent.Callable

/**
 * Represents the `graql` command.
 * Used to execute a single Graql query.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "graql",
        description = "Execute a single Graql query",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Graql implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Query to run")
    String query

    @Override
    Integer call() throws Exception {
        if (query == null || query.isEmpty()) {
            System.err.println("Missing Graql query")
            return -1
        }
        def startTime = System.currentTimeMillis()
        def graknClient = new GraknClient()
        def tx = graknClient.makeReadSession()

        try {
            println "# Query: $query"
            println "# Result:"
            def result = graknClient.executeQuery(tx, query)
            if (result.size() == 1 && result.get(0) instanceof Numeric) {
                println((result.get(0) as Numeric).number())
            } else if (!result.isEmpty()) {
                result.each {
                    it.forEach({ key, value ->
                        println key.toString() + " = " + value.asAttribute().value().toString()
                    })
                }
            } else {
                println "N/A"
            }
            println "\n# Query time: " + humanReadableFormat(Duration.ofMillis(System.currentTimeMillis() - startTime))
            return 0
        } finally {
            tx.close()
            graknClient.close()
        }
    }

    static String humanReadableFormat(Duration duration) {
        return duration.toString().substring(2)
                .replaceAll('(\\d[HMS])(?!$)', '$1 ')
                .toLowerCase()
    }
}
