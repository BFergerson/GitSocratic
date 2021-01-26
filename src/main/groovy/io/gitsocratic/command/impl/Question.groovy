package io.gitsocratic.command.impl

import grakn.client.concept.answer.ConceptMap
import grakn.client.concept.answer.Numeric
import grakn.client.rpc.QueryFuture
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.question.SourceQuestion
import io.gitsocratic.command.result.QuestionCommandResult
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `question` command.
 * Used to execute a single source code question.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
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
            if (outputLogging) log.info "# Question: " + question.formattedQuestion
            //if (outputLogging) log.info "# Query: " + query.replace("\n", " ")
            def result = graknClient.executeQuery(tx, query)
            def queryTimeMs = System.currentTimeMillis() - startTime

            def questionAnswer = null
            if (result.size() == 1 && result.get(0) instanceof Numeric) {
                questionAnswer = (result.get(0) as Numeric).asLong().longValue()
                if (outputLogging) log.info("Result: " + questionAnswer as String)
            } else if (result.size() == 1 && result.get(0) instanceof QueryFuture) {
                def numericResult = (result.get(0) as QueryFuture).get()
                result = [numericResult as Numeric] as List<ConceptMap>
                if (outputLogging) log.info("Result: " + (result.get(0) as Numeric).asLong().longValue() as String)
            } else if (!result.isEmpty()) {
                if (outputLogging) log.info "Result:"
                result.each {
                    it.map().forEach({ key, value ->
                        if (outputLogging) log.info "\t" + key.toString() + " = " + value.asAttribute().value.toString()
                    })
                }
                if (result.size() == 1) {
                    questionAnswer = result.get(0).get("function_name").asAttribute().value
                } else {
                    questionAnswer = result.collect { it.get("function_name").asAttribute().value }
                }
            } else {
                if (outputLogging) log.info "Result: N/A"
            }
            return new QuestionCommandResult(0, questionAnswer, queryTimeMs)
        } finally {
            tx.close()
            graknClient.close()
        }
    }

    SourceQuestion getQuestion() {
        return question
    }

    static class QuestionTypeConverter implements CommandLine.ITypeConverter<SourceQuestion> {
        SourceQuestion convert(String value) throws Exception {
            return SourceQuestion.toSourceQuestion(value?.toLowerCase())
        }
    }
}
