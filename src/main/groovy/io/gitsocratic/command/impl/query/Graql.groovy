package io.gitsocratic.command.impl.query

import ai.grakn.graql.answer.Value
import io.gitsocratic.client.GraknClient
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "graql",
        description = "Execute a single Graql query",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Graql implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Query to run")
    private String query

    @Override
    Integer call() throws Exception {
        if (query == null || query.isEmpty()) {
            System.err.println("Missing Graql query")
            return -1
        }

        def startTime = System.currentTimeMillis()
        def graknClient = new GraknClient()
        try {
            println "# Query: $query"
            println "# Result:"
            def result = graknClient.executeQuery(query)
            if (result.get(0) instanceof Value) {
                def number = (result.get(0) as Value).number()
                println number
            } else {
                println result
            }
            println "# Query time: " + (System.currentTimeMillis() - startTime) + "ms"
            return 0
        } finally {
            graknClient.close()
        }
    }
}
