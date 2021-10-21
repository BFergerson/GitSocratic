package io.gitsocratic.command.impl.query

import com.vaticle.typedb.client.api.answer.ConceptMap
import com.vaticle.typedb.client.api.answer.Numeric
import com.vaticle.typedb.client.api.query.QueryFuture
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.result.QueryCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `graql` command.
 * Used to execute a single Graql query.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "graql",
        description = "Execute a single Graql query",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Graql implements Callable<Integer> {

//    @CommandLine.Parameters(index = "0", description = "Query to run")
    private String query

    @CommandLine.Option(names = ["-f", "--file"], description = "Query file to run")
    private File file

    @SuppressWarnings("unused")
    protected Graql() {
        //used by Picocli
    }

    Graql(String query) {
        this.query = Objects.requireNonNull(query)
    }

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    QueryCommandResult.GraqlResponse execute() throws Exception {
        return executeCommand(false)
    }

    private QueryCommandResult.GraqlResponse executeCommand(boolean outputLogging) throws Exception {
        if (file != null) query = file.text
        if (query == null || query.isEmpty()) {
            if (outputLogging) log.error "Missing Graql query"
            return new QueryCommandResult.GraqlResponse(-1)
        }

        def startTime = System.currentTimeMillis()

        try (def graknClient = new GraknClient()) {
            try (def tx = graknClient.makeReadSession()) {
                //if (outputLogging) log.info "# Query: " + query
                def result = graknClient.executeQuery(tx, query)
                def queryTimeMs = System.currentTimeMillis() - startTime
                //if (outputLogging) log.info "# Query Time: " + (queryTimeMs / 1000.0) + "s"

                if (result.size() == 1 && result.get(0) instanceof Numeric) {
                    if (outputLogging) log.info("Result: " + (result.get(0) as Numeric).asLong().longValue() as String)
                } else if (result.size() == 1 && result.get(0) instanceof QueryFuture) {
                    def value = (result.get(0) as QueryFuture).get()
                    result = [value as Numeric] as List<ConceptMap>
                    if (outputLogging) log.info("Result: " + (result.get(0) as Numeric).asLong().longValue() as String)
                } else if (!result.isEmpty()) {
                    if (outputLogging) log.info "Result:"
                    result.each {
                        it.map().forEach({ key, value ->
                            if (outputLogging) log.info "\t" + key.toString() + " = " + value.asAttribute().value.toString()
                        })
                    }
                } else {
                    if (outputLogging) log.info "Result: N/A"
                }
                return new QueryCommandResult.GraqlResponse(0, result, queryTimeMs)
            }
        }
    }

    String getQuery() {
        return query
    }
}
