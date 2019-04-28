package io.gitsocratic.command.impl

import grakn.core.concept.answer.Numeric
import groovy.transform.ToString
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.question.SourceQuestion
import io.gitsocratic.command.result.QuestionCommandResult
import picocli.CommandLine

import java.time.Duration
import java.util.concurrent.Callable

/**
 * Represents the `question` command.
 * Used to execute a single source code question.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "question",
        description = "Execute a single source code question",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Question implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", converter = QuestionTypeConverter.class)
    private SourceQuestion question

    @SuppressWarnings("unused")
    protected Question() {
        //used by Picocli
    }

    Question(SourceQuestion question) {
        this.question = Objects.requireNonNull(question)
    }

    @Override
    Integer call() throws Exception {
        return executeCommand(true).status
    }

    QuestionCommandResult execute() throws Exception {
        return executeCommand(false)
    }

    private QuestionCommandResult executeCommand(boolean outputLogging) throws Exception {
        def startTime = System.currentTimeMillis()
        def query = question.query
        def graknClient = new GraknClient()
        def tx = graknClient.makeReadSession()

        try {
            if (outputLogging) println "# Question: " + question.formattedQuestion
            if (outputLogging) println "# Result:"
            def result = graknClient.executeQuery(tx, query)
            def queryTimeMs = System.currentTimeMillis() - startTime

            if (outputLogging) {
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
                println "\n# Query time: " + humanReadableFormat(Duration.ofMillis(queryTimeMs))
            }
            return new QuestionCommandResult(0, result, queryTimeMs)
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

    static class QuestionTypeConverter implements CommandLine.ITypeConverter<SourceQuestion> {
        SourceQuestion convert(String value) throws Exception {
            return SourceQuestion.toSourceQuestion(value?.toLowerCase())
        }
    }
}
